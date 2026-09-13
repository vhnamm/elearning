import http, { setAccessToken, API_ORIGIN } from "./http";

export const googleLoginUrl = `${API_ORIGIN}/oauth2/authorization/google`;

export const login = async ({ email, password }) => {
  const { data } = await http.post("/auth/login", { email, password });
  setAccessToken(data.data.accessToken);
  return data.data;
};

export const register = async ({ email, password, fullName }) => {
  const { data } = await http.post("/auth/register", { email, password, fullName });
  return data;
};

export const confirmOtp = async ({ email, otp }) => {
  const { data } = await http.post("/auth/confirm-otp", { email, otp });
  return data.data;
};

export const logout = async () => {
  await http.post("/auth/logout");
  setAccessToken(null);
};

export const refresh = async () => {
  const { data } = await http.post("/auth/refresh-token");
  setAccessToken(data.data.accessToken);
  return data.data;
};

export const getMe = async () => {
  const { data } = await http.get("/auth/me");
  return data.data;
};

export const resendOtp = async ({email, password, fullName}) => {
    const {data} = await http.post("/auth/register", {email, password, fullName})
    return data
}
