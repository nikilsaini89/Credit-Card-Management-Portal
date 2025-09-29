import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import LandingPage from '../LandingPage.vue'
import { RouterLinkStub } from '@vue/test-utils'

describe('LandingPage.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders main header', () => {
    const wrapper = mount(LandingPage, {
      global: {
        stubs: { RouterLink: RouterLinkStub }
      }
    })
    expect(wrapper.text()).toContain('Take Control of Your Finances')
    expect(wrapper.text()).toContain('Swipe Smart')
  })

  it('has "Get Started Today" button linking to /login', () => {
    const wrapper = mount(LandingPage, {
      global: {
        stubs: { RouterLink: RouterLinkStub }
      }
    })
    const button = wrapper.findComponent(RouterLinkStub)
    expect(button.exists()).toBe(true)
    expect(button.props().to).toBe('/login')
    expect(button.text()).toBe('Get Started Today')
  })

  it('renders all features', () => {
    const wrapper = mount(LandingPage)
    const features = wrapper.findAll('[v-for="feature in features"]')
    expect(features.length).toBe(3)
    expect(wrapper.text()).toContain('Instant Alerts')
    expect(wrapper.text()).toContain('Spending Insights')
    expect(wrapper.text()).toContain('BNPL')
  })

  it('renders all advantages', () => {
    const wrapper = mount(LandingPage)
    expect(wrapper.text()).toContain('Cashback & Rewards')
    expect(wrapper.text()).toContain('Secure Payments')
    expect(wrapper.text()).toContain('Budget Management')
  })

  it('changes tagline over time', async () => {
    vi.useFakeTimers()
    const wrapper = mount(LandingPage)
    const firstTagline = wrapper.text().includes('your spending')
    expect(firstTagline).toBe(true)

    vi.advanceTimersByTime(3000)
    await wrapper.vm.$nextTick()
    expect(wrapper.text()).toContain('your budget')

    vi.advanceTimersByTime(3000)
    await wrapper.vm.$nextTick()
    expect(wrapper.text()).toContain('your rewards')

    vi.useRealTimers()
  })
})
