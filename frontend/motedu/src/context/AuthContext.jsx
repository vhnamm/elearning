import { createContext, useEffect, useState } from "react";
import * as authService from "~services/auth.service";
import { setAccessToken } from "~services/http";

export const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [isInitializing, setIsInitializing] = useState(true);

  useEffect(() => {
    authService
      .getMe()
      .then((profile) => setUser(profile))
      .catch(() => {
        setAccessToken(null);
        setUser(null);
      })
      .finally(() => setIsInitializing(false));
  }, []);

  const login = async (credentials) => {//nơi khác phải gọi qua AuthContext
    await authService.login(credentials);
    const profile = await authService.getMe();
    setUser(profile);
  };

  // Dùng cho callback OAuth2 (Google): BE đã redirect kèm accessToken, refreshToken đã nằm ở cookie httpOnly
  const loginWithAccessToken = async (accessToken) => {
    setAccessToken(accessToken);
    const profile = await authService.getMe();
    setUser(profile);
  };

  const logout = async () => { //api auth phải gọi qua context vì còn set trạng thái toàn cục
    await authService.logout();
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{ user, isAuthenticated: !!user, isInitializing, login, logout, loginWithAccessToken }}
    >
      {children}
    </AuthContext.Provider>
  );
}
