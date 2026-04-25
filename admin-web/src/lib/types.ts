export interface ApiEnvelope<T> {
  success: boolean;
  data?: T;
  message?: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: {
    id: string;
    email?: string | null;
    nickname: string;
    isMinor: boolean;
    membershipLevel: string;
  };
}

export interface AdminUser {
  id: string;
  email?: string | null;
  phone?: string | null;
  nickname: string;
  birthYear?: number | null;
  ageVerified: boolean;
  isMinor: boolean;
  guardianConsent: boolean;
  membershipLevel: string;
  createdAt: string;
  deletedAt?: string | null;
}

export interface AdminRole {
  id: string;
  name: string;
  category: string;
  relationshipType: string;
  personality: string;
  speechStyle: string;
  safetyLevel: string;
  isAdultOnly: boolean;
  isOfficial: boolean;
  status: string;
  createdAt: string;
  createdByUser?: {
    id: string;
    email?: string | null;
    nickname: string;
  } | null;
}

export interface AdminSafetyEvent {
  id: string;
  eventType: string;
  riskLevel: string;
  actionTaken: string;
  createdAt: string;
  user?: {
    id: string;
    email?: string | null;
    nickname: string;
    isMinor: boolean;
  };
  session?: {
    id: string;
    title: string;
    roleId: string;
    riskLevel: string;
  } | null;
  message?: {
    id: string;
    senderType: string;
    content: string;
    safetyLabel?: string | null;
    createdAt: string;
  } | null;
}

export interface AdminChatLog {
  id: string;
  senderType: string;
  content: string;
  safetyLabel?: string | null;
  createdAt: string;
  session: {
    id: string;
    title: string;
    riskLevel: string;
    role: {
      id: string;
      name: string;
      relationshipType: string;
      isAdultOnly: boolean;
    };
    user: {
      id: string;
      email?: string | null;
      nickname: string;
      isMinor: boolean;
    };
  };
  reports: Array<{
    id: string;
    reason: string;
    status: string;
    createdAt: string;
  }>;
}

export interface AdminReport {
  id: string;
  reason: string;
  status: string;
  reviewNote?: string | null;
  reviewedAt?: string | null;
  createdAt: string;
  user: {
    id: string;
    email?: string | null;
    nickname: string;
  };
  reviewedBy?: {
    id: string;
    email?: string | null;
    nickname: string;
  } | null;
  message: {
    id: string;
    senderType: string;
    content: string;
    safetyLabel?: string | null;
    createdAt: string;
  };
}

export interface AdminAuditLog {
  id: string;
  action: string;
  targetType: string;
  targetId?: string | null;
  metadata?: unknown;
  createdAt: string;
  adminUser: {
    id: string;
    email?: string | null;
    nickname: string;
  };
}
