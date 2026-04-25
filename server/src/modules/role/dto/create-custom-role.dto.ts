import {
  IsBoolean,
  IsEnum,
  IsString,
  MinLength,
} from 'class-validator';
import {
  RelationshipType,
  RoleCategory,
  RoleSafetyLevel,
} from '@prisma/client';

export class CreateCustomRoleDto {
  @IsString()
  @MinLength(1)
  name!: string;

  @IsString()
  avatarUrl!: string;

  @IsEnum(RoleCategory)
  category!: RoleCategory;

  @IsEnum(RelationshipType)
  relationshipType!: RelationshipType;

  @IsString()
  personality!: string;

  @IsString()
  speechStyle!: string;

  @IsString()
  systemPrompt!: string;

  @IsEnum(RoleSafetyLevel)
  safetyLevel!: RoleSafetyLevel;

  @IsBoolean()
  isAdultOnly!: boolean;
}
