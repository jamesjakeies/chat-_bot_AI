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
import { ConfirmSensitiveMemoryDto } from './dto/confirm-sensitive-memory.dto';
import { CreateMemoryDto } from './dto/create-memory.dto';
import { ListMemoriesQueryDto } from './dto/list-memories-query.dto';
import { RoleMemorySettingDto } from './dto/role-memory-setting.dto';
import { UpdateMemoryDto } from './dto/update-memory.dto';
import { MemoryService } from './memory.service';

@Controller('memories')
@UseGuards(JwtAuthGuard)
export class MemoryController {
  constructor(private readonly memoryService: MemoryService) {}

  @Get()
  @UsageAction('MEMORY_LIST')
  async list(@CurrentUser() user: JwtUser, @Query() query: ListMemoriesQueryDto) {
    return {
      success: true,
      data: await this.memoryService.listMemories(user.sub, query),
    };
  }

  @Post()
  @UsageAction('MEMORY_CREATE')
  async create(@CurrentUser() user: JwtUser, @Body() dto: CreateMemoryDto) {
    return {
      success: true,
      data: await this.memoryService.createMemory(user.sub, dto),
    };
  }

  @Patch(':id')
  @UsageAction('MEMORY_UPDATE')
  async update(
    @CurrentUser() user: JwtUser,
    @Param('id') memoryId: string,
    @Body() dto: UpdateMemoryDto,
  ) {
    return {
      success: true,
      data: await this.memoryService.updateMemory(user.sub, memoryId, dto),
    };
  }

  @Delete(':id')
  @UsageAction('MEMORY_DELETE')
  async delete(@CurrentUser() user: JwtUser, @Param('id') memoryId: string) {
    return {
      success: true,
      data: await this.memoryService.deleteMemory(user.sub, memoryId),
    };
  }

  @Post('confirm-sensitive')
  @UsageAction('MEMORY_CONFIRM_SENSITIVE')
  async confirmSensitive(
    @CurrentUser() user: JwtUser,
    @Body() dto: ConfirmSensitiveMemoryDto,
  ) {
    return {
      success: true,
      data: await this.memoryService.confirmSensitive(user.sub, dto),
    };
  }

  @Patch('role-settings/:roleId')
  @UsageAction('MEMORY_ROLE_SETTING')
  async updateRoleSetting(
    @CurrentUser() user: JwtUser,
    @Param('roleId') roleId: string,
    @Body() dto: RoleMemorySettingDto,
  ) {
    return {
      success: true,
      data: await this.memoryService.updateRoleMemorySetting(user.sub, roleId, dto),
    };
  }
}
