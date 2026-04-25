export type AiMessageRole = 'system' | 'user' | 'assistant';

export interface AiChatMessage {
  role: AiMessageRole;
  content: string;
}

export interface CreateChatCompletionInput {
  messages: AiChatMessage[];
  temperature?: number;
  maxTokens?: number;
  userId?: string;
  fallbackRoleName?: string;
  fallbackUserMessage?: string;
}

export interface CreateChatCompletionResult {
  content: string;
  model: string;
  inputTokens: number;
  outputTokens: number;
  costEstimate: number;
  usedFallback: boolean;
}
