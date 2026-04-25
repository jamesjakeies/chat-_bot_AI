import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import {
  AiChatMessage,
  CreateChatCompletionInput,
  CreateChatCompletionResult,
} from './ai-gateway.types';

interface OpenAiCompatibleResponse {
  choices?: Array<{
    message?: {
      content?: string;
    };
  }>;
  usage?: {
    prompt_tokens?: number;
    completion_tokens?: number;
    total_tokens?: number;
  };
  model?: string;
}

@Injectable()
export class AiGatewayService {
  private readonly logger = new Logger(AiGatewayService.name);

  constructor(private readonly configService: ConfigService) {}

  async createChatCompletion(
    input: CreateChatCompletionInput,
  ): Promise<CreateChatCompletionResult> {
    const apiKey = this.configService.get<string>('AI_API_KEY');
    const model = this.configService.get<string>('AI_MODEL') ?? 'gpt-4o-mini';

    if (!apiKey) {
      return this.buildFallbackResponse(input, model);
    }

    try {
      const baseUrl = (
        this.configService.get<string>('AI_API_BASE_URL') ??
        'https://api.openai.com/v1'
      ).replace(/\/+$/, '');
      const temperature = input.temperature ?? this.getNumberEnv('AI_TEMPERATURE', 0.7);
      const maxTokens = input.maxTokens ?? this.getNumberEnv('AI_MAX_TOKENS', 700);

      const response = await fetch(`${baseUrl}/chat/completions`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${apiKey}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          model,
          messages: input.messages,
          temperature,
          max_tokens: maxTokens,
          user: input.userId,
        }),
      });

      if (!response.ok) {
        const body = await response.text();
        this.logger.warn(
          `AI provider returned ${response.status}: ${body.slice(0, 240)}`,
        );
        return this.buildFallbackResponse(input, model);
      }

      const data = (await response.json()) as OpenAiCompatibleResponse;
      const content = data.choices?.[0]?.message?.content?.trim();

      if (!content) {
        this.logger.warn('AI provider returned an empty completion.');
        return this.buildFallbackResponse(input, model);
      }

      const inputTokens =
        data.usage?.prompt_tokens ?? this.estimateTokens(input.messages);
      const outputTokens =
        data.usage?.completion_tokens ?? this.estimateTokens([{ role: 'assistant', content }]);

      return {
        content,
        model: data.model ?? model,
        inputTokens,
        outputTokens,
        costEstimate: this.estimateCost(inputTokens, outputTokens),
        usedFallback: false,
      };
    } catch (error) {
      this.logger.warn(`AI provider call failed: ${String(error)}`);
      return this.buildFallbackResponse(input, model);
    }
  }

  private buildFallbackResponse(
    input: CreateChatCompletionInput,
    model: string,
  ): CreateChatCompletionResult {
    const roleName = input.fallbackRoleName ?? '心屿 AI';
    const userMessage = input.fallbackUserMessage ?? this.getLastUserMessage(input.messages);
    const content = this.buildLocalReply(roleName, userMessage);
    const inputTokens = this.estimateTokens(input.messages);
    const outputTokens = this.estimateTokens([{ role: 'assistant', content }]);

    return {
      content,
      model: `${model}:local-fallback`,
      inputTokens,
      outputTokens,
      costEstimate: 0,
      usedFallback: true,
    };
  }

  private buildLocalReply(roleName: string, userMessage: string): string {
    const focus = userMessage.trim() || '今天的状态';

    switch (roleName) {
      case '树洞倾听者':
        return `我先安静听你说。关于“${focus}”，现在最压着你的那一部分是什么？`;
      case '情绪支持伙伴':
        return `我们先把它放慢一点看。你提到“${focus}”，这里更像是疲惫、委屈，还是焦虑？`;
      case '温柔男友':
        return `我在。你先不用把自己撑得那么紧，关于“${focus}”，慢慢讲给我听就好。`;
      case '高冷男友':
        return `嗯，我听着。别急着自己扛，“${focus}”这件事先说最关键的一点。`;
      case '治愈女友':
        return `辛苦了。我们先轻轻抱一下这份感受，再一点点看“${focus}”要怎么放下来。`;
      case '成熟姐姐':
        return `先稳住。我们把“${focus}”拆成事实、感受和下一步，不急着下结论。`;
      case '睡前陪伴':
        return `今晚先不追着问题跑。把呼吸放慢一点，关于“${focus}”，只说一点点也可以。`;
      case '职场导师':
        return `先结构化处理：“${focus}”里有哪些事实、卡点和你能马上推进的一步？`;
      default:
        return `我会以 AI 角色的身份陪你聊。你刚刚说“${focus}”，我们可以先从最重要的感受开始。`;
    }
  }

  private getLastUserMessage(messages: AiChatMessage[]): string {
    return (
      [...messages]
        .reverse()
        .find((message) => message.role === 'user')
        ?.content.trim() || '我今天想聊聊'
    );
  }

  private getNumberEnv(key: string, fallback: number): number {
    const value = Number(this.configService.get<string>(key));
    return Number.isFinite(value) ? value : fallback;
  }

  private estimateTokens(messages: AiChatMessage[]): number {
    const text = messages.map((message) => message.content).join('\n');
    return Math.max(1, Math.ceil(text.length / 4));
  }

  private estimateCost(inputTokens: number, outputTokens: number): number {
    const inputRate = this.getNumberEnv('AI_INPUT_TOKEN_PRICE_PER_1K', 0);
    const outputRate = this.getNumberEnv('AI_OUTPUT_TOKEN_PRICE_PER_1K', 0);
    return Number(
      ((inputTokens / 1000) * inputRate + (outputTokens / 1000) * outputRate).toFixed(6),
    );
  }
}
