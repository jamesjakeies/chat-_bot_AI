import { SubscriptionPlan } from '@prisma/client';
import { IsEnum } from 'class-validator';

export class MockUpgradeDto {
  @IsEnum(SubscriptionPlan)
  plan!: SubscriptionPlan;
}
