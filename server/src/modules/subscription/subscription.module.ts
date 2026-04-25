import { Module } from '@nestjs/common';
import { AuthModule } from '../auth/auth.module';
import { SubscriptionController } from './subscription.controller';
import { SubscriptionService } from './subscription.service';
import { UsageLimitService } from './usage-limit.service';

@Module({
  imports: [AuthModule],
  controllers: [SubscriptionController],
  providers: [SubscriptionService, UsageLimitService],
  exports: [UsageLimitService, SubscriptionService],
})
export class SubscriptionModule {}
