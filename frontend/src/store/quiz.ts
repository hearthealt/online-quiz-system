import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Question, UserAnswer, QuizSession, AnswerRequest } from '@/types/index'
import * as questionApi from '@/api/question'
import * as answerApi from '@/api/answer'

export const useQuizStore = defineStore('quiz', () => {
  // 状态
  const currentQuestion = ref<Question | null>(null)
  const questions = ref<Question[]>([])
  const currentQuestionIndex = ref(0)
  const userAnswers = ref<Map<number, string>>(new Map())
  const answerResults = ref<UserAnswer[]>([])
  const currentSession = ref<QuizSession | null>(null)
  const isLoading = ref(false)

  // 计算属性
  const totalQuestions = computed(() => questions.value.length)
  const hasNextQuestion = computed(() => currentQuestionIndex.value < totalQuestions.value - 1)
  const hasPrevQuestion = computed(() => currentQuestionIndex.value > 0)
  const currentQuestionNum = computed(() => currentQuestionIndex.value + 1)
  const progress = computed(() => {
    if (totalQuestions.value === 0) return 0
    return Math.round((currentQuestionNum.value / totalQuestions.value) * 100)
  })

  // 根据题库ID获取题目
  const fetchQuestionsByBankId = async (bankId: number) => {
    try {
      isLoading.value = true
      const result = await questionApi.getQuestionsByBankId(bankId)
      questions.value = result
      currentQuestionIndex.value = 0
      currentQuestion.value = result[0] || null
      userAnswers.value.clear()
      answerResults.value = []
      return result
    } catch (error) {
      throw error
    } finally {
      isLoading.value = false
    }
  }

  // 根据题目ID列表获取题目
  const fetchQuestionsByIds = async (ids: number[]) => {
    try {
      isLoading.value = true

      if (ids.length > 50) {
        // 题目数量较多，使用批量获取API
        const result = await questionApi.getQuestionsByIds(ids)
        questions.value = result
      } else {
        // 题目数量较少时，使用并发获取
        const promises = ids.map(id => questionApi.getQuestion(id))
        questions.value = await Promise.all(promises)
      }

      currentQuestionIndex.value = 0
      currentQuestion.value = questions.value[0] || null
      userAnswers.value.clear()
      answerResults.value = []
      return questions.value
    } catch (error) {
      throw error
    } finally {
      isLoading.value = false
    }
  }

  // 获取题目详情
  const fetchQuestion = async (id: number) => {
    try {
      isLoading.value = true
      const question = await questionApi.getQuestion(id)
      currentQuestion.value = question
      return question
    } catch (error) {
      throw error
    } finally {
      isLoading.value = false
    }
  }

  // 设置当前题目
  const setCurrentQuestion = (index: number) => {
    if (index >= 0 && index < questions.value.length) {
      currentQuestionIndex.value = index
      currentQuestion.value = questions.value[index]
    }
  }

  // 下一题
  const nextQuestion = () => {
    if (hasNextQuestion.value) {
      setCurrentQuestion(currentQuestionIndex.value + 1)
    }
  }

  // 上一题
  const prevQuestion = () => {
    if (hasPrevQuestion.value) {
      setCurrentQuestion(currentQuestionIndex.value - 1)
    }
  }

  // 保存用户答案
  const saveUserAnswer = (questionId: number, answer: string) => {
    userAnswers.value.set(questionId, answer)
  }

  // 获取用户答案
  const getUserAnswer = (questionId: number): string | undefined => {
    return userAnswers.value.get(questionId)
  }

  // 提交答案
  const submitAnswer = async (answerData: AnswerRequest) => {
    try {
      if (!answerData.userAnswer) {
        throw new Error('用户答案不能为空')
      }

      const result = await answerApi.submitAnswer(answerData)
      answerResults.value.push(result)

      if (answerData.questionId && answerData.userAnswer) {
        saveUserAnswer(answerData.questionId, answerData.userAnswer)
      }

      return result
    } catch (error) {
      throw error
    }
  }

  // 批量提交答案
  const submitAnswers = async (answersData: AnswerRequest[]) => {
    try {
      const validAnswers = answersData.filter(answer => answer.userAnswer && answer.userAnswer.trim() !== '')

      if (validAnswers.length === 0) {
        return []
      }

      const results = await answerApi.submitAnswers(validAnswers)
      answerResults.value = results
      return results
    } catch (error) {
      throw error
    }
  }

  // 开始新的答题会话
  const startQuizSession = (sessionData: {
    bankId?: number
    bankName?: string
    mode?: 'practice' | 'exam'
  }) => {
    const sessionId = `session_${Date.now()}`
    currentSession.value = {
      sessionId,
      bankId: sessionData.bankId,
      bankName: sessionData.bankName,
      mode: sessionData.mode || 'practice',
      totalQuestions: 0,
      currentIndex: 0,
      correctCount: 0,
      startTime: new Date().toISOString(),
      status: 'ongoing'
    }
    return sessionId
  }

  // 结束答题会话
  const endQuizSession = () => {
    if (currentSession.value) {
      currentSession.value.status = 'completed'
      currentSession.value.endTime = new Date().toISOString()

      // 计算统计信息
      const correctCount = answerResults.value.filter(r => r.isCorrect === true).length
      currentSession.value.correctCount = correctCount
    }
  }

  // 重置答题状态
  const resetQuizState = () => {
    currentQuestion.value = null
    questions.value = []
    currentQuestionIndex.value = 0
    userAnswers.value.clear()
    answerResults.value = []
    currentSession.value = null
  }

  // 获取答题统计
  const getQuizStats = computed(() => {
    const total = answerResults.value.length
    const correct = answerResults.value.filter(r => r.isCorrect === true).length
    const accuracy = total > 0 ? Math.round((correct / total) * 100) : 0

    return {
      total,
      correct,
      wrong: total - correct,
      accuracy
    }
  })

  // 获取答题结果
  const getQuizResult = () => {
    if (!currentSession.value) return null

    const startTime = new Date(currentSession.value.startTime!)
    const endTime = currentSession.value.endTime ? new Date(currentSession.value.endTime) : new Date()
    const duration = Math.floor((endTime.getTime() - startTime.getTime()) / 1000)

    // 构建答题详情
    let answersWithQuestions = []

    if (answerResults.value.length > 0) {
      answersWithQuestions = answerResults.value.map(answer => {
        const question = questions.value.find(q => q.id === answer.questionId)
        return {
          ...answer,
          question
        }
      })
    } else {
      answersWithQuestions = questions.value.map(question => {
        const userAnswer = userAnswers.value.get(question.id!)
        return {
          questionId: question.id!,
          userAnswer: userAnswer || '',
          isCorrect: false,
          question
        }
      })
    }

    return {
      sessionId: currentSession.value.sessionId,
      mode: currentSession.value.mode,
      totalQuestions: questions.value.length,
      correctCount: currentSession.value.correctCount || 0,
      duration,
      answers: answersWithQuestions
    }
  }

  return {
    // 状态
    currentQuestion,
    questions,
    currentQuestionIndex,
    userAnswers,
    answerResults,
    currentSession,
    isLoading,

    // 计算属性
    totalQuestions,
    hasNextQuestion,
    hasPrevQuestion,
    currentQuestionNum,
    progress,
    getQuizStats,

    // 方法
    fetchQuestionsByBankId,
    fetchQuestionsByIds,
    fetchQuestion,
    setCurrentQuestion,
    nextQuestion,
    prevQuestion,
    saveUserAnswer,
    getUserAnswer,
    submitAnswer,
    submitAnswers,
    startQuizSession,
    endQuizSession,
    resetQuizState,
    getQuizResult
  }
})
