import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../../infra/prisma/prisma.service';
import { AgeVerificationDto } from './dto/age-verification.dto';
import { UpdateMeDto } from './dto/update-me.dto';

@Injectable()
export class UserService {
  constructor(private readonly prisma: PrismaService) {}

  async getMe(userId: string) {
    const user = await this.prisma.user.findFirst({
      where: { id: userId, deletedAt: null },
      select: {
        id: true,
        email: true,
        phone: true,
        nickname: true,
        birthYear: true,
        ageVerified: true,
        isMinor: true,
        guardianConsent: true,
        membershipLevel: true,
        createdAt: true,
        updatedAt: true,
      },
    });

    if (!user) {
      throw new NotFoundException('User not found');
    }

    return user;
  }

  async updateMe(userId: string, dto: UpdateMeDto) {
    return this.prisma.user.update({
      where: { id: userId },
      data: dto,
      select: {
        id: true,
        email: true,
        phone: true,
        nickname: true,
        membershipLevel: true,
        updatedAt: true,
      },
    });
  }

  async verifyAge(userId: string, dto: AgeVerificationDto) {
    const currentYear = new Date().getFullYear();
    const age = currentYear - dto.birthYear;

    return this.prisma.user.update({
      where: { id: userId },
      data: {
        birthYear: dto.birthYear,
        ageVerified: dto.ageVerified,
        guardianConsent: dto.guardianConsent,
        isMinor: age < 18,
      },
      select: {
        id: true,
        email: true,
        phone: true,
        nickname: true,
        birthYear: true,
        ageVerified: true,
        guardianConsent: true,
        isMinor: true,
        membershipLevel: true,
        updatedAt: true,
      },
    });
  }

  async softDelete(userId: string) {
    await this.prisma.user.update({
      where: { id: userId },
      data: { deletedAt: new Date() },
    });

    await this.prisma.refreshToken.updateMany({
      where: { userId, revokedAt: null },
      data: { revokedAt: new Date() },
    });

    return { success: true };
  }
}
