import { defineStore } from 'pinia'
import request from '../utils/request'

interface UserInfo {
  userId: number
  username: string
  realName: string
  roles: string[]
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null') as UserInfo | null
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    roles: (state) => state.user?.roles || []
  },
  actions: {
    async login(username: string, password: string) {
      const data: any = await request.post('/system/auth/login', { username, password })
      this.token = data.accessToken
      this.user = {
        userId: data.userId,
        username: data.username,
        realName: data.realName,
        roles: data.roles || []
      }
      localStorage.setItem('token', this.token)
      localStorage.setItem('user', JSON.stringify(this.user))
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.clear()
    }
  }
})
