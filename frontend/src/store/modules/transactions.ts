import axios from 'axios'
import type { Module } from 'vuex'

// Types
interface Transaction {
  id: string
  transactionId?: string
  serialNo?: number
  merchant: string
  category: string
  cardId: number
  cardType: string
  lastFour: string
  amount: number
  status: string
  isBnpl: boolean
  createdAt: string
}

interface TransactionsState {
  transactions: Transaction[]
  loading: boolean
  error: string | null
}

interface RootState {
  // Add other module states here if needed
}

const state: TransactionsState = {
  transactions: [
    {
      id: '1',
      transactionId: 'TXN001234567',
      serialNo: 1234567,
      merchant: 'Amazon',
      category: 'shopping',
      cardId: 1,
      cardType: 'VISA',
      lastFour: '1234',
      amount: -2499,
      status: 'completed',
      isBnpl: true,
      createdAt: '2024-01-15T14:30:00Z'
    },
    {
      id: '2',
      transactionId: 'TXN001234568',
      serialNo: 1234568,
      merchant: 'Zomato',
      category: 'food',
      cardId: 1,
      cardType: 'Mastercard',
      lastFour: '5678',
      amount: -450,
      status: 'completed',
      isBnpl: false,
      createdAt: '2024-01-15T12:15:00Z'
    },
    {
      id: '3',
      transactionId: 'TXN001234569',
      serialNo: 1234569,
      merchant: 'Uber',
      category: 'travel',
      cardId: 1,
      cardType: 'VISA',
      lastFour: '1234',
      amount: -320,
      status: 'pending',
      isBnpl: false,
      createdAt: '2024-01-14T18:45:00Z'
    },
    {
      id: '4',
      transactionId: 'TXN001234570',
      serialNo: 1234570,
      merchant: 'Netflix',
      category: 'entertainment',
      cardId: 1,
      cardType: 'Mastercard',
      lastFour: '5678',
      amount: -1599,
      status: 'failed',
      isBnpl: false,
      createdAt: '2024-01-13T09:00:00Z'
    },
    {
      id: '5',
      transactionId: 'TXN001234571',
      serialNo: 1234571,
      merchant: 'Cashback Reward',
      category: 'other',
      cardId: 1,
      cardType: 'VISA',
      lastFour: '1234',
      amount: 150,
      status: 'completed',
      isBnpl: false,
      createdAt: '2024-01-13T09:00:00Z'
    }
  ],
  loading: false,
  error: null
}

const mutations = {
  SET_LOADING(state: TransactionsState, loading: boolean) {
    state.loading = loading
  },
  SET_TRANSACTIONS(state: TransactionsState, transactions: Transaction[]) {
    state.transactions = transactions
  },
  SET_ERROR(state: TransactionsState, error: string | null) {
    state.error = error
  },
  ADD_TRANSACTION(state: TransactionsState, transaction: Transaction) {
    state.transactions.unshift(transaction)
  }
}

const actions = {
  async fetchTransactions({ commit }: any, userId: number) {
    commit('SET_LOADING', true)
    try {
      const response = await axios.get(`http://localhost:8080/api/transactions/${userId}?page=0&size=50`)
      // Handle the response structure from backend
      const transactions = response.data.transactions || response.data
      commit('SET_TRANSACTIONS', transactions)
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 
                          error.response?.data?.error || 
                          'Failed to fetch transactions'
      commit('SET_ERROR', errorMessage)
      console.error('Error fetching transactions:', error)
    } finally {
      commit('SET_LOADING', false)
    }
  },

  async createTransaction({ commit }: any, transactionData: Partial<Transaction>) {
    commit('SET_LOADING', true)
    try {
      const response = await axios.post('http://localhost:8080/api/transactions', transactionData)
      commit('ADD_TRANSACTION', response.data)
      return response.data
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 
                          error.response?.data?.error || 
                          'Failed to create transaction'
      commit('SET_ERROR', errorMessage)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  }
}

const getters = {
  getTransactionsByCard: (state: TransactionsState) => (cardId: number) => {
    return state.transactions.filter(transaction => transaction.cardId === cardId)
  },
  getBnplTransactions: (state: TransactionsState) => {
    return state.transactions.filter(transaction => transaction.isBnpl)
  },
  getTransactionsByStatus: (state: TransactionsState) => (status: string) => {
    return state.transactions.filter(transaction => transaction.status === status)
  }
}

const transactionsModule: Module<TransactionsState, RootState> = {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}

export default transactionsModule
export type { Transaction, TransactionsState }
