<script setup lang="ts">
const message = 'AI-Knowledge-Hub 前端骨架已就绪'
</script>

<template>
  <main class="home">
    <h1>AI-Knowledge-Hub</h1>
    <p>{{ message }}</p>
  </main>
</template>

<style scoped>
.home {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  font-family: system-ui, -apple-system, sans-serif;
  color: #1f2937;
}
</style>
