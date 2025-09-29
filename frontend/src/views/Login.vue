<template>
  <div class="min-h-screen flex flex-col justify-center px-100 py-12 bg-gradient-to-br from-white to-yellow-300">
  
    <div class="text-center mb-8">
      <h1 class="text-3xl font-bold text-gray-900">CreditCard Portal</h1>
      <p v-if="!isRegister" class="text-sm text-gray-600">Manage your credit cards with ease</p>
      <p v-else class="text-sm text-gray-600">Create your account and get started</p>
    </div>

    <div class="flex justify-center">
      <div class="bg-white p-8 w-full max-w-md rounded-xl shadow-lg">
   
        <div class="flex justify-center mb-6">
          <button :class="{ 'bg-gray-900 text-white': !isRegister, 'bg-gray-100': isRegister }" 
                  @click="isRegister = false"
                  class="flex-1 py-3 font-semibold transition-colors duration-200">
            Login
          </button>
          <button :class="{ 'bg-gray-900 text-white': isRegister, 'bg-gray-100': !isRegister }" 
                  @click="isRegister = true"
                  class="flex-1 py-3 font-semibold transition-colors duration-200">
            Register
          </button>
        </div>

       
        <form v-if="!isRegister" @submit.prevent="handleLogin" class="space-y-5">
          <div>
            <label class="block text-sm font-medium mb-1">Email</label>
            <input v-model="loginForm.email" 
                   type="email" 
                   placeholder="Enter your email" 
                   class="w-full p-3 border border-gray-300 rounded-lg focus:border-yellow-300 focus:ring-2 focus:ring-yellow-200 outline-none transition" />
            <span class="block mt-1 text-sm text-red-600" v-if="errors.email">{{ errors.email }}</span>
          </div>

          <div>
            <label class="block text-sm font-medium mb-1">Password</label>
            <input v-model="loginForm.password" 
                   type="password" 
                   placeholder="Enter your password" 
                   class="w-full p-3 border border-gray-300 rounded-lg focus:border-yellow-300 focus:ring-2 focus:ring-yellow-200 outline-none transition" />
            <span class="block mt-1 text-sm text-red-600" v-if="errors.password">{{ errors.password }}</span>
          </div>

          <button class="w-full py-3 font-semibold rounded-lg bg-gray-900 text-white hover:bg-yellow-300 hover:text-gray-900 transition-colors" 
                  type="submit">
            Sign In
          </button>
        </form>

       
        <form v-else @submit.prevent="handleRegister" class="space-y-5">
          <div>
            <label class="block text-sm font-medium mb-1">Name</label>
            <input v-model="registerForm.name" 
                   type="text" 
                   placeholder="Your full name" 
                   class="w-full p-3 border border-gray-300 rounded-lg focus:border-yellow-300 focus:ring-2 focus:ring-yellow-200 outline-none transition" />
            <span class="block mt-1 text-sm text-red-600" v-if="errors.name">{{ errors.name }}</span>
          </div>

          <div>
            <label class="block text-sm font-medium mb-1">Email</label>
            <input v-model="registerForm.email" 
                   type="email" 
                   placeholder="Enter your email" 
                   class="w-full p-3 border border-gray-300 rounded-lg focus:border-yellow-300 focus:ring-2 focus:ring-yellow-200 outline-none transition" />
            <span class="block mt-1 text-sm text-red-600" v-if="errors.email">{{ errors.email }}</span>
          </div>

          <div>
            <label class="block text-sm font-medium mb-1">Password</label>
            <input v-model="registerForm.password" 
                   type="password" 
                   placeholder="Create a password" 
                   class="w-full p-3 border border-gray-300 rounded-lg focus:border-yellow-300 focus:ring-2 focus:ring-yellow-200 outline-none transition" />
            <span class="block mt-1 text-sm text-red-600" v-if="errors.password">{{ errors.password }}</span>
          </div>

          <div>
            <label class="block text-sm font-medium mb-1">Phone Number</label>
            <input v-model="registerForm.phoneNumber" 
                   type="text" 
                   placeholder="10-digit phone number" 
                   class="w-full p-3 border border-gray-300 rounded-lg focus:border-yellow-300 focus:ring-2 focus:ring-yellow-200 outline-none transition" />
            <span class="block mt-1 text-sm text-red-600" v-if="errors.phoneNumber">{{ errors.phoneNumber }}</span>
          </div>

          <div>
            <label class="block text-sm font-medium mb-1">Address</label>
            <input v-model="registerForm.address" 
                   type="text" 
                   placeholder="Your address" 
                   class="w-full p-3 border border-gray-300 rounded-lg focus:border-yellow-300 focus:ring-2 focus:ring-yellow-200 outline-none transition" />
            <span class="block mt-1 text-sm text-red-600" v-if="errors.address">{{ errors.address }}</span>
          </div>

          <div>
            <label class="block text-sm font-medium mb-1">Annual Income</label>
            <input v-model="registerForm.annualIncome" 
                   type="number" 
                   placeholder="Annual income" 
                   class="w-full p-3 border border-gray-300 rounded-lg focus:border-yellow-300 focus:ring-2 focus:ring-yellow-200 outline-none transition" />
            <span class="block mt-1 text-sm text-red-600" v-if="errors.annualIncome">{{ errors.annualIncome }}</span>
          </div>

          <button class="w-full py-3 font-semibold rounded-lg bg-gray-900 text-white hover:bg-yellow-300 hover:text-gray-900 transition-colors" 
                  type="submit">
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


const validateLogin = () => {
  errors.email = !loginForm.email ? "Email is required" : "";
  errors.password = !loginForm.password ? "Password is required" : "";
  return !errors.email && !errors.password;
};


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
  }
  else {
    toast.warn("Please fix the highlighted errors.");
  }
};

const handleRegister = async () => {
  if (validateRegister()) {
    try {
      await store.dispatch("auth/register", registerForm);
      router.push("/");
       toast.success("Registeration successful! 🎉");
       toast.success("Login with your credentials now!");


    } catch (err) {
      toast.error("Login failed. Please try again."+err);

      console.error("Registration failed:", err);
    }
  }
  else {
    toast.warn("Please fix the highlighted errors.");
  }
};
</script>
