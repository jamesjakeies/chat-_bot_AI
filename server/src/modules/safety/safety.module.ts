import { Module } from '@nestjs/common';
import { AuthModule } from '../auth/auth.module';
import { SafetyClassifier } from './safety.classifier';
import { SafetyController } from './safety.controller';
import { SafetyService } from './safety.service';

@Module({
  imports: [AuthModule],
  controllers: [SafetyController],
  providers: [SafetyClassifier, SafetyService],
  exports: [SafetyService],
})
export class SafetyModule {}
