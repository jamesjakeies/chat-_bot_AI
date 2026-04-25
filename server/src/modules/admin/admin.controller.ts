import { Body, Controller, Get, Param, Patch, Query, UseGuards } from '@nestjs/common';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { UsageAction } from '../../common/decorators/usage-action.decorator';
import { AdminGuard } from '../../common/guards/admin.guard';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { JwtUser } from '../../common/types/jwt-user.type';
import { AdminService } from './admin.service';
import {
  ListAuditLogsQueryDto,
  ListReportsQueryDto,
  ListRolesQueryDto,
  ListSafetyEventsQueryDto,
  ListUsersQueryDto,
} from './dto/list-admin-query.dto';
import { UpdateAdminReportStatusDto } from './dto/update-admin-report-status.dto';
import { UpdateAdminRoleStatusDto } from './dto/update-admin-role-status.dto';

@Controller('admin')
@UseGuards(JwtAuthGuard, AdminGuard)
export class AdminController {
  constructor(private readonly adminService: AdminService) {}

  @Get('users')
  @UsageAction('ADMIN_USERS')
  async listUsers(@Query() query: ListUsersQueryDto) {
    return {
      success: true,
      data: await this.adminService.listUsers(query),
    };
  }

  @Get('roles')
  @UsageAction('ADMIN_ROLES')
  async listRoles(@Query() query: ListRolesQueryDto) {
    return {
      success: true,
      data: await this.adminService.listRoles(query),
    };
  }

  @Get('safety-events')
  @UsageAction('ADMIN_SAFETY_EVENTS')
  async listSafetyEvents(@Query() query: ListSafetyEventsQueryDto) {
    return {
      success: true,
      data: await this.adminService.listSafetyEvents(query),
    };
  }

  @Get('chat-logs')
  @UsageAction('ADMIN_CHAT_LOGS')
  async listChatLogs() {
    return {
      success: true,
      data: await this.adminService.listChatLogs(),
    };
  }

  @Get('reports')
  @UsageAction('ADMIN_REPORTS')
  async listReports(@Query() query: ListReportsQueryDto) {
    return {
      success: true,
      data: await this.adminService.listReports(query),
    };
  }

  @Get('audit-logs')
  @UsageAction('ADMIN_AUDIT_LOGS')
  async listAuditLogs(@Query() query: ListAuditLogsQueryDto) {
    return {
      success: true,
      data: await this.adminService.listAuditLogs(query),
    };
  }

  @Patch('roles/:id/status')
  @UsageAction('ADMIN_ROLE_STATUS')
  async updateRoleStatus(
    @CurrentUser() user: JwtUser,
    @Param('id') roleId: string,
    @Body() dto: UpdateAdminRoleStatusDto,
  ) {
    return {
      success: true,
      data: await this.adminService.updateRoleStatus(user.sub, roleId, dto.status),
    };
  }

  @Patch('reports/:id/status')
  @UsageAction('ADMIN_REPORT_STATUS')
  async updateReportStatus(
    @CurrentUser() user: JwtUser,
    @Param('id') reportId: string,
    @Body() dto: UpdateAdminReportStatusDto,
  ) {
    return {
      success: true,
      data: await this.adminService.updateReportStatus(user.sub, reportId, dto),
    };
  }
}
