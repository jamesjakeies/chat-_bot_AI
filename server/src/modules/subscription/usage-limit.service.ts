import { ForbiddenException, Injectable, NotFoundException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { MembershipLevel, Role, SenderType } from '@prisma/client';
import { PrismaService } from '../../infra/prisma/prisma.service';
import { FREE_BASE_ROLE_NAMES, MEMBERSHIP_LIMITS } from './subscription-limits';

@Injectable()
export class UsageLimitService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly configService: ConfigService,
  ) {}

  async checkMessageLimit(userId: string): Promise<void> {
    const { limits, usage } = await this.getUsageSnapshot(userId);

    if (usage.messagesToday >= limits.dailyMessages) {
      throw new ForbiddenException({
        code: 'MESSAGE_LIMIT_REACHED',
        message: '今日消息额度已用完。你可以明天再来，或升级会员解锁更多额度。',
        limits,
        usage,
      });
    }
  }

  async checkRoleAccess(
    userId: string,
    role: Pick<Role, 'name' | 'isOfficial'>,
  ): Promise<void> {
    if (this.configService.get<string>('ADMIN_DEV_ALLOW_ALL') === 'true') {
      return;
    }

    const user = await this.findUser(userId);
    const limits = MEMBERSHIP_LIMITS[user.membershipLevel];

    if (
      user.membershipLevel === MembershipLevel.FREE &&
      role.isOfficial &&
      !FREE_BASE_ROLE_NAMES.includes(role.name)
    ) {
      throw new ForbiddenException({
        code: 'ROLE_LOCKED',
        message: '免费版当前可使用 3 个基础角色。升级会员后可解锁全部基础角色。',
        limits,
      });
    }
  }

  async checkCustomRoleLimit(userId: string): Promise<void> {
    const { user, limits } = await this.getUserAndLimits(userId);
    const customRoleCount = await this.prisma.role.count({
      where: {
        createdByUserId: user.id,
        isOfficial: false,
        deletedAt: null,
      },
    });

    if (customRoleCount >= limits.customRoleLimit) {
      throw new ForbiddenException({
        code: 'CUSTOM_ROLE_LIMIT_REACHED',
        message: '自定义角色数量已达到当前会员上限。',
        limits,
        usage: { customRoleCount },
      });
    }
  }

  async checkMemoryLimit(userId: string): Promise<void> {
    const { limits, usage } = await this.getUsageSnapshot(userId);

    if (usage.memories >= limits.memoryLimit) {
      throw new ForbiddenException({
        code: 'MEMORY_LIMIT_REACHED',
        message: '长期记忆数量已达到当前会员上限。你可以删除旧记忆或升级会员。',
        limits,
        usage,
      });
    }
  }

  async checkWeeklyMoodSummaryAccess(userId: string): Promise<void> {
    const { limits } = await this.getUserAndLimits(userId);

    if (!limits.weeklyMoodSummary) {
      throw new ForbiddenException({
        code: 'WEEKLY_MOOD_SUMMARY_LOCKED',
        message: '情绪周报为月会员及以上权益。当前可以继续使用每日情绪打卡。',
        limits,
      });
    }
  }

  async getUsageSnapshot(userId: string) {
    const { user, limits } = await this.getUserAndLimits(userId);
    const todayStart = new Date();
    todayStart.setHours(0, 0, 0, 0);

    const [messagesToday, customRoles, memories] = await Promise.all([
      this.prisma.chatMessage.count({
        where: {
          senderType: SenderType.USER,
          deletedAt: null,
          createdAt: {
            gte: todayStart,
          },
          session: {
            userId: user.id,
            deletedAt: null,
          },
        },
      }),
      this.prisma.role.count({
        where: {
          createdByUserId: user.id,
          isOfficial: false,
          deletedAt: null,
        },
      }),
      this.prisma.memory.count({
        where: {
          userId: user.id,
          userConsented: true,
          deletedAt: null,
        },
      }),
    ]);

    return {
      membershipLevel: user.membershipLevel,
      limits,
      usage: {
        messagesToday,
        customRoles,
        memories,
      },
    };
  }

  private async getUserAndLimits(userId: string) {
    const user = await this.findUser(userId);

    return {
      user,
      limits: MEMBERSHIP_LIMITS[user.membershipLevel],
    };
  }

  private async findUser(userId: string) {
    const user = await this.prisma.user.findFirst({
      where: {
        id: userId,
        deletedAt: null,
      },
    });

    if (!user) {
      throw new NotFoundException('User not found');
    }

    return user;
  }
}
