import axios from 'axios';
import type { CreditCard } from '../model/credit-card';

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';


const api = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' }
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token && config.headers) {
    (config.headers as any).Authorization = `Bearer ${token}`;
  }
  return config;
});

export const getCards = async (): Promise<CreditCard[]> => {
  try {
    const res = await api.get<CreditCard[]>('/cards');
    return Array.isArray(res.data) ? res.data : [];
  } catch (err) {
    console.error('card-service.getCards failed', err);
    return [];
  }
};

export const getCardById = async (
  cardId: string | number
): Promise<{ card: CreditCard; transactions: any[] } | null> => {
  try {
    const res = await api.get(`/cards/${cardId}`);
    if (res.data && res.data.card) {
      return {
        card: res.data.card,
        transactions: res.data.transactions || []
      };
    }
    return null;
  } catch (err) {
    console.error('card-service.getCardById failed', err);
    return null;
  }
};

export const getCardPan = async (cardId: string | number): Promise<string | null> => {
  try {
    const res = await api.get(`/cards/${cardId}/pan`);
    if (!res || res.status !== 200) return null;
    if (typeof res.data === 'string') return res.data;
    if (res.data && (res.data.cardNumber || res.data.pan)) return res.data.cardNumber || res.data.pan;
    return null;
  } catch (err) {
    console.error('card-service.getCardPan failed', err);
    return null;
  }
};

export const updateCardStatus = async (
  cardId: any,
  newStatus: string
): Promise<CreditCard | null> => {
  try {
    const res = await api.put<CreditCard>(`/cards/${cardId}/status`, { cardStatus: newStatus });
    return res.data ?? null;
  } catch (err) {
    console.error('card-service.updateCardStatus failed', err);
    return null;
  }
};

export const updateCardLimit = async (cardId: string | number, newLimit: number): Promise<any> => {
  try {
    const res = await api.put(`/cards/${cardId}/limit`, { newCreditLimit: newLimit });
    return res.data;
  } catch (err) {
    console.error('card-service.updateCardLimit failed', err);
    throw err;
  }
};
