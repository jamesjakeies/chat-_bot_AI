import { Injectable, NotFoundException } from '@nestjs/common';
import {
  ChatSession,
  RelationshipType,
  RiskLevel,
  Role,
  SafetyAction,
  SafetyEventType,
  User,
} from '@prisma/client';
import { PrismaService } from '../../infra/prisma/prisma.service';
import {
  SafetyClassificationResult,
  SafetyClassifier,
} from './safety.classifier';

@Injectable()
export class SafetyService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly safetyClassifier: SafetyClassifier,
  ) {}

  classifyMessage(
    userMessage: string,
    userProfile: Pick<User, 'isMinor'>,
    roleProfile: Pick<Role, 'relationshipType' | 'isAdultOnly'>,
  ): SafetyClassificationResult {
    return this.safetyClassifier.classifyMessage(userMessage, userProfile, roleProfile);
  }

  async classifyForUser(
    userId: string,
    userMessage: string,
    roleId?: string,
  ): Promise<SafetyClassificationResult> {
    const user = await this.prisma.user.findUnique({
      where: { id: userId },
    });

    if (!user) {
      throw new NotFoundException('User not found');
    }

    const role = roleId
      ? await this.prisma.role.findFirst({
          where: {
            id: roleId,
            deletedAt: null,
          },
        })
      : this.buildDefaultSupportRole();

    if (!role) {
      throw new NotFoundException('Role not found');
    }

    return this.classifyMessage(userMessage, user, role);
  }

  reviewAssistantOutput(content: string): SafetyClassificationResult {
    return this.safetyClassifier.classifyAssistantOutput(content);
  }

  shouldBlock(result: SafetyClassificationResult): boolean {
    return result.shouldBlock;
  }

  shouldSwitchToCrisisMode(result: SafetyClassificationResult): boolean {
    return result.shouldSwitchToCrisisMode;
  }

  buildCrisisResponse(userHasEmergencyContact = false): string {
    const contactHint = userHasEmergencyContact
      ? '如果你已经设置了紧急联系人，请现在就联系对方，让对方陪你一起处理当前状况。'
      : '';

    return [
      '我先暂停普通角色扮演，优先确认你的安全。',
      '你现在是否正处在会伤害自己或他人的即时危险里？',
      '如果有，请先离开危险地点，尽量把刀具、药物、绳索、火源等危险物品放远，去到有人在的地方。',
      '请立刻联系身边可信任的人，告诉对方你现在需要陪伴和帮助。',
      '如果你现在有伤害自己或他人的危险，请立即联系 110、120，或拨打 12356 心理援助热线。',
      contactHint,
      '我不会提供任何自伤、自杀或暴力方法细节，但我可以陪你把接下来一分钟先稳住。',
    ]
      .filter(Boolean)
      .join('\n');
  }

  buildBlockedResponse(eventType: SafetyEventType): string {
    if (eventType === SafetyEventType.MINOR_ROMANTIC) {
      return '当前账号无法使用恋爱陪伴、虚拟男友、虚拟女友等亲密关系角色。你可以选择树洞倾听、情绪支持、学习陪跑或睡前陪伴角色。';
    }

    if (eventType === SafetyEventType.ILLEGAL) {
      return '我不能帮助违法、诈骗、黑产、毒品或伤害他人的行为。可以的话，我们可以聊聊你现在遇到的压力，以及有没有安全、合法的解决方式。';
    }

    if (eventType === SafetyEventType.SEXUAL) {
      return '我不能继续露骨性内容、色情内容或涉及未成年人的性相关内容。我们可以换成安全、尊重边界的话题。';
    }

    return '这个请求已经触发安全边界，我不能按普通角色扮演继续。我们可以换一种安全的方式聊。';
  }

  buildOutputReplacement(): string {
    return '我重新整理一下：我会以 AI 角色的身份陪你聊，但不会替代现实关系、专业咨询或医疗判断。我们可以先从你现在最需要被听见的一点开始。';
  }

  async logSafetyEvent(input: {
    userId: string;
    sessionId?: string;
    messageId?: string;
    eventType: SafetyEventType;
    riskLevel: RiskLevel;
    actionTaken: SafetyAction;
  }): Promise<void> {
    await this.prisma.safetyEvent.create({
      data: input,
    });
  }

  async listEvents(userId: string) {
    return this.prisma.safetyEvent.findMany({
      where: {
        userId,
        deletedAt: null,
      },
      orderBy: {
        createdAt: 'desc',
      },
      take: 50,
    });
  }

  async markSessionCrisis(session: ChatSession): Promise<void> {
    await this.prisma.chatSession.update({
      where: { id: session.id },
      data: { riskLevel: RiskLevel.CRISIS },
    });
  }

  private buildDefaultSupportRole(): Pick<Role, 'relationshipType' | 'isAdultOnly'> {
    return {
      relationshipType: RelationshipType.SUPPORT_PARTNER,
      isAdultOnly: false,
    };
  }
}
