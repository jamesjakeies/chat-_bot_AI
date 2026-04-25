import {
  MemoryType,
  MembershipLevel,
  PrismaClient,
  RelationshipType,
  RiskLevel,
  RoleCategory,
  RoleSafetyLevel,
  RoleStatus,
  SenderType,
  SensitivityLevel,
  SubscriptionPlan,
  SubscriptionStatus,
} from '@prisma/client';

const prisma = new PrismaClient();
const bcrypt = require('bcryptjs') as {
  hash(data: string, saltOrRounds: string | number): Promise<string>;
};

const demoPassword = 'Demo123456';

async function main(): Promise<void> {
  const passwordHash = await bcrypt.hash(demoPassword, 10);

  const demoUser = await prisma.user.upsert({
    where: { email: 'demo@xinyu.local' },
    update: {
      nickname: 'Demo User',
      birthYear: 2000,
      ageVerified: true,
      isMinor: false,
      guardianConsent: false,
      membershipLevel: MembershipLevel.PREMIUM,
      deletedAt: null,
    },
    create: {
      email: 'demo@xinyu.local',
      passwordHash,
      nickname: 'Demo User',
      birthYear: 2000,
      ageVerified: true,
      isMinor: false,
      guardianConsent: false,
      membershipLevel: MembershipLevel.PREMIUM,
      subscriptions: {
        create: {
          plan: SubscriptionPlan.PREMIUM,
          status: SubscriptionStatus.ACTIVE,
          startedAt: new Date(),
        },
      },
    },
  });

  await prisma.subscription.upsert({
    where: { id: 'seed-demo-premium-subscription' },
    update: {
      userId: demoUser.id,
      plan: SubscriptionPlan.PREMIUM,
      status: SubscriptionStatus.ACTIVE,
      startedAt: new Date(),
      deletedAt: null,
    },
    create: {
      id: 'seed-demo-premium-subscription',
      userId: demoUser.id,
      plan: SubscriptionPlan.PREMIUM,
      status: SubscriptionStatus.ACTIVE,
      startedAt: new Date(),
    },
  });

  const roles = await seedOfficialRoles();
  const listenerRole = roles.find((role) => role.name === '树洞倾听者') ?? roles[0];

  await prisma.userRole.upsert({
    where: {
      userId_roleId: {
        userId: demoUser.id,
        roleId: listenerRole.id,
      },
    },
    update: {
      intimacyLevel: 2,
      memoryEnabled: true,
      lastInteractionAt: new Date(),
      deletedAt: null,
    },
    create: {
      userId: demoUser.id,
      roleId: listenerRole.id,
      intimacyLevel: 2,
      memoryEnabled: true,
      lastInteractionAt: new Date(),
    },
  });

  const session = await prisma.chatSession.upsert({
    where: { id: 'seed-demo-chat-session' },
    update: {
      userId: demoUser.id,
      roleId: listenerRole.id,
      title: 'Demo chat',
      riskLevel: RiskLevel.NORMAL,
      deletedAt: null,
    },
    create: {
      id: 'seed-demo-chat-session',
      userId: demoUser.id,
      roleId: listenerRole.id,
      title: 'Demo chat',
      riskLevel: RiskLevel.NORMAL,
    },
  });

  await prisma.chatMessage.upsert({
    where: { id: 'seed-demo-message-user-1' },
    update: {
      sessionId: session.id,
      senderType: SenderType.USER,
      content: '今天有点累，想找人说说。',
      deletedAt: null,
    },
    create: {
      id: 'seed-demo-message-user-1',
      sessionId: session.id,
      senderType: SenderType.USER,
      content: '今天有点累，想找人说说。',
    },
  });

  await prisma.chatMessage.upsert({
    where: { id: 'seed-demo-message-ai-1' },
    update: {
      sessionId: session.id,
      senderType: SenderType.AI,
      content: '我会先听你说。今天最消耗你的那一部分是什么？',
      tokenCount: 18,
      deletedAt: null,
    },
    create: {
      id: 'seed-demo-message-ai-1',
      sessionId: session.id,
      senderType: SenderType.AI,
      content: '我会先听你说。今天最消耗你的那一部分是什么？',
      tokenCount: 18,
    },
  });

  await prisma.memory.upsert({
    where: { id: 'seed-demo-memory-1' },
    update: {
      userId: demoUser.id,
      roleId: listenerRole.id,
      memoryType: MemoryType.PREFERENCE,
      content: '用户更喜欢先被倾听，再听到建议。',
      sensitivityLevel: SensitivityLevel.NORMAL,
      userConsented: true,
      deletedAt: null,
    },
    create: {
      id: 'seed-demo-memory-1',
      userId: demoUser.id,
      roleId: listenerRole.id,
      memoryType: MemoryType.PREFERENCE,
      content: '用户更喜欢先被倾听，再听到建议。',
      sensitivityLevel: SensitivityLevel.NORMAL,
      userConsented: true,
    },
  });

  await prisma.moodLog.upsert({
    where: { id: 'seed-demo-mood-1' },
    update: {
      userId: demoUser.id,
      moodScore: 6,
      moodLabel: '有点累',
      pressureSources: ['工作', '睡眠'],
      note: '演示数据：今天需要轻一点的节奏。',
      deletedAt: null,
    },
    create: {
      id: 'seed-demo-mood-1',
      userId: demoUser.id,
      moodScore: 6,
      moodLabel: '有点累',
      pressureSources: ['工作', '睡眠'],
      note: '演示数据：今天需要轻一点的节奏。',
    },
  });

  console.log('Seed completed.');
  console.log('Demo account: demo@xinyu.local');
  console.log(`Demo password: ${demoPassword}`);
}

async function seedOfficialRoles() {
  const roleInputs = [
    {
      name: '树洞倾听者',
      category: RoleCategory.EMOTIONAL_SUPPORT,
      relationshipType: RelationshipType.LISTENER,
      personality: '不评判、安静、耐心',
      speechStyle: '短句、开放式提问、少建议',
      systemPrompt: 'Listen first, ask open-ended questions, do not diagnose.',
      safetyLevel: RoleSafetyLevel.STRICT,
      isAdultOnly: false,
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
    },
  ];

  const roles = [];

  for (const input of roleInputs) {
    const existing = await prisma.role.findFirst({
      where: {
        name: input.name,
        isOfficial: true,
        deletedAt: null,
      },
    });

    const data = {
      ...input,
      isOfficial: true,
      status: RoleStatus.ACTIVE,
      deletedAt: null,
    };

    if (existing) {
      roles.push(
        await prisma.role.update({
          where: { id: existing.id },
          data,
        }),
      );
    } else {
      roles.push(await prisma.role.create({ data }));
    }
  }

  return roles;
}

main()
  .catch((error) => {
    console.error(error);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
