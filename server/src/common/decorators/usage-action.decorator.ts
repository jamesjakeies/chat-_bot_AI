import { SetMetadata } from '@nestjs/common';

export const USAGE_ACTION_KEY = 'usage_action';

export const UsageAction = (action: string): MethodDecorator =>
  SetMetadata(USAGE_ACTION_KEY, action);
