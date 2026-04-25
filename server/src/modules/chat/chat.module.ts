import { Module } from '@nestjs/common';
import { AuthModule } from '../auth/auth.module';
import { AiGatewayModule } from '../ai-gateway/ai-gateway.module';
import { PromptModule } from '../prompt/prompt.module';
import { SafetyModule } from '../safety/safety.module';
import { SubscriptionModule } from '../subscription/subscription.module';
import { UsageLogModule } from '../usage-log/usage-log.module';
import { ChatController } from './chat.controller';
import { ChatService } from './chat.service';

@Module({
  imports: [
    AuthModule,
    SafetyModule,
    AiGatewayModule,
    PromptModule,
    UsageLogModule,
    SubscriptionModule,
  ],
  controllers: [ChatController],
  providers: [ChatService],
})
export class ChatModule {}
