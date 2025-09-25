<template>
  <div class="profile-page">
    <h1>My Profile</h1>
    <p class="subtitle">Manage your personal information and preferences.</p>

    <div class="card">
      <h2>Personal Information</h2>
      <p class="section-subtitle">Your basic personal details and contact information.</p>
      <form @submit.prevent="handleUpdate">
        <label>Full Name</label>
        <input v-model="form.name" required />

        <label>Phone Number</label>
        <input v-model="form.phoneNumber" required />

        <label>Address</label>
        <input v-model="form.address" required />

   

        <button type="submit">Update Profile</button>
      </form>
    </div>

    <div class="card">
      <h2>Financial Information</h2>
      <p class="section-subtitle">Your income details for credit assessment.</p>
      <div class="info-row">
        <label>Annual Income</label>
        <input v-model.number="form.annualIncome" required type="number" />
      </div>
      <div class="info-row">
        <label>BNPL Eligibility</label>
        <span class="eligibility" :class="{ eligible: isEligible }">
          {{ isEligible ? 'Eligible' : 'Not Eligible' }}
        </span>
        <p class="caption">You can use Buy Now, Pay Later services.</p>
      </div>
    </div>

    <div class="card">
      <h2>Account Information</h2>
      <p class="section-subtitle">Your account details and membership information.</p>
      <div class="info-row">
        <label>Account Created</label>
        <span>{{ accountCreated }}</span>
      </div>
      <!-- <div class="info-row">
        <label>User ID</label>
        <span>#{{ user.id }}</span>
      </div> -->
      <!-- <div class="info-row">
        <label>Account Type</label>
        <span>{{ user.role }}</span>
      </div> -->
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, onMounted, computed } from 'vue';
import { useStore } from 'vuex';
import { User } from '../types/User';

export default defineComponent({
  setup() {
    const store = useStore();
    const user: User = store.getters['auth/user'];

    const form = reactive<Omit<User, 'id' | 'email'>>({
      name: '',
      phoneNumber: '',
      address: '',
      annualIncome: 0
    });

    onMounted(async () => {
      if (user?.id) {
        const profileData: User = await store.dispatch('user/fetchProfile', user.id);
        form.name = profileData.name;
        form.phoneNumber = profileData.phoneNumber;
        form.address = profileData.address;
        form.annualIncome = profileData.annualIncome;
      }
    });

    const handleUpdate = async () => {
      try {
        await store.dispatch('user/updateProfile', {
          userId: user.id,
          payload: form
        });
        alert('Profile updated successfully!');
      } catch (error) {
        console.error('Update failed:', error);
        alert('Something went wrong while updating your profile.');
      }
    };

    const isEligible = computed(() => form.annualIncome > 100000);
    const accountCreated = computed(() => {
      const date = new Date();
      return date.toLocaleDateString('en-IN', {
        day: 'numeric',
        month: 'long',
        year: 'numeric'
      });
    });

    return { form, handleUpdate, user, isEligible, accountCreated };
  }
});
</script>

<style scoped>
.profile-page {
  max-width: 800px;
  margin: 2rem auto;
  padding: 1rem;
  font-family: 'Segoe UI', sans-serif;
}
h1 {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}
.subtitle {
  color: #666;
  margin-bottom: 2rem;
}
.card {
  background: #fff;
  border-radius: 8px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  box-shadow: 0 0 8px rgba(0, 0, 0, 0.05);
}
h2 {
  font-size: 1.3rem;
  margin-bottom: 0.3rem;
}
.section-subtitle {
  color: #888;
  margin-bottom: 1rem;
}
label {
  display: block;
  margin-top: 1rem;
  font-weight: 600;
}
input {
  width: 100%;
  padding: 0.5rem;
  margin-top: 0.3rem;
  border: 1px solid #ccc;
  border-radius: 4px;
}
button {
  margin-top: 1.5rem;
  padding: 0.7rem 1.2rem;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
}
.info-row {
  margin-top: 1rem;
}
.eligibility {
  display: inline-block;
  padding: 0.3rem 0.8rem;
  border-radius: 12px;
  font-weight: bold;
  background-color: #ddd;
}
.eligible {
  background-color: #28a745;
  color: white;
}
.caption {
  font-size: 0.85rem;
  color: #666;
  margin-top: 0.3rem;
}
</style>
