import type { Module } from "vuex/types/index.js"
import type {
  AuthState,
  LoginCredentials,
  RegisterCredentials,
  User,
  AuthResponse
} from "../../types/auth"
import type { RootState } from "../index"
import api from "../../services/api"

const authModule: Module<AuthState, RootState> = {
  namespaced: true,

  state: (): AuthState => ({
    user: null,
    token: localStorage.getItem("token"),
    isAuthenticated: !!localStorage.getItem("token"),
    loading: false,
    error: null
  }),

  getters: {
    isAuthenticated: (state): boolean => state.isAuthenticated,
    currentUser: (state): User | null => state.user,
    isLoading: (state): boolean => state.loading,
    error: (state): string | null => state.error
  },

  mutations: {
    SET_LOADING(state, loading: boolean) {
      state.loading = loading
    },

    SET_ERROR(state, error: string | null) {
      state.error = error
    },

    SET_AUTH_SUCCESS(state, { user, token }: { user: User; token: string }) {
      state.user = user
      state.token = token
      state.isAuthenticated = true
      state.error = null
      localStorage.setItem("token", token)
    },

    CLEAR_AUTH(state) {
      state.user = null
      state.token = null
      state.isAuthenticated = false
      localStorage.removeItem("token")
    }
  },

  actions: {
    async login({ commit }, credentials: LoginCredentials): Promise<AuthResponse> {
      commit("SET_LOADING", true)
      commit("SET_ERROR", null)

      try {
        const response: AuthResponse = await api.login(credentials)

        const user: User = {
          id: response.user.id,
          email: response.user.email,
          name: response.user.name,
          role: response.user.role as "user" | "admin"
        }

        commit("SET_AUTH_SUCCESS", { user, token: response.token })
        return response
      } catch (error: unknown) {
        const errorMessage =
          error instanceof Error ? error.message : "Login failed"
        commit("SET_ERROR", errorMessage)
        throw error
      } finally {
        commit("SET_LOADING", false)
      }
    },

    async register({ commit }, credentials: RegisterCredentials): Promise<AuthResponse> {
      commit("SET_LOADING", true)
      commit("SET_ERROR", null)

      try {
        const response: AuthResponse = await api.register(credentials)
        return response
      } catch (error: unknown) {
        const errorMessage =
          error instanceof Error ? error.message : "Registration failed"
        commit("SET_ERROR", errorMessage)
        throw error
      } finally {
        commit("SET_LOADING", false)
      }
    },

    async logout({ commit }) {
      try {
        await api.logout()
      } catch (error) {
        console.error("Logout API call failed:", error)
      } finally {
        commit("CLEAR_AUTH")
      }
    }
  }
}

export default authModule
