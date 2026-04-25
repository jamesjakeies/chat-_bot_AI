import {
  Body,
  Controller,
  Delete,
  Get,
  Patch,
  UseGuards,
} from '@nestjs/common';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { UsageAction } from '../../common/decorators/usage-action.decorator';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { JwtUser } from '../../common/types/jwt-user.type';
import { AgeVerificationDto } from './dto/age-verification.dto';
import { UpdateMeDto } from './dto/update-me.dto';
import { UserService } from './user.service';

@Controller('users')
@UseGuards(JwtAuthGuard)
export class UserController {
  constructor(private readonly userService: UserService) {}

  @Get('me')
  @UsageAction('USER_GET_ME')
  async getMe(@CurrentUser() user: JwtUser) {
    return {
      success: true,
      data: await this.userService.getMe(user.sub),
    };
  }

  @Patch('me')
  @UsageAction('USER_UPDATE_ME')
  async updateMe(@CurrentUser() user: JwtUser, @Body() dto: UpdateMeDto) {
    return {
      success: true,
      data: await this.userService.updateMe(user.sub, dto),
    };
  }

  @Patch('me/age-verification')
  @UsageAction('USER_AGE_VERIFICATION')
  async verifyAge(@CurrentUser() user: JwtUser, @Body() dto: AgeVerificationDto) {
    return {
      success: true,
      data: await this.userService.verifyAge(user.sub, dto),
    };
  }

  @Delete('me')
  @UsageAction('USER_DELETE_ME')
  async deleteMe(@CurrentUser() user: JwtUser) {
    return {
      success: true,
      data: await this.userService.softDelete(user.sub),
    };
  }
}
