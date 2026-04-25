import { Body, Controller, Post } from '@nestjs/common';
import { UsageAction } from '../../common/decorators/usage-action.decorator';
import { AuthService } from './auth.service';
import { LoginDto } from './dto/login.dto';
import { RefreshTokenDto } from './dto/refresh-token.dto';
import { RegisterDto } from './dto/register.dto';

@Controller('auth')
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  @Post('register')
  @UsageAction('AUTH_REGISTER')
  async register(@Body() dto: RegisterDto) {
    return {
      success: true,
      data: await this.authService.register(dto),
    };
  }

  @Post('login')
  @UsageAction('AUTH_LOGIN')
  async login(@Body() dto: LoginDto) {
    return {
      success: true,
      data: await this.authService.login(dto),
    };
  }

  @Post('refresh')
  @UsageAction('AUTH_REFRESH')
  async refresh(@Body() dto: RefreshTokenDto) {
    return {
      success: true,
      data: await this.authService.refresh(dto),
    };
  }

  @Post('logout')
  @UsageAction('AUTH_LOGOUT')
  async logout(@Body() dto: RefreshTokenDto) {
    return {
      success: true,
      data: await this.authService.logout(dto),
    };
  }
}
