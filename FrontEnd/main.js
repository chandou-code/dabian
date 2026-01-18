import { createApp } from 'vue'
import App from './App'
import store from './store'

App.mpType = 'app'

// 添加全局数据对象
const app = createApp({
  ...App,
  globalData: {
    selectedLocation: null
  }
})

app.use(store)
app.mount('#app')