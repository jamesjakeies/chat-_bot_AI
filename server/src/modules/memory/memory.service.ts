import { ForbiddenException, Injectable, NotFoundException } from '@nestjs/common';
import { MemoryType, SensitivityLevel } from '@prisma/client';
import { PrismaService } from '../../infra/prisma/prisma.service';
import { UsageLimitService } from '../subscription/usage-limit.service';
import { ConfirmSensitiveMemoryDto } from './dto/confirm-sensitive-memory.dto';
import { CreateMemoryDto } from './dto/create-memory.dto';
import { ListMemoriesQueryDto } from './dto/list-memories-query.dto';
import { RoleMemorySettingDto } from './dto/role-memory-setting.dto';
import { UpdateMemoryDto } from './dto/update-memory.dto';

@Injectable()
export class MemoryService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly usageLimitService: UsageLimitService,
  ) {}

  async listMemories(userId: string, query: ListMemoriesQueryDto) {
    const includePending = query.includePending === 'true';

    return this.prisma.memory.findMany({
      where: {
        userId,
        ...(query.roleId ? { roleId: query.roleId } : {}),
        deletedAt: null,
        ...(includePending ? {} : { userConsented: true }),
      },
      include: {
        role: {
          select: {
            id: true,
            name: true,
          },
        },
      },
      orderBy: {
        updatedAt: 'desc',
      },
    });
  }

  async createMemory(userId: string, dto: CreateMemoryDto) {
    const role = await this.prisma.role.findFirst({
      where: {
        id: dto.roleId,
        deletedAt: null,
        OR: [{ isOfficial: true }, { createdByUserId: userId }],
      },
    });

    if (!role) {
      throw new NotFoundException('Role not found');
    }

    const userRole = await this.prisma.userRole.upsert({
      where: {
        userId_roleId: {
          userId,
          roleId: role.id,
        },
      },
      create: {
        userId,
        roleId: role.id,
      },
      update: {},
    });

    if (!userRole.memoryEnabled) {
      throw new ForbiddenException('Memory is disabled for this role.');
    }

    const sensitivityLevel =
      dto.sensitivityLevel ?? this.classifySensitivity(dto.content);
    const memoryType = dto.memoryType ?? this.classifyMemoryType(dto.content);
    const isSensitive = sensitivityLevel !== SensitivityLevel.NORMAL;
    const userConsented = isSensitive ? false : dto.userConsented ?? true;

    if (userConsented) {
      await this.usageLimitService.checkMemoryLimit(userId);
    }

    return this.prisma.memory.create({
      data: {
        userId,
        roleId: role.id,
        memoryType,
        content: dto.content.trim(),
        sensitivityLevel,
        userConsented,
        sourceMessageId: dto.sourceMessageId,
      },
      include: {
        role: {
          select: {
            id: true,
            name: true,
          },
        },
      },
    });
  }

  async updateMemory(userId: string, memoryId: string, dto: UpdateMemoryDto) {
    await this.assertMemoryOwnership(userId, memoryId);

    if (dto.userConsented === true) {
      await this.usageLimitService.checkMemoryLimit(userId);
    }

    return this.prisma.memory.update({
      where: {
        id: memoryId,
      },
      data: dto,
      include: {
        role: {
          select: {
            id: true,
            name: true,
          },
        },
      },
    });
  }

  async deleteMemory(userId: string, memoryId: string) {
    await this.assertMemoryOwnership(userId, memoryId);
    await this.prisma.memory.update({
      where: {
        id: memoryId,
      },
      data: {
        deletedAt: new Date(),
      },
    });

    return { success: true };
  }

  async confirmSensitive(userId: string, dto: ConfirmSensitiveMemoryDto) {
    const memory = await this.assertMemoryOwnership(userId, dto.memoryId);

    if (dto.accepted) {
      await this.usageLimitService.checkMemoryLimit(userId);

      return this.prisma.memory.update({
        where: {
          id: memory.id,
        },
        data: {
          userConsented: true,
        },
        include: {
          role: {
            select: {
              id: true,
              name: true,
            },
          },
        },
      });
    }

    return this.prisma.memory.update({
      where: {
        id: memory.id,
      },
      data: {
        deletedAt: new Date(),
      },
      include: {
        role: {
          select: {
            id: true,
            name: true,
          },
        },
      },
    });
  }

  async updateRoleMemorySetting(
    userId: string,
    roleId: string,
    dto: RoleMemorySettingDto,
  ) {
    await this.prisma.userRole.upsert({
      where: {
        userId_roleId: {
          userId,
          roleId,
        },
      },
      create: {
        userId,
        roleId,
        memoryEnabled: dto.memoryEnabled,
      },
      update: {
        memoryEnabled: dto.memoryEnabled,
      },
    });

    return {
      success: true,
    };
  }

  private async assertMemoryOwnership(userId: string, memoryId: string) {
    const memory = await this.prisma.memory.findFirst({
      where: {
        id: memoryId,
        userId,
        deletedAt: null,
      },
    });

    if (!memory) {
      throw new NotFoundException('Memory not found');
    }

    return memory;
  }

  private classifySensitivity(content: string): SensitivityLevel {
    const text = content.toLowerCase();

    if (
      /住址|地址|学校|公司|身份证|手机号|真实姓名|银行卡|密码/.test(text)
    ) {
      return SensitivityLevel.HIGHLY_SENSITIVE;
    }

    if (
      /家庭冲突|亲密关系|财务|债务|收入|抑郁|焦虑|自杀|自残|心理|医疗|病|药/.test(
        text,
      )
    ) {
      return SensitivityLevel.SENSITIVE;
    }

    return SensitivityLevel.NORMAL;
  }

  private classifyMemoryType(content: string): MemoryType {
    const text = content.toLowerCase();

    if (/叫我|称呼|昵称/.test(text)) return MemoryType.PREFERENCE;
    if (/聊天风格|说话方式|安慰|鼓励/.test(text)) return MemoryType.CHAT_STYLE;
    if (/目标|计划|每天|学习|英语|运动/.test(text)) return MemoryType.GOAL;
    if (/睡觉|失眠|早起|作息|晚上/.test(text)) return MemoryType.ROUTINE;
    if (/压力|工作|学习|家庭|人际/.test(text)) return MemoryType.STRESSOR;
    if (/抑郁|焦虑|心理|情绪/.test(text)) return MemoryType.MENTAL_HEALTH;
    if (/医院|医生|药|病|身体/.test(text)) return MemoryType.MEDICAL;
    if (/家庭|父母|妈妈|爸爸/.test(text)) return MemoryType.FAMILY;
    if (/恋爱|男友|女友|伴侣|分手/.test(text)) return MemoryType.RELATIONSHIP;
    if (/钱|财务|债务|收入|工资/.test(text)) return MemoryType.FINANCE;
    if (/身份证|真实姓名|手机号/.test(text)) return MemoryType.IDENTITY;
    if (/住址|地址|学校|公司/.test(text)) return MemoryType.LOCATION;

    return MemoryType.OTHER;
  }
}
