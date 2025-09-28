import { Module } from 'vuex'
import { getCards, updateCardStatus } from '../../services/cards-service'

export interface MyCard {
  id: number
  cardNumber: string
  cardHolderName: string
  cardStatus: string
  creditLimit: number
  availableLimit: number
  expiryDate: string
  cvv: number
  cardType?: {
    name: string
    networkType: string
    description: string
  }
}

export interface MyCardsState {
  myCards: MyCard[]
  loading: boolean
  error: string | null
}

const state: MyCardsState = {
  myCards: [],
  loading: false,
  error: null
}

const mutations = {
  SET_LOADING(state: MyCardsState, loading: boolean) {
    state.loading = loading
  },
  
  SET_MY_CARDS(state: MyCardsState, cards: MyCard[]) {
    state.myCards = cards
  },
  
  SET_ERROR(state: MyCardsState, error: string | null) {
    state.error = error
  },
  
  UPDATE_CARD_STATUS(state: MyCardsState, { cardId, newStatus }: { cardId: number, newStatus: string }) {
    const cardIndex = state.myCards.findIndex(card => card.id === cardId)
    if (cardIndex !== -1) {
      state.myCards[cardIndex] = {
        ...state.myCards[cardIndex],
        cardStatus: newStatus
      }
    }
  }
}

const actions = {
  async fetchMyCards({ commit }: any) {
    try {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)
      const cards = await getCards()
      commit('SET_MY_CARDS', cards)
      return cards
    } catch (error: any) {
      commit('SET_ERROR', error.message)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },
  
  async updateMyCardStatus({ commit }: any, { cardId, newStatus }: { cardId: number, newStatus: string }) {
    try {
      commit('SET_LOADING', true)
      await updateCardStatus(cardId, newStatus)
      commit('UPDATE_CARD_STATUS', { cardId, newStatus })
    } catch (error: any) {
      commit('SET_ERROR', error.message)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  }
}

const getters = {
  myCards: (state: MyCardsState) => state.myCards,
  loading: (state: MyCardsState) => state.loading,
  error: (state: MyCardsState) => state.error,
  
  activeMyCards: (state: MyCardsState) => state.myCards.filter(card => card.cardStatus === 'ACTIVE'),
  blockedMyCards: (state: MyCardsState) => state.myCards.filter(card => card.cardStatus === 'BLOCKED'),
  
  activeMyCardsCount: (state: MyCardsState) => state.myCards.filter(card => card.cardStatus === 'ACTIVE').length,
  blockedMyCardsCount: (state: MyCardsState) => state.myCards.filter(card => card.cardStatus === 'BLOCKED').length,
  
  getMyCardById: (state: MyCardsState) => (id: number) => state.myCards.find(card => card.id === id)
}

const myCardsModule: Module<MyCardsState, any> = {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}

export default myCardsModule 