import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  Post,
  UseGuards,
} from '@nestjs/common';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { UsageAction } from '../../common/decorators/usage-action.decorator';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { JwtUser } from '../../common/types/jwt-user.type';
import { ChatService } from './chat.service';
import { CreateMessageDto } from './dto/create-message.dto';
import { CreateSessionDto } from './dto/create-session.dto';

@Controller('chat')
@UseGuards(JwtAuthGuard)
export class ChatController {
  constructor(private readonly chatService: ChatService) {}

  @Post('sessions')
  @UsageAction('CHAT_CREATE_SESSION')
  async createSession(@CurrentUser() user: JwtUser, @Body() dto: CreateSessionDto) {
    return {
      success: true,
      data: await this.chatService.createSession(user.sub, dto),
    };
  }

  @Get('sessions')
  @UsageAction('CHAT_LIST_SESSIONS')
  async listSessions(@CurrentUser() user: JwtUser) {
    return {
      success: true,
      data: await this.chatService.listSessions(user.sub),
    };
  }

  @Get('sessions/:id/messages')
  @UsageAction('CHAT_GET_MESSAGES')
  async getMessages(@CurrentUser() user: JwtUser, @Param('id') sessionId: string) {
    return {
      success: true,
      data: await this.chatService.getMessages(user.sub, sessionId),
    };
  }

  @Post('sessions/:id/messages')
  @UsageAction('CHAT_SEND_MESSAGE')
  async sendMessage(
    @CurrentUser() user: JwtUser,
    @Param('id') sessionId: string,
    @Body() dto: CreateMessageDto,
  ) {
    return {
      success: true,
      data: await this.chatService.sendMessage(user.sub, sessionId, dto),
    };
  }

  @Delete('sessions/:id')
  @UsageAction('CHAT_DELETE_SESSION')
  async deleteSession(@CurrentUser() user: JwtUser, @Param('id') sessionId: string) {
    return {
      success: true,
      data: await this.chatService.deleteSession(user.sub, sessionId),
    };
  }
}
