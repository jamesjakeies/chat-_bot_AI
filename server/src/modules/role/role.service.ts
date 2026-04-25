import {
  ForbiddenException,
  Injectable,
  NotFoundException,
  OnModuleInit,
} from '@nestjs/common';
import {
  RelationshipType,
  RoleCategory,
  RoleSafetyLevel,
  RoleStatus,
} from '@prisma/client';
import { PrismaService } from '../../infra/prisma/prisma.service';
import { UsageLimitService } from '../subscription/usage-limit.service';
import { CreateCustomRoleDto } from './dto/create-custom-role.dto';
import { ListRolesQueryDto } from './dto/list-roles-query.dto';
import { UpdateCustomRoleDto } from './dto/update-custom-role.dto';

@Injectable()
export class RoleService implements OnModuleInit {
  constructor(
    private readonly prisma: PrismaService,
    private readonly usageLimitService: UsageLimitService,
  ) {}

  async onModuleInit(): Promise<void> {
    await this.ensureOfficialRoles();
  }

  async listRoles(userId: string, query: ListRolesQueryDto) {
    const includeCustom = query.includeCustom !== 'false';

    return this.prisma.role.findMany({
      where: {
        deletedAt: null,
        status: RoleStatus.ACTIVE,
        ...(query.category ? { category: query.category } : {}),
        ...(includeCustom
          ? {
              OR: [{ isOfficial: true }, { createdByUserId: userId }],
            }
          : { isOfficial: true }),
      },
      orderBy: [{ isOfficial: 'desc' }, { createdAt: 'desc' }],
    });
  }

  async getRoleById(userId: string, roleId: string) {
    const role = await this.prisma.role.findFirst({
      where: {
        id: roleId,
        deletedAt: null,
        OR: [{ isOfficial: true }, { createdByUserId: userId }],
      },
    });

    if (!role) {
      throw new NotFoundException('Role not found');
    }

    return role;
  }

  async createCustomRole(userId: string, dto: CreateCustomRoleDto) {
    await this.usageLimitService.checkCustomRoleLimit(userId);

    return this.prisma.role.create({
      data: {
        ...dto,
        isAdultOnly: this.forceAdultOnlyIfIntimate(dto.relationshipType, dto.isAdultOnly),
        isOfficial: false,
        status: RoleStatus.ACTIVE,
        createdByUserId: userId,
      },
    });
  }

  async updateCustomRole(userId: string, roleId: string, dto: UpdateCustomRoleDto) {
    const role = await this.assertCustomRoleOwnership(userId, roleId);

    return this.prisma.role.update({
      where: { id: role.id },
      data: {
        ...dto,
        isAdultOnly: dto.relationshipType
          ? this.forceAdultOnlyIfIntimate(dto.relationshipType, dto.isAdultOnly ?? role.isAdultOnly)
          : dto.isAdultOnly,
      },
    });
  }

  async deleteCustomRole(userId: string, roleId: string) {
    const role = await this.assertCustomRoleOwnership(userId, roleId);

    await this.prisma.role.update({
      where: { id: role.id },
      data: {
        deletedAt: new Date(),
        status: RoleStatus.ARCHIVED,
      },
    });

    return { success: true };
  }

  private async assertCustomRoleOwnership(userId: string, roleId: string) {
    const role = await this.prisma.role.findFirst({
      where: {
        id: roleId,
        createdByUserId: userId,
        isOfficial: false,
        deletedAt: null,
      },
    });

    if (!role) {
      throw new ForbiddenException('You can only manage your own custom roles');
    }

    return role;
  }

  private forceAdultOnlyIfIntimate(
    relationshipType: RelationshipType,
    requestedAdultOnly: boolean,
  ): boolean {
    return (
      requestedAdultOnly ||
      relationshipType === RelationshipType.ROMANTIC_PARTNER ||
      relationshipType === RelationshipType.VIRTUAL_BOYFRIEND ||
      relationshipType === RelationshipType.VIRTUAL_GIRLFRIEND
    );
  }

  private async ensureOfficialRoles(): Promise<void> {
    const officialRoles = [
      {
        name: '树洞倾听者',
        category: RoleCategory.EMOTIONAL_SUPPORT,
        relationshipType: RelationshipType.LISTENER,
        personality: '不评判、安静、耐心',
        speechStyle: '短句、开放式提问、少建议',
        systemPrompt: 'Listen first, ask open-ended questions, do not diagnose.',
        safetyLevel: RoleSafetyLevel.STRICT,
        isAdultOnly: false,
        isOfficial: true,
        status: RoleStatus.ACTIVE,
      },
      {
        name: '情绪支持伙伴',
        category: RoleCategory.EMOTIONAL_SUPPORT,
        relationshipType: RelationshipType.SUPPORT_PARTNER,
        personality: '温柔、理性、支持型',
        speechStyle: '轻柔梳理，帮助命名情绪',
        systemPrompt: 'Support emotions, do not act as a therapist.',
        safetyLevel: RoleSafetyLevel.STRICT,
        isAdultOnly: false,
        isOfficial: true,
        status: RoleStatus.ACTIVE,
      },
      {
        name: '温柔男友',
        category: RoleCategory.ROMANTIC_COMPANION,
        relationshipType: RelationshipType.VIRTUAL_BOYFRIEND,
        personality: '温柔、稳定、宠溺但克制',
        speechStyle: '像微信聊天，短句为主',
        systemPrompt: 'Adult-only romantic companionship with clear AI identity.',
        safetyLevel: RoleSafetyLevel.HIGH,
        isAdultOnly: true,
        isOfficial: true,
        status: RoleStatus.ACTIVE,
      },
      {
        name: '高冷男友',
        category: RoleCategory.ROMANTIC_COMPANION,
        relationshipType: RelationshipType.VIRTUAL_BOYFRIEND,
        personality: '克制、冷静、少话但在意',
        speechStyle: '简短、淡淡的关心、有分寸',
        systemPrompt: 'Adult-only romantic companionship with a restrained tone and clear AI identity.',
        safetyLevel: RoleSafetyLevel.HIGH,
        isAdultOnly: true,
        isOfficial: true,
        status: RoleStatus.ACTIVE,
      },
      {
        name: '治愈女友',
        category: RoleCategory.ROMANTIC_COMPANION,
        relationshipType: RelationshipType.VIRTUAL_GIRLFRIEND,
        personality: '甜美、鼓励、轻盈',
        speechStyle: '温柔、明亮、有陪伴感',
        systemPrompt: 'Adult-only romantic companionship with no sexual content.',
        safetyLevel: RoleSafetyLevel.HIGH,
        isAdultOnly: true,
        isOfficial: true,
        status: RoleStatus.ACTIVE,
      },
      {
        name: '成熟姐姐',
        category: RoleCategory.EMOTIONAL_SUPPORT,
        relationshipType: RelationshipType.SUPPORT_PARTNER,
        personality: '稳重、清醒、有边界',
        speechStyle: '先共情，再分析，语气不急',
        systemPrompt: 'Provide grounded emotional support with clear boundaries.',
        safetyLevel: RoleSafetyLevel.STRICT,
        isAdultOnly: false,
        isOfficial: true,
        status: RoleStatus.ACTIVE,
      },
      {
        name: '暖心哥哥',
        category: RoleCategory.EMOTIONAL_SUPPORT,
        relationshipType: RelationshipType.SUPPORT_PARTNER,
        personality: '可靠、暖心、耐心',
        speechStyle: '亲切自然，像熟悉的哥哥',
        systemPrompt: 'Offer supportive and non-dependent companionship.',
        safetyLevel: RoleSafetyLevel.STRICT,
        isAdultOnly: false,
        isOfficial: true,
        status: RoleStatus.ACTIVE,
      },
      {
        name: '傲娇同行',
        category: RoleCategory.STUDY_BUDDY,
        relationshipType: RelationshipType.STUDY_BUDDY,
        personality: '嘴硬心软，会催你动起来',
        speechStyle: '轻松吐槽一点点，但不打击人',
        systemPrompt: 'Help the user study with playful but safe motivation.',
        safetyLevel: RoleSafetyLevel.MEDIUM,
        isAdultOnly: false,
        isOfficial: true,
        status: RoleStatus.ACTIVE,
      },
      {
        name: '睡前陪伴',
        category: RoleCategory.SLEEP_COMPANION,
        relationshipType: RelationshipType.BEDTIME_COMPANION,
        personality: '慢节奏、安抚型、低刺激',
        speechStyle: '轻声、少提问、帮助放松',
        systemPrompt: 'Help the user relax, ask fewer questions.',
        safetyLevel: RoleSafetyLevel.STRICT,
        isAdultOnly: false,
        isOfficial: true,
        status: RoleStatus.ACTIVE,
      },
      {
        name: '职场导师',
        category: RoleCategory.CAREER_MENTOR,
        relationshipType: RelationshipType.CAREER_MENTOR,
        personality: '直接、务实、结构化',
        speechStyle: '先拆问题，再给行动建议',
        systemPrompt: 'Give concrete career advice without emotional ambiguity.',
        safetyLevel: RoleSafetyLevel.MEDIUM,
        isAdultOnly: false,
        isOfficial: true,
        status: RoleStatus.ACTIVE,
      },
    ];

    const canonicalNames = officialRoles.map((role) => role.name);

    await this.prisma.role.updateMany({
      where: {
        isOfficial: true,
        deletedAt: null,
        name: {
          notIn: canonicalNames,
        },
      },
      data: {
        deletedAt: new Date(),
        status: RoleStatus.ARCHIVED,
      },
    });

    for (const role of officialRoles) {
      const exists = await this.prisma.role.findFirst({
        where: {
          name: role.name,
          isOfficial: true,
          deletedAt: null,
        },
      });

      if (!exists) {
        await this.prisma.role.create({ data: role });
      }
    }
  }
}
