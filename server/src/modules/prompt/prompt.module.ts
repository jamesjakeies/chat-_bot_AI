import { Module } from '@nestjs/common';
import { RolePromptBuilder } from './role-prompt-builder.service';

@Module({
  providers: [RolePromptBuilder],
  exports: [RolePromptBuilder],
})
export class PromptModule {}
