import { Body, Controller, Get, Post, UseGuards } from '@nestjs/common';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { UsageAction } from '../../common/decorators/usage-action.decorator';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { JwtUser } from '../../common/types/jwt-user.type';
import { CreateMoodDto } from './dto/create-mood.dto';
import { MoodService } from './mood.service';

@Controller('moods')
@UseGuards(JwtAuthGuard)
export class MoodController {
  constructor(private readonly moodService: MoodService) {}

  @Post()
  @UsageAction('MOOD_CREATE')
  async create(@CurrentUser() user: JwtUser, @Body() dto: CreateMoodDto) {
    return {
      success: true,
      data: await this.moodService.createMood(user.sub, dto),
    };
  }

  @Get('recent')
  @UsageAction('MOOD_RECENT')
  async recent(@CurrentUser() user: JwtUser) {
    return {
      success: true,
      data: await this.moodService.recent(user.sub),
    };
  }

  @Get('weekly-summary')
  @UsageAction('MOOD_WEEKLY_SUMMARY')
  async weeklySummary(@CurrentUser() user: JwtUser) {
    return {
      success: true,
      data: await this.moodService.weeklySummary(user.sub),
    };
  }
}
