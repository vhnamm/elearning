import http from "~services/http.js";

export const getInstructorCourses = async ({
  keyword,
  status,
  min,
  max,
  page = 0,
  size = 10,
  sort = "createdAt,desc",
} = {}) => {
  const { data } = await http.get("/instructor/courses", {
    params: { keyword, status, min, max, page, size, sort },
  });
  return data.data;
};

export const getPopularCourses = async (size = 4) => {
  const { data } = await http.get("/courses/popular", {
    params: { size },
  });
  return data.data;
};

export const searchCourses = async ({
  keyword,
  categoryIds = [],
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
  levels.forEach((level) => params.append("levels", level));
  if (minRating) params.set("minRating", minRating);
  if (priceType && priceType !== "all") params.set("priceType", priceType);
  if (min !== "" && min != null) params.set("min", min);
  if (max !== "" && max != null) params.set("max", max);
  if (sort && sort !== "popular") params.set("sort", sort);
  params.set("page", String(page));
  params.set("size", String(size));

  const { data } = await http.get("/search/courses", { params });
  return data.data;
};

export const getPublishedCourses = async ({
  keyword,
  min,
  max,
  page = 0,
  size = 8,
  sort = "createdAt,desc",
} = {}) => {
  const { data } = await http.get("/courses", {
    params: { keyword, min, max, page, size, sort },
  });
  return data.data;
};
