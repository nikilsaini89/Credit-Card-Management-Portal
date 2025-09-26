<template>
  <div class="profile-page">
    <div class="container">
      <h1>My Profile</h1>
      <p class="subtitle">Manage your personal information and preferences.</p>

      <div class="card">
        <h2>Personal Information</h2>
        <p class="section-subtitle">Your basic personal details and contact information.</p>
        <form @submit.prevent="handleUpdate" class="form">
          <div class="form-group">
            <label>Full Name</label>
            <input v-model="form.name" required />
          </div>
          <div class="form-group">
            <label>Phone Number</label>
            <input v-model="form.phoneNumber" required />
          </div>
          <div class="form-group">
            <label>Address</label>
            <input v-model="form.address" required />
          </div>
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
          <label>Email</label>
          <span>{{ user.email }}</span>
        </div>
        <div class="info-row">
          <label>User ID</label>
          <span>#{{ user.id }}</span>
        </div>
        <div class="info-row">
          <label>Account Type</label>
          <span>{{ user.role }}</span>
        </div>
        <div class="info-row">
          <label>Account Created</label>
          <span>{{ accountCreated }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, onMounted, computed } from 'vue';
import { useStore } from 'vuex';

interface User {
  id: string;
  email: string;
  role: string;
  name: string;
  phoneNumber: string;
  address: string;
  annualIncome: number;
}

export default defineComponent({
  setup() {
    const store = useStore();
    // Initialize user with data from decoded JWT payload
    const user: User = store.getters['auth/user'] || {
      id: '2',
      email: 'anjali.sharma@example.com',
      role: 'USER',
      name: '',
      phoneNumber: '',
      address: '',
      annualIncome: 0
    };

    const form = reactive<Omit<User, 'id' | 'email' | 'role'>>({
      name: '',
      phoneNumber: '',
      address: '',
      annualIncome: 0
    });

    onMounted(async () => {
      try {
        if (user.id) {
          const profileData: User = await store.dispatch('user/fetchProfile', user.id);
          Object.assign(form, {
            name: profileData.name,
            phoneNumber: profileData.phoneNumber,
            address: profileData.address,
            annualIncome: profileData.annualIncome
          });
        }
      } catch (error) {
        console.error('Failed to load profile:', error);
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
  background-color: #f5f5f5;
  min-height: 100vh;
  padding: 2rem 1rem;
  font-family: 'Segoe UI', Arial, sans-serif;
}

.container {
  max-width: 900px;
  margin: 0 auto;
}

h1 {
  font-size: 2.25rem;
  font-weight: 700;
  color: #1a202c;
  margin-bottom: 0.5rem;
  text-align: center;
}

.subtitle {
  font-size: 1rem;
  color: #4a5568;
  margin-bottom: 2rem;
  text-align: center;
}

.card {
  background: #ffffff;
  border-radius: 0.5rem;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

h2 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 0.5rem;
}

.section-subtitle {
  font-size: 0.9rem;
  color: #718096;
  margin-bottom: 1.5rem;
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
  font-size: 0.9rem;
  font-weight: 500;
  color: #2d3748;
  margin-bottom: 0.3rem;
}

input {
  padding: 0.75rem;
  border: 1px solid #e2e8f0;
  border-radius: 0.375rem;
  font-size: 1rem;
  color: #1a202c;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

input:focus {
  outline: none;
  border-color: #3182ce;
  box-shadow: 0 0 0 3px rgba(49, 130, 206, 0.2);
}

button {
  padding: 0.75rem 1.5rem;
  background-color: #3182ce;
  color: #ffffff;
  border: none;
  border-radius: 0.375rem;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

button:hover {
  background-color: #2b6cb0;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid #edf2f7;
}

.info-row:last-child {
  border-bottom: none;
}

.eligibility {
  padding: 0.4rem 1rem;
  border-radius: 1rem;
  font-size: 0.85rem;
  font-weight: 600;
  background-color: #e2e8f0;
  color: #4a5568;
}

.eligible {
  background-color: #38a169;
  color: #ffffff;
}

.caption {
  font-size: 0.85rem;
  color: #718096;
  margin-top: 0.5rem;
}

/* Responsive Design */
@media (max-width: 768px) {
  .container {
    padding: 0 1rem;
  }

  h1 {
    font-size: 1.75rem;
  }

  h2 {
    font-size: 1.25rem;
  }

  .card {
    padding: 1rem;
  }

  .form {
    gap: 0.75rem;
  }

  input, button {
    font-size: 0.9rem;
  }

  .info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }

  .info-row label {
    margin-bottom: 0.25rem;
  }

  .info-row input {
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
    font-size: 0.9rem;
  }

  .card {
    margin-bottom: 1rem;
  }

  button {
    width: 100%;
  }
}
</style>