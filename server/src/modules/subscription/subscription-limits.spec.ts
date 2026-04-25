import { MembershipLevel, SubscriptionPlan } from '@prisma/client';
import {
  FREE_BASE_ROLE_NAMES,
  MEMBERSHIP_LIMITS,
  membershipLevelToSubscriptionPlan,
  subscriptionPlanToMembershipLevel,
} from './subscription-limits';

describe('subscription limits', () => {
  it('defines the expected free, monthly, and premium quotas', () => {
    expect(MEMBERSHIP_LIMITS.FREE.dailyMessages).toBe(30);
    expect(MEMBERSHIP_LIMITS.FREE.baseRoleLimit).toBe(3);
    expect(MEMBERSHIP_LIMITS.MONTHLY.dailyMessages).toBe(300);
    expect(MEMBERSHIP_LIMITS.MONTHLY.weeklyMoodSummary).toBe(true);
    expect(MEMBERSHIP_LIMITS.PREMIUM.dailyMessages).toBe(1000);
    expect(MEMBERSHIP_LIMITS.PREMIUM.longContext).toBe(true);
  });

  it('maps subscription plans and membership levels consistently', () => {
    expect(subscriptionPlanToMembershipLevel(SubscriptionPlan.MONTHLY)).toBe(
      MembershipLevel.MONTHLY,
    );
    expect(membershipLevelToSubscriptionPlan(MembershipLevel.PREMIUM)).toBe(
      SubscriptionPlan.PREMIUM,
    );
  });

  it('keeps the free official role allowlist explicit', () => {
    expect(FREE_BASE_ROLE_NAMES).toEqual(['树洞倾听者', '情绪支持伙伴', '睡前陪伴']);
  });
});
