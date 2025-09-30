import axios from 'axios';
import type { CreditCard, CardType } from '../model/credit-card';
import type { CardApplication, UpdateApplicationStatusRequest } from '../types/cardApplication';
import { getUserIdFromToken } from '../utils/getTokenData';

const BASE_URL = 'http://localhost:8080';


export const getCards = async (): Promise<CreditCard[]> => {
  const token = localStorage.getItem("token");
  const userId = getUserIdFromToken();
  try {
    const res = await axios.get<CreditCard[]>(`${BASE_URL}/cards`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return res.data;
  } catch (err) {
    return [];
  }
};

export const getCardTypes = async (): Promise<CardType[]> => {
  const token = localStorage.getItem("token");
  const userId = getUserIdFromToken();
  try {
    const res = await axios.get<CardType[]>(`${BASE_URL}/cards/type`, {
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
    const token = localStorage.getItem("token");
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

export const getCardApplications = async (): Promise<CardApplication[]> => {
  try {
    const token = localStorage.getItem("token");
    
    const res = await axios.get<CardApplication[]>(`${BASE_URL}/card/application`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return res.data;
  } catch (err) {
    console.error('Error fetching card applications:', err);
    return [];
  }
};

export const updateApplicationStatus = async (
  applicationId: number,
  status: 'ACCEPTED' | 'REJECTED'
): Promise<CardApplication | null> => {
  try { 
    const token = localStorage.getItem("token");
    const res = await axios.patch<CardApplication>(
      `${BASE_URL}/card/application/${applicationId}`,
      { status },
      {
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
      }
    );
    return res.data;
  } catch (err) {
    console.error('Error updating application status:', err);
    return null;
  }
};
