<template>
  <div class="login-page">
    <div class="login-header">
      <!-- Logo and Header -->
      <!-- <CreditCardIcon class="logo" /> -->
      <h1>CreditCard Portal</h1>
      <p>Manage your credit cards with ease</p>
    </div>

    <div class="login-container">
      <div class="login-box">
        <!-- Form Header -->
        <div class="form-header">
          <h2>Sign In</h2>
          <p>Enter your credentials to access your account</p>
        </div>

        <!-- Login Form -->
        <form @submit.prevent="handleSubmit" class="login-form">
          <!-- Email Field -->
          <div class="form-group">
            <label for="email">Email</label>
            <div class="input-group">
              <div class="input-icon">
                <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                    d="M16 12a4 4 0 10-8 0 4 4 0 008 0zm0 0v1.5a2.5 2.5 0 005 0V12a9 9 0 10-9 9m4.5-1.206a8.959 8.959 0 01-4.5 1.207" />
                </svg>
              </div>
              <input
                id="email"
                type="email"
                placeholder="Enter your email"
                v-model="form.email"
                :disabled="loading"
              />
            </div>
          </div>

          <!-- Password Field -->
          <div class="form-group">
            <label for="password">Password</label>
            <div class="input-group">
              <div class="input-icon">
                <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                    d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                </svg>
              </div>
              <input
                id="password"
                type="password"
                placeholder="Enter your password"
                v-model="form.password"
                :disabled="loading"
              />
            </div>
          </div>

          <!-- Error Message -->
          <div v-if="error" class="error-box">
            <div class="error-content">
              <svg class="error-icon" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
              </svg>
              <span>{{ error }}</span>
            </div>
          </div>

          <!-- Submit Button -->
          <button type="submit" :disabled="loading" class="btn-primary">
            <LoadingSpinner v-if="loading" />
            <span v-else>Sign In</span>
          </button>

          <!-- Links -->
          <div class="form-links">
            <router-link to="/forgot-password" class="link">Forgot your password?</router-link>
            <p>Don't have an account? <router-link to="/signup" class="link">Sign up</router-link></p>
          </div>
        </form>

        
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import CreditCardIcon from '../components/CreditCardIcon.vue'
import LoadingSpinner from '../components/LoadingSpinner.vue'
import type { LoginCredentials } from '../types/auth'

const store = useStore()
const router = useRouter()

const form = reactive<LoginCredentials>({
  email: '',
  password: ''
})

const loading = computed(() => store.getters['auth/isLoading'])
const error = computed(() => store.getters['auth/error'])

const handleSubmit = async () => {
  try {
    await store.dispatch('auth/login', form)
    router.push('/dashboard')
  } catch (err) {
    console.error('Login failed:', err)
  }
}
</script>

<style scoped>
.login-page {
  background: linear-gradient(135deg, #ffffff 50%,#e5cd56 100%);
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 3rem 1.5rem;
  background-color: #f9fafb;
}

.login-header {
  text-align: center;
  margin-bottom: 2rem;
}

.login-header h1 {
  font-size: 2rem;
  font-weight: 700;
  margin-top: 1rem;
  color: #111827;
}

.login-header p {
  font-size: 0.875rem;
  color: #4b5563;
}

.login-container {
  display: flex;
  justify-content: center;
}

.login-box {
  background-color: #fff;
  padding: 2rem;
  width: 100%;
  border-radius: 1rem;
  max-width: 28rem;
  border: 1px solid #e5e7eb;
  border-radius: 1rem;
   box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15); 
  transition: transform 0.3s, box-shadow 0.3s;
}

.form-header h2 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #111827;
}

.form-header p {
  font-size: 0.875rem;
  color: #4b5563;
  margin-top: 0.25rem;
}

.login-form .form-group {
  margin-bottom: 1.5rem;
}

.login-form label {
  display: block;
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
  margin-bottom: 0.5rem;
}

.input-group {
  position: relative;
  width:400px;
}

.input-group input {
  width: 100%;
  padding: 0.75rem 0.75rem 0.75rem 2.5rem;
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
  background-color: #fff;
  font-size: 0.875rem;
  color: #111827;
  outline: none;
}

.input-group input:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.input-icon {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  width: 1.25rem;
  height: 1.25rem;
  transform: translateY(-50%);
  color: #9ca3af;
  pointer-events: none;
}

.btn-primary {
  width: 100%;
  padding: 0.75rem;
  font-size: 0.875rem;
  font-weight: 500;
  color: #fff;
  background-color: #111827;
  border-radius: 0.5rem;
  border: none;
  cursor: pointer;
  transition: background-color 0.2s;
  display: flex;
  justify-content: center;
  align-items: center;
}

.btn-primary:hover{
  color: #111827;
  background-color: #e5cd56;
  transform: translateY(-1px);
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-primary:hover:not(:disabled) {
  background-color: #e5cd56;
}

.error-box {
  background-color: #fef2f2;
  border-radius: 0.375rem;
  padding: 1rem;
  margin-bottom: 1rem;
}

.error-content {
  display: flex;
  align-items: center;
}

.error-icon {
  width: 1.25rem;
  height: 1.25rem;
  color: #ef4444;
  flex-shrink: 0;
}

.error-content span {
  margin-left: 0.75rem;
  font-size: 0.875rem;
  color: #b91c1c;
}

.form-links {
  text-align: center;
  font-size: 0.875rem;
  margin-top: 0.5rem;
}

.form-links .link {
  color: #111827;
  text-decoration: underline;
  cursor: pointer;
}

.demo-credentials {
  border-top: 1px solid #e5e7eb;
  margin-top: 2rem;
  padding-top: 1.5rem;
  font-size: 0.75rem;
  color: #6b7280;
}

.demo-credentials .demo-title {
  font-weight: 500;
  margin-bottom: 0.5rem;
}
</style>
