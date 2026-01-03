// 物品管理相关API
import { get, post, put, del } from './request'

// 发布失物信息
export const publishLostItem = (itemInfo) => {
  return post('/items/lost-items', itemInfo)
}

// 发布招领信息
export const publishFoundItem = (itemInfo) => {
  return post('/items/found-items', itemInfo)
}

// 获取失物列表
export const getLostItems = (params) => {
  return get('/items/lost-items', params)
}

// 获取招领列表
export const getFoundItems = (params) => {
  return get('/items/found-items', params)
}

// 搜索物品
export const searchItems = (params) => {
  return get('/search/text', params)
}

// 获取失物详情
export const getLostItemDetail = (itemId) => {
  return get(`/items/lost-item/${itemId}`)
}

// 获取招领详情
export const getFoundItemDetail = (itemId) => {
  return get(`/items/found-item/${itemId}`)
}

// 更新物品信息
export const updateItem = (itemId, itemInfo) => {
  return put(`/items/items/${itemId}`, itemInfo)
}

// 删除物品信息
export const deleteItem = (itemId) => {
  return del(`/items/items/${itemId}`)
}

// 更新物品图片关联关系
export const updateItemImageAssociation = (data) => {
  return post('/items/update-image-association', data)
}