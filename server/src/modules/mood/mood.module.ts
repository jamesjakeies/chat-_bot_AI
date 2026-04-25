import { Module } from '@nestjs/common';
import { AuthModule } from '../auth/auth.module';
import { SubscriptionModule } from '../subscription/subscription.module';
import { MoodController } from './mood.controller';
import { MoodService } from './mood.service';

@Module({
  imports: [AuthModule, SubscriptionModule],
  controllers: [MoodController],
  providers: [MoodService],
  exports: [MoodService],
})
export class MoodModule {}
