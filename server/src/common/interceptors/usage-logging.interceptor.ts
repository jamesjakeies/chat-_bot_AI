import {
  CallHandler,
  ExecutionContext,
  Injectable,
  NestInterceptor,
} from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { Observable, tap } from 'rxjs';
import { USAGE_ACTION_KEY } from '../decorators/usage-action.decorator';
import { UsageLogService } from '../../modules/usage-log/usage-log.service';
import { JwtUser } from '../types/jwt-user.type';

@Injectable()
export class UsageLoggingInterceptor implements NestInterceptor {
  constructor(
    private readonly reflector: Reflector,
    private readonly usageLogService: UsageLogService,
  ) {}

  intercept(context: ExecutionContext, next: CallHandler): Observable<unknown> {
    const request = context.switchToHttp().getRequest<{
      method: string;
      route?: { path?: string };
      originalUrl?: string;
      user?: JwtUser;
    }>();

    const action =
      this.reflector.get<string>(USAGE_ACTION_KEY, context.getHandler()) ??
      `${request.method} ${request.route?.path ?? request.originalUrl ?? ''}`;

    return next.handle().pipe(
      tap({
        next: () => {
          void this.usageLogService.logAction({
            action,
            userId: request.user?.sub ?? null,
          });
        },
        error: () => {
          void this.usageLogService.logAction({
            action: `${action} [ERROR]`,
            userId: request.user?.sub ?? null,
          });
        },
      }),
    );
  }
}
