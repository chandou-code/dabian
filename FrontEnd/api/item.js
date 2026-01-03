import request from '@/utils/request'

// 获取物品详情
export function getItemDetail(itemId) {
  return request({
    url: `/api/item/${itemId}`,
    method: 'get'
  })
}

// 提交线索
export function submitClue(itemId, clueData) {
  return request({
    url: `/api/item/${itemId}/clue`,
    method: 'post',
    data: clueData
  })
}

// 获取物品线索列表
export function getItemClues(itemId) {
  return request({
    url: `/api/item/${itemId}/clues`,
    method: 'get'
  })
}

// 标记物品为已找回
export function markAsRecovered(itemId) {
  return request({
    url: `/items/${itemId}/recovered`,
    method: 'post'
  })
}