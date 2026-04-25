import { ForbiddenException, Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../../infra/prisma/prisma.service';
import { CreateReportDto } from './dto/create-report.dto';

@Injectable()
export class ReportService {
  constructor(private readonly prisma: PrismaService) {}

  async createReport(userId: string, dto: CreateReportDto) {
    const message = await this.prisma.chatMessage.findFirst({
      where: {
        id: dto.messageId,
        deletedAt: null,
      },
      include: {
        session: {
          select: {
            userId: true,
          },
        },
      },
    });

    if (!message) {
      throw new NotFoundException('Message not found');
    }

    if (message.session.userId !== userId) {
      throw new ForbiddenException('You can only report messages in your own sessions.');
    }

    return this.prisma.report.create({
      data: {
        userId,
        messageId: message.id,
        reason: dto.reason.trim(),
      },
    });
  }
}
