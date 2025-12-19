// 系统相关API
import { get } from './request'

// 系统健康检查
export const healthCheck = () => {
  return get('/test/health')
}