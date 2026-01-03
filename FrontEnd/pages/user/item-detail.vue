<template>
  <view class="item-detail-container">
    <!-- 顶部物品信息 -->
    <view class="item-header">
      <view class="item-images" v-if="itemDetail.images && itemDetail.images.length > 0">
        <swiper 
          class="image-swiper"
          :indicator-dots="true"
          :autoplay="false"
          indicator-color="rgba(255, 255, 255, 0.5)"
          indicator-active-color="#2196F3"
        >
          <swiper-item v-for="(image, index) in itemDetail.images" :key="index">
            <image 
              :src="cleanImageUrl(image)" 
              mode="aspectFit"
              class="swiper-image"
              @click="previewImage(index)"
              @load="onItemImageLoad(image, index)"
              @error="onItemImageError(image, index, $event)"
            />
          </swiper-item>
        </swiper>
      </view>
      
      <view class="item-info">
        <view class="item-title-row">
          <text class="item-title">{{ itemDetail.itemName || '未知物品' }}</text>
          <view class="item-type-badge" :class="itemDetail.type">
            {{ itemDetail.type === 'lost' ? '失物' : '招领' }}
          </view>
        </view>
        
        <view class="item-meta">
          <view class="meta-item">
            <text class="meta-label">时间:</text>
            <text class="meta-value">{{ formatDate(itemDetail.eventTime) || '未知' }}</text>
          </view>
          <view class="meta-item">
            <text class="meta-label">地点:</text>
            <text class="meta-value">{{ itemDetail.location || '未知' }}</text>
          </view>
          <view class="meta-item">
            <text class="meta-label">分类:</text>
            <text class="meta-value">{{ itemDetail.category || '其他' }}</text>
          </view>
        </view>
        
        <!-- 发布者信息 -->
        <view class="publisher-info">
          <text class="publisher-label">发布者信息</text>
          <view class="publisher-details">
            <text class="publisher-name">{{ itemDetail.userName || '匿名用户' }}</text>
            <text class="publisher-contact" v-if="itemDetail.userPhone">
              联系方式: {{ maskPhone(itemDetail.userPhone) }}
            </text>
            <text class="publisher-contact" v-if="itemDetail.userEmail">
              邮箱: {{ maskEmail(itemDetail.userEmail) }}
            </text>
          </view>
        </view>
        
        <!-- 物品描述 -->
        <view class="item-description" v-if="itemDetail.description">
          <text class="desc-label">详细描述</text>
          <text class="desc-content">{{ itemDetail.description }}</text>
        </view>
      </view>
    </view>
    
    <!-- 操作按钮 -->
    <view class="action-buttons">
      <button class="action-btn clue-btn" @click="showClueModal">
        提供线索
      </button>
      <button class="action-btn contact-btn" @click="contactPublisher">
        联系发布者
      </button>
      <!-- 只有物品发布者才能看到的标记已找回按钮 -->
      <button 
        class="action-btn recovered-btn" 
        @click="markAsRecovered"
        v-if="isPublisher && itemDetail.status !== 'claimed'"
      >
        标记为已找回
      </button>
    </view>
    
    <!-- 线索列表 -->
    <view class="clues-section">
      <view class="section-header">
        <text class="section-title">相关线索</text>
        <text class="clue-count">{{ clues.length }}条线索</text>
      </view>
      
      <view class="clues-list">
        <view v-if="cluesLoading" class="loading-placeholder">
          <text>加载中...</text>
        </view>
        
        <view v-else-if="clues.length === 0" class="empty-placeholder">
          <text>暂无线索，快来提供第一条线索吧！</text>
        </view>
        
        <view v-else v-for="(clue, index) in clues" :key="clue.id" class="clue-item">
          <view class="clue-content">
            <text class="clue-text">{{ clue.content }}</text>
            <view class="clue-images" v-if="clue.images && clue.images.length > 0">
              <image 
                v-for="(image, imgIndex) in clue.images" 
                :key="imgIndex"
                :src="getImageUrl(image)" 
                mode="aspectFill"
                class="clue-image"
                @click="previewClueImage(clue.images, imgIndex)"
                @load="onClueImageLoad(image, imgIndex, clue.id)"
                @error="onClueImageError(image, imgIndex, clue.id, $event)"
              />
            </view>
          </view>
          
          <view class="clue-meta">
            <text class="clue-provider" v-if="clue.contactName">
              {{ clue.contactName }}
            </text>
            <text class="clue-phone" v-if="clue.contactPhone">
              {{ maskPhone(clue.contactPhone) }}
            </text>
            <text class="clue-time">{{ formatRelativeTime(clue.created_at) }}</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 线索提交弹窗 -->
    <uni-popup ref="cluePopup" type="bottom">
      <view class="clue-popup">
        <view class="popup-header">
          <text class="popup-title">提供线索</text>
          <button class="cancel-btn" @click="hideClueModal">取消</button>
        </view>
        
        <scroll-view class="clue-form" scroll-y>
          <view class="form-item">
            <text class="form-label">线索描述 *</text>
            <textarea 
              v-model="clueForm.content"
              class="form-textarea"
              placeholder="请详细描述您发现的线索信息..."
              maxlength="1000"
              auto-height
            />
          </view>
          
          <view class="form-item">
            <text class="form-label">您的姓名</text>
            <input 
              v-model="clueForm.contactName"
              class="form-input"
              placeholder="选填，方便我们联系您"
              maxlength="50"
            />
          </view>
          
          <view class="form-item">
            <text class="form-label">联系电话</text>
            <input 
              v-model="clueForm.contactPhone"
              class="form-input"
              placeholder="选填，保护隐私请放心填写"
              type="number"
              maxlength="11"
            />
          </view>
          
          <view class="form-item">
            <text class="form-label">上传图片</text>
            <view class="image-upload">
              <view 
                v-for="(image, index) in clueImages" 
                :key="index"
                class="upload-item"
              >
                <image :src="image" mode="aspectFill" class="upload-image" />
                <button class="delete-btn" @click="deleteClueImage(index)">×</button>
              </view>
              <button class="upload-btn" @click="chooseClueImage" v-if="clueImages.length < 3">
                <text class="upload-icon">+</text>
                <text class="upload-text">添加图片</text>
              </button>
            </view>
          </view>
        </scroll-view>
        
        <view class="form-footer">
          <button class="submit-btn" @click="submitClue" :disabled="!clueForm.content.trim()">
            提交线索
          </button>
        </view>
      </view>
    </uni-popup>
    
    <!-- 加载状态 -->
    <view class="loading-overlay" v-if="loading">
      <view class="loading-content">
        <text class="loading-text">加载中...</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getItemDetail, submitClue, getItemClues, markAsRecovered } from '@/api/item'
import UniPopup from '@/components/uni-popup.vue'
import { mapGetters } from 'vuex'

export default {
  name: 'ItemDetail',
  components: {
    UniPopup
  },
  data() {
    return {
      itemId: null,
      itemDetail: {},
      loading: false,
      
      // 线索相关
      clueForm: {
        content: '',
        contactName: '',
        contactPhone: ''
      },
      clueImages: [],
      
      // 线索列表
      clues: [],
      cluesLoading: false
    }
  },
  computed: {
    ...mapGetters(['user', 'isLoggedIn']),
    
    // 判断当前用户是否为物品发布者
    isPublisher() {
      if (!this.isLoggedIn || !this.user || !this.itemDetail) {
        return false
      }
      // 注意：这里的submitterId是后端返回的发布者ID，需要与当前登录用户的ID匹配
      return this.user.id === this.itemDetail.submitterId
    }
  },
  
  onLoad(options) {
    console.log('=== 物品详情页onLoad ===')
    console.log('接收到的options:', options)
    console.log('当前时间:', new Date().toISOString())
    
    if (options.id) {
      this.itemId = options.id
      console.log('设置物品ID:', this.itemId)
      console.log('开始并行加载数据...')
      
      Promise.all([
        this.loadItemDetail(),
        this.loadCluesList()
      ]).then(() => {
        console.log('=== 页面初始数据加载完成 ===')
      }).catch((error) => {
        console.error('=== 初始数据加载异常 ===', error)
      })
    } else {
      console.error('=== 错误：没有获取到物品ID ===')
      uni.showToast({
        title: '参数错误',
        icon: 'none'
      })
      uni.navigateBack()
    }
  },
  
  onPullDownRefresh() {
    console.log('=== 下拉刷新开始 ===')
    console.log('刷新时间:', new Date().toISOString())
    
    Promise.all([
      this.loadItemDetail(),
      this.loadCluesList()
    ]).then(() => {
      console.log('=== 下拉刷新完成 ===')
    }).catch((error) => {
      console.error('=== 下拉刷新异常 ===', error)
    }).finally(() => {
      console.log('停止下拉刷新动画')
      uni.stopPullDownRefresh()
    })
  },
  
  methods: {
    // 加载物品详情
    async loadItemDetail() {
      try {
        console.log('开始加载物品详情, itemId:', this.itemId)
        this.loading = true
        const result = await getItemDetail(this.itemId)
        console.log('API返回的完整结果:', result)
        if (result.success) {
          this.itemDetail = result.data
          console.log('设置的itemDetail:', this.itemDetail)
          // 设置页面标题
          uni.setNavigationBarTitle({
            title: this.itemDetail.itemName || '物品详情'
          })
        } else {
          uni.showToast({
            title: result.message || '加载失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('加载物品详情失败:', error)
        uni.showToast({
          title: '网络错误',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    
    // 加载线索列表
    async loadCluesList() {
      if (!this.itemId) {
        console.warn('loadCluesList: itemId为空，跳过加载')
        return
      }
      
      try {
        console.log('=== 开始加载线索列表 ===')
        console.log('itemId:', this.itemId)
        this.cluesLoading = true
        const result = await getItemClues(this.itemId)
        console.log('线索列表API返回完整结果:', result)
        
        if (result.success) {
          this.clues = result.data || []
          console.log('设置的clues数组:', this.clues)
          console.log('线索数量:', this.clues.length)
          
          // 详细日志每个线索的信息
          this.clues.forEach((clue, index) => {
            console.log(`线索${index + 1}:`, {
              id: clue.id,
              content: clue.content,
              images: clue.images,
              created_at: clue.created_at,
              contactName: clue.contactName
            })
          })
        } else {
          console.warn('获取线索列表失败:', result.message)
          this.clues = []
        }
      } catch (error) {
        console.error('=== 加载线索列表异常 ===')
        console.error('错误类型:', error.name)
        console.error('错误信息:', error.message)
        console.error('错误堆栈:', error.stack)
        this.clues = []
      } finally {
        this.cluesLoading = false
        console.log('=== 线索列表加载完成 ===')
      }
    },
    
    // 预览线索图片
    previewClueImage(images, index) {
      console.log('=== 预览线索图片 ===')
      console.log('原始图片数组:', images)
      console.log('点击的图片索引:', index)
      
      if (images && images.length > 0) {
        const fullUrls = images.map((img, i) => {
          const url = this.getImageUrl(img)
          console.log(`图片${i + 1}:`, img, '->', url)
          return url
        })
        
        console.log('最终预览URL数组:', fullUrls)
        console.log('当前显示的URL:', fullUrls[index] || fullUrls[0])
        
        uni.previewImage({
          urls: fullUrls,
          current: fullUrls[index] || fullUrls[0],
          success: () => {
            console.log('图片预览成功')
          },
          fail: (error) => {
            console.error('图片预览失败:', error)
          }
        })
      } else {
        console.warn('没有可预览的图片')
      }
    },
    
    // 显示线索弹窗
    showClueModal() {
      console.log('=== 显示线索弹窗 ===')
      console.log('弹窗组件引用:', this.$refs.cluePopup)
      
      try {
        this.$refs.cluePopup.openPopup()
        console.log('弹窗打开成功')
      } catch (error) {
        console.error('打开弹窗失败:', error)
      }
    },
    
    // 隐藏线索弹窗
    hideClueModal() {
      console.log('=== 隐藏线索弹窗 ===')
      console.log('弹窗组件引用:', this.$refs.cluePopup)
      
      try {
        if (this.$refs.cluePopup && this.$refs.cluePopup.closePopup) {
          console.log('调用closePopup方法')
          this.$refs.cluePopup.closePopup()
          console.log('弹窗关闭成功')
        } else {
          console.warn('弹窗组件或closePopup方法不存在')
        }
        this.resetClueForm()
      } catch (error) {
        console.error('=== 关闭弹窗异常 ===')
        console.error('错误类型:', error.name)
        console.error('错误信息:', error.message)
        console.error('错误堆栈:', error.stack)
        this.resetClueForm()
      }
    },
    
    // 重置线索表单
    resetClueForm() {
      this.clueForm = {
        content: '',
        contactName: '',
        contactPhone: ''
      }
      this.clueImages = []
    },
    
    // 选择线索图片
    chooseClueImage() {
      console.log('=== 开始选择线索图片 ===')
      console.log('当前已有图片数量:', this.clueImages.length)
      console.log('最多可再选择数量:', 3 - this.clueImages.length)
      
      uni.chooseImage({
        count: 3 - this.clueImages.length,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          console.log('选择图片成功:', res)
          console.log('选择的临时文件路径:', res.tempFilePaths)
          console.log('文件信息:', res.tempFiles)
          
          const newImages = res.tempFilePaths
          this.clueImages = [...this.clueImages, ...newImages]
          console.log('更新后的clueImages数组:', this.clueImages)
          console.log('当前总图片数量:', this.clueImages.length)
        },
        fail: (error) => {
          console.error('=== 选择图片失败 ===')
          console.error('错误信息:', error)
          uni.showToast({
            title: '选择图片失败',
            icon: 'none'
          })
        }
      })
    },
    
    // 删除线索图片
    deleteClueImage(index) {
      this.clueImages.splice(index, 1)
    },
    
    // 提交线索
    async submitClue() {
      console.log('=== 开始提交线索 ===')
      console.log('表单数据:', this.clueForm)
      console.log('待上传图片数量:', this.clueImages.length)
      console.log('图片文件路径:', this.clueImages)
      
      if (!this.clueForm.content.trim()) {
        console.warn('线索描述为空，阻止提交')
        uni.showToast({
          title: '请填写线索描述',
          icon: 'none'
        })
        return
      }
      
      try {
        // 先上传图片
        const uploadedImages = []
        console.log('=== 开始批量上传图片 ===')
        
        for (let i = 0; i < this.clueImages.length; i++) {
          console.log(`上传第${i + 1}张图片...`)
          try {
            const uploadResult = await this.uploadClueImage(this.clueImages[i])
            uploadedImages.push(uploadResult)
            console.log(`第${i + 1}张图片上传成功:`, uploadResult)
          } catch (error) {
            console.error(`第${i + 1}张图片上传失败:`, error)
            // 继续上传其他图片，不阻断整个流程
          }
        }
        
        console.log('=== 图片上传完成 ===')
        console.log('成功上传的图片:', uploadedImages)
        console.log('上传成功数量:', uploadedImages.length)
        
        const clueData = {
          ...this.clueForm,
          images: uploadedImages
        }
        
        console.log('=== 准备提交线索数据 ===')
        console.log('最终线索数据:', clueData)
        
        const result = await submitClue(this.itemId, clueData)
        console.log('=== 线索提交API返回 ===')
        console.log('完整结果:', result)
        
        if (result.success) {
          console.log('线索提交成功')
          uni.showToast({
            title: '线索提交成功',
            icon: 'success'
          })
          this.hideClueModal()
          // 刷新线索列表
          console.log('刷新线索列表...')
          await this.loadCluesList()
        } else {
          console.error('线索提交失败:', result.message)
          uni.showToast({
            title: result.message || '提交失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('=== 提交线索异常 ===')
        console.error('错误类型:', error.name)
        console.error('错误信息:', error.message)
        console.error('错误堆栈:', error.stack)
        uni.showToast({
          title: '网络错误',
          icon: 'none'
        })
      }
    },
    
    // 上传线索图片
    async uploadClueImage(filePath) {
      console.log('=== 开始上传线索图片 ===')
      console.log('本地文件路径:', filePath)
      
      const token = uni.getStorageSync('token')
      console.log('使用token:', token ? '已获取' : '未获取')
      
      const uploadUrl = 'http://localhost:18080/api/upload/image?folder=clue_images'
      console.log('上传URL:', uploadUrl)
      
      return new Promise((resolve, reject) => {
        const uploadTask = uni.uploadFile({
          url: uploadUrl,
          filePath: filePath,
          name: 'file',
          header: {
            'Authorization': `Bearer ${token}`
          },
          success: (res) => {
            console.log('=== 图片上传成功 ===')
            console.log('HTTP状态码:', res.statusCode)
            console.log('响应数据:', res.data)
            
            try {
              const data = JSON.parse(res.data)
              console.log('解析后的响应:', data)
              
              if (data.success) {
                const imageUrl = data.data.url
                console.log('上传成功，图片URL:', imageUrl)
                resolve(imageUrl)
              } else {
                console.error('上传失败，服务器返回错误:', data.message)
                reject(new Error(data.message || '上传失败'))
              }
            } catch (e) {
              console.error('=== JSON解析失败 ===')
              console.error('原始响应:', res.data)
              console.error('解析错误:', e)
              reject(new Error('解析响应失败'))
            }
          },
          fail: (error) => {
            console.error('=== 图片上传失败 ===')
            console.error('错误类型:', error.errMsg || error.message)
            console.error('完整错误对象:', error)
            
            // 根据错误类型提供更具体的错误信息
            let errorMessage = '上传失败'
            if (error.errMsg) {
              if (error.errMsg.includes('timeout')) {
                errorMessage = '上传超时，请检查网络'
              } else if (error.errMsg.includes('fail')) {
                errorMessage = '网络连接失败'
              }
            }
            reject(new Error(errorMessage))
          }
        })
        
        // 监听上传进度
        uploadTask.onProgressUpdate((res) => {
          console.log('上传进度:', res.progress + '%')
          console.log('已上传字节数:', res.totalBytesSent)
          console.log('总字节数:', res.totalBytesExpectedToSend)
        })
      })
    },
    
    // 联系发布者
    contactPublisher() {
      const phone = this.itemDetail.userPhone
      if (phone) {
        uni.makePhoneCall({
          phoneNumber: phone
        })
      } else {
        uni.showToast({
          title: '暂无联系方式',
          icon: 'none'
        })
      }
    },
    
    // 预览图片
    previewImage(index) {
      if (this.itemDetail.images && this.itemDetail.images.length > 0) {
        const urls = this.itemDetail.images.map(img => this.cleanImageUrl(img))
        uni.previewImage({
          urls: urls,
          current: index
        })
      }
    },
    
    // 清理图片URL
    cleanImageUrl(url) {
      if (!url) return ''
      return url.replace(/^"|"$/g, '').trim()
    },
    
    // 获取完整图片URL
    getImageUrl(imageUrl) {
      console.log('=== 构建图片URL ===')
      console.log('原始imageUrl:', imageUrl)
      console.log('类型:', typeof imageUrl)
      
      if (!imageUrl) {
        console.warn('imageUrl为空')
        return ''
      }
      
      // 如果已经是完整URL，直接返回
      if (imageUrl.startsWith('http')) {
        console.log('已是完整URL，直接返回:', imageUrl)
        return imageUrl
      }
      
      // 如果是相对路径，拼接服务器地址
      // 尝试多种可能的路径
      const possibleUrls = [
        `http://localhost:18080/api/uploads/${imageUrl}`, // 带context path
        `http://localhost:18080/uploads/${imageUrl}`  // 不带context path
      ]
      
      console.log('尝试的URL列表:')
      possibleUrls.forEach((url, index) => {
        console.log(`  ${index + 1}. ${url}`)
      })
      
      const finalUrl = possibleUrls[0]
      console.log('选择的最终URL:', finalUrl)
      return finalUrl
    },
    
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return ''
      try {
        const date = new Date(dateString)
        const year = date.getFullYear()
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        return `${year}-${month}-${day}`
      } catch (error) {
        return dateString
      }
    },
    
    // 格式化相对时间
    formatRelativeTime(dateString) {
      if (!dateString) return ''
      try {
        const date = new Date(dateString)
        const now = new Date()
        const diff = now - date
        
        const minutes = Math.floor(diff / 60000)
        const hours = Math.floor(diff / 3600000)
        const days = Math.floor(diff / 86400000)
        
        if (minutes < 1) return '刚刚'
        if (minutes < 60) return `${minutes}分钟前`
        if (hours < 24) return `${hours}小时前`
        if (days < 7) return `${days}天前`
        
        return this.formatDate(dateString)
      } catch (error) {
        return dateString
      }
    },
    
    // 手机号脱敏
    maskPhone(phone) {
      if (!phone || phone.length < 11) return phone
      return phone.substring(0, 3) + '****' + phone.substring(7)
    },
    
    // 邮箱脱敏
    maskEmail(email) {
      if (!email || !email.includes('@')) return email
      const [name, domain] = email.split('@')
      const maskedName = name.substring(0, 2) + '***' + name.substring(name.length - 1)
      return maskedName + '@' + domain
    },
    
    // 线索图片加载成功
    onClueImageLoad(imageUrl, index, clueId) {
      console.log(`=== 线索图片加载成功 ===`)
      console.log('线索ID:', clueId)
      console.log('图片索引:', index)
      console.log('图片URL:', imageUrl)
      console.log('完整URL:', this.getImageUrl(imageUrl))
    },
    
    // 线索图片加载失败
    onClueImageError(imageUrl, index, clueId, event) {
      console.error(`=== 线索图片加载失败 ===`)
      console.error('线索ID:', clueId)
      console.error('图片索引:', index)
      console.error('原始imageUrl:', imageUrl)
      console.error('尝试的URL:', this.getImageUrl(imageUrl))
      console.error('错误事件:', event)
      
      // 尝试其他可能的URL
      const alternativeUrls = [
        `http://localhost:18080/uploads/${imageUrl}`,
        `http://localhost:18080/api/${imageUrl}`
      ]
      
      console.error('尝试备用URL:', alternativeUrls)
    },
    
    // 物品图片加载成功
    onItemImageLoad(imageUrl, index) {
      console.log(`=== 物品图片加载成功 ===`)
      console.log('物品ID:', this.itemId)
      console.log('图片索引:', index)
      console.log('原始imageUrl:', imageUrl)
      console.log('清理后的URL:', this.cleanImageUrl(imageUrl))
    },
    
    // 物品图片加载失败
    onItemImageError(imageUrl, index, event) {
      console.error(`=== 物品图片加载失败 ===`)
      console.error('物品ID:', this.itemId)
      console.error('图片索引:', index)
      console.error('原始imageUrl:', imageUrl)
      console.error('清理后的URL:', this.cleanImageUrl(imageUrl))
      console.error('错误事件:', event)
    },
    
    // 标记物品为已找回
    async markAsRecovered() {
      try {
        console.log('=== 开始标记物品为已找回 ===')
        console.log('物品ID:', this.itemId)
        
        // 调用封装好的API方法
        const response = await markAsRecovered(this.itemId)
        
        console.log('API返回结果:', response)
        
        if (response.success) {
          uni.showToast({
            title: '标记成功',
            icon: 'success'
          })
          // 更新物品状态
          this.itemDetail.status = 'claimed'
        } else {
          uni.showToast({
            title: response.message || '标记失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('=== 标记物品为已找回失败 ===')
        console.error('错误类型:', error.name)
        console.error('错误信息:', error.message)
        console.error('错误堆栈:', error.stack)
        uni.showToast({
          title: '网络错误',
          icon: 'none'
        })
      }
    }
  }
}
</script>

<style scoped>
.item-detail-container {
  min-height: 100vh;
  background: #f5f5f5;
}

/* 顶部物品信息 */
.item-header {
  background: white;
  margin-bottom: 12px;
}

.image-swiper {
  height: 300px;
  width: 100%;
}

.swiper-image {
  width: 100%;
  height: 100%;
}

.item-info {
  padding: 20px;
}

.item-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.item-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  flex: 1;
  margin-right: 15px;
}

.item-type-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  color: white;
}

.item-type-badge.lost {
  background: #FF5722;
}

.item-type-badge.found {
  background: #4CAF50;
}

.item-meta {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
}

.meta-label {
  color: #666;
  margin-right: 10px;
  min-width: 60px;
}

.meta-value {
  color: #333;
  font-weight: 500;
}

.publisher-info {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 12px;
  margin-bottom: 20px;
}

.publisher-label {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;
  display: block;
}

.publisher-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.publisher-name {
  font-size: 15px;
  font-weight: 500;
  color: #2196F3;
}

.publisher-contact {
  font-size: 14px;
  color: #666;
}

.item-description {
  margin-bottom: 20px;
}

.desc-label {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;
  display: block;
}

.desc-content {
  font-size: 15px;
  line-height: 1.6;
  color: #666;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 15px;
  padding: 20px;
  background: white;
  margin-bottom: 12px;
}

.action-btn {
  flex: 1;
  height: 48px;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 500;
  border: none;
  color: white;
}

.clue-btn {
  background: #2196F3;
}

.contact-btn {
  background: #FF9800;
}

.recovered-btn {
  background: #4CAF50;
}

/* 线索列表 */
.clues-section {
  background: white;
  margin-bottom: 12px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.clue-count {
  font-size: 14px;
  color: #666;
}

.clues-list {
  padding: 0 20px;
}

.loading-placeholder,
.empty-placeholder {
  padding: 40px 20px;
  text-align: center;
  color: #999;
  font-size: 15px;
}

.clue-item {
  padding: 20px 0;
  border-bottom: 1px solid #f5f5f5;
}

.clue-item:last-child {
  border-bottom: none;
}

.clue-content {
  margin-bottom: 12px;
}

.clue-text {
  font-size: 15px;
  line-height: 1.6;
  color: #333;
  display: block;
  margin-bottom: 12px;
}

.clue-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.clue-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  border: 1px solid #eee;
  background-color: #f5f5f5;
  object-fit: cover;
}

.clue-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 13px;
  color: #666;
}

.clue-provider {
  color: #2196F3;
  font-weight: 500;
}

.clue-time {
  margin-left: auto;
}

/* 弹窗样式 */
.clue-popup {
  background: white;
  border-radius: 20px 20px 0 0;
  max-height: 80vh;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.popup-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.cancel-btn {
  background: none;
  border: none;
  color: #666;
  font-size: 16px;
}





.submit-btn {
  background: #2196F3;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
}

.submit-btn:disabled {
  background: #ccc;
}

/* 线索表单 */
.clue-form {
  max-height: 50vh;
  padding: 20px;
}

.form-item {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 10px;
}

.form-input, .form-textarea {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 15px;
  background: #fafafa;
}

.form-textarea {
  min-height: 100px;
  resize: none;
}

.image-upload {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.upload-item {
  position: relative;
  width: 80px;
  height: 80px;
}

.upload-image {
  width: 100%;
  height: 100%;
  border-radius: 8px;
}

.delete-btn {
  position: absolute;
  top: -5px;
  right: -5px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #ff4444;
  color: white;
  border: none;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-btn {
  width: 80px;
  height: 80px;
  border: 1px dashed #ccc;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: none;
  color: #999;
}

.upload-icon {
  font-size: 20px;
  margin-bottom: 5px;
}

.upload-text {
  font-size: 12px;
}

.form-footer {
  padding: 20px;
  border-top: 1px solid #f0f0f0;
}

/* 加载状态 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.loading-content {
  text-align: center;
}

.loading-text {
  font-size: 16px;
  color: #666;
}
</style>