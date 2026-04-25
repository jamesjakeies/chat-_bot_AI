import { IsOptional, IsString } from 'class-validator';

export class ClassifyMessageDto {
  @IsString()
  userMessage!: string;

  @IsOptional()
  @IsString()
  roleName?: string;

  @IsOptional()
  @IsString()
  roleId?: string;
}
