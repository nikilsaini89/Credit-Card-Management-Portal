<template>
  <div class="auth-page">
    <!-- Header -->
    <div class="auth-header">
      <h1>CreditCard Portal</h1>
      <p v-if="!isRegister">Manage your credit cards with ease</p>
      <p v-else>Create your account and get started</p>
    </div>

    <div class="auth-container">
      <div class="auth-box">
        <!-- Toggle Buttons -->
        <div class="toggle">
          <button :class="{ active: !isRegister }" @click="isRegister = false">Login</button>
          <button :class="{ active: isRegister }" @click="isRegister = true">Register</button>
        </div>

        <!-- LOGIN FORM -->
        <form v-if="!isRegister" @submit.prevent="handleLogin" class="auth-form">
          <div class="form-group">
            <label>Email</label>
            <input v-model="loginForm.email" type="email" placeholder="Enter your email" />
            <span class="error" v-if="errors.email">{{ errors.email }}</span>
          </div>

          <div class="form-group">
            <label>Password</label>
            <input v-model="loginForm.password" type="password" placeholder="Enter your password" />
            <span class="error" v-if="errors.password">{{ errors.password }}</span>
          </div>

          <button class="btn-primary" type="submit">Sign In</button>
        </form>

        <!-- REGISTER FORM -->
        <form v-else @submit.prevent="handleRegister" class="auth-form">
          <div class="form-group">
            <label>Name</label>
            <input v-model="registerForm.name" type="text" placeholder="Your full name" />
            <span class="error" v-if="errors.name">{{ errors.name }}</span>
          </div>

          <div class="form-group">
            <label>Email</label>
            <input v-model="registerForm.email" type="email" placeholder="Enter your email" />
            <span class="error" v-if="errors.email">{{ errors.email }}</span>
          </div>

          <div class="form-group">
            <label>Password</label>
            <input v-model="registerForm.password" type="password" placeholder="Create a password" />
            <span class="error" v-if="errors.password">{{ errors.password }}</span>
          </div>

          <div class="form-group">
            <label>Phone Number</label>
            <input v-model="registerForm.phoneNumber" type="text" placeholder="10-digit phone number" />
            <span class="error" v-if="errors.phoneNumber">{{ errors.phoneNumber }}</span>
          </div>

          <div class="form-group">
            <label>Address</label>
            <input v-model="registerForm.address" type="text" placeholder="Your address" />
            <span class="error" v-if="errors.address">{{ errors.address }}</span>
          </div>

          <div class="form-group">
            <label>Annual Income</label>
            <input v-model="registerForm.annualIncome" type="number" placeholder="Annual income" />
            <span class="error" v-if="errors.annualIncome">{{ errors.annualIncome }}</span>
          </div>

          <button class="btn-primary" type="submit">Register</button>
        </form>
      </div>
    </div>
  </div>
</template>


<script setup lang="ts">
import { reactive, ref } from "vue";
import { useStore } from "vuex";
import { useRouter } from "vue-router";

const store = useStore();
const router = useRouter();

const isRegister = ref(false);

// Forms
const loginForm = reactive({ email: "", password: "" });
const registerForm = reactive({
  name: "",
  email: "",
  password: "",
  phoneNumber: "",
  address: "",
  annualIncome: null as number | null,
});

// Validation errors
const errors = reactive<Record<string, string>>({});

// Validate login
const validateLogin = () => {
  errors.email = !loginForm.email ? "Email is required" : "";
  errors.password = !loginForm.password ? "Password is required" : "";
  return !errors.email && !errors.password;
};

// Validate register
const validateRegister = () => {
  errors.name = !registerForm.name ? "Name is required" : "";
  errors.email = !registerForm.email
    ? "Email is required"
    : !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(registerForm.email)
    ? "Invalid email"
    : "";
  errors.password =
    registerForm.password.length < 6 ? "Password must be at least 6 characters" : "";
  errors.phoneNumber = !/^\d{10}$/.test(registerForm.phoneNumber)
    ? "Phone must be 10 digits"
    : "";
  errors.address = !registerForm.address ? "Address is required" : "";
  errors.annualIncome =
    !registerForm.annualIncome || registerForm.annualIncome <= 0
      ? "Annual income must be positive"
      : "";
  return Object.values(errors).every((v) => !v);
};

// Submit handlers
const handleLogin = async () => {
  if (validateLogin()) {
    try {
      await store.dispatch("auth/login", loginForm);
      router.push("/dashboard");
    } catch (err) {
      console.error("Login failed:", err);
    }
  }
};

const handleRegister = async () => {
  if (validateRegister()) {
    try {
      await store.dispatch("auth/register", registerForm);
      router.push("/");
    } catch (err) {
      console.error("Registration failed:", err);
    }
  }
};
</script>


<style scoped>
.auth-page {
  background: linear-gradient(135deg, #ffffff 50%, #e5cd56 100%);
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 3rem 1.5rem;
}

.auth-header {
  text-align: center;
  margin-bottom: 2rem;
}

.auth-header h1 {
  font-size: 2rem;
  font-weight: 700;
  color: #111827;
}

.auth-header p {
  font-size: 0.9rem;
  color: #4b5563;
}

.auth-container {
  display: flex;
  justify-content: center;
}

.auth-box {
  background: #fff;
  padding: 2rem;
  max-width: 30rem;
  width: 100%;
  border-radius: 1rem;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.toggle {
  display: flex;
  justify-content: center;
  margin-bottom: 1.5rem;
}

.toggle button {
  flex: 1;
  padding: 0.75rem;
  font-weight: 600;
  border: none;
  background: #f3f4f6;
  cursor: pointer;
  transition: background 0.2s;
}

.toggle button.active {
  background: #111827;
  color: #fff;
}

.form-group {
  margin-bottom: 1.25rem;
}

.form-group label {
  display: block;
  font-size: 0.9rem;
  margin-bottom: 0.4rem;
  font-weight: 500;
}

.form-group input {
  width: 95%;
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 0.5rem;
  outline: none;
  transition: border 0.2s;
}

.form-group input:focus {
  border-color: #e5cd56;
  box-shadow: 0 0 0 2px rgba(229, 205, 86, 0.25);
}

.error {
  display: block;
  margin-top: 0.25rem;
  font-size: 0.8rem;
  color: #dc2626;
}

.btn-primary {
  width: 95%;
  padding: 0.9rem;
  font-weight: 600;
  border: none;
  border-radius: 0.5rem;
  background: #111827;
  color: #fff;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-primary:hover {
  background: #e5cd56;
  color: #111827;
}
</style>
