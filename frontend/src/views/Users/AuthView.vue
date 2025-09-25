<!-- AuthView.vue -->
<template>
  <AuthForm />
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import AuthForm from '../../components/AuthForm.vue';

export default defineComponent({
  components: { AuthForm }
});
</script>
