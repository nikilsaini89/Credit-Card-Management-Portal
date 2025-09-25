<template>
  <div class="mycards">
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-3xl font-bold text-gray-900 mb-3">My Credit Cards</h1>
        <p class="text-gray-500">Manage your credit cards and their settings</p>
      </div>

      <button
        class="flex items-center gap-2 bg-gray-900 text-white px-5 py-2.5 rounded-xl font-medium hover:bg-[#2463ea] transition"
      >
        <img src="../../assets/plus-sign.svg" alt="icon" class="icon" />
        Apply for New Card
      </button>
    </div>
    <div class="gap-2 items-center font-weight-700">
      <img class="icon-green" src="../../assets/active-card.svg" alt="active card">
      <div class="font-600 font-bold text-lg">Active Cards ({{ activeCardsCount }})</div>
      <div class="flex flex-wrap gap-4 mt-4">
        <div v-for="card in activeCards" :key="card.id" 
        >
          <Card 
            :card="card"
            :showMenu="true"
          />
        </div>
      </div>
    </div> 
     <div class="gap-2 flex items-center font-weight-700 mt-6" v-if="blockedCardsCount">
      <img class="icon-red" src="../../assets/blocked-card.svg" alt="active card">
      <div class="font-600 font-bold text-lg">Blocked Cards ({{ blockedCardsCount }})</div>
    </div> 
  </div>
</template>

<script>
import Card from '../../components/Card.vue';
import { getCards } from '../../services/cards-service';


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
</style>
