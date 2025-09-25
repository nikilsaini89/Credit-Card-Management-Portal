import axios from 'axios';
import type { CreditCard } from '../model/credit-card';

const BASE_URL = 'http://localhost:8080';

const token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhYmhheS52ZXJtYUBleGFtcGxlLmNvbSIsInJvbGUiOiJVU0VSIiwidXNlcklkIjoyLCJpYXQiOjE3NTg4MjIzMzgsImV4cCI6MTc1ODgyNTkzOH0.uyPnHVpjedx26mT_y4F2JUowXo9En8BH2L44b7J3O60"

export const getCards = async (): Promise<CreditCard[]> => {
  try {
    const res = await axios.get<CreditCard[]>(`${BASE_URL}/cards/1`, {
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
