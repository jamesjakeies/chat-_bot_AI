import { MemoryType, SensitivityLevel } from '@prisma/client';
import { IsBoolean, IsEnum, IsOptional, IsString, MinLength } from 'class-validator';

export class CreateMemoryDto {
  @IsString()
  roleId!: string;

  @IsString()
  @MinLength(1)
  content!: string;

  @IsOptional()
  @IsEnum(MemoryType)
  memoryType?: MemoryType;

  @IsOptional()
  @IsEnum(SensitivityLevel)
  sensitivityLevel?: SensitivityLevel;

  @IsOptional()
  @IsBoolean()
  userConsented?: boolean;

  @IsOptional()
  @IsString()
  sourceMessageId?: string;
}
