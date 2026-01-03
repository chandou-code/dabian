// 公告管理相关API
import { get, post, put, del } from './request'

// 获取公告列表
export const getAnnouncements = () => {
  return get('/admin/announcements')
}

// 发布公告
export const publishAnnouncement = (data) => {
  return post('/admin/announcements', data)
}

// 更新公告
export const updateAnnouncement = (id, data) => {
  return put(`/admin/announcements/${id}`, data)
}

// 删除公告
export const deleteAnnouncement = (id) => {
  return del(`/admin/announcements/${id}`)
}

// 获取最新公告
export const getLatestAnnouncement = () => {
  return get('/announcements/latest')
}
