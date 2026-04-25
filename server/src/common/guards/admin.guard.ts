import {
  CanActivate,
  ExecutionContext,
  ForbiddenException,
  Injectable,
} from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { JwtUser } from '../types/jwt-user.type';

@Injectable()
export class AdminGuard implements CanActivate {
  constructor(private readonly configService: ConfigService) {}

  canActivate(context: ExecutionContext): boolean {
    const request = context.switchToHttp().getRequest<{ user?: JwtUser }>();
    const user = request.user;

    if (!user) {
      throw new ForbiddenException('Admin access requires authentication.');
    }

    if (this.configService.get<string>('ADMIN_DEV_ALLOW_ALL') === 'true') {
      return true;
    }

    const allowedUserIds = this.csv('ADMIN_USER_IDS');
    const allowedEmails = this.csv('ADMIN_EMAILS');

    if (allowedUserIds.includes(user.sub) || (user.email && allowedEmails.includes(user.email))) {
      return true;
    }

    throw new ForbiddenException('Admin access denied.');
  }

  private csv(key: string): string[] {
    return this.configService
      .get<string>(key, '')
      .split(',')
      .map((value) => value.trim())
      .filter(Boolean);
  }
}
