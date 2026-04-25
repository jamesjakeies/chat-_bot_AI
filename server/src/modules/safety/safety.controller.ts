import { Body, Controller, Get, Post, UseGuards } from '@nestjs/common';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { UsageAction } from '../../common/decorators/usage-action.decorator';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { JwtUser } from '../../common/types/jwt-user.type';
import { ClassifyMessageDto } from './dto/classify-message.dto';
import { SafetyService } from './safety.service';

@Controller('safety')
@UseGuards(JwtAuthGuard)
export class SafetyController {
  constructor(private readonly safetyService: SafetyService) {}

  @Post('classify')
  @UsageAction('SAFETY_CLASSIFY')
  async classify(@CurrentUser() user: JwtUser, @Body() dto: ClassifyMessageDto) {
    return {
      success: true,
      data: await this.safetyService.classifyForUser(
        user.sub,
        dto.userMessage,
        dto.roleId,
      ),
    };
  }

  @Get('events')
  @UsageAction('SAFETY_EVENTS')
  async listEvents(@CurrentUser() user: JwtUser) {
    return {
      success: true,
      data: await this.safetyService.listEvents(user.sub),
    };
  }
}
