import { IsBoolean, IsString } from 'class-validator';

export class ConfirmSensitiveMemoryDto {
  @IsString()
  memoryId!: string;

  @IsBoolean()
  accepted!: boolean;
}
