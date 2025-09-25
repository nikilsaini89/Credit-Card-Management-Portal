import { createStore } from "vuex"
import type { AuthState } from '../types/auth'
import auth from './modules/auth'

export interface RootState {
  auth: AuthState
}

export default createStore<RootState>({
  modules: {
    auth
  },
  strict: process.env.NODE_ENV !== 'production'
})