import { createContext, useEffect, useState } from "react";
import * as authService from "~services/auth.service";
import { setAccessToken } from "~services/http";

export const AuthContext = createContext(null);

function decodeUser(accessToken) {
  if (!accessToken) return null;
  try {
    const payload = JSON.parse(atob(accessToken.split(".")[1]));
    return { email: payload.sub, ...payload };
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [isInitializing, setIsInitializing] = useState(true);

  useEffect(() => {
    authService
      .refresh()
      .then(({ accessToken }) => setUser(decodeUser(accessToken)))
      .catch(() => setUser(null))
      .finally(() => setIsInitializing(false));
  }, []);

  const login = async (credentials) => {//nơi khác phải gọi qua AuthContext
    const { accessToken } = await authService.login(credentials);
    setUser(decodeUser(accessToken));
  };

  // Dùng cho callback OAuth2 (Google): BE đã redirect kèm accessToken, refreshToken đã nằm ở cookie httpOnly
  const loginWithAccessToken = (accessToken) => {
    setAccessToken(accessToken);
    setUser(decodeUser(accessToken));
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
