import { MembershipLevel, SubscriptionPlan } from '@prisma/client';

export interface MembershipLimits {
  dailyMessages: number;
  baseRoleLimit: number | null;
  customRoleLimit: number;
  memoryLimit: number;
  weeklyMoodSummary: boolean;
  proactiveGreeting: boolean;
  longContext: boolean;
}

export const MEMBERSHIP_LIMITS: Record<MembershipLevel, MembershipLimits> = {
  FREE: {
    dailyMessages: 30,
    baseRoleLimit: 3,
    customRoleLimit: 1,
    memoryLimit: 3,
    weeklyMoodSummary: false,
    proactiveGreeting: false,
    longContext: false,
  },
  MONTHLY: {
    dailyMessages: 300,
    baseRoleLimit: null,
    customRoleLimit: 10,
    memoryLimit: 100,
    weeklyMoodSummary: true,
    proactiveGreeting: true,
    longContext: false,
  },
  PREMIUM: {
    dailyMessages: 1000,
    baseRoleLimit: null,
    customRoleLimit: 50,
    memoryLimit: 1000,
    weeklyMoodSummary: true,
    proactiveGreeting: true,
    longContext: true,
  },
};

export function subscriptionPlanToMembershipLevel(
  plan: SubscriptionPlan,
): MembershipLevel {
  switch (plan) {
    case SubscriptionPlan.MONTHLY:
      return MembershipLevel.MONTHLY;
    case SubscriptionPlan.PREMIUM:
      return MembershipLevel.PREMIUM;
    case SubscriptionPlan.FREE:
    default:
      return MembershipLevel.FREE;
  }
}

export function membershipLevelToSubscriptionPlan(
  level: MembershipLevel,
): SubscriptionPlan {
  switch (level) {
    case MembershipLevel.MONTHLY:
      return SubscriptionPlan.MONTHLY;
    case MembershipLevel.PREMIUM:
      return SubscriptionPlan.PREMIUM;
    case MembershipLevel.FREE:
    default:
      return SubscriptionPlan.FREE;
  }
}

export const FREE_BASE_ROLE_NAMES = ['树洞倾听者', '情绪支持伙伴', '睡前陪伴'];
