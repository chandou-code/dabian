// 审核相关API
import { get, put } from './request'

// 获取审核员仪表板数据
export const getReviewerDashboard = () => {
  return get('/review/dashboard')
}

// 获取待审核列表
export const getPendingReviews = (params) => {
  return get('/review/pending', params)
}

// 审核物品
export const reviewItem = (id, data) => {
  return put(`/review/${id}`, data)
}

// 批量审核
export const batchReview = (data) => {
  return put('/review/batch', data)
}

// 获取审核历史
export const getReviewHistory = (params) => {
  return get('/review/history', params)
}
