import { Body, Controller, Get, Post, UseGuards } from '@nestjs/common';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { UsageAction } from '../../common/decorators/usage-action.decorator';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { JwtUser } from '../../common/types/jwt-user.type';
import { MockUpgradeDto } from './dto/mock-upgrade.dto';
import { SubscriptionService } from './subscription.service';

@Controller('subscriptions')
@UseGuards(JwtAuthGuard)
export class SubscriptionController {
  constructor(private readonly subscriptionService: SubscriptionService) {}

  @Get('me')
  @UsageAction('SUBSCRIPTION_ME')
  async getMe(@CurrentUser() user: JwtUser) {
    return {
      success: true,
      data: await this.subscriptionService.getMe(user.sub),
    };
  }

  @Post('mock-upgrade')
  @UsageAction('SUBSCRIPTION_MOCK_UPGRADE')
  async mockUpgrade(@CurrentUser() user: JwtUser, @Body() dto: MockUpgradeDto) {
    return {
      success: true,
      data: await this.subscriptionService.mockUpgrade(user.sub, dto),
    };
  }
}
