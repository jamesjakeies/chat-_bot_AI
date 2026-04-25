import { IsOptional, IsString } from 'class-validator';

export class ListMemoriesQueryDto {
  @IsOptional()
  @IsString()
  roleId?: string;

  @IsOptional()
  @IsString()
  includePending?: string;
}
