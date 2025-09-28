<template>
  <div class="mycards">
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-3xl font-bold text-gray-900 mb-3">My Credit Cards</h1>
        <p class="text-gray-500">Manage your credit cards and their settings</p>
      </div>

      <button
        class="flex items-center gap-2 bg-[#ffd60a] text-black px-5 py-2.5 rounded-xl font-medium hover:bg-[#e5cd56] transition"
        @click="$router.push('/apply-card')"
      >
        <img src="../../assets/plus-sign.svg" alt="icon" />
        Apply for New Card
      </button>
    </div>
    
    <!-- Loading State -->
    <div v-if="loading" class="flex justify-center py-12">
      <div class="w-8 h-8 border-4 border-gray-200 border-t-blue-600 rounded-full animate-spin"></div>
    </div>
    
    <!-- Error State -->
    <div v-else-if="error" class="flex justify-center py-12">
      <div class="text-red-600">{{ error }}</div>
    </div>
    
    <!-- Cards Content -->
    <div v-else>
      <div class="items-center font-weight-700" v-if="activeMyCardsCount">
        <div class="flex gap-2 mb-8">
          <img class="icon-green" src="../../assets/active-card.svg" alt="active card">
          <div class="font-600 font-bold text-lg">Active Cards ({{ activeMyCardsCount }})</div>
        </div>
        <div class="flex flex-wrap gap-4 mt-4">
          <div class="mx-auto lg:mx-0" v-for="card in activeMyCards" :key="card.id">
            <Card 
              :card="card"
              :showMenu="false"
              :showToggle="true"
              @block="handleBlock"
              @view-details="goToCardDetails"
            />
          </div>
        </div>
      </div> 
      
      <div class="items-center font-weight-700 mt-6" v-if="blockedMyCardsCount">
        <div class="flex gap-2 mb-8">
          <img class="icon-red" src="../../assets/blocked-card.svg" alt="active card">
          <div class="font-600 font-bold text-lg">Blocked Cards ({{ blockedMyCardsCount }})</div>
        </div>
        <div class="flex flex-wrap gap-4 mt-4">
          <div class="mx-auto lg:mx-0" v-for="card in blockedMyCards" :key="card.id">
            <Card 
              :card="card"
              :showMenu="false"
              :showToggle="true"
              @block="handleBlock"
            />
          </div>
        </div>
      </div> 
      
      <div v-if="!myCards.length" class="flex justify-center mt-100">
        <div class="flex flex-col items-center bg-gray-50 border border-dashed border-gray-300 rounded-xl p-8 max-w-md text-center">
          <!-- Icon -->
          <div class="flex items-center justify-center w-20 h-20 rounded-full bg-gray-100 mb-4">
            <svg class="w-12 h-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="1.5"
                d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"
              />
            </svg>
          </div>

          <!-- Title -->
          <h3 class="text-xl font-semibold text-gray-900 mb-2">No Credit Cards Found</h3>

          <!-- Description -->
          <p class="text-gray-500 mb-6">
            Get started by applying for your first credit card
          </p>

          <!-- Button -->
          <button
            class="flex items-center gap-2 bg-[#ffd60a] text-black px-5 py-2.5 rounded-xl font-medium hover:bg-[#e5cd56] transition"
            @click="$router.push('/apply-card')"
          >
            <img src="../../assets/plus-sign.svg" alt="Add" class="w-4 h-4" />
            Apply for New Card
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Card from '../../components/Card.vue'
import { LOCAL_STORAGE } from '../../constants/constants'
import { mapGetters, mapActions } from 'vuex'
import type { MyCard } from '../../store/modules/myCards'

export default defineComponent({
  name: "MyCards",

  components: {
    Card
  },

  computed: {
    ...mapGetters('myCards', [
      'myCards',
      'loading',
      'error',
      'activeMyCards',
      'blockedMyCards',
      'activeMyCardsCount',
      'blockedMyCardsCount'
    ])
  },

  async mounted() {
    try {
      await this.fetchMyCards()
    } catch (error) {
      console.error('Error loading cards:', error)
    }
  },

  methods: {
    ...mapActions('myCards', ['fetchMyCards', 'updateMyCardStatus']),
    
    async handleBlock({ card, newStatus }: { card: MyCard, newStatus: string }) {
      try {
        await this.updateMyCardStatus({ cardId: card.id, newStatus })
      } catch (error) {
        console.error('Error updating card status:', error)
      }
    },
    
    goToCardDetails(cardId: number) {
      const card = this.myCards.find((c: MyCard) => c.id === cardId)
      if (card) {
        localStorage.setItem(LOCAL_STORAGE.SELECTED_CARD, JSON.stringify(card))
      }
      if (cardId && this.$router) {
        this.$router.push({ name: 'CardDetail', params: { id: cardId } })
      }
    }
  }
})
</script>

<style>
.icon{
  filter: invert(1);
}

.icon-green {
  filter: invert(45%) sepia(99%) saturate(372%) hue-rotate(85deg) brightness(95%) contrast(92%);
}

.icon-red {
  filter: invert(29%) sepia(96%) saturate(7484%) hue-rotate(358deg) brightness(96%) contrast(101%);
}

.mt-100{
  margin: 100px;
}
</style>
