import { IsOptional, IsString, MinLength } from 'class-validator';

export class CreateSessionDto {
  @IsString()
  roleId!: string;

  @IsOptional()
  @IsString()
  @MinLength(1)
  title?: string;
}
