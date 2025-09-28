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
    <div class="items-center font-weight-700" v-if="activeCardsCount">
      <div class="flex gap-2 mb-8">
        <img class="icon-green" src="../../assets/active-card.svg" alt="active card">
        <div class="font-600 font-bold text-lg">Active Cards ({{ activeCardsCount }})</div>
      </div>
      <div class="flex flex-wrap gap-4 mt-4">
        <div class="mx-auto lg:mx-0" v-for="card in activeCards" :key="card.id"
        >
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
    <div class= "items-center font-weight-700 mt-6" v-if="blockedCardsCount">
      <div class="flex gap-2 mb-8">
        <img class="icon-red" src="../../assets/blocked-card.svg" alt="active card">
        <div class="font-600 font-bold text-lg">Blocked Cards ({{ blockedCardsCount }})</div>
      </div>
      <div class="flex flex-wrap gap-4 mt-4">
        <div class="mx-auto lg:mx-0" v-for="card in blockedCards" :key="card.id"
        >
          <Card 
            :card="card"
            :showMenu="false"
            :showToggle="true"
            @block="handleBlock"
          />
        </div>
      </div>
    </div> 
    <div v-if="!creditCards.length" class="flex justify-center mt-100">
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
</template>

<script>
import Card from '../../components/Card.vue';
import { LOCAL_STORAGE } from '../../constants/constants';
import { getCards, updateCardStatus } from '../../services/cards-service';

export default {
  name: "MyCards",

  components: {
    Card
  },

  data() {
    return {
      creditCards: [],
    }
  },

  async mounted() {
    this.creditCards = await getCards();
  },

  computed: {
    activeCardsCount() {
      return this.creditCards.filter(card => card.cardStatus === 'ACTIVE').length;
    },
    
    blockedCardsCount(){
      return this.creditCards.filter(card => card.cardStatus === 'BLOCKED').length;
    },

    activeCards(){
      return this.creditCards.filter(card => card.cardStatus === 'ACTIVE');
    },

    blockedCards(){
      return this.creditCards.filter(card => card.cardStatus === 'BLOCKED');
    }
  },

  methods: {
    async handleBlock({ card, newStatus }) {
      try {
        const cardIndex = this.creditCards.findIndex(currentCard => currentCard.id === card.id);
        if (cardIndex !== -1) {
          this.creditCards[cardIndex] = {
              ...this.creditCards[cardIndex],
              cardStatus: newStatus
          };
          await updateCardStatus(card.id, newStatus);
          
          // Show success toast message
          this.showToast(
            newStatus === 'BLOCKED' ? 'Card blocked successfully' : 'Card unblocked successfully',
            'success'
          );
        }
      } catch (error) {
        // Show error toast message
        this.showToast('Failed to update card status. Please try again.', 'error');
      }
    },
    
    goToCardDetails(cardId) {
      const card = this.creditCards.find(c => c.id === cardId);
      if (card) {
        localStorage.setItem(LOCAL_STORAGE.SELECTED_CARD, JSON.stringify(card));
      }
      if (cardId && this.$router) {
        this.$router.push({ name: 'CardDetail', params: { id: cardId } })
      }
    },

    showToast(message, type = 'info') {
      // Check if toast library is available
      if (this.$toast) {
        if (type === 'success') {
          this.$toast.success(message);
        } else if (type === 'error') {
          this.$toast.error(message);
        } else {
          this.$toast.info(message);
        }
      } else {
        alert(message);
      }
    }
  }
};
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
