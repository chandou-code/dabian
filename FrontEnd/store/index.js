import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

const store = new Vuex.Store({
  state: {
    user: null,
    token: '',
    isLoggedIn: false,
    userRole: null // 'user', 'admin', 'reviewer'
  },
  
  mutations: {
    SET_USER(state, user) {
      state.user = user
      state.isLoggedIn = true
      state.userRole = user.role
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
      commit('SET_USER', user)
      commit('SET_TOKEN', token)
      uni.setStorageSync('user', user)
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