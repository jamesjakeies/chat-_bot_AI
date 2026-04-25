import { Controller, Get } from '@nestjs/common';
import { PrismaService } from '../../infra/prisma/prisma.service';

@Controller('health')
export class HealthController {
  constructor(private readonly prisma: PrismaService) {}

  @Get()
  async check() {
    await this.prisma.$queryRaw`SELECT 1`;

    return {
      success: true,
      data: {
        status: 'ok',
        service: 'xinyu-ai-server',
        timestamp: new Date().toISOString(),
      },
    };
  }
}
