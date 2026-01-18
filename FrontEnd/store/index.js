import { createStore } from 'vuex'

const store = createStore({
  state: {
    user: null,
    token: '',
    isLoggedIn: false,
    userRole: null // 'user', 'admin', 'reviewer'
  },
  
  mutations: {
    SET_USER(state, user) {
      console.log('SET_USER mutation被调用，用户信息:', user)
      state.user = user
      state.isLoggedIn = true
      state.userRole = user ? user.role : null
      console.log('设置用户角色:', state.userRole)
    },
    
    SET_TOKEN(state, token) {
      state.token = token
      uni.setStorageSync('token', token)
    },
    
    LOGOUT(state) {
      state.user = null
      state.token = ''
      state.isLoggedIn = false
      state.userRole = null
      uni.removeStorageSync('token')
      uni.removeStorageSync('user')
    },
    
    INIT_USER(state) {
      const token = uni.getStorageSync('token')
      const user = uni.getStorageSync('user')
      
      if (token && user) {
        state.token = token
        state.user = user
        state.isLoggedIn = true
        state.userRole = user.role
      }
    }
  },
  
  actions: {
    login({ commit }, { user, token }) {
      console.log('store login action被调用:', { user, token })
      commit('SET_USER', user)
      commit('SET_TOKEN', token)
      uni.setStorageSync('user', user)
      console.log('用户信息和token已保存到storage')
    },
    
    logout({ commit }) {
      commit('LOGOUT')
    },
    
    initUser({ commit }) {
      commit('INIT_USER')
    }
  },
  
  getters: {
    isLoggedIn: state => state.isLoggedIn,
    userRole: state => state.userRole,
    user: state => state.user,
    token: state => state.token
  }
})

export default store