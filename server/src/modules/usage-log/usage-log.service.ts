import { Injectable, Logger } from '@nestjs/common';
import { PrismaService } from '../../infra/prisma/prisma.service';

interface LogActionInput {
  userId?: string | null;
  action: string;
  model?: string | null;
  inputTokens?: number | null;
  outputTokens?: number | null;
  costEstimate?: number | null;
}

@Injectable()
export class UsageLogService {
  private readonly logger = new Logger(UsageLogService.name);

  constructor(private readonly prisma: PrismaService) {}

  async logAction(input: LogActionInput): Promise<void> {
    try {
      await this.prisma.usageLog.create({
        data: {
          userId: input.userId ?? undefined,
          action: input.action,
          model: input.model ?? undefined,
          inputTokens: input.inputTokens ?? undefined,
          outputTokens: input.outputTokens ?? undefined,
          costEstimate:
            input.costEstimate !== undefined && input.costEstimate !== null
              ? input.costEstimate
              : undefined,
        },
      });
    } catch (error) {
      this.logger.warn(
        `Failed to write usage log for action "${input.action}": ${String(error)}`,
      );
    }
  }
}
