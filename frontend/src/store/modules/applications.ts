import { Module } from 'vuex';
import { getCardApplications, updateApplicationStatus } from '../../services/cards-service';
import type { CardApplication } from '../../types/cardApplication';
import { RootState } from '../index';

export interface ApplicationsState {
  applications: CardApplication[];
  loading: boolean;
  error: string | null;
}

const applications: Module<ApplicationsState, RootState> = {
  namespaced: true,

  state: (): ApplicationsState => ({
    applications: [],
    loading: false,
    error: null,
  }),

  mutations: {
    setApplications(state, applications: CardApplication[]) {
      state.applications = applications;
    },
    setLoading(state, loading: boolean) {
      state.loading = loading;
    },
    setError(state, error: string | null) {
      state.error = error;
    },
    updateApplication(state, updatedApplication: CardApplication) {
      const index = state.applications.findIndex(app => app.id === updatedApplication.id);
      if (index !== -1) {
        state.applications[index] = updatedApplication;
      }
    },
  },

  actions: {
    async fetchApplications({ commit }) {
      try {
        commit('setLoading', true);
        commit('setError', null);
        const applications = await getCardApplications();
        commit('setApplications', applications);
      } catch (error) {
        commit('setError', 'Failed to fetch applications');
        console.error('Error fetching applications:', error);
      } finally {
        commit('setLoading', false);
      }
    },

    async updateApplicationStatus({ commit }, { applicationId, status }: { applicationId: number; status: 'ACCEPTED' | 'REJECTED' }) {
      try {
        commit('setLoading', true);
        commit('setError', null);
        const updatedApplication = await updateApplicationStatus(applicationId, status);
        if (updatedApplication) {
          commit('updateApplication', updatedApplication);
        }
        return updatedApplication;
      } catch (error) {
        commit('setError', 'Failed to update application status');
        console.error('Error updating application status:', error);
        throw error;
      } finally {
        commit('setLoading', false);
      }
    },
  },

  getters: {
    applications: (state) => state.applications,
    loading: (state) => state.loading,
    error: (state) => state.error,
    pendingApplications: (state) => state.applications.filter(app => app.status === 'PENDING'),
    acceptedApplications: (state) => state.applications.filter(app => app.status === 'ACCEPTED'),
    rejectedApplications: (state) => state.applications.filter(app => app.status === 'REJECTED'),
  },
};

export default applications; 