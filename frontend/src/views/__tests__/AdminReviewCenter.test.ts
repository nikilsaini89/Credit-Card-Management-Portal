import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createStore } from 'vuex'
import AdminReviewCenter from '../AdminReviewCenter.vue'

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
        fetchApplications: vi.fn(),
        updateApplicationStatus: vi.fn()
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

describe('AdminReviewCenter Component', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  // Test 1: Check if the page title is displayed
  it('shows the main title', () => {
    const wrapper = mount(AdminReviewCenter, {
      global: {
        plugins: [mockStore]
      }
    })
    expect(wrapper.text()).toContain('Admin Review Center')
  })

  // Test 2: Check if subtitle is shown
  it('shows the subtitle text', () => {
    const wrapper = mount(AdminReviewCenter, {
      global: {
        plugins: [mockStore]
      }
    })
    expect(wrapper.text()).toContain('Review and approve applications')
  })

  // Test 3: Check if empty state is shown when no applications
  it('shows empty state when no applications', () => {
    const wrapper = mount(AdminReviewCenter, {
      global: {
        plugins: [mockStore]
      }
    })
    expect(wrapper.text()).toContain('No applications found')
  })

  // Test 4: Check if component loads without errors
  it('loads without crashing', () => {
    const wrapper = mount(AdminReviewCenter, {
      global: {
        plugins: [mockStore]
      }
    })
    expect(wrapper.exists()).toBe(true)
  })

  // Test 5: Check if section title is displayed
  it('shows card applications section title', () => {
    const wrapper = mount(AdminReviewCenter, {
      global: {
        plugins: [mockStore]
      }
    })
    expect(wrapper.text()).toContain('Card Applications')
  })
}) 