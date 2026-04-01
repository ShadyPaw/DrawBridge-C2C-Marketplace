import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 1)
  const userId = computed(() => user.value?.id)
  const nickname = computed(() => user.value?.nickname || user.value?.username || '用户')
  const avatar = computed(() => user.value?.avatar)

  let ws = null

  function connectWs() {
    if (!userId.value || ws) return
    const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
    const wsUrl = `${protocol}//localhost:8080/ws/chat/${userId.value}`
    ws = new WebSocket(wsUrl)
    
    ws.onmessage = (event) => {
      try {
        const msg = JSON.parse(event.data)
        if (msg.type === 'READ_RECEIPT') {
          window.dispatchEvent(new CustomEvent('chat-read-receipt', { detail: msg }))
        } else {
          window.dispatchEvent(new CustomEvent('new-chat-message', { detail: msg }))
        }
      } catch(e) {}
    }
    
    ws.onclose = () => {
      ws = null
      if (isLoggedIn.value) setTimeout(connectWs, 3000)
    }
  }

  function disconnectWs() {
    if (ws) {
      ws.onclose = null
      ws.close()
      ws = null
    }
  }

  function sendWsMessage(payload) {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify(payload))
    }
  }

  function setLoginData(data) {
    token.value = data.token
    user.value = data.user
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(data.user))
    connectWs()
  }

  function updateUser(userData) {
    user.value = { ...user.value, ...userData }
    localStorage.setItem('user', JSON.stringify(user.value))
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    disconnectWs()
  }

  return {
    token, user, isLoggedIn, isAdmin, userId, nickname, avatar,
    setLoginData, updateUser, logout, connectWs, disconnectWs, sendWsMessage
  }
})
