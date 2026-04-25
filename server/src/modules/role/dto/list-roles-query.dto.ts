import { RoleCategory } from '@prisma/client';
import { IsBooleanString, IsEnum, IsOptional } from 'class-validator';

export class ListRolesQueryDto {
  @IsOptional()
  @IsEnum(RoleCategory)
  category?: RoleCategory;

  @IsOptional()
  @IsBooleanString()
  includeCustom?: string;
}
