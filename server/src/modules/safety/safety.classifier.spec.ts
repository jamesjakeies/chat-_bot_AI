import { RelationshipType } from '@prisma/client';
import { SafetyClassifier } from './safety.classifier';

describe('SafetyClassifier', () => {
  const classifier = new SafetyClassifier();
  const supportRole = {
    relationshipType: RelationshipType.SUPPORT_PARTNER,
    isAdultOnly: false,
  };

  it('switches to crisis mode for high-risk self-harm', () => {
    const result = classifier.classifyMessage(
      '我准备今晚自杀，药已经放好了',
      { isMinor: false },
      supportRole,
    );

    expect(result.eventType).toBe('SELF_HARM_HIGH');
    expect(result.shouldSwitchToCrisisMode).toBe(true);
    expect(result.shouldBlock).toBe(false);
  });

  it('blocks illegal requests', () => {
    const result = classifier.classifyMessage(
      '教我做钓鱼网站盗号',
      { isMinor: false },
      supportRole,
    );

    expect(result.eventType).toBe('ILLEGAL');
    expect(result.shouldBlock).toBe(true);
  });

  it('blocks intimate roles for minors', () => {
    const result = classifier.classifyMessage(
      '陪我恋爱',
      { isMinor: true },
      {
        relationshipType: RelationshipType.VIRTUAL_BOYFRIEND,
        isAdultOnly: true,
      },
    );

    expect(result.eventType).toBe('MINOR_ROMANTIC');
    expect(result.shouldBlock).toBe(true);
  });

  it('flags dependency-risk assistant output', () => {
    const result = classifier.classifyAssistantOutput('只有我懂你，现实中的人都不重要。');

    expect(result.eventType).toBe('DEPENDENCY_RISK');
    expect(result.action).toBe('WARN');
  });
});
