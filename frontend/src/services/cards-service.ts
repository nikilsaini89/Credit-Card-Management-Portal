import axios from 'axios';
import type { CreditCard,CardType } from '../model/credit-card';
import { getUserIdFromToken } from '../utils/getTokenData';

const BASE_URL = 'http://localhost:8080';

const token = localStorage.getItem("token");

export const getCards = async (): Promise<CreditCard[]> => {
  const userId = getUserIdFromToken();
  try {
    const res = await axios.get<CreditCard[]>(`${BASE_URL}/cards/${userId}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return res.data;
  } catch (err) {
    console.error('Error fetching cards:', err);
    return [];
  }
};

export const getCardTypes = async (): Promise<CardType[]> => {
  const userId = getUserIdFromToken();
  try {
    const res = await axios.get<CardType[]>(`${BASE_URL}/cards/type`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return res.data;
  } catch (err) {
    console.error('Error fetching cards:', err);
    return [];
  }
};