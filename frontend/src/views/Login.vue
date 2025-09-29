<template>
  <div class="min-h-screen flex flex-col justify-center px-3 sm:px-5 md:px-7 lg:px-8 py-5 bg-gradient-to-br from-yellow-100 via-yellow-200 to-yellow-300">
    <div class="text-center mb-5 sm:mb-7">
      <h1 class="text-lg sm:text-xl md:text-2xl font-bold text-gray-900 mb-1.5">CreditCard Portal</h1>
      <p class="text-xs sm:text-sm text-gray-600">
        {{ !isRegister ? "Manage your credit cards with ease" : "Create your account and get started" }}
      </p>
    </div>
 
    <div class="flex justify-center">
      <div class="bg-white p-3 sm:p-4 md:p-5 w-full max-w-[18rem] sm:max-w-xs md:max-w-sm rounded-lg shadow-md border border-gray-200">
        <div class="flex mb-4 rounded-md overflow-hidden border border-gray-200 shadow-sm">
          <button @click="isRegister = false"
                  :class="{'bg-gray-900 text-white': !isRegister, 'bg-white text-gray-700': isRegister}"
                  class="flex-1 py-1 sm:py-1.5 text-xs font-medium transition-all duration-300 ease-in-out hover:bg-gray-800 hover:text-white focus:outline-none focus:ring-2 focus:ring-yellow-300">
            Login
          </button>
          <button @click="isRegister = true"
                  :class="{'bg-gray-900 text-white': isRegister, 'bg-white text-gray-700': !isRegister}"
                  class="flex-1 py-1 sm:py-1.5 text-xs font-medium transition-all duration-300 ease-in-out hover:bg-gray-800 hover:text-white focus:outline-none focus:ring-2 focus:ring-yellow-300">
            Register
          </button>
        </div>
 
        <form v-if="!isRegister" @submit.prevent="handleLogin" class="space-y-2 sm:space-y-3">
          <div>
            <label class="block text-xs font-medium mb-0.5">Email</label>
            <input v-model="loginForm.email" type="email" placeholder="Enter your email"
                   class="w-full text-xs p-1.5 sm:p-2 border border-gray-300 rounded-md focus:border-yellow-400 focus:ring-2 focus:ring-yellow-200 outline-none transition-all" />
            <span v-if="errors.email" class="text-xs text-red-600 mt-0.5 block">{{ errors.email }}</span>
          </div>
 
          <div>
            <label class="block text-xs font-medium mb-0.5">Password</label>
            <input v-model="loginForm.password" type="password" placeholder="Enter your password"
                   class="w-full text-xs p-1.5 sm:p-2 border border-gray-300 rounded-md focus:border-yellow-400 focus:ring-2 focus:ring-yellow-200 outline-none transition-all" />
            <span v-if="errors.password" class="text-xs text-red-600 mt-0.5 block">{{ errors.password }}</span>
          </div>
 
          <button type="submit"
                  class="w-full text-xs py-1.5 sm:py-2 font-semibold rounded-md bg-gray-900 text-white hover:bg-yellow-400 hover:text-gray-900 transition-all focus:outline-none focus:ring-2 focus:ring-yellow-300">
            Sign In
          </button>
        </form>
 
        <form v-else @submit.prevent="handleRegister" class="space-y-2 sm:space-y-3">
          <div>
            <label class="block text-xs font-medium mb-0.5">Name</label>
            <input v-model="registerForm.name" type="text" placeholder="Your full name"
                   class="w-full text-xs p-1.5 sm:p-2 border border-gray-300 rounded-md focus:border-yellow-400 focus:ring-2 focus:ring-yellow-200 outline-none transition-all" />
            <span v-if="errors.name" class="text-xs text-red-600 mt-0.5 block">{{ errors.name }}</span>
          </div>
 
          <div>
            <label class="block text-xs font-medium mb-0.5">Email</label>
            <input v-model="registerForm.email" type="email" placeholder="Enter your email"
                   class="w-full text-xs p-1.5 sm:p-2 border border-gray-300 rounded-md focus:border-yellow-400 focus:ring-2 focus:ring-yellow-200 outline-none transition-all" />
            <span v-if="errors.email" class="text-xs text-red-600 mt-0.5 block">{{ errors.email }}</span>
          </div>
 
          <div>
            <label class="block text-xs font-medium mb-0.5">Password</label>
            <input v-model="registerForm.password" type="password" placeholder="Create a password"
                   class="w-full text-xs p-1.5 sm:p-2 border border-gray-300 rounded-md focus:border-yellow-400 focus:ring-2 focus:ring-yellow-200 outline-none transition-all" />
            <span v-if="errors.password" class="text-xs text-red-600 mt-0.5 block">{{ errors.password }}</span>
          </div>
 
          <div>
            <label class="block text-xs font-medium mb-0.5">Phone Number</label>
            <input v-model="registerForm.phoneNumber" type="text" placeholder="10-digit phone number"
                   class="w-full text-xs p-1.5 sm:p-2 border border-gray-300 rounded-md focus:border-yellow-400 focus:ring-2 focus:ring-yellow-200 outline-none transition-all" />
            <span v-if="errors.phoneNumber" class="text-xs text-red-600 mt-0.5 block">{{ errors.phoneNumber }}</span>
          </div>
 
          <div>
            <label class="block text-xs font-medium mb-0.5">Address</label>
            <input v-model="registerForm.address" type="text" placeholder="Your address"
                   class="w-full text-xs p-1.5 sm:p-2 border border-gray-300 rounded-md focus:border-yellow-400 focus:ring-2 focus:ring-yellow-200 outline-none transition-all" />
            <span v-if="errors.address" class="text-xs text-red-600 mt-0.5 block">{{ errors.address }}</span>
          </div>
 
          <div>
            <label class="block text-xs font-medium mb-0.5">Annual Income</label>
            <input v-model="registerForm.annualIncome" type="number" placeholder="Annual income"
                   class="w-full text-xs p-1.5 sm:p-2 border border-gray-300 rounded-md focus:border-yellow-400 focus:ring-2 focus:ring-yellow-200 outline-none transition-all" />
            <span v-if="errors.annualIncome" class="text-xs text-red-600 mt-0.5 block">{{ errors.annualIncome }}</span>
          </div>
 
          <button type="submit"
                  class="w-full text-xs py-1.5 sm:py-2 font-semibold rounded-md bg-gray-900 text-white hover:bg-yellow-400 hover:text-gray-900 transition-all focus:outline-none focus:ring-2 focus:ring-yellow-300">
            Register
          </button>
        </form>
      </div>
    </div>
  </div>
</template>
 
<script setup lang="ts">
import { reactive, ref } from "vue";
import { useStore } from "vuex";
import { useRouter } from "vue-router";
import { toast } from "vue3-toastify";
 
const store = useStore();
const router = useRouter();
 
const isRegister = ref(false);
 
const loginForm = reactive({ email: "", password: "" });
const registerForm = reactive({
  name: "",
  email: "",
  password: "",
  phoneNumber: "",
  address: "",
  annualIncome: null as number | null,
});
 
const errors = reactive<Record<string, string>>({});
 
// Login Validation
const validateLogin = () => {
  errors.email = !loginForm.email ? "Email is required" : "";
  errors.password = !loginForm.password ? "Password is required" : "";
  return !errors.email && !errors.password;
};
 
// Register Validation
const validateRegister = () => {
   errors.name = !registerForm.name ? "Name is required" : "";
  errors.email = !registerForm.email
    ? "Email is required"
    : !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(registerForm.email)
    ? "Invalid email"
    : "";
  const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{6,}$/;
  errors.password = !passwordRegex.test(registerForm.password)
    ? "Password must be min 6 chars, include uppercase, lowercase, number & special char"
    : "";
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
 
// Login Handler
const handleLogin = async () => {
  if (validateLogin()) {
    try {
      await store.dispatch("auth/login", loginForm);
      router.push("/dashboard");
      toast.success("Login successful! 🎉");
    } catch (err) {
      toast.error("Login failed. Please try again.");
      console.error("Login failed:", err);
    }
  } else {
    toast.warn("Please fix the highlighted errors.");
  }
};
 
// Register Handler
const handleRegister = async () => {
  if (validateRegister()) {
    try {
      await store.dispatch("auth/register", registerForm);
       Object.assign(registerForm, {
        name: "",
        email: "",
        password: "",
        phoneNumber: "",
        address: "",
        annualIncome: null
      });
      router.push("/login");
      toast.success("Registration successful! 🎉");
      toast.success("Login with your credentials now!");
    } catch (err) {
      toast.error("Registration failed. Please try again." + err);
      console.error("Registration failed:", err);
    }
  } else {
    toast.warn("Please fix the highlighted errors.");
  }
};
</script>
 