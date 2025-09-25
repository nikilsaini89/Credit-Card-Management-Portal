import { Module } from 'vuex';
import { User } from '../types/User';
import { RootState } from '../store'; 
import { getUserProfile, updateUserProfile } from '../services/userService';

// Define the shape of the user state
export interface UserState {
  profile: User | null;
}

// Vuex module
const user: Module<UserState, RootState> = {
  namespaced: true,
  state: (): UserState => ({
    profile: null
  }),

  mutations: {
    setProfile(state, payload: User) {
      state.profile = payload;
    }
  },

  actions: {
    async fetchProfile({ commit }, userId: number) {
      const res = await getUserProfile(userId);
      commit('setProfile', res.data);
    },

    async updateProfile({ commit }, { userId, payload }: {
      userId: number;
      payload: Omit<User, 'id' | 'email'>
    }) {
      const res = await updateUserProfile(userId, payload);
      commit('setProfile', res.data);
    }
  },

  getters: {
    profile: (state): User | null => state.profile
  }
};

export default user;
