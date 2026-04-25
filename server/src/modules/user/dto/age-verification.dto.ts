import { Type } from 'class-transformer';
import { IsBoolean, IsInt, Max, Min } from 'class-validator';

export class AgeVerificationDto {
  @Type(() => Number)
  @IsInt()
  @Min(1900)
  @Max(2100)
  birthYear!: number;

  @IsBoolean()
  ageVerified!: boolean;

  @IsBoolean()
  guardianConsent!: boolean;
}
