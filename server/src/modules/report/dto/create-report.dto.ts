import { IsString, MinLength } from 'class-validator';

export class CreateReportDto {
  @IsString()
  messageId!: string;

  @IsString()
  @MinLength(2)
  reason!: string;
}
