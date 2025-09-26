<template>
  <div class="profile-page">
    <div class="container">
      <h1>My Profile</h1>
      <p class="subtitle">Manage your personal information and preferences.</p>

      <!-- Personal Information -->
      <div class="card">
        <h2>Personal Information</h2>
        <p class="section-subtitle">Your basic personal details and contact information.</p>
        <form @submit.prevent="handleUpdate" class="form">
          <div class="form-group">
            <label for="name">Full Name</label>
            <input id="name" v-model="form.name" placeholder="Enter full name" required />
          </div>
          <div class="form-group">
            <label for="phone">Phone Number</label>
            <input id="phone" v-model="form.phoneNumber" placeholder="Enter phone number" required />
          </div>
          <div class="form-group">
            <label for="address">Address</label>
            <input id="address" v-model="form.address" placeholder="Enter address" required />
          </div>
          <button type="submit">Update Profile</button>
        </form>
      </div>

      <!-- Financial Information -->
      <div class="card">
        <h2>Financial Information</h2>
        <p class="section-subtitle">Your income details for credit assessment.</p>
        <div class="info-row">
          <label for="income">Annual Income</label>
          <input id="income" type="number" v-model.number="form.annualIncome" placeholder="Enter annual income" required />
        </div>
        <div class="info-row">
          <label>BNPL Eligibility</label>
          <span class="eligibility" :class="{ eligible: isEligible }">
            {{ isEligible ? 'Eligible' : 'Not Eligible' }}
          </span>
          <p class="caption">You can use Buy Now, Pay Later services.</p>
        </div>
      </div>

      <!-- Account Information -->
      <div class="card">
        <h2>Account Information</h2>
        <p class="section-subtitle">Your account details and membership information.</p>
        <div class="info-row" v-for="(value, label) in accountInfo" :key="label">
          <label>{{ label }}</label>
          <span>{{ value }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, onMounted, computed } from 'vue';
import { useStore } from 'vuex';
import type { User } from '../types/User';

function decodeToken(token: string): { id: number; email: string; role: string } | null {
  try {
    const jwt = token.replace(/^Bearer\s+/i, '');
    const payload = JSON.parse(atob(jwt.split('.')[1]));
    return {
      id: Number(payload.userId),
      email: payload.sub,
      role: payload.role
    };
  } catch {
    return null;
  }
}

interface ProfileForm {
  name: string;
  phoneNumber: string;
  address: string;
  annualIncome: number;
}

export default defineComponent({
  setup() {
    const store = useStore();

    const form = reactive<ProfileForm>({
      name: '',
      phoneNumber: '',
      address: '',
      annualIncome: 0
    });

    const token = localStorage.getItem('token');
    const decoded = token ? decodeToken(token) : null;

    onMounted(async () => {
      if (!decoded?.id) {
        console.warn('Invalid or missing token');
        return;
      }

      try {
        const profileData: User = await store.dispatch('user/fetchProfile', decoded.id);
        Object.assign(form, {
          name: profileData.name,
          phoneNumber: profileData.phoneNumber,
          address: profileData.address,
          annualIncome: profileData.annualIncome
        });
      } catch (error) {
        console.error('Failed to load profile:', error);
      }
    });

    const handleUpdate = async () => {
      if (!decoded?.id) {
        alert('User ID not found in token');
        return;
      }

      try {
        await store.dispatch('user/updateProfile', {
          userId: decoded.id,
          payload: form
        });
        alert('Profile updated successfully!');
      } catch (error) {
        console.error('Update failed:', error);
        alert('Something went wrong while updating your profile.');
      }
    };

    const isEligible = computed(() => form.annualIncome > 100000);

    const accountInfo = computed(() => {
      const user: User | null = store.getters['user/profile'];
      return {
        Email: user?.email || '-',
        'User ID': user?.id ? `#${user.id}` : '-',
       
      };
    });

    return { form, handleUpdate, isEligible, accountInfo };
  }
});
</script>




<style scoped>
.profile-page {
  background-color: #f9fafb;
  min-height: 100vh;
  padding: 2rem 1rem;
  font-family: 'Segoe UI', Arial, sans-serif;
}

.container {
  max-width: 900px;
  margin: 0 auto;
}

h1 {
  font-size: 2rem;
  font-weight: 700;
  text-align: center;
  color: #1f2937;
  margin-bottom: 0.5rem;
}

.subtitle {
  font-size: 1rem;
  text-align: center;
  color: #4b5563;
  margin-bottom: 2rem;
}

.card {
  background-color: #ffffff;
  border-radius: 0.5rem;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
}

h2 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #111827;
  margin-bottom: 0.5rem;
}

.section-subtitle {
  font-size: 0.875rem;
  color: #6b7280;
  margin-bottom: 1rem;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
}

label {
  font-weight: 500;
  margin-bottom: 0.25rem;
  color: #374151;
}

input {
  padding: 0.625rem;
  border: 1px solid #d1d5db;
  border-radius: 0.375rem;
  font-size: 1rem;
  transition: border-color 0.2s ease;
}

input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

button {
  padding: 0.75rem;
  background: #ffd60a;
  color: #0b2540;
  border: none;
  border-radius: 0.375rem;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.2s ease;
}

button:hover {
  background:  #0b2540;
  color: #ffd60a;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0;
  border-bottom: 1px solid #e5e7eb;
}

.info-row:last-child {
  border-bottom: none;
}

.eligibility {
  padding: 0.25rem 0.75rem;
  border-radius: 1rem;
  font-size: 0.875rem;
  font-weight: 600;
  background-color: #e5e7eb;
  color: #374151;
}

.eligible {
  background-color: #10b981;
  color: #ffffff;
}

.caption {
  font-size: 0.75rem;
  color: #6b7280;
  margin-top: 0.25rem;
}

/* Responsive */
@media (max-width: 768px) {
  .info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;
  }
  button {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .profile-page {
    padding: 1rem 0.5rem;
  }
  h1 {
    font-size: 1.5rem;
  }
  .subtitle {
    font-size: 0.875rem;
  }
}
</style>
