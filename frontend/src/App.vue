<!-- src/App.vue -->
<template>
  <div id="app">
    <NavigationBar v-if="!['/', '/register'].includes($route.path)" />
    <router-view />
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
/* You can add global styles here */
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
}
</style>
