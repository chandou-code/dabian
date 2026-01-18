import { get, post, put, del } from './request'

// ============ 认证相关 ============
// 用户登录
export const login = (credentials) => {
  return post('/auth/login', credentials)
}

// 用户注册
export const register = (userInfo) => {
  return post('/auth/register', userInfo)
}

// 获取用户信息
export const getUserInfo = () => {
  return get('/user/info')
}

// ============ 任务相关 ============
// 发布任务
export const createTask = (taskData) => {
  return post('/task/publish', taskData)
}

// 获取任务列表
export const getTasks = (params) => {
  console.log('调用getTasks API，参数:', params)
  return get('/task/list', params)
}

// 获取任务详情
export const getTaskDetail = (taskId) => {
  return get(`/task/${taskId}/detail`)
}

// 接受任务
export const acceptTask = (taskId) => {
  return post(`/task/${taskId}/accept`)
}

// 取消任务
export const cancelTask = (taskId) => {
  return post(`/task/${taskId}/cancel`)
}

// 完成任务
export const completeTask = (taskId, data) => {
  return post(`/task/${taskId}/complete`, data)
}

// 获取我的任务
export const getMyTasks = (params) => {
  return get('/task/user-tasks', params)
}

// ============ 订单相关 ============
// 获取订单列表
export const getOrders = (params) => {
  return get('/orders', params)
}

// 获取订单详情
export const getOrderDetail = (orderId) => {
  return get(`/orders/${orderId}`)
}

// 更新订单状态
export const updateOrderStatus = (orderId, status) => {
  return put(`/orders/${orderId}/status`, { status })
}

// 获取订单统计数据
export const getOrderStats = () => {
  return get('/orders/stats')
}

// ============ 聊天相关 ============
// 获取聊天消息列表
export const getMessages = (conversationId, params) => {
  return get(`/chat/${conversationId}/messages`, params)
}

// 发送消息
export const sendMessage = (conversationId, messageData) => {
  return post(`/chat/${conversationId}/messages`, messageData)
}

// 获取会话列表
export const getConversations = () => {
  return get('/chat/conversations')
}

// ============ 评价相关 ============
// 提交评价
export const submitReview = (orderId, reviewData) => {
  return post(`/reviews/${orderId}`, reviewData)
}

// 获取用户评价
export const getUserReviews = (userId, params) => {
  return get(`/reviews/user/${userId}`, params)
}

// ============ 地图相关 ============
// 获取附近任务
export const getNearbyTasks = (params) => {
  return get('/map/nearby', params)
}

// 获取路径规划
export const getRoutePlan = (start, end) => {
  return post('/map/route', { start, end })
}

// ============ 统计相关 ============// 获取用户统计
export const getUserStats = () => {
  return get('/stats/user')
}

// 获取平台统计
export const getPlatformStats = () => {
  return get('/stats/platform')
}

// ============ 首页相关 ============// 获取首页所有数据
export const getHomeAllData = () => {
  return get('/home/all')
}

// 获取轮播图数据
export const getBanners = () => {
  return get('/home/banners')
}

// 获取用户统计数据
export const getHomeUserStats = () => {
  return get('/home/stats')
}

// 获取推荐跑腿员
export const getRecommendedRunners = () => {
  return get('/home/recommended-runners')
}

// 获取平台公告
export const getNotices = () => {
  return get('/home/notices')
}

// ============ 通知相关 ============
// 获取通知列表
export const getNotifications = (params) => {
  return get('/notifications', params)
}

// 获取未读通知数量
export const getUnreadCount = () => {
  return get('/notifications/unread-count')
}

// 标记通知为已读
export const markAsRead = (id) => {
  return put(`/notifications/${id}/read`)
}

// 标记所有通知为已读
export const markAllAsRead = () => {
  return put('/notifications/read-all')
}

// 清除所有通知
export const clearAllNotifications = () => {
  return del('/notifications/clear-all')
}

// ============ 跑腿员申请相关 ============
// 获取跑腿员申请列表
export const getRunnerApplications = (params) => {
  return get('/runner/applications/list', params)
}

// 批准跑腿员申请
export const approveRunnerApplication = (id, comment) => {
  return put(`/runner/applications/approve/${id}`, { comment })
}

// 拒绝跑腿员申请
export const rejectRunnerApplication = (id, comment) => {
  return put(`/runner/applications/reject/${id}`, { comment })
}

// 获取跑腿员申请详情
export const getRunnerApplicationDetail = (id) => {
  return get(`/runner/applications/detail/${id}`)
}
