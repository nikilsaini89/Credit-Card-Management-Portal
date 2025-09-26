import { Module } from 'vuex';
import { User } from '../types/User';
import { login, register,logout } from '../services/authService';
import { getUserProfile } from '../services/userService';
import { RootState } from '../store';

export interface AuthState {
  user: User | null;
  token: string | null;
}


function decodeToken(token: string): { id: number; email: string; role: string } | null {
  try {
    const jwt = token.replace(/^Bearer\s+/i, '');
    const parts = jwt.split('.');
    if (parts.length !== 3) return null;

    const payload = JSON.parse(atob(parts[1]));
    console.log('Decoded payload:', payload);

    const id = Number(payload.userId);
    const email = payload.sub;
    const role = payload.role;

    if (!email || !role || isNaN(id)) {
      throw new Error('Invalid token payload');
    }

    return { id, email, role };
  } catch (error) {
    console.error('Failed to decode token:', error);
    return null;
  }
}



const auth: Module<AuthState, RootState> = {
  namespaced: true,

  state: (): AuthState => {
    const token = localStorage.getItem('token');
    const decoded = decodeToken(token || '');
    const user = decoded ? JSON.parse(localStorage.getItem('user') || 'null') : null;

    return {
      token: decoded ? token : null,
      user
    };
  },

  mutations: {
    setToken(state, token: string) {
      state.token = token;
      localStorage.setItem('token', token);
    },
    setUser(state, user: User) {
      state.user = user;
      localStorage.setItem('user', JSON.stringify(user));
    },
    logout(state) {
     
      localStorage.removeItem('token');
      localStorage.removeItem('user');
       state.token = null;
      state.user = null;
    }
  },

  actions: {
    async login({ commit, dispatch }, credentials: { email: string; password: string }) {
      const res = await login(credentials.email, credentials.password);
      commit('setToken', res.data.token);
      await dispatch('getUserProfile');
    },  

    async register({ commit, dispatch }, userData: Omit<User, 'id'> & { password: string }) {
      const res = await register(userData);
      commit('setToken', res.data.token);
      await dispatch('getUserProfile');
    },
    async logout({ commit }) {
    await logout(); 
    commit('clearUser'); 
  },

    async getUserProfile({ state, commit }) {
      if (!state.token) return;
      const decoded = decodeToken(state.token);
      if (!decoded) return;
      const res = await getUserProfile(decoded.id);
      commit('setUser', res.data);
      return res.data;
    }
  },

  getters: {
    isAuthenticated: (state): boolean => !!state.token,
    token: (state): string | null => state.token
  }
};

export default auth;
