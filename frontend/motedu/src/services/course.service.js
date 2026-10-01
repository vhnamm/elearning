import http from "~services/http.js";
import axios from 'axios';

// Danh sách khóa học của giảng viên đang đăng nhập.
export const getInstructorCourses = async ({
  keyword,
  status,
  min,
  max,
  page = 0,
  size = 10,
  sort = "createdAt,desc",
} = {}) => {
  const { data } = await http.get("/users/me/instructed-courses", {
    params: { keyword, status, min, max, page, size, sort },
  });
  return data.data;
};

export const saveDraftCourse = async ({ title, categoryId }) => {
  const { data } = await http.post("/courses", { title, categoryId });
  return data.data;
};

export const getCourseBasicInfo = async (courseId) => {
  const { data } = await http.get(`/courses/${courseId}/basic-info`);
  return data.data;
};

export const getPopularCourses = async (size = 4) => {
  const { data } = await http.get("/courses/popular", {
    params: { size },
  });
  return data.data;
};

// Gọi GET /courses với từ khóa, bộ lọc, sắp xếp và phân trang.
export const searchCourses = async ({
  keyword,
  categoryId,
  subcategoryIds = [],
  levels = [],
  minRating,
  priceType,
  min,
  max,
  sort,
  page = 0,
  size = 5,
} = {}) => {
  const params = new URLSearchParams();
  if (keyword) params.set("keyword", keyword);
  if (categoryId) params.set("categoryId", categoryId);
  subcategoryIds.forEach((id) => params.append("subcategoryIds", id));
  levels.forEach((level) => params.append("levels", level));
  if (minRating) params.set("minRating", minRating);
  if (priceType && priceType !== "all") params.set("priceType", priceType);
  if (min !== "" && min != null) params.set("min", min);
  if (max !== "" && max != null) params.set("max", max);
  if (sort && sort !== "popular") params.set("sort", sort);
  params.set("page", String(page));
  params.set("size", String(size));

  const { data } = await http.get("/courses", { params });
  return data.data;
};

export const getPublishedCourses = async ({
  keyword,
  min,
  max,
  page = 0,
  size = 8,
  sort = "newest",
} = {}) => {
  const { data } = await http.get("/courses", {
    params: { keyword, min, max, page, size, sort },
  });
  return data.data;
};

export const getCourseDetailPublic = async (courseId) => {
  const { data } = await http.get(`/courses/${courseId}`);
  return data.data;
};

export const checkUserEnrollmentApi = async (courseId) => {
  try {
    const response = await axios.get(`/api/v1/enrollments/check/${courseId}`);
    if (typeof response.data === 'object' && response.data !== null) {
      return response.data.data === true;
    }

    // Nếu Backend trả về boolean nguyên thủy
    return response.data === true;

  } catch (error) {
    // Cứ có lỗi (kể cả 401 do chưa đăng nhập) thì mặc định là chưa mua!
    return false;
  }
};

export const enrollFreeCourseApi = async (courseId) => {
  try {
    const response = await axios.post(`/api/v1/enrollments/free/${courseId}`);
    return response.data;
  } catch (error) {
    console.error("Lỗi đăng ký khóa học:", error);
    throw error;
  }
};