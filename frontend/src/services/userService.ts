import { BASE_URL } from '../constants/constants';
import axios from 'axios';
import type { User } from '../types/User';

export const getUserProfile = async (userId: number) => {
  return axios.get(`${BASE_URL}/profile/${userId}`);
};

export const updateUserProfile = async (
  userId: number,
  payload: Omit<User, 'id' | 'email' | 'password'>
) => {
  return axios.put(`${BASE_URL}/profile/${userId}`, payload);
};
