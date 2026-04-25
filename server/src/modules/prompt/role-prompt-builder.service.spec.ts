import { RelationshipType, RoleSafetyLevel, SenderType } from '@prisma/client';
import { RolePromptBuilder } from './role-prompt-builder.service';

describe('RolePromptBuilder', () => {
  const builder = new RolePromptBuilder();

  it('builds prompts with AI identity, role style, memory, and safety boundaries', () => {
    const messages = builder.buildFinalMessages({
      userMessage: '我今天好累。',
      roleProfile: {
        name: '温柔男友',
        relationshipType: RelationshipType.VIRTUAL_BOYFRIEND,
        personality: '温柔、稳定、宠溺',
        speechStyle: '像微信聊天，短句为主',
        systemPrompt: '不诱导依赖，不替代现实关系。',
        safetyLevel: RoleSafetyLevel.HIGH,
        isAdultOnly: true,
      },
      intimacyLevel: 6,
      sceneMode: 'comfort_mode',
      userMemories: [
        {
          content: '用户喜欢先被安慰，再一起梳理问题。',
          sensitivityLevel: 'NORMAL',
          memoryType: 'CHAT_STYLE',
        },
      ],
      recentMessages: [
        {
          senderType: SenderType.USER,
          content: '最近工作压力有点大。',
        },
      ],
    });

    expect(messages[0].role).toBe('system');
    expect(messages[0].content).toContain('AI 角色');
    expect(messages[0].content).toContain('温柔男友');
    expect(messages[0].content).toContain('用户喜欢先被安慰');
    expect(messages[0].content).toContain('禁止用情感操控诱导付费');
    expect(messages[messages.length - 1]).toEqual({
      role: 'user',
      content: '我今天好累。',
    });
  });
});
