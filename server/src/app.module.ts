import { Module } from '@nestjs/common';
import { APP_FILTER, APP_INTERCEPTOR } from '@nestjs/core';
import { ConfigModule } from '@nestjs/config';
import { PrismaModule } from './infra/prisma/prisma.module';
import { HttpExceptionFilter } from './common/filters/http-exception.filter';
import { UsageLoggingInterceptor } from './common/interceptors/usage-logging.interceptor';
import { AuthModule } from './modules/auth/auth.module';
import { UserModule } from './modules/user/user.module';
import { RoleModule } from './modules/role/role.module';
import { ChatModule } from './modules/chat/chat.module';
import { SafetyModule } from './modules/safety/safety.module';
import { UsageLogModule } from './modules/usage-log/usage-log.module';
import { MemoryModule } from './modules/memory/memory.module';
import { MoodModule } from './modules/mood/mood.module';
import { SubscriptionModule } from './modules/subscription/subscription.module';
import { AdminModule } from './modules/admin/admin.module';
import { ReportModule } from './modules/report/report.module';
import { HealthModule } from './modules/health/health.module';

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
      envFilePath: '.env',
    }),
    PrismaModule,
    UsageLogModule,
    SubscriptionModule,
    SafetyModule,
    AuthModule,
    UserModule,
    RoleModule,
    ChatModule,
    MemoryModule,
    MoodModule,
    ReportModule,
    AdminModule,
    HealthModule,
  ],
  providers: [
    {
      provide: APP_FILTER,
      useClass: HttpExceptionFilter,
    },
    {
      provide: APP_INTERCEPTOR,
      useClass: UsageLoggingInterceptor,
    },
  ],
})
export class AppModule {}
