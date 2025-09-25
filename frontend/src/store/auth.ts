import { Module } from 'vuex';
import { User } from '../types/User';
import { login, register } from '../services/authService';
import { RootState } from '../store';

export interface AuthState {
  user: User | null;
  token: string | null;
}

const auth: Module<AuthState, RootState> = {
  namespaced: true,

  state: (): AuthState => ({
    user: JSON.parse(localStorage.getItem('user') || 'null'),
    token: localStorage.getItem('token')
  }),

  mutations: {
    setUser(state, payload: User) {
      state.user = payload;
      localStorage.setItem('user', JSON.stringify(payload));
    },
    setToken(state, token: string) {
      state.token = token;
      localStorage.setItem('token', token);
    },
    logout(state) {
      state.user = null;
      state.token = null;
      localStorage.removeItem('user');
      localStorage.removeItem('token');
    }
  },

  actions: {
    async login({ commit }, credentials: { email: string; password: string }) {
      const res = await login(credentials.email, credentials.password);
      commit('setUser', res.data.user);
      commit('setToken', res.data.token);
    },

    async register({ commit }, userData: Omit<User, 'id'> & { password: string }) {
      const res = await register(userData);
      commit('setUser', res.data.user);
      commit('setToken', res.data.token);
    }
  },

  getters: {
    isAuthenticated: (state): boolean => !!state.token,
    user: (state): User | null => state.user,
    token: (state): string | null => state.token
  }
};

export default auth;
