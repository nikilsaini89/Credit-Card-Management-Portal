<!-- src/App.vue -->
<template>
  <div id="app">
    <NavigationBar v-if="!['/', '/register'].includes($route.path)" />
    <div class="main-container">
      <router-view />

    </div>
  </div>
</template>

<script lang="ts">
import NavigationBar from './components/NavigationBar.vue';

export default {
  name: 'App',
  components:{
    NavigationBar
  }
}
</script>

<style>
NavigationBar{
  font-family: "Inter", system-ui, Arial;
}
.main-container {
  max-width: 1180px;
  margin: 28px auto;
  padding: 0 12px;
  font-family: "Inter", system-ui, Arial;
}
</style>
