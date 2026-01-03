import request from '@/utils/request'

/**
 * 获取物品详情
 */
export function getItemDetail(id) {
  return request({
    url: `/items/${id}/detail`,
    method: 'GET'
  })
}

/**
 * 提交线索
 */
export function submitClue(itemId, clueData) {
  return request({
    url: `/items/${itemId}/clues`,
    method: 'POST',
    data: clueData
  })
}