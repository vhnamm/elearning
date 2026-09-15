import axios from "axios";

export const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/v1";

// Root gốc của backend (bỏ /api/v1), dùng cho các URL nằm ngoài REST API như OAuth2 authorization endpoint
export const API_ORIGIN = BASE_URL.replace(/\/api\/v1\/?$/, "");

const ACCESS_TOKEN_KEY = "accessToken";

export const setAccessToken = (token) => {
  if (token) {
    localStorage.setItem(ACCESS_TOKEN_KEY, token);
  } else {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
  }
};

export const getAccessToken = () => localStorage.getItem(ACCESS_TOKEN_KEY);

const http = axios.create({
  baseURL: BASE_URL,
  withCredentials: true, // gửi kèm cookie refreshToken (httpOnly)
});

//interceptor cho mỗi rq dc gửi đi, tự gắn accessToken vào header(nếu có)
http.interceptors.request.use((config) => {
  const accessToken = getAccessToken();
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
});

let refreshPromise = null;

//tự chạy khi response trả về (mục đích là nếu AT hết hạn -> 401, sẽ tự gọi refresh)
// nếu thành công thì thôi, ko gọi refresh nữa, trả response ra luôn
http.interceptors.response.use(
  (response) => response,

  async (error) => {
    
    const { config, response } = error;

    // Các endpoint auth công khai trả 401 vì lỗi nghiệp vụ (sai mật khẩu, sai OTP...),
    // không phải vì access token hết hạn -> không được auto refresh & retry.
    const isPublicAuthEndpoint = ["/auth/refresh-token", "/auth/login", "/auth/register", "/auth/confirm-otp"].some(
      (path) => config.url?.includes(path)
    );

    if (response?.status !== 401 || config._retry || isPublicAuthEndpoint) {
      return Promise.reject(error);
    }
    config._retry = true;

    try {
      if (!refreshPromise) {
        refreshPromise = http.post("/auth/refresh-token").finally(() => {
          refreshPromise = null;
        });
      }
      const { data } = await refreshPromise;
      setAccessToken(data.data.accessToken);
      config.headers.Authorization = `Bearer ${data.data.accessToken}`;
      return http(config);

    } catch (refreshError) {
      setAccessToken(null);
      return Promise.reject(refreshError);
    }
  }
);

export default http;
