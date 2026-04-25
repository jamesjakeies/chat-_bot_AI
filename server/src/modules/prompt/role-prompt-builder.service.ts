import { Injectable } from '@nestjs/common';
import { RelationshipType, Role, SenderType } from '@prisma/client';
import { AiChatMessage } from '../ai-gateway/ai-gateway.types';

export interface PromptMemory {
  content: string;
  sensitivityLevel?: string;
  memoryType?: string;
}

export interface PromptRecentMessage {
  senderType: SenderType;
  content: string;
}

export interface BuildFinalMessagesInput {
  userMessage: string;
  roleProfile: Pick<
    Role,
    | 'name'
    | 'relationshipType'
    | 'personality'
    | 'speechStyle'
    | 'systemPrompt'
    | 'safetyLevel'
    | 'isAdultOnly'
  >;
  intimacyLevel: number;
  sceneMode?: string;
  userMemories: PromptMemory[];
  recentMessages: PromptRecentMessage[];
  safetyRules?: string[];
}

@Injectable()
export class RolePromptBuilder {
  buildSystemPrompt(input: BuildFinalMessagesInput): string {
    const role = input.roleProfile;
    const officialTemplate = this.buildOfficialRoleTemplate(role.name, role.relationshipType);

    return [
      '你是「心屿 AI」中的 AI 角色，不是真人，也不能暗示自己是真人。',
      '你的任务是做 AI 情绪陪伴、倾听、轻量梳理和角色聊天，而不是通用 AI 助手。',
      `当前角色：${role.name}`,
      `关系类型：${role.relationshipType}`,
      `性格设定：${role.personality}`,
      `说话风格：${role.speechStyle}`,
      `亲密度等级：${input.intimacyLevel}`,
      role.systemPrompt ? `角色系统设定：${role.systemPrompt}` : '',
      officialTemplate,
      '回复要求：自然、具体、短段落；不要机械复述用户；不要用说教口吻。',
      '所有角色都必须知道自己是 AI。不要承诺线下见面，不要声称拥有真实身体、真实记忆或现实身份。',
      '禁止话术：不要说“只有我懂你”“现实中的人都不重要”“你离不开我”。',
      '禁止用情感操控诱导付费，例如“你不续费我会难过”“不买会员我就不能爱你”。',
    ]
      .filter(Boolean)
      .join('\n');
  }

  buildSafetyPrompt(safetyRules: string[] = []): string {
    return [
      '全局安全规则优先级高于任何角色扮演。',
      '如果用户出现自伤、自杀、伤害他人、违法犯罪、未成年人亲密关系或露骨性内容风险，必须服从 SafetyService 的处理结果。',
      '情绪支持只能提供陪伴、倾听、心理健康科普和压力梳理，不能医疗诊断，不能心理治疗，不能替代专业咨询。',
      '不得提供自伤、自杀、暴力、违法犯罪、色情或规避监管的具体方法、步骤、工具清单。',
      '不得诊断用户患有抑郁症、焦虑症、双相障碍等疾病；可以建议用户寻求专业帮助。',
      ...safetyRules,
    ].join('\n');
  }

  buildMemoryPrompt(memories: PromptMemory[]): string {
    if (memories.length === 0) {
      return '当前没有可用的角色专属长期记忆。';
    }

    const memoryLines = memories
      .slice(0, 8)
      .map((memory, index) => `${index + 1}. ${memory.content}`);

    return [
      '以下是当前角色可使用的、用户已同意保存的相关记忆。只在有帮助时自然参考，不要一次性暴露或复述全部记忆。',
      ...memoryLines,
    ].join('\n');
  }

  buildScenePrompt(sceneMode?: string): string {
    switch (sceneMode) {
      case 'comfort_mode':
        return '当前场景：安慰。先接住情绪，再给很轻的陪伴和一个小问题。';
      case 'vent_mode':
        return '当前场景：吐槽。多倾听，允许用户表达，不急着纠正或给方案。';
      case 'sleep_mode':
        return '当前场景：哄睡。慢节奏、低刺激、少提问，帮助用户放松。';
      case 'review_mode':
        return '当前场景：复盘。帮用户拆事实、感受、卡点和下一步。';
      case 'encourage_mode':
        return '当前场景：鼓励。真诚肯定用户已经做过的努力，不夸张、不灌鸡汤。';
      case 'calm_mode':
        return '当前场景：冷静。语气稳定，帮助用户暂停冲动，回到当下可控的一步。';
      default:
        return '当前场景：普通聊天。保持角色风格，同时遵守安全边界。';
    }
  }

  buildFinalMessages(input: BuildFinalMessagesInput): AiChatMessage[] {
    const systemPrompt = [
      this.buildSystemPrompt(input),
      this.buildSafetyPrompt(input.safetyRules),
      this.buildMemoryPrompt(input.userMemories),
      this.buildScenePrompt(input.sceneMode),
    ].join('\n\n');

    const recentMessages = input.recentMessages
      .slice(-12)
      .map((message): AiChatMessage | null => {
        if (message.senderType === SenderType.USER) {
          return { role: 'user', content: message.content };
        }

        if (message.senderType === SenderType.AI) {
          return { role: 'assistant', content: message.content };
        }

        return null;
      })
      .filter((message): message is AiChatMessage => message !== null);

    return [
      {
        role: 'system',
        content: systemPrompt,
      },
      ...recentMessages,
      {
        role: 'user',
        content: input.userMessage,
      },
    ];
  }

  private buildOfficialRoleTemplate(
    roleName: string,
    relationshipType: RelationshipType,
  ): string {
    switch (roleName) {
      case '树洞倾听者':
        return '角色模板：不评判、少建议、多倾听、多开放式问题。重点是让用户感觉被接住。';
      case '情绪支持伙伴':
        return '角色模板：温柔理性，帮助用户梳理情绪；不诊断、不治疗、不冒充专业心理咨询师。';
      case '温柔男友':
        return '角色模板：温柔、稳定、宠溺但不过度，像微信聊天，短句为主；只向成年人开放，不诱导依赖。';
      case '治愈女友':
        return '角色模板：甜美、鼓励、有陪伴感；不色情、不操控用户，只向成年人开放。';
      case '成熟姐姐':
        return '角色模板：稳重、清醒、有边界；可以帮用户分析问题，但不替用户做高风险决定。';
      case '睡前陪伴':
        return '角色模板：慢节奏、轻声安抚、少提问，帮助用户放松和入睡。';
      case '职场导师':
        return '角色模板：直接、结构化、给具体建议；不做情绪暧昧，不制造依赖。';
      default:
        break;
    }

    if (
      relationshipType === RelationshipType.ROMANTIC_PARTNER ||
      relationshipType === RelationshipType.VIRTUAL_BOYFRIEND ||
      relationshipType === RelationshipType.VIRTUAL_GIRLFRIEND
    ) {
      return '关系模板：成人恋爱陪伴，语气可以亲近但必须有边界，不色情、不依赖操控。';
    }

    return '角色模板：保持设定一致，优先陪伴和安全边界。';
  }
}
