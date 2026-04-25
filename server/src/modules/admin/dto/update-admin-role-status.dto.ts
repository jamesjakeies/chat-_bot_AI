import { RoleStatus } from '@prisma/client';
import { IsEnum } from 'class-validator';

export class UpdateAdminRoleStatusDto {
  @IsEnum(RoleStatus)
  status!: RoleStatus;
}
