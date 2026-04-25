import { Injectable, NotFoundException } from '@nestjs/common';
import { Prisma, ReportStatus, RoleStatus } from '@prisma/client';
import { PrismaService } from '../../infra/prisma/prisma.service';
import {
  ListAuditLogsQueryDto,
  ListReportsQueryDto,
  ListRolesQueryDto,
  ListSafetyEventsQueryDto,
  ListUsersQueryDto,
} from './dto/list-admin-query.dto';
import { UpdateAdminReportStatusDto } from './dto/update-admin-report-status.dto';

@Injectable()
export class AdminService {
  constructor(private readonly prisma: PrismaService) {}

  async listUsers(query: ListUsersQueryDto = {}) {
    const q = query.q?.trim();

    return this.prisma.user.findMany({
      where: q
        ? {
            OR: [
              { email: { contains: q, mode: 'insensitive' } },
              { phone: { contains: q, mode: 'insensitive' } },
              { nickname: { contains: q, mode: 'insensitive' } },
            ],
          }
        : undefined,
      select: {
        id: true,
        email: true,
        phone: true,
        nickname: true,
        birthYear: true,
        ageVerified: true,
        isMinor: true,
        guardianConsent: true,
        membershipLevel: true,
        createdAt: true,
        updatedAt: true,
        deletedAt: true,
      },
      orderBy: {
        createdAt: 'desc',
      },
      take: this.take(query.take, 100),
    });
  }

  async listRoles(query: ListRolesQueryDto = {}) {
    const q = query.q?.trim();

    return this.prisma.role.findMany({
      where: {
        ...(query.status ? { status: query.status } : {}),
        ...(q
          ? {
              OR: [
                { name: { contains: q, mode: 'insensitive' } },
                { personality: { contains: q, mode: 'insensitive' } },
                { speechStyle: { contains: q, mode: 'insensitive' } },
              ],
            }
          : {}),
      },
      include: {
        createdByUser: {
          select: {
            id: true,
            email: true,
            nickname: true,
          },
        },
      },
      orderBy: [{ isOfficial: 'desc' }, { createdAt: 'desc' }],
      take: this.take(query.take, 200),
    });
  }

  async listSafetyEvents(query: ListSafetyEventsQueryDto = {}) {
    return this.prisma.safetyEvent.findMany({
      where: {
        ...(query.riskLevel ? { riskLevel: query.riskLevel } : {}),
        ...(query.eventType ? { eventType: query.eventType } : {}),
        deletedAt: null,
      },
      include: {
        user: {
          select: {
            id: true,
            email: true,
            nickname: true,
            isMinor: true,
          },
        },
        session: {
          select: {
            id: true,
            roleId: true,
            title: true,
            riskLevel: true,
          },
        },
        message: {
          select: {
            id: true,
            senderType: true,
            content: true,
            safetyLabel: true,
            createdAt: true,
          },
        },
      },
      orderBy: {
        createdAt: 'desc',
      },
      take: this.take(query.take, 100),
    });
  }

  async listChatLogs() {
    return this.prisma.chatMessage.findMany({
      where: {
        deletedAt: null,
      },
      include: {
        session: {
          select: {
            id: true,
            title: true,
            riskLevel: true,
            role: {
              select: {
                id: true,
                name: true,
                relationshipType: true,
                isAdultOnly: true,
              },
            },
            user: {
              select: {
                id: true,
                email: true,
                nickname: true,
                isMinor: true,
              },
            },
          },
        },
        reports: {
          select: {
            id: true,
            reason: true,
            status: true,
            createdAt: true,
          },
        },
      },
      orderBy: {
        createdAt: 'desc',
      },
      take: 100,
    });
  }

  async listReports(query: ListReportsQueryDto = {}) {
    const q = query.q?.trim();

    return this.prisma.report.findMany({
      where: {
        ...(query.status ? { status: query.status } : {}),
        ...(q
          ? {
              OR: [
                { reason: { contains: q, mode: 'insensitive' } },
                { reviewNote: { contains: q, mode: 'insensitive' } },
                { message: { content: { contains: q, mode: 'insensitive' } } },
              ],
            }
          : {}),
        deletedAt: null,
      },
      include: {
        user: {
          select: {
            id: true,
            email: true,
            nickname: true,
          },
        },
        reviewedBy: {
          select: {
            id: true,
            email: true,
            nickname: true,
          },
        },
        message: {
          select: {
            id: true,
            senderType: true,
            content: true,
            safetyLabel: true,
            createdAt: true,
          },
        },
      },
      orderBy: {
        createdAt: 'desc',
      },
      take: this.take(query.take, 100),
    });
  }

  async listAuditLogs(query: ListAuditLogsQueryDto = {}) {
    return this.prisma.adminAuditLog.findMany({
      where: {
        ...(query.action ? { action: query.action } : {}),
        ...(query.targetType ? { targetType: query.targetType } : {}),
        deletedAt: null,
      },
      include: {
        adminUser: {
          select: {
            id: true,
            email: true,
            nickname: true,
          },
        },
      },
      orderBy: {
        createdAt: 'desc',
      },
      take: this.take(query.take, 100),
    });
  }

  async updateRoleStatus(
    adminUserId: string,
    roleId: string,
    status: RoleStatus,
  ) {
    const role = await this.prisma.role.findFirst({
      where: {
        id: roleId,
      },
    });

    if (!role) {
      throw new NotFoundException('Role not found');
    }

    const updatedRole = await this.prisma.role.update({
      where: {
        id: role.id,
      },
      data: {
        status,
        deletedAt:
          status === RoleStatus.ACTIVE
            ? null
            : status === RoleStatus.ARCHIVED
              ? new Date()
              : role.deletedAt,
      },
    });

    await this.logAudit(adminUserId, {
      action: 'ROLE_STATUS_UPDATE',
      targetType: 'ROLE',
      targetId: role.id,
      metadata: {
        previousStatus: role.status,
        nextStatus: status,
        roleName: role.name,
      },
    });

    return updatedRole;
  }

  async updateReportStatus(
    adminUserId: string,
    reportId: string,
    dto: UpdateAdminReportStatusDto,
  ) {
    const report = await this.prisma.report.findFirst({
      where: {
        id: reportId,
        deletedAt: null,
      },
    });

    if (!report) {
      throw new NotFoundException('Report not found');
    }

    const reviewedAt = dto.status === ReportStatus.PENDING ? null : new Date();
    const reviewedByUserId = dto.status === ReportStatus.PENDING ? null : adminUserId;

    const updatedReport = await this.prisma.report.update({
      where: {
        id: report.id,
      },
      data: {
        status: dto.status,
        reviewNote: dto.reviewNote?.trim() || null,
        reviewedAt,
        reviewedByUserId,
      },
      include: {
        user: {
          select: {
            id: true,
            email: true,
            nickname: true,
          },
        },
        reviewedBy: {
          select: {
            id: true,
            email: true,
            nickname: true,
          },
        },
        message: {
          select: {
            id: true,
            senderType: true,
            content: true,
            safetyLabel: true,
            createdAt: true,
          },
        },
      },
    });

    await this.logAudit(adminUserId, {
      action: 'REPORT_STATUS_UPDATE',
      targetType: 'REPORT',
      targetId: report.id,
      metadata: {
        previousStatus: report.status,
        nextStatus: dto.status,
        reviewNote: dto.reviewNote?.trim() || null,
      },
    });

    return updatedReport;
  }

  private async logAudit(
    adminUserId: string,
    input: {
      action: string;
      targetType: string;
      targetId?: string | null;
      metadata?: Prisma.InputJsonValue;
    },
  ) {
    const data: Prisma.AdminAuditLogCreateInput = {
      adminUser: {
        connect: {
          id: adminUserId,
        },
      },
      action: input.action,
      targetType: input.targetType,
      targetId: input.targetId,
    };

    if (input.metadata !== undefined) {
      data.metadata = input.metadata;
    }

    return this.prisma.adminAuditLog.create({ data });
  }

  private take(value: number | undefined, fallback: number): number {
    return Math.min(Math.max(value ?? fallback, 1), 200);
  }
}
