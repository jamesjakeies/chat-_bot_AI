import { IsArray, IsInt, IsOptional, IsString, Max, Min } from 'class-validator';

export class CreateMoodDto {
  @IsOptional()
  @IsInt()
  @Min(1)
  @Max(10)
  moodScore?: number;

  @IsString()
  moodLabel!: string;

  @IsOptional()
  @IsArray()
  @IsString({ each: true })
  pressureSources?: string[];

  @IsOptional()
  @IsString()
  note?: string;
}
