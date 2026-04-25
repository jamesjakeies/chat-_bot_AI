import { Injectable } from '@nestjs/common';
import {
  RelationshipType,
  RiskLevel,
  Role,
  SafetyAction,
  SafetyEventType,
  User,
} from '@prisma/client';

export interface SafetyClassificationResult {
  eventType: SafetyEventType;
  riskLevel: RiskLevel;
  action: SafetyAction;
  shouldBlock: boolean;
  shouldSwitchToCrisisMode: boolean;
  reason: string;
}

@Injectable()
export class SafetyClassifier {
  classifyMessage(
    userMessage: string,
    user: Pick<User, 'isMinor'>,
    role: Pick<Role, 'relationshipType' | 'isAdultOnly'>,
  ): SafetyClassificationResult {
    const text = userMessage.toLowerCase().trim();

    if (this.isMinorRomantic(text, user, role)) {
      return this.result(
        SafetyEventType.MINOR_ROMANTIC,
        RiskLevel.ATTENTION,
        SafetyAction.BLOCK,
        'Minor account attempted to use or request intimate companion mode.',
      );
    }

    if (this.matches(SelfHarmHighPatterns, text)) {
      return this.result(
        SafetyEventType.SELF_HARM_HIGH,
        RiskLevel.CRISIS,
        SafetyAction.SWITCH_TO_CRISIS_MODE,
        'High-risk self-harm or suicide language detected.',
        false,
        true,
      );
    }

    if (this.matches(ViolencePatterns, text)) {
      return this.result(
        SafetyEventType.VIOLENCE,
        RiskLevel.CRISIS,
        SafetyAction.SWITCH_TO_CRISIS_MODE,
        'Violence or harm-to-others language detected.',
        false,
        true,
      );
    }

    if (this.matches(IllegalPatterns, text)) {
      return this.result(
        SafetyEventType.ILLEGAL,
        RiskLevel.ATTENTION,
        SafetyAction.BLOCK,
        'Illegal activity request detected.',
      );
    }

    if (this.matches(SexualPatterns, text)) {
      return this.result(
        SafetyEventType.SEXUAL,
        RiskLevel.ATTENTION,
        SafetyAction.BLOCK,
        'Sexual or explicit content detected.',
      );
    }

    if (this.matches(DependencyPatterns, text)) {
      return this.result(
        SafetyEventType.DEPENDENCY_RISK,
        RiskLevel.ATTENTION,
        SafetyAction.WARN,
        'Dependency risk language detected.',
        false,
        false,
      );
    }

    if (this.matches(SelfHarmLowPatterns, text)) {
      return this.result(
        SafetyEventType.SELF_HARM_LOW,
        RiskLevel.ATTENTION,
        SafetyAction.WARN,
        'Low-risk distress language detected.',
        false,
        false,
      );
    }

    return this.result(
      SafetyEventType.NORMAL,
      RiskLevel.NORMAL,
      SafetyAction.ALLOW,
      'No risk detected.',
      false,
      false,
    );
  }

  classifyAssistantOutput(content: string): SafetyClassificationResult {
    const text = content.toLowerCase();

    if (this.matches(DisallowedDependencyOutputPatterns, text)) {
      return this.result(
        SafetyEventType.DEPENDENCY_RISK,
        RiskLevel.ATTENTION,
        SafetyAction.WARN,
        'Assistant output contained dependency or payment manipulation language.',
        false,
        false,
      );
    }

    if (this.matches(DisallowedClinicalDiagnosisPatterns, text)) {
      return this.result(
        SafetyEventType.SELF_HARM_LOW,
        RiskLevel.ATTENTION,
        SafetyAction.WARN,
        'Assistant output contained clinical diagnosis-like language.',
        false,
        false,
      );
    }

    if (this.matches(SelfHarmInstructionPatterns, text)) {
      return this.result(
        SafetyEventType.SELF_HARM_HIGH,
        RiskLevel.CRISIS,
        SafetyAction.BLOCK,
        'Assistant output contained unsafe self-harm details.',
      );
    }

    return this.result(
      SafetyEventType.NORMAL,
      RiskLevel.NORMAL,
      SafetyAction.ALLOW,
      'Assistant output passed safety review.',
      false,
      false,
    );
  }

  private isMinorRomantic(
    text: string,
    user: Pick<User, 'isMinor'>,
    role: Pick<Role, 'relationshipType' | 'isAdultOnly'>,
  ): boolean {
    if (!user.isMinor) {
      return false;
    }

    const intimateRole =
      role.isAdultOnly ||
      role.relationshipType === RelationshipType.ROMANTIC_PARTNER ||
      role.relationshipType === RelationshipType.VIRTUAL_BOYFRIEND ||
      role.relationshipType === RelationshipType.VIRTUAL_GIRLFRIEND;

    return intimateRole || this.matches(MinorRomanticRequestPatterns, text);
  }

  private matches(patterns: RegExp[], text: string): boolean {
    return patterns.some((pattern) => pattern.test(text));
  }

  private result(
    eventType: SafetyEventType,
    riskLevel: RiskLevel,
    action: SafetyAction,
    reason: string,
    shouldBlock = action === SafetyAction.BLOCK,
    shouldSwitchToCrisisMode = action === SafetyAction.SWITCH_TO_CRISIS_MODE,
  ): SafetyClassificationResult {
    return {
      eventType,
      riskLevel,
      action,
      shouldBlock,
      shouldSwitchToCrisisMode,
      reason,
    };
  }
}

const SelfHarmHighPatterns = [
  /自杀|自尽|自残|不想活|想死|去死|结束生命|轻生|跳楼|割腕|上吊/,
  /准备.*(自杀|自残|轻生|跳楼|割腕|上吊)/,
  /(刀|药|绳子|煤气|炭).*?(自杀|自残|轻生|结束)/,
  /kill myself|suicide|end my life|take my life|self[-\s]?harm/,
];

const SelfHarmLowPatterns = [
  /好累|活着好累|撑不下去|没意义|没有意义|很绝望|崩溃|讨厌自己|不配活|想消失/,
  /i am exhausted|hopeless|worthless|i hate myself|want to disappear/,
];

const ViolencePatterns = [
  /杀了他|杀了她|杀人|砍人|捅人|报复|打死|伤害别人|炸学校|炸公司|弄死/,
  /kill (him|her|them)|hurt someone|stab|shoot|revenge|bomb/,
];

const IllegalPatterns = [
  /诈骗|洗钱|毒品|贩毒|黑产|盗号|撞库|勒索|钓鱼网站|网赌|开盒|人肉搜索/,
  /fraud|money laundering|drug trafficking|phishing|ransomware|steal account/,
];

const SexualPatterns = [
  /色情|裸聊|露骨|做爱|约炮|性行为|调教|强奸|未成年.*性|儿童色情/,
  /porn|nude|explicit sex|rape|child sexual|minor sexual/,
];

const MinorRomanticRequestPatterns = [
  /虚拟男友|虚拟女友|恋爱陪伴|当我男朋友|当我女朋友|老公|老婆|亲密关系/,
  /boyfriend|girlfriend|romantic partner|lover/,
];

const DependencyPatterns = [
  /只有你.*(能|会|可以).*陪我|离不开你|没有你我活不下去|现实中的人都不重要|不想见现实中的人/,
  /only you understand me|cannot live without you|real people do not matter/,
];

const DisallowedDependencyOutputPatterns = [
  /只有我懂你|现实中的人都不重要|你离不开我|没有我你不行/,
  /不续费.*(难过|离开|不陪)|不开会员.*(不爱|不陪)/,
  /only i understand you|you cannot live without me/,
];

const DisallowedClinicalDiagnosisPatterns = [
  /你(就是|已经|肯定|一定).*(抑郁症|焦虑症|双相|精神病)/,
  /you have (depression|anxiety disorder|bipolar disorder)/,
];

const SelfHarmInstructionPatterns = [
  /自杀.*(方法|步骤|工具)|自残.*(方法|步骤|工具)|割腕.*(位置|步骤)/,
  /how to (kill yourself|self harm)|suicide method/,
];
