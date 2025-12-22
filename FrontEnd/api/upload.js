// 图片上传相关API
import { request } from './request'

// 上传单个图片
export const uploadImage = (file, folder = 'general') => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/upload/image',
    method: 'POST',
    data: formData,
    header: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 上传多个图片
export const uploadImages = (files, folder = 'general') => {
  const formData = new FormData()
  files.forEach((file, index) => {
    formData.append('files', file)
  })
  formData.append('folder', folder)
  
  return request({
    url: '/upload/images',
    method: 'POST',
    data: formData,
    header: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 为物品上传图片
export const uploadItemImages = (files, itemType, itemId) => {
  const token = uni.getStorageSync('token')
  
  // 创建多个上传任务Promise
  const uploadPromises = files.map((file) => {
    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: 'http://localhost:18080/api/upload/item-images',
        filePath: file.path,
        name: 'files',
        formData: {
          itemType: itemType,
          relatedItemId: itemId ? itemId.toString() : ''
        },
        header: {
          Authorization: token ? `Bearer ${token}` : ''
        },
        success: (res) => {
          try {
            const response = JSON.parse(res.data)
            resolve(response)
          } catch (e) {
            reject(new Error('解析响应失败'))
          }
        },
        fail: (err) => {
          reject(err)
        }
      })
    })
  })
  
  // 等待所有文件上传完成
  return Promise.all(uploadPromises).then(results => {
    // 合并所有上传结果
    const allFiles = []
    results.forEach(result => {
      if (result.code === 200 && result.data) {
        // 将相对URL转换为绝对URL
        const filesWithAbsoluteUrl = result.data.map(file => {
          // 如果URL已经是绝对路径则不处理，否则添加完整的后端服务器地址
          if (file.url && !file.url.startsWith('http')) {
            return {
              ...file,
              url: 'http://localhost:18080/api' + file.url
            }
          }
          return file
        })
        allFiles.push(...filesWithAbsoluteUrl)
      }
    })
    
    return {
      code: 200,
      message: '上传成功',
      data: allFiles
    }
  }).catch(error => {
    console.error('图片上传失败:', error)
    throw error
  })
}

// 获取物品图片列表
export const getItemImages = (itemId) => {
  return request({
    url: `/upload/item-images/${itemId}`,
    method: 'GET'
  }).then(response => {
    // 将相对URL转换为绝对URL
    if (response && response.data && Array.isArray(response.data)) {
        response.data = response.data.map(file => {
            if (file.url && !file.url.startsWith('http')) {
                return {
                    ...file,
                    url: 'http://localhost:18080/api' + file.url
                }
            }
            return file
        })
    }
    return response
  })
}

// 删除物品图片
export const deleteItemImage = (uploadId) => {
  return request({
    url: `/upload/images/${uploadId}`,
    method: 'DELETE'
  })
}

// 删除通用图片
export const deleteImage = (fileUrl) => {
  return request({
    url: '/upload/image',
    method: 'DELETE',
    data: { fileUrl }
  })
}