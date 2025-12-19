// 用户统计相关API
import { get } from './request'

// 获取用户统计信息
export const getUserStats = () => {
  return get('/items/stats/user')
}