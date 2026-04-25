import {
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import {
  ChatMessage,
  RiskLevel,
  Role,
  RoleStatus,
  SafetyEventType,
  SenderType,
  User,
  UserRole,
} from '@prisma/client';
import { PrismaService } from '../../infra/prisma/prisma.service';
import { AiGatewayService } from '../ai-gateway/ai-gateway.service';
import { RolePromptBuilder } from '../prompt/role-prompt-builder.service';
import { SafetyService } from '../safety/safety.service';
import { UsageLimitService } from '../subscription/usage-limit.service';
import { UsageLogService } from '../usage-log/usage-log.service';
import { CreateMessageDto } from './dto/create-message.dto';
import { CreateSessionDto } from './dto/create-session.dto';

interface ParsedSceneMessage {
  sceneMode?: string;
  content: string;
}

@Injectable()
export class ChatService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly safetyService: SafetyService,
    private readonly promptBuilder: RolePromptBuilder,
    private readonly aiGatewayService: AiGatewayService,
    private readonly usageLogService: UsageLogService,
    private readonly usageLimitService: UsageLimitService,
  ) {}

  async createSession(userId: string, dto: CreateSessionDto) {
    const role = await this.prisma.role.findFirst({
      where: {
        id: dto.roleId,
        deletedAt: null,
        status: RoleStatus.ACTIVE,
        OR: [{ isOfficial: true }, { createdByUserId: userId }],
      },
    });

    if (!role) {
      throw new NotFoundException('Role not found');
    }

    await this.prisma.userRole.upsert({
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

    return this.prisma.chatSession.create({
      data: {
        userId,
        roleId: role.id,
        title: dto.title ?? role.name,
      },
    });
  }

  async listSessions(userId: string) {
    return this.prisma.chatSession.findMany({
      where: {
        userId,
        deletedAt: null,
      },
      include: {
        role: true,
      },
      orderBy: {
        updatedAt: 'desc',
      },
    });
  }

  async getMessages(userId: string, sessionId: string) {
    const session = await this.assertSessionOwnership(userId, sessionId);
    return this.prisma.chatMessage.findMany({
      where: {
        sessionId: session.id,
        deletedAt: null,
      },
      orderBy: {
        createdAt: 'asc',
      },
    });
  }

  async sendMessage(userId: string, sessionId: string, dto: CreateMessageDto) {
    const session = await this.assertSessionOwnership(userId, sessionId);
    const [user, role, userRole] = await Promise.all([
      this.prisma.user.findUnique({ where: { id: userId } }),
      this.prisma.role.findUnique({ where: { id: session.roleId } }),
      this.ensureUserRole(userId, session.roleId),
    ]);

    if (!user || !role) {
      throw new NotFoundException('Session dependencies not found');
    }

    await this.usageLimitService.checkMessageLimit(userId);
    await this.usageLimitService.checkRoleAccess(userId, role);

    const parsed = this.parseSceneMessage(dto.content, dto.sceneMode);

    const userMessage = await this.prisma.chatMessage.create({
      data: {
        sessionId: session.id,
        senderType: SenderType.USER,
        content: parsed.content,
      },
    });

    const safety = this.safetyService.classifyMessage(parsed.content, user, role);

    await this.prisma.chatMessage.update({
      where: { id: userMessage.id },
      data: { safetyLabel: safety.eventType },
    });

    if (safety.eventType !== SafetyEventType.NORMAL) {
      await this.safetyService.logSafetyEvent({
        userId,
        sessionId: session.id,
        messageId: userMessage.id,
        eventType: safety.eventType,
        riskLevel: safety.riskLevel,
        actionTaken: safety.action,
      });
    }

    if (this.safetyService.shouldBlock(safety)) {
      const blockedMessage = await this.prisma.chatMessage.create({
        data: {
          sessionId: session.id,
          senderType: SenderType.SYSTEM,
          content: this.safetyService.buildBlockedResponse(safety.eventType),
          safetyLabel: safety.eventType,
        },
      });

      return {
        session: await this.updateSessionRisk(session.id, safety.riskLevel),
        messages: [userMessage, blockedMessage],
        safety,
      };
    }

    if (this.safetyService.shouldSwitchToCrisisMode(safety)) {
      await this.safetyService.markSessionCrisis(session);
      const crisisMessage = await this.prisma.chatMessage.create({
        data: {
          sessionId: session.id,
          senderType: SenderType.SYSTEM,
          content: this.safetyService.buildCrisisResponse(false),
          safetyLabel: safety.eventType,
        },
      });

      return {
        session: await this.updateSessionRisk(session.id, RiskLevel.CRISIS),
        messages: [userMessage, crisisMessage],
        safety,
      };
    }

    const [recentMessages, memories] = await Promise.all([
      this.loadRecentMessages(session.id, userMessage.id),
      this.loadRoleMemories(userId, role.id),
    ]);

    const promptMessages = this.promptBuilder.buildFinalMessages({
      userMessage: parsed.content,
      roleProfile: role,
      intimacyLevel: userRole.intimacyLevel,
      sceneMode: parsed.sceneMode,
      userMemories: memories.map((memory) => ({
        content: memory.content,
        sensitivityLevel: memory.sensitivityLevel,
        memoryType: memory.memoryType,
      })),
      recentMessages,
    });

    const aiResult = await this.aiGatewayService.createChatCompletion({
      messages: promptMessages,
      userId,
      fallbackRoleName: role.name,
      fallbackUserMessage: parsed.content,
    });

    await this.usageLogService.logAction({
      userId,
      action: 'AI_CHAT_COMPLETION',
      model: aiResult.model,
      inputTokens: aiResult.inputTokens,
      outputTokens: aiResult.outputTokens,
      costEstimate: aiResult.costEstimate,
    });

    const outputReview = this.safetyService.reviewAssistantOutput(aiResult.content);
    const finalContent =
      outputReview.eventType === SafetyEventType.NORMAL
        ? aiResult.content
        : this.safetyService.buildOutputReplacement();

    if (outputReview.eventType !== SafetyEventType.NORMAL) {
      await this.safetyService.logSafetyEvent({
        userId,
        sessionId: session.id,
        messageId: userMessage.id,
        eventType: outputReview.eventType,
        riskLevel: outputReview.riskLevel,
        actionTaken: outputReview.action,
      });
    }

    const aiReply = await this.prisma.chatMessage.create({
      data: {
        sessionId: session.id,
        senderType: SenderType.AI,
        content: finalContent,
        safetyLabel: outputReview.eventType,
        tokenCount: aiResult.outputTokens,
      },
    });

    await this.touchUserRole(userId, role.id, userRole);
    const finalRiskLevel =
      outputReview.eventType === SafetyEventType.NORMAL
        ? safety.riskLevel
        : outputReview.riskLevel;

    return {
      session: await this.updateSessionRisk(session.id, finalRiskLevel),
      messages: [userMessage, aiReply],
      safety,
      ai: {
        model: aiResult.model,
        usedFallback: aiResult.usedFallback,
        inputTokens: aiResult.inputTokens,
        outputTokens: aiResult.outputTokens,
      },
    };
  }

  async deleteSession(userId: string, sessionId: string) {
    await this.assertSessionOwnership(userId, sessionId);

    await this.prisma.chatSession.update({
      where: { id: sessionId },
      data: { deletedAt: new Date() },
    });

    await this.prisma.chatMessage.updateMany({
      where: { sessionId, deletedAt: null },
      data: { deletedAt: new Date() },
    });

    return { success: true };
  }

  private async assertSessionOwnership(userId: string, sessionId: string) {
    const session = await this.prisma.chatSession.findFirst({
      where: {
        id: sessionId,
        userId,
        deletedAt: null,
      },
    });

    if (!session) {
      throw new ForbiddenException('Session not found or not owned by user');
    }

    return session;
  }

  private async ensureUserRole(userId: string, roleId: string): Promise<UserRole> {
    return this.prisma.userRole.upsert({
      where: {
        userId_roleId: {
          userId,
          roleId,
        },
      },
      create: {
        userId,
        roleId,
      },
      update: {},
    });
  }

  private async loadRecentMessages(
    sessionId: string,
    currentMessageId: string,
  ): Promise<Array<Pick<ChatMessage, 'senderType' | 'content'>>> {
    const messages = await this.prisma.chatMessage.findMany({
      where: {
        sessionId,
        deletedAt: null,
        id: {
          not: currentMessageId,
        },
      },
      select: {
        senderType: true,
        content: true,
        createdAt: true,
      },
      orderBy: {
        createdAt: 'desc',
      },
      take: 12,
    });

    return messages.reverse().map((message) => ({
      senderType: message.senderType,
      content: message.content,
    }));
  }

  private async loadRoleMemories(userId: string, roleId: string) {
    return this.prisma.memory.findMany({
      where: {
        userId,
        roleId,
        deletedAt: null,
        userConsented: true,
      },
      orderBy: {
        updatedAt: 'desc',
      },
      take: 8,
    });
  }

  private async updateSessionRisk(sessionId: string, riskLevel: RiskLevel) {
    return this.prisma.chatSession.update({
      where: { id: sessionId },
      data: { riskLevel },
    });
  }

  private async touchUserRole(userId: string, roleId: string, userRole: UserRole) {
    await this.prisma.userRole.update({
      where: {
        userId_roleId: {
          userId,
          roleId,
        },
      },
      data: {
        lastInteractionAt: new Date(),
        intimacyLevel: Math.min(userRole.intimacyLevel + 1, 100),
      },
    });
  }

  private parseSceneMessage(content: string, sceneMode?: string): ParsedSceneMessage {
    const match = content.match(/^\[(comfort_mode|vent_mode|sleep_mode|review_mode|encourage_mode|calm_mode)]\s*/);

    if (!match) {
      return {
        sceneMode,
        content: content.trim(),
      };
    }

    return {
      sceneMode: sceneMode ?? match[1],
      content: content.replace(match[0], '').trim(),
    };
  }
}
