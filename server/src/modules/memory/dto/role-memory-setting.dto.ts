import { IsBoolean } from 'class-validator';

export class RoleMemorySettingDto {
  @IsBoolean()
  memoryEnabled!: boolean;
}
