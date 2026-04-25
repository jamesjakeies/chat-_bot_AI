import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  Patch,
  Post,
  Query,
  UseGuards,
} from '@nestjs/common';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { UsageAction } from '../../common/decorators/usage-action.decorator';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { JwtUser } from '../../common/types/jwt-user.type';
import { CreateCustomRoleDto } from './dto/create-custom-role.dto';
import { ListRolesQueryDto } from './dto/list-roles-query.dto';
import { UpdateCustomRoleDto } from './dto/update-custom-role.dto';
import { RoleService } from './role.service';

@Controller('roles')
@UseGuards(JwtAuthGuard)
export class RoleController {
  constructor(private readonly roleService: RoleService) {}

  @Get()
  @UsageAction('ROLE_LIST')
  async listRoles(
    @CurrentUser() user: JwtUser,
    @Query() query: ListRolesQueryDto,
  ) {
    return {
      success: true,
      data: await this.roleService.listRoles(user.sub, query),
    };
  }

  @Get(':id')
  @UsageAction('ROLE_DETAIL')
  async getRole(@CurrentUser() user: JwtUser, @Param('id') roleId: string) {
    return {
      success: true,
      data: await this.roleService.getRoleById(user.sub, roleId),
    };
  }

  @Post('custom')
  @UsageAction('ROLE_CREATE_CUSTOM')
  async createCustomRole(
    @CurrentUser() user: JwtUser,
    @Body() dto: CreateCustomRoleDto,
  ) {
    return {
      success: true,
      data: await this.roleService.createCustomRole(user.sub, dto),
    };
  }

  @Patch('custom/:id')
  @UsageAction('ROLE_UPDATE_CUSTOM')
  async updateCustomRole(
    @CurrentUser() user: JwtUser,
    @Param('id') roleId: string,
    @Body() dto: UpdateCustomRoleDto,
  ) {
    return {
      success: true,
      data: await this.roleService.updateCustomRole(user.sub, roleId, dto),
    };
  }

  @Delete('custom/:id')
  @UsageAction('ROLE_DELETE_CUSTOM')
  async deleteCustomRole(@CurrentUser() user: JwtUser, @Param('id') roleId: string) {
    return {
      success: true,
      data: await this.roleService.deleteCustomRole(user.sub, roleId),
    };
  }
}
