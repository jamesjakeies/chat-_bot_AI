import { IsBoolean, IsEnum, IsOptional, IsString } from 'class-validator';
import {
  RelationshipType,
  RoleCategory,
  RoleSafetyLevel,
} from '@prisma/client';

export class UpdateCustomRoleDto {
  @IsOptional()
  @IsString()
  name?: string;

  @IsOptional()
  @IsString()
  avatarUrl?: string;

  @IsOptional()
  @IsEnum(RoleCategory)
  category?: RoleCategory;

  @IsOptional()
  @IsEnum(RelationshipType)
  relationshipType?: RelationshipType;

  @IsOptional()
  @IsString()
  personality?: string;

  @IsOptional()
  @IsString()
  speechStyle?: string;

  @IsOptional()
  @IsString()
  systemPrompt?: string;

  @IsOptional()
  @IsEnum(RoleSafetyLevel)
  safetyLevel?: RoleSafetyLevel;

  @IsOptional()
  @IsBoolean()
  isAdultOnly?: boolean;
}
