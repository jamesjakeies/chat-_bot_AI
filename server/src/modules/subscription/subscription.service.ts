import { Injectable, NotFoundException } from '@nestjs/common';
import { SubscriptionPlan, SubscriptionStatus } from '@prisma/client';
import { PrismaService } from '../../infra/prisma/prisma.service';
import { MockUpgradeDto } from './dto/mock-upgrade.dto';
import {
  membershipLevelToSubscriptionPlan,
  MEMBERSHIP_LIMITS,
  subscriptionPlanToMembershipLevel,
} from './subscription-limits';
import { UsageLimitService } from './usage-limit.service';

@Injectable()
export class SubscriptionService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly usageLimitService: UsageLimitService,
  ) {}

  async getMe(userId: string) {
    const user = await this.prisma.user.findFirst({
      where: {
        id: userId,
        deletedAt: null,
      },
    });

    if (!user) {
      throw new NotFoundException('User not found');
    }

    const activeSubscription = await this.prisma.subscription.findFirst({
      where: {
        userId,
        status: SubscriptionStatus.ACTIVE,
        deletedAt: null,
      },
      orderBy: {
        createdAt: 'desc',
      },
    });

    const usageSnapshot = await this.usageLimitService.getUsageSnapshot(userId);

    return {
      membershipLevel: user.membershipLevel,
      plan: activeSubscription?.plan ?? membershipLevelToSubscriptionPlan(user.membershipLevel),
      status: activeSubscription?.status ?? SubscriptionStatus.ACTIVE,
      startedAt: activeSubscription?.startedAt ?? user.createdAt,
      expiresAt: activeSubscription?.expiresAt ?? null,
      limits: MEMBERSHIP_LIMITS[user.membershipLevel],
      usage: usageSnapshot.usage,
    };
  }

  async mockUpgrade(userId: string, dto: MockUpgradeDto) {
    const membershipLevel = subscriptionPlanToMembershipLevel(dto.plan);
    const now = new Date();
    const expiresAt = dto.plan === SubscriptionPlan.FREE
      ? null
      : new Date(now.getTime() + 30 * 24 * 60 * 60 * 1000);

    await this.prisma.user.update({
      where: {
        id: userId,
      },
      data: {
        membershipLevel,
      },
    });

    await this.prisma.subscription.updateMany({
      where: {
        userId,
        status: SubscriptionStatus.ACTIVE,
        deletedAt: null,
      },
      data: {
        status: SubscriptionStatus.CANCELED,
      },
    });

    await this.prisma.subscription.create({
      data: {
        userId,
        plan: dto.plan,
        status: SubscriptionStatus.ACTIVE,
        startedAt: now,
        expiresAt,
      },
    });

    return this.getMe(userId);
  }
}
