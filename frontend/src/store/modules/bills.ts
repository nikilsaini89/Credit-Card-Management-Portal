import axios from 'axios'

// Types
interface CreditCardBill {
  cardId: number
  cardNumber: string
  cardHolderName: string
  statementDate: string
  dueDate: string
  totalStatementAmount: number
  amountDue: number
  availableCredit: number
  creditLimit: number
  creditLimitUsage: number
  spendingTrend: string
  lastFour: string
  network: string
}

// State
const state = {
  bill: null as CreditCardBill | null,
  loading: false,
  error: null as string | null
}

// Getters
const getters = {
  getBill: (state: any) => state.bill,
  isLoading: (state: any) => state.loading,
  getError: (state: any) => state.error
}

// Mutations
const mutations = {
  SET_BILL(state: any, bill: CreditCardBill) {
    state.bill = bill
  },
  SET_LOADING(state: any, loading: boolean) {
    state.loading = loading
  },
  SET_ERROR(state: any, error: string | null) {
    state.error = error
  }
}

// Actions
const actions = {
  async fetchBill({ commit }: any, { userId, cardId }: { userId: number, cardId: number }) {
    commit('SET_LOADING', true)
    try {
      const response = await axios.get(`http://localhost:8080/api/bills/${userId}/${cardId}`)
      commit('SET_BILL', response.data)
    } catch (error: any) {
      const errorMessage = error.response?.data?.message ||
                          error.response?.data?.error ||
                          'Failed to fetch credit card bill'
      console.error('Error fetching credit card bill:', error)
      commit('SET_ERROR', errorMessage)
    } finally {
      commit('SET_LOADING', false)
    }
  }
}

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
}
