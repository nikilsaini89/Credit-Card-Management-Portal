import axios from 'axios';
import type { CreditCard } from '../model/credit-card';

const BASE_URL = 'http://localhost:8080';

const token = localStorage.getItem("token");

export const getCards = async (): Promise<CreditCard[]> => {
  try {
    const res = await axios.get<CreditCard[]>(`${BASE_URL}/cards/1`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return res.data;
  } catch (err) {
    return [];
  }
};


export const updateCardStatus = async (
  cardId: any,
  newStatus: string
): Promise<CreditCard | null> => {
  try {
    const res = await axios.put<CreditCard>(
      `${BASE_URL}/cards/${cardId}/status`,
      {
        cardStatus: newStatus,
      },
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    );
    return res.data;
  } catch (err) {
    return null;
  }
};
