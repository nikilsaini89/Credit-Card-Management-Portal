import { refreshToken } from "../services/authService";

export const initializeAuth = async () => {
  const token = localStorage.getItem("token");

  if (!token) {
    try {
      // ask backend to use refresh cookie
      const newToken = await refreshToken();
      return {
        accessToken: newToken,
        user: JSON.parse(localStorage.getItem("user") || "{}"),
      };
    } catch (err) {
      console.error("Session restore failed:", err);
      return null;
    }
  }

  return {
    accessToken: token,
    user: JSON.parse(localStorage.getItem("user") || "{}"),
  };
};
