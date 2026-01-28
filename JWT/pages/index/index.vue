<template>
  <view class="auth-center">
    <!-- 登录/注册表单 -->
    <view v-if="!isLoggedIn" class="login-section">
      <view class="app-logo">
        <image src="/static/logo.png" class="logo"></image>
        <text class="app-title">校园服务平台</text>
        <text class="app-subtitle">一站式校园服务解决方案</text>
      </view>
      
      <view class="auth-form">
        <!-- 标签切换 -->
        <view class="tab-container">
          <text :class="{ active: activeTab === 'login' }" @click="activeTab = 'login'" class="tab-item">登录</text>
          <text :class="{ active: activeTab === 'register' }" @click="activeTab = 'register'" class="tab-item">注册</text>
        </view>
        
        <!-- 登录表单 -->
        <view v-if="activeTab === 'login'" class="form-content">
          <view class="form-item">
            <view class="input-wrapper">
              <text class="input-icon">👤</text>
              <input v-model="loginForm.username" placeholder="请输入用户名" class="form-input"/>
            </view>
          </view>
          <view class="form-item">
            <view class="input-wrapper">
              <text class="input-icon">🔒</text>
              <input v-model="loginForm.password" type="password" placeholder="请输入密码" class="form-input"/>
            </view>
          </view>
          <button @click="handleLogin" :disabled="isLoading" class="auth-btn">{{ isLoading ? '登录中...' : '登录' }}</button>
        </view>
        
        <!-- 注册表单 -->
        <view v-else class="form-content">
          <view class="form-item">
            <view class="input-wrapper">
              <text class="input-icon">👤</text>
              <input v-model="registerForm.username" placeholder="请输入用户名" class="form-input"/>
            </view>
          </view>
          <view class="form-item">
            <view class="input-wrapper">
              <text class="input-icon">📧</text>
              <input v-model="registerForm.email" placeholder="请输入邮箱" class="form-input"/>
            </view>
          </view>
          <view class="form-item">
            <view class="input-wrapper">
              <text class="input-icon">📱</text>
              <input v-model="registerForm.phone" placeholder="请输入手机号" class="form-input"/>
            </view>
          </view>
          <view class="form-item">
            <view class="input-wrapper">
              <text class="input-icon">📝</text>
              <input v-model="registerForm.realName" placeholder="请输入真实姓名" class="form-input"/>
            </view>
          </view>
          <view class="form-item">
            <view class="input-wrapper">
              <text class="input-icon">🏫</text>
              <input v-model="registerForm.college" placeholder="请输入学院" class="form-input"/>
            </view>
          </view>
          <view class="form-item">
            <view class="input-wrapper">
              <text class="input-icon">🎓</text>
              <input v-model="registerForm.grade" placeholder="请输入年级（如：2022级）" class="form-input"/>
            </view>
          </view>
          <view class="form-item">
            <view class="input-wrapper">
              <text class="input-icon">📚</text>
              <input v-model="registerForm.major" placeholder="请输入专业" class="form-input"/>
            </view>
          </view>
          <view class="form-item">
            <view class="input-wrapper">
              <text class="input-icon">⚧️</text>
              <input v-model="registerForm.gender" placeholder="请输入性别（男/女）" class="form-input"/>
            </view>
          </view>
          <view class="form-item">
            <view class="input-wrapper">
              <text class="input-icon">🔒</text>
              <input v-model="registerForm.password" type="password" placeholder="请输入密码" class="form-input"/>
            </view>
          </view>
          <view class="form-item">
            <view class="input-wrapper">
              <text class="input-icon">🔒</text>
              <input v-model="registerForm.confirmPassword" type="password" placeholder="请确认密码" class="form-input"/>
            </view>
          </view>
          <button @click="handleRegister" :disabled="isLoading" class="auth-btn">{{ isLoading ? '注册中...' : '注册' }}</button>
        </view>
      </view>
    </view>
    
    <!-- 模块选择 -->
    <view v-else class="module-section">
      <view class="welcome">
        <text class="welcome-text">欢迎回来，{{ userInfo.realName || userInfo.username }}</text>
        <button @click="handleLogout" class="logout-btn">退出登录</button>
      </view>
      
      <view class="module-grid">
        <view class="module-card" @click="navigateToModule('lost-found')">
          <text class="module-icon">🔍</text>
          <text class="module-title">失物招领</text>
          <text class="module-desc">寻找丢失物品，发布招领信息</text>
        </view>
        
        <view class="module-card" @click="navigateToModule('errand')">
          <text class="module-icon">🏃</text>
          <text class="module-title">校园跑腿</text>
          <text class="module-desc">发布跑腿任务，赚取零花钱</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
	export default {
		data() {
			return {
				activeTab: 'login',
				isLoading: false,
				isLoggedIn: false,
				userInfo: {},
				loginForm: {
					username: '',
					password: ''
				},
				registerForm: {
				username: '',
				password: '',
				confirmPassword: '',
				realName: '',
				phone: '',
				email: '',
				college: '',
				grade: '',
				major: '',
				gender: ''
			},
				tokens: {
					lostFound: '',
					errand: ''
				}
			}
		},
		
		onLoad() {
			// 检查本地存储中的登录状态
			this.checkLocalStorage();
		},
		
		methods: {
			// 登录处理
			handleLogin() {
				if (!this.loginForm.username || !this.loginForm.password) {
					uni.showToast({
						title: '请输入用户名和密码',
						icon: 'none'
					});
					return;
				}
				
				this.isLoading = true;
				
				// 同时登录两个系统
				Promise.all([
					this.loginLostFound(),
					this.loginErrand()
				]).then(([lostFoundRes, errandRes]) => {
					this.isLoading = false;
					
					if (lostFoundRes.success && errandRes.success) {
						this.tokens.lostFound = lostFoundRes.token;
						this.tokens.errand = errandRes.token;
						this.userInfo = lostFoundRes.user;
						this.isLoggedIn = true;
						
						// 保存Token到本地存储
						uni.setStorageSync('lostFoundToken', lostFoundRes.token);
						uni.setStorageSync('errandToken', errandRes.token);
						uni.setStorageSync('userInfo', lostFoundRes.user);
						uni.setStorageSync('isLoggedIn', true);
						
						uni.showToast({
							title: '登录成功',
							icon: 'success'
						});
					} else if (lostFoundRes.success && !errandRes.success) {
						// 失物招领登录成功，跑腿登录失败
						uni.showToast({
							title: '登录部分成功，校园跑腿系统登录失败',
							icon: 'none'
						});
					} else if (!lostFoundRes.success && errandRes.success) {
						// 失物招领登录失败，跑腿登录成功
						uni.showToast({
							title: '登录部分成功，失物招领系统登录失败',
							icon: 'none'
						});
					} else {
						uni.showToast({
							title: '登录失败，请检查账号密码或稍后重试',
							icon: 'none'
						});
					}
				}).catch((error) => {
					this.isLoading = false;
					uni.showToast({
						title: '登录失败：' + (error.message || '网络错误'),
						icon: 'none'
					});
				});
			},
			
			// 注册处理
			handleRegister() {
				// 数据验证
				if (!this.registerForm.username || this.registerForm.username.length < 3) {
					uni.showToast({
						title: '用户名至少3个字符',
						icon: 'none'
					});
					return;
				}
				
				if (!this.registerForm.email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.registerForm.email)) {
					uni.showToast({
						title: '请输入正确的邮箱',
						icon: 'none'
					});
					return;
				}
				
				if (!this.registerForm.phone || !/^1[3-9]\d{9}$/.test(this.registerForm.phone)) {
					uni.showToast({
						title: '请输入正确的手机号',
						icon: 'none'
					});
					return;
				}
				
				if (!this.registerForm.realName) {
					uni.showToast({
						title: '请输入真实姓名',
						icon: 'none'
					});
					return;
				}
				
				if (!this.registerForm.college) {
					uni.showToast({
						title: '请输入学院',
						icon: 'none'
					});
					return;
				}
				
				if (!this.registerForm.grade) {
					uni.showToast({
						title: '请输入年级',
						icon: 'none'
					});
					return;
				}
				
				if (!this.registerForm.major) {
					uni.showToast({
						title: '请输入专业',
						icon: 'none'
					});
					return;
				}
				
				if (!this.registerForm.gender) {
					uni.showToast({
						title: '请输入性别',
						icon: 'none'
					});
					return;
				}
				
				if (!this.registerForm.password || this.registerForm.password.length < 6) {
					uni.showToast({
						title: '密码至少6个字符',
						icon: 'none'
					});
					return;
				}
				
				if (this.registerForm.password !== this.registerForm.confirmPassword) {
					uni.showToast({
						title: '两次输入的密码不一致',
						icon: 'none'
					});
					return;
				}
				
				this.isLoading = true;
				
				// 同时注册两个系统
				Promise.all([
					this.registerLostFound(),
					this.registerErrand()
				]).then(([lostFoundRes, errandRes]) => {
					this.isLoading = false;
					
					if (lostFoundRes.success && errandRes.success) {
						this.tokens.lostFound = lostFoundRes.token;
						this.tokens.errand = errandRes.token;
						this.userInfo = lostFoundRes.user;
						this.isLoggedIn = true;
						
						// 保存Token到本地存储
						uni.setStorageSync('lostFoundToken', lostFoundRes.token);
						uni.setStorageSync('errandToken', errandRes.token);
						uni.setStorageSync('userInfo', lostFoundRes.user);
						uni.setStorageSync('isLoggedIn', true);
						
						uni.showToast({
							title: '注册成功',
							icon: 'success'
						});
					} else if (lostFoundRes.success && !errandRes.success) {
						// 失物招领注册成功，跑腿注册失败
						uni.showToast({
							title: '注册部分成功，校园跑腿系统注册失败',
							icon: 'none'
						});
					} else if (!lostFoundRes.success && errandRes.success) {
						// 失物招领注册失败，跑腿注册成功
						uni.showToast({
							title: '注册部分成功，失物招领系统注册失败',
							icon: 'none'
						});
					} else {
						uni.showToast({
							title: '注册失败，请检查信息或稍后重试',
							icon: 'none'
						});
					}
				}).catch((error) => {
					this.isLoading = false;
					uni.showToast({
						title: '注册失败：' + (error.message || '网络错误'),
						icon: 'none'
					});
				});
			},
			
			// 登录失物招领系统
			loginLostFound() {
				return new Promise((resolve) => {
					uni.request({
						url: 'http://localhost:18080/api/auth/login',
						method: 'POST',
						data: this.loginForm,
						header: {
							'Content-Type': 'application/json'
						},
						success: (res) => {
							if (res.data && res.data.code === 200) {
								resolve({
									success: true,
									user: res.data.data.user,
									token: res.data.data.token
								});
							} else {
								resolve({ success: false });
							}
						},
						fail: (err) => {
							console.error('登录失物招领系统失败:', err);
							resolve({ success: false });
						}
					});
				});
			},
			
			// 登录校园跑腿系统
			loginErrand() {
				return new Promise((resolve) => {
					uni.request({
						url: 'http://localhost:18083/api/auth/login',
						method: 'POST',
						data: this.loginForm,
						header: {
							'Content-Type': 'application/json'
						},
						success: (res) => {
							if (res.data && res.data.code === 200) {
								resolve({
									success: true,
									user: res.data.data.user,
									token: res.data.data.token
								});
							} else {
								resolve({ success: false });
							}
						},
						fail: (err) => {
							console.error('登录校园跑腿系统失败:', err);
							resolve({ success: false });
						}
					});
				});
			},
			
			// 注册失物招领系统
			registerLostFound() {
				return new Promise((resolve) => {
					uni.request({
						url: 'http://localhost:18080/api/auth/register',
						method: 'POST',
						header: {
							'Content-Type': 'application/json'
						},
						data: {
							username: this.registerForm.username,
							password: this.registerForm.password,
							realName: this.registerForm.realName,
							phone: this.registerForm.phone,
							email: this.registerForm.email,
							college: this.registerForm.college,
							grade: this.registerForm.grade,
							major: this.registerForm.major,
							gender: this.registerForm.gender === '男' ? 1 : 2
						},
						success: (res) => {
							if (res.data && res.data.code === 200) {
								resolve({
									success: true,
									user: res.data.data.user,
									token: res.data.data.token
								});
							} else {
								resolve({ success: false });
							}
						},
						fail: (err) => {
							console.error('注册失物招领系统失败:', err);
							resolve({ success: false });
						}
					});
				});
			},
			
			// 注册校园跑腿系统
			registerErrand() {
				return new Promise((resolve) => {
					uni.request({
						url: 'http://localhost:18083/api/auth/register',
						method: 'POST',
						header: {
							'Content-Type': 'application/json'
						},
						data: {
							username: this.registerForm.username,
							password: this.registerForm.password,
							realName: this.registerForm.realName,
							phone: this.registerForm.phone,
							nickname: this.registerForm.realName,
							email: this.registerForm.email,
							college: this.registerForm.college,
							grade: this.registerForm.grade,
							major: this.registerForm.major,
							gender: this.registerForm.gender
						},
						success: (res) => {
							if (res.data && res.data.code === 200) {
								resolve({
									success: true,
									user: res.data.data.user,
									token: res.data.data.token
								});
							} else {
								resolve({ success: false });
							}
						},
						fail: (err) => {
							console.error('注册校园跑腿系统失败:', err);
							resolve({ success: false });
						}
					});
				});
			},
			
			// 模块跳转
navigateToModule(module) {
  console.log('=== 开始跳转到模块 ===')
  console.log('模块:', module)
  
  const token = module === 'lost-found' ? this.tokens.lostFound : this.tokens.errand;
  console.log('使用的token:', token)
  console.log('token长度:', token ? token.length : 0)
  
  if (!token) {
    uni.showToast({
      title: 'Token获取失败，请重新登录',
      icon: 'none'
    });
    console.error('=== Token获取失败，跳转中止 ===')
    return;
  }
  
  let url;
  if (module === 'lost-found') {
    // 跳转到指定的失物招领页面，并携带token
    url = 'http://localhost:5174/#/?token=' + encodeURIComponent(token);
    console.log('跳转到失物招领系统:', url)
  } else {
    // 跳转到指定的校园跑腿页面，并携带token
    url = 'http://localhost:5173/#/?token=' + encodeURIComponent(token);
    console.log('跳转到校园跑腿系统:', url)
  }
  
  // 跳转到对应系统
  console.log('=== 执行跳转 ===')
  window.location.href = url;
},
			
			// 退出登录
			handleLogout() {
				this.isLoggedIn = false;
				this.userInfo = {};
				this.tokens = { lostFound: '', errand: '' };
				uni.showToast({
					title: '已退出登录',
					icon: 'success'
				});
			}
		}
	}
</script>

<style>
	.auth-center {
		min-height: 100vh;
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		padding: 40rpx;
	}

	.login-section {
		max-width: 600rpx;
		margin: 0 auto;
		padding-top: 100rpx;
	}

	.app-logo {
		display: flex;
		flex-direction: column;
		align-items: center;
		margin-bottom: 80rpx;
	}

	.logo {
		width: 160rpx;
		height: 160rpx;
		margin-bottom: 30rpx;
	}

	.app-title {
		font-size: 48rpx;
		font-weight: bold;
		color: white;
		margin-bottom: 15rpx;
	}

	.app-subtitle {
		font-size: 28rpx;
		color: rgba(255, 255, 255, 0.8);
	}

	.auth-form {
		background: white;
		border-radius: 24rpx;
		padding: 40rpx;
		box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.15);
	}

	.tab-container {
		display: flex;
		margin-bottom: 40rpx;
		border-bottom: 2rpx solid #f0f0f0;
	}

	.tab-item {
		flex: 1;
		text-align: center;
		padding: 20rpx 0;
		font-size: 32rpx;
		color: #999;
		position: relative;
	}

	.tab-item.active {
		color: #667eea;
		font-weight: bold;
	}

	.tab-item.active::after {
		content: '';
		position: absolute;
		bottom: -2rpx;
		left: 20%;
		width: 60%;
		height: 4rpx;
		background: #667eea;
		border-radius: 2rpx;
	}

	.form-content {
		width: 100%;
	}

	.form-item {
		margin-bottom: 30rpx;
	}

	.input-wrapper {
		display: flex;
		align-items: center;
		background: #f8f8f8;
		border-radius: 12rpx;
		padding: 24rpx 30rpx;
		border: 2rpx solid transparent;
		transition: all 0.3s;
	}

	.input-wrapper:focus-within {
		border-color: #667eea;
		background: white;
	}

	.input-icon {
		font-size: 36rpx;
		margin-right: 20rpx;
		color: #999;
	}

	.form-input {
		flex: 1;
		font-size: 28rpx;
		color: #333;
	}

	.auth-btn {
		width: 100%;
		height: 88rpx;
		background: linear-gradient(45deg, #667eea, #764ba2);
		color: white;
		border: none;
		border-radius: 12rpx;
		font-size: 32rpx;
		font-weight: bold;
		margin-top: 20rpx;
	}

	.auth-btn:disabled {
		opacity: 0.7;
	}

	.module-section {
		max-width: 800rpx;
		margin: 0 auto;
		padding-top: 100rpx;
	}

	.welcome {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 60rpx;
		padding: 30rpx;
		background: rgba(255, 255, 255, 0.1);
		border-radius: 16rpx;
	}

	.welcome-text {
		font-size: 36rpx;
		color: white;
		font-weight: bold;
	}

	.logout-btn {
		padding: 15rpx 30rpx;
		background: rgba(255, 255, 255, 0.2);
		color: white;
		border: none;
		border-radius: 8rpx;
		font-size: 24rpx;
	}

	.module-grid {
		display: grid;
		grid-template-columns: 1fr 1fr;
		gap: 40rpx;
	}

	.module-card {
		background: white;
		border-radius: 20rpx;
		padding: 50rpx 30rpx;
		text-align: center;
		box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
		transition: transform 0.3s, box-shadow 0.3s;
	}

	.module-card:hover {
		transform: translateY(-10rpx);
		box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.15);
	}

	.module-icon {
		font-size: 80rpx;
		margin-bottom: 20rpx;
	}

	.module-title {
		font-size: 32rpx;
		font-weight: bold;
		color: #333;
		margin-bottom: 10rpx;
	}

	.module-desc {
		font-size: 24rpx;
		color: #666;
	}

	/* 响应式设计 */
	@media (max-width: 400px) {
		.auth-center {
			padding: 20rpx;
		}

		.module-grid {
			grid-template-columns: 1fr;
		}

		.module-card {
			padding: 40rpx 20rpx;
		}
	}
</style>
