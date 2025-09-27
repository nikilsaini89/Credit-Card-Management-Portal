import axios from 'axios'
import type { Module } from 'vuex'

interface CategoryBreakdown {
  name: string
  percentage: number
  amount: number
}

interface TopMerchant {
  name: string
  count: number
  amount: number
}

interface MonthlyData {
  year: number
  month: number
  amount: number
}

interface AnalyticsData {
  thisMonth: number
  lastMonth: number
  change: number
  categoryBreakdown: CategoryBreakdown[]
  topMerchants: TopMerchant[]
  monthlyData: MonthlyData[]
}

interface AnalyticsState {
  data: AnalyticsData
  loading: boolean
  error: string | null
}

interface RootState {
  
}

const state: AnalyticsState = {
  data: {
    thisMonth: 0,
    lastMonth: 0,
    change: 0,
    categoryBreakdown: [],
    topMerchants: [],
    monthlyData: []
  },
  loading: false,
  error: null
}

const mutations = {
  SET_LOADING(state: AnalyticsState, loading: boolean) {
    state.loading = loading
  },
  SET_ANALYTICS(state: AnalyticsState, analytics: AnalyticsData) {
    state.data = analytics
  },
  SET_ERROR(state: AnalyticsState, error: string | null) {
    state.error = error
  }
}

const actions = {
  async fetchAnalytics({ commit }: any, userId: number) {
    commit('SET_LOADING', true)
    try {
      // Fetch main analytics data
      const analyticsResponse = await axios.get(`http://localhost:8080/api/analytics/${userId}`)
      const analyticsData = analyticsResponse.data
      
      // Fetch monthly spending data
      const monthlyResponse = await axios.get(`http://localhost:8080/api/analytics/${userId}/monthly`)
      const monthlyData = monthlyResponse.data || []
      
      // Transform backend response to frontend format
      const transformedData: AnalyticsData = {
        thisMonth: analyticsData.thisMonth ? Number(analyticsData.thisMonth) : 0,
        lastMonth: analyticsData.lastMonth ? Number(analyticsData.lastMonth) : 0,
        change: analyticsData.change || 0,
        categoryBreakdown: (analyticsData.categoryBreakdown || []).map((cat: any) => ({
          name: cat.name,
          percentage: cat.percentage || 0,
          amount: cat.amount ? Number(cat.amount) : 0,
          color: cat.color || getDefaultColor(cat.name)
        })),
        topMerchants: (analyticsData.topMerchants || []).map((merchant: any) => ({
          name: merchant.name,
          count: merchant.count || 0,
          amount: merchant.amount ? Number(merchant.amount) : 0
        })),
        monthlyData: monthlyData.map((item: any) => ({
          year: item[0] || new Date().getFullYear(),
          month: item[1] || new Date().getMonth() + 1,
          amount: item[2] ? Number(item[2]) : 0
        }))
      }
      
      console.log('Analytics data received:', {
        analyticsData,
        monthlyData,
        transformedData
      })
      
      commit('SET_ANALYTICS', transformedData)
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 
                          error.response?.data?.error || 
                          'Failed to fetch analytics'
      commit('SET_ERROR', errorMessage)
      console.error('Error fetching analytics:', error)
    } finally {
      commit('SET_LOADING', false)
    }
  }
}

// Helper function to assign default colors to categories
function getDefaultColor(categoryName: string): string {
  const colorMap: { [key: string]: string } = {
    'Shopping': 'bg-yellow-500',
    'Food': 'bg-orange-500', 
    'Travel': 'bg-blue-900',
    'Entertainment': 'bg-purple-500',
    'Healthcare': 'bg-green-500',
    'Transportation': 'bg-red-500',
    'Utilities': 'bg-gray-500',
    'Education': 'bg-indigo-500'
  }
  return colorMap[categoryName] || 'bg-gray-400'
}

const getters = {
  getSpendingTrend: (state: AnalyticsState) => {
    return {
      current: state.data.thisMonth,
      previous: state.data.lastMonth,
      change: state.data.change
    }
  }
}

const analyticsModule: Module<AnalyticsState, RootState> = {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}

export default analyticsModule
export type { AnalyticsData, AnalyticsState, CategoryBreakdown, TopMerchant }
