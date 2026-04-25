import {
  ConflictException,
  Injectable,
  UnauthorizedException,
} from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { JwtService } from '@nestjs/jwt';
import { SubscriptionPlan, SubscriptionStatus, User } from '@prisma/client';
import * as bcrypt from 'bcryptjs';
import { randomUUID } from 'crypto';
import { PrismaService } from '../../infra/prisma/prisma.service';
import { LoginDto } from './dto/login.dto';
import { RefreshTokenDto } from './dto/refresh-token.dto';
import { RegisterDto } from './dto/register.dto';

export interface AuthPayload {
  accessToken: string;
  refreshToken: string;
  user: {
    id: string;
    email: string | null;
    nickname: string;
    isMinor: boolean;
    membershipLevel: string;
  };
}

@Injectable()
export class AuthService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly jwtService: JwtService,
    private readonly configService: ConfigService,
  ) {}

  async register(dto: RegisterDto): Promise<AuthPayload> {
    const existingUser = await this.prisma.user.findFirst({
      where: {
        OR: [{ email: dto.email }, ...(dto.phone ? [{ phone: dto.phone }] : [])],
      },
    });

    if (existingUser) {
      throw new ConflictException('Email or phone already registered');
    }

    const passwordHash = await bcrypt.hash(dto.password, 10);
    const user = await this.prisma.user.create({
      data: {
        email: dto.email,
        phone: dto.phone,
        nickname: dto.nickname,
        passwordHash,
        subscriptions: {
          create: {
            plan: SubscriptionPlan.FREE,
            status: SubscriptionStatus.ACTIVE,
            startedAt: new Date(),
          },
        },
      },
    });

    return this.issueTokens(user);
  }

  async login(dto: LoginDto): Promise<AuthPayload> {
    const user = await this.prisma.user.findFirst({
      where: {
        email: dto.email,
        deletedAt: null,
      },
    });

    if (!user) {
      throw new UnauthorizedException('Invalid credentials');
    }

    const isValid = await bcrypt.compare(dto.password, user.passwordHash);
    if (!isValid) {
      throw new UnauthorizedException('Invalid credentials');
    }

    return this.issueTokens(user);
  }

  async refresh(dto: RefreshTokenDto): Promise<AuthPayload> {
    const payload = await this.verifyRefreshToken(dto.refreshToken);
    const tokenRecord = await this.prisma.refreshToken.findUnique({
      where: { id: payload.tokenId },
      include: { user: true },
    });

    if (!tokenRecord || tokenRecord.revokedAt || tokenRecord.expiresAt < new Date()) {
      throw new UnauthorizedException('Refresh token is no longer valid');
    }

    const matches = await bcrypt.compare(dto.refreshToken, tokenRecord.tokenHash);
    if (!matches) {
      throw new UnauthorizedException('Refresh token is no longer valid');
    }

    await this.prisma.refreshToken.update({
      where: { id: tokenRecord.id },
      data: { revokedAt: new Date() },
    });

    return this.issueTokens(tokenRecord.user);
  }

  async logout(dto: RefreshTokenDto): Promise<{ success: true }> {
    const payload = await this.verifyRefreshToken(dto.refreshToken);
    await this.prisma.refreshToken.updateMany({
      where: {
        id: payload.tokenId,
        revokedAt: null,
      },
      data: {
        revokedAt: new Date(),
      },
    });
    return { success: true };
  }

  private async issueTokens(user: User): Promise<AuthPayload> {
    const accessToken = await this.jwtService.signAsync(
      {
        sub: user.id,
        email: user.email,
      },
      {
        secret: this.configService.getOrThrow<string>('JWT_SECRET'),
        expiresIn: this.configService.get<string>('ACCESS_TOKEN_TTL', '15m'),
      },
    );

    const refreshTokenId = randomUUID();
    const refreshToken = await this.jwtService.signAsync(
      {
        sub: user.id,
        tokenId: refreshTokenId,
        type: 'refresh',
      },
      {
        secret: this.configService.getOrThrow<string>('JWT_REFRESH_SECRET'),
        expiresIn: this.configService.get<string>('REFRESH_TOKEN_TTL', '30d'),
      },
    );

    await this.prisma.refreshToken.create({
      data: {
        id: refreshTokenId,
        userId: user.id,
        tokenHash: await bcrypt.hash(refreshToken, 10),
        expiresAt: this.computeRefreshExpiration(),
      },
    });

    return {
      accessToken,
      refreshToken,
      user: {
        id: user.id,
        email: user.email,
        nickname: user.nickname,
        isMinor: user.isMinor,
        membershipLevel: user.membershipLevel,
      },
    };
  }

  private async verifyRefreshToken(token: string): Promise<{
    sub: string;
    tokenId: string;
    type: string;
  }> {
    try {
      const payload = await this.jwtService.verifyAsync<{
        sub: string;
        tokenId: string;
        type: string;
      }>(token, {
        secret: this.configService.getOrThrow<string>('JWT_REFRESH_SECRET'),
      });
      if (payload.type !== 'refresh') {
        throw new UnauthorizedException('Invalid refresh token');
      }
      return payload;
    } catch {
      throw new UnauthorizedException('Invalid refresh token');
    }
  }

  private computeRefreshExpiration(): Date {
    const now = new Date();
    const ttl = this.configService.get<string>('REFRESH_TOKEN_TTL', '30d');
    const match = ttl.match(/^(\d+)([mhd])$/);

    if (!match) {
      now.setDate(now.getDate() + 30);
      return now;
    }

    const value = Number(match[1]);
    const unit = match[2];

    if (unit === 'm') {
      now.setMinutes(now.getMinutes() + value);
    } else if (unit === 'h') {
      now.setHours(now.getHours() + value);
    } else {
      now.setDate(now.getDate() + value);
    }

    return now;
  }
}
