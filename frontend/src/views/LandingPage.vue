<template>
 
  <header class="bg-white text-blue-900 py-6 sticky top-0 z-20 shadow-md">
    <div class="container mx-auto px-4 flex justify-between items-center">
      <h1 class="text-3xl font-extrabold tracking-tight">Swipe Smart</h1>
      <nav class="space-x-6" role="navigation" aria-label="Main navigation">
        <a href="#features" class="hover:text-yellow-400 transition font-medium focus:outline-none focus:ring-2 focus:ring-yellow-400" aria-label="Features section">Features</a>
        <a href="#advantages" class="hover:text-yellow-400 transition font-medium focus:outline-none focus:ring-2 focus:ring-yellow-400" aria-label="Advantages section">Advantages</a>
        <a href="#contact" class="hover:text-yellow-400 transition font-medium focus:outline-none focus:ring-2 focus:ring-yellow-400" aria-label="Contact section">Contact</a>
      </nav>
    </div>
  </header>
 
 
  <section class="relative overflow-hidden py-32 bg-gradient-to-b from-white to-gray-100">
    <div class="container mx-auto px-4 text-center relative z-10">
      <h2 class="text-5xl md:text-6xl font-extrabold mb-6 motion-safe:animate-fade-in-up">Take Control of Your Finances</h2>
      <p class="text-xl mb-10 max-w-3xl mx-auto motion-safe:animate-fade-in-up motion-safe:delay-200">
        Swipe Smart simplifies <span class="font-bold text-yellow-400">{{ currentTagline }}</span> with powerful tools.
      </p>
      <router-link to="/login">
  <button
    class="bg-blue-900 text-white px-10 py-4 rounded-full text-lg font-semibold hover:bg-blue-800 transition focus:outline-none focus:ring-2 focus:ring-yellow-400"
  >
    Get Started Today
  </button>
</router-link>
    </div>
  </section>
 
  <section id="features" class="py-24 bg-gray-100 text-blue-900">
    <div class="container mx-auto px-4">
      <h2 class="text-4xl font-bold text-center mb-16">Powerful Features</h2>
      <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-8">
        <div
          v-for="feature in features"
          :key="feature.id"
          class="bg-yellow-400/10 p-8 rounded-2xl shadow-lg hover:scale-105 transition"
        >
          <img :src="feature.icon" :alt="feature.title" class="w-16 h-16 mb-6 mx-auto" />
          <h3 class="text-2xl font-semibold mb-4 text-blue-900">{{ feature.title }}</h3>
          <p>{{ feature.description }}</p>
        </div>
      </div>
    </div>
  </section>
 
  <section id="advantages" class="py-24 bg-white text-blue-900">
    <div class="container mx-auto px-4">
      <h2 class="text-4xl font-bold text-center mb-16">Advantages of Using a Credit Card</h2>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-10">
        <div class="bg-yellow-400/10 p-8 rounded-2xl shadow-lg hover:scale-105 transition">
          <img src="https://img.icons8.com/?size=100&id=GUqqHpQDjGqG&format=png&color=000000" alt="Cashback" class="w-16 h-16 mb-6 mx-auto"/>
          <h3 class="text-2xl font-semibold mb-4 text-blue-900">Cashback & Rewards</h3>
          <p>Earn rewards on every purchase and get cashback for smarter spending.</p>
        </div>
        <div class="bg-yellow-400/10 p-8 rounded-2xl shadow-lg hover:scale-105 transition">
          <img src="https://img.icons8.com/?size=100&id=fwSm4t5INyWx&format=png&color=000000" alt="Secure" class="w-16 h-16 mb-6 mx-auto"/>
          <h3 class="text-2xl font-semibold mb-4 text-blue-900">Secure Payments</h3>
          <p>Advanced security features to protect your transactions and personal data.</p>
        </div>
        <div class="bg-yellow-400/10 p-8 rounded-2xl shadow-lg hover:scale-105 transition">
          <img src="https://img.icons8.com/?size=100&id=34028&format=png&color=000000" alt="Budget" class="w-16 h-16 mb-6 mx-auto"/>
          <h3 class="text-2xl font-semibold mb-4 text-blue-900">Budget Management</h3>
          <p>Track your expenses and manage your monthly budget efficiently.</p>
        </div>
      </div>
    </div>
  </section>
 
 
  <footer id="contact" class="bg-blue-900 text-white py-16">
    <div class="container mx-auto px-4 text-center">
      <h3 class="text-2xl font-bold mb-4">Swipe Smart</h3>
      <p class="text-lg mb-6">Join thousands of users managing their credit cards smarter.</p>
      <p class="text-base mb-6">
        Contact us at <a href="mailto:support@swipesmart.com" class="underline hover:text-yellow-400">support@swipesmart.com</a>
      </p>
      <button @click="scrollToTop" class="mt-8 bg-yellow-400 text-blue-900 px-6 py-3 rounded-full font-semibold hover:bg-yellow-500 transition">
        Back to Top
      </button>
    </div>
  </footer>
</template>
 
<script lang="ts">
import { defineComponent, ref, onMounted } from "vue";
 
export default defineComponent({
  name: "LandingPage",
  setup() {
    const features = ref([
      { id: 1, title: "Instant Alerts", description: "Real-time notifications for every transaction.", icon: "https://img.icons8.com/emoji/96/000000/credit-card-emoji.png" },
      { id: 2, title: "Spending Insights", description: "Visual charts to track your expenses easily.", icon: "https://img.icons8.com/emoji/96/000000/bar-chart-emoji.png" },
      { id: 3, title: "BNPL", description: "BNPL lets you split your purchase into easy installments—buy today, pay later without extra stress.", icon: "https://img.icons8.com/?size=100&id=bpb1NWfrmmHv&format=png&color=000000" }
    ]);
 
    const taglines = ["your spending", "your budget", "your rewards"];
    const currentTagline = ref(taglines[0]);
    let index = 0;
 
    onMounted(() => {
      setInterval(() => {
        index = (index + 1) % taglines.length;
        currentTagline.value = taglines[index];
      }, 3000);
    });
 
    const scrollToTop = () => window.scrollTo({ top: 0, behavior: "smooth" });
 
    return { features, currentTagline, scrollToTop };
  }
});
</script>
 
 