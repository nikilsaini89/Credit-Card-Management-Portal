import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createStore } from 'vuex'
import AdminDashboard from '../AdminDashboard.vue'

// Mock the store
const mockStore = createStore({
  modules: {
    applications: {
      namespaced: true,
      getters: {
        applications: () => [],
        loading: () => false
      },
      actions: {
        fetchApplications: vi.fn()
      }
    },
    auth: {
      namespaced: true,
      getters: {
        user: () => ({ name: 'Admin User', email: 'admin@test.com' })
      }
    }
  }
})

describe('AdminDashboard Component', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  // Test 1: Check if the main title is displayed
  it('shows the dashboard title', () => {
    const wrapper = mount(AdminDashboard, {
      global: {
        plugins: [mockStore]
      }
    })
    expect(wrapper.text()).toContain('Admin Dashboard')
  })

  // Test 2: Check if welcome message is shown
  it('shows welcome message with admin name', () => {
    const wrapper = mount(AdminDashboard, {
      global: {
        plugins: [mockStore]
      }
    })
   expect(wrapper.text()).toContain("Welcome back, Admin!")
  })

  // Test 3: Check if loading state is shown initially
  it('shows loading state initially', () => {
    const wrapper = mount(AdminDashboard, {
      global: {
        plugins: [mockStore]
      }
    })
    expect(wrapper.text()).toContain('Loading dashboard data')
  })

  // Test 4: Check if component loads without errors
  it('loads without crashing', () => {
    const wrapper = mount(AdminDashboard, {
      global: {
        plugins: [mockStore]
      }
    })
    expect(wrapper.exists()).toBe(true)
  })

  // Test 5: Check if admin name is displayed in welcome message
  it('displays admin name in welcome message', () => {
    const wrapper = mount(AdminDashboard, {
      global: {
        plugins: [mockStore]
      }
    })
    expect(wrapper.text()).toContain("Welcome back, Admin!")
  })
}) 