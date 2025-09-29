import axios from "axios";

export const api = axios.create({
  baseURL: import.meta.env.VITE_BASE_URL || "http://localhost:8080/api",
  withCredentials: true, // needed for cookies
});

// Request interceptor: attach access token
api.interceptors.request.use(config => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers["Authorization"] = `Bearer ${token}`;
  }
  return config;
});
