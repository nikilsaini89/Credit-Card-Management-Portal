<!-- src/App.vue -->
<template>
  <div id="app">
    <!-- This is where pages from router (Dashboard, Profile, etc.) will render -->
    <router-view />
  </div>
</template>

<script lang="ts">
export default {
  name: 'App'
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