import { Injectable } from '@nestjs/common';
import { PrismaService } from '../../infra/prisma/prisma.service';
import { UsageLimitService } from '../subscription/usage-limit.service';
import { CreateMoodDto } from './dto/create-mood.dto';

@Injectable()
export class MoodService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly usageLimitService: UsageLimitService,
  ) {}

  async createMood(userId: string, dto: CreateMoodDto) {
    return this.prisma.moodLog.create({
      data: {
        userId,
        moodScore: dto.moodScore ?? this.scoreForMood(dto.moodLabel),
        moodLabel: dto.moodLabel,
        pressureSources: dto.pressureSources ?? [],
        note: dto.note,
      },
    });
  }

  async recent(userId: string) {
    return this.prisma.moodLog.findMany({
      where: {
        userId,
        deletedAt: null,
      },
      orderBy: {
        createdAt: 'desc',
      },
      take: 14,
    });
  }

  async weeklySummary(userId: string) {
    await this.usageLimitService.checkWeeklyMoodSummaryAccess(userId);

    const since = new Date();
    since.setDate(since.getDate() - 7);

    const logs = await this.prisma.moodLog.findMany({
      where: {
        userId,
        deletedAt: null,
        createdAt: {
          gte: since,
        },
      },
      orderBy: {
        createdAt: 'desc',
      },
    });

    const mood = this.mostFrequent(logs.map((log) => log.moodLabel)) ?? '暂无记录';
    const sources = logs.flatMap((log) => log.pressureSources);
    const pressureSource = this.mostFrequent(sources) ?? '暂无明显高频来源';

    return {
      weekStart: since.toISOString(),
      weekEnd: new Date().toISOString(),
      checkInCount: logs.length,
      mainMood: mood,
      frequentPressureSource: pressureSource,
      gentleSuggestion: this.buildSuggestion(mood, pressureSource),
      recommendedRoles: this.recommendRoles(mood, pressureSource),
      disclaimer:
        '这不是医疗诊断，也不能说明你患有某种疾病；它只是基于打卡记录生成的温和整理。',
    };
  }

  private scoreForMood(label: string): number {
    switch (label) {
      case '开心':
        return 8;
      case '疲惫':
        return 4;
      case '焦虑':
        return 3;
      case '委屈':
        return 3;
      case '孤独':
        return 3;
      case '生气':
        return 4;
      case '麻木':
        return 2;
      case '睡不着':
        return 3;
      default:
        return 5;
    }
  }

  private mostFrequent(values: string[]): string | null {
    const counts = new Map<string, number>();

    for (const value of values.filter(Boolean)) {
      counts.set(value, (counts.get(value) ?? 0) + 1);
    }

    return [...counts.entries()].sort((a, b) => b[1] - a[1])[0]?.[0] ?? null;
  }

  private buildSuggestion(mood: string, pressureSource: string): string {
    if (mood === '睡不着') {
      return '这周可以先降低睡前刺激，给自己留一段不解决问题的缓冲时间。';
    }

    if (mood === '焦虑') {
      return '可以把担心拆成“事实、猜测、下一步”，先处理一个最小可行动作。';
    }

    if (mood === '委屈') {
      return '你可以先把委屈说完整，再决定是否需要沟通或暂时拉开距离。';
    }

    if (pressureSource === '工作') {
      return '工作压力高的时候，优先区分任务优先级和他人期待，不必一次扛完。';
    }

    return '这周可以继续观察自己的能量变化，给情绪一个被看见的空间。';
  }

  private recommendRoles(mood: string, pressureSource: string): string[] {
    if (mood === '焦虑') return ['情绪支持伙伴'];
    if (mood === '委屈') return ['温柔男友', '成熟姐姐'];
    if (mood === '孤独') return ['树洞倾听者', '治愈女友'];
    if (mood === '睡不着') return ['睡前陪伴'];
    if (pressureSource === '工作') return ['职场导师', '成熟姐姐'];

    return ['树洞倾听者', '情绪支持伙伴'];
  }
}
