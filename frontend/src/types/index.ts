// API响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

// 用户相关类型
export interface User {
  id?: number
  username: string
  nickname?: string
  email?: string
  avatar?: string
  role?: 'admin' | 'user'
  permissions?: string[]
  status?: number
  lastLoginTime?: string
  createdAt?: string
  updatedAt?: string
}

export interface LoginRequest {
  username: string
  password: string
  rememberMe?: boolean
}

export interface RegisterRequest {
  username: string
  password: string
  confirmPassword: string
  nickname?: string
  email?: string
  verificationCode?: string
}

// 发送验证码请求
export interface SendVerificationCodeRequest {
  email: string
}

// 带验证码的注册请求
export interface RegisterWithVerificationRequest {
  username: string
  password: string
  nickname?: string
  email: string
  verificationCode: string
}

// 联系管理员请求
export interface ContactRequest {
  type: 'password' | 'account' | 'feature' | 'other'
  email: string
  username?: string
  subject: string
  description: string
}

// 题库类型
export interface QuestionBank {
  id?: number
  name: string
  description?: string
  questionCount?: number
  status?: number
  createdBy?: number
  createdAt?: string
  updatedAt?: string
}

// 题目相关类型
export type QuestionType = 'single' | 'multiple' | 'judge' | 'essay'
export type AnswerMode = 'practice' | 'exam'
export type SessionStatus = 'ongoing' | 'completed' | 'abandoned'

export interface Question {
  id?: number
  bankId?: number
  type: QuestionType
  content: string
  options?: string[]
  correctAnswer: string
  analysis?: string
  sortOrder?: number
  status?: number
  createdBy?: number
  createdAt?: string
  updatedAt?: string
}

// 答题相关类型
export interface AnswerRequest {
  questionId: number
  userAnswer?: string
}

export interface UserAnswer {
  id?: number
  sessionId?: number
  questionId: number
  userAnswer?: string
  isCorrect?: boolean
  createdAt?: string
}

// 收藏类型
export interface Favorite {
  id?: number
  userId?: number
  bankId?: number
  questionId: number
  notes?: string
  createdAt?: string
}

// 收藏详情（带题目信息）
export interface FavoriteWithDetail extends Favorite {
  question?: Question
}

// 错题类型
export interface WrongQuestion {
  id?: number
  userId?: number
  bankId?: number
  questionId: number
  errorCount?: number
  lastErrorTime?: string
  lastErrorAnswer?: string
  status?: number
  createdAt?: string
  updatedAt?: string
}

// 错题详情（带题目信息）
export interface WrongQuestionWithDetail extends WrongQuestion {
  question?: Question
}

// 答题会话类型
export interface QuizSession {
  id?: number
  sessionId?: string
  userId?: number
  bankId?: number
  bankName?: string
  mode?: AnswerMode
  totalQuestions?: number
  currentIndex?: number
  correctCount?: number
  startTime?: string
  endTime?: string
  status?: SessionStatus
  createdAt?: string
  updatedAt?: string
}

// 会话详情（包含题目和答案）
export interface SessionDetail {
  session: QuizSession
  questions: Question[]
  answers: UserAnswer[]
}

// 分页类型
export interface PageInfo {
  current: number
  size: number
  total: number
  pages: number
}

export interface PageResult<T> {
  records: T[]
  current: number
  size: number
  total: number
  pages: number
}

// 权限管理相关类型
export interface UserQueryParams {
  page?: number
  size?: number
  username?: string
  nickname?: string
  status?: number
}

export interface UpdatePermissionsParams {
  userId: number
  permissions: string[]
}

export interface UpdateUserStatusParams {
  userId: number
  status: number
}

export interface ResetPasswordParams {
  userId: number
}

// 统计类型
export interface AnswerStats {
  totalAnswers: number
  correctAnswers: number
  accuracy: number
}

export interface SystemStats {
  totalQuestions: number
  totalUsers: number
  totalSessions: number
  totalBanks: number
}

export interface UserStats {
  totalAnswers: number
  correctAnswers: number
  accuracy: number
}

export interface RankingItem {
  id: number
  username: string
  nickname: string
  avatar: string
  correctAnswers: number
  totalAnswers: number
  accuracy: number
  rank: number
}

export interface DetailedStats {
  totalAnswers: number
  correctAnswers: number
  accuracy: number
  wrongQuestionCount: number
  dailyStats: Array<{
    date: string
    totalAnswers: number
    correctAnswers: number
    accuracy: number
  }>
}

export interface AnswerTrendItem {
  date: string
  totalAnswers: number
  correctAnswers: number
  accuracy: number
}

export interface TypeStatsItem {
  type: string
  count: number
}

export interface WrongQuestionStats {
  totalWrongQuestions: number
  masteredQuestions: number
  unmasteredQuestions: number
}

// 文件上传类型
export interface UploadResponse {
  url: string
  relativePath: string
  fileName: string
  fileSize?: string
}

export interface BatchUploadResponse {
  successCount: number
  failCount: number
  files: Array<{
    url?: string
    relativePath?: string
    fileName: string
    status: 'success' | 'failed'
    error?: string
  }>
}

// 导入结果类型
export interface ImportResult {
  bankId: number
  bankName: string
  successCount: number
  failCount: number
  skipCount?: number
}
