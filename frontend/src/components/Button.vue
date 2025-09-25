<template>
  <button class="apply-button">
    <img :src="iconSrc" alt="icon" class="icon" />
    <span class="button-text">{{ text }}</span>
  </button>
</template>

<script>
export default {
  name: 'Button',
  props: {
    icon: {
      type: String,
      required: true,
    },
    text: {
      type: String,
      required: true,
    },
  },
  computed: {
    iconSrc() {
      // Correct the path to use a relative path
      const icons = import.meta.glob('/src/assets/*.svg', { eager: true });
      const iconPath = icons[`/src/assets/${this.icon}`];
      return iconPath || ''; // Return an empty string if icon is not found
    }
  },
};
</script>

<style scoped>
.apply-button {
  display: flex;
  align-items: center;
  padding: 10px 20px;
  background-color: #1d253c;
  color: white;
  font-size: 1rem;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.apply-button:hover {
  background-color: #2e3a52;
}

.icon {
  margin-right: 8px;
  width: 20px;
  height: 20px;
}

.button-text {
  font-size: 1rem;
}
</style>
