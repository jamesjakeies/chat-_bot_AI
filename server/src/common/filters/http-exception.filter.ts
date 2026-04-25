import {
  ArgumentsHost,
  Catch,
  ExceptionFilter,
  HttpException,
  HttpStatus,
} from '@nestjs/common';
import { Prisma } from '@prisma/client';
import { Response } from 'express';

@Catch()
export class HttpExceptionFilter implements ExceptionFilter {
  catch(exception: unknown, host: ArgumentsHost): void {
    const response = host.switchToHttp().getResponse<Response>();

    if (exception instanceof Prisma.PrismaClientKnownRequestError) {
      const status =
        exception.code === 'P2002'
          ? HttpStatus.CONFLICT
          : HttpStatus.BAD_REQUEST;
      response.status(status).json({
        success: false,
        message: this.formatPrismaError(exception),
      });
      return;
    }

    if (exception instanceof HttpException) {
      const exceptionResponse = exception.getResponse();
      const message =
        typeof exceptionResponse === 'object' &&
        exceptionResponse !== null &&
        'message' in exceptionResponse
          ? (exceptionResponse as { message?: unknown }).message
          : exception.message;

      response.status(exception.getStatus()).json({
        success: false,
        message: Array.isArray(message) ? message.join('; ') : String(message),
        details: typeof exceptionResponse === 'object' ? exceptionResponse : undefined,
      });
      return;
    }

    response.status(HttpStatus.INTERNAL_SERVER_ERROR).json({
      success: false,
      message: 'Internal server error',
    });
  }

  private formatPrismaError(
    exception: Prisma.PrismaClientKnownRequestError,
  ): string {
    if (exception.code === 'P2002') {
      return 'Resource already exists';
    }
    return 'Database request failed';
  }
}
