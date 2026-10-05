import http from "~services/http.js";

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
  const { data } = await http.get("/courses/instructed-courses", {
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
  categoryIds = [],
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
  categoryIds.forEach((id) => params.append("categoryIds", id));
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

export const getCourseCurriculumPublic = async (courseId) => {
  const { data } = await http.get(`/courses/${courseId}/curriculum`);
  return data.data;
};

export const checkUserEnrollmentApi = async (courseId) => {
  const { data } = await http.get(`/enrollments/${courseId}/status`);
  return data?.data === true;
};

export const enrollFreeCourseApi = async (courseId) => {
  const { data } = await http.post('/enrollments', { courseId });
  return data;
};
