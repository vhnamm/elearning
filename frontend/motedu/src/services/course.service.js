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

export const getCourseBasicInfo = async (courseId, versionId) => {
  const { data } = await http.get(`/courses/${courseId}/versions/${versionId}/basic-info`);
  return data.data;
};

// Đề cương của một version, dành cho giảng viên soạn thảo (có trạng thái upload, số câu hỏi...).
export const getInstructorCurriculum = async (courseId, versionId) => {
  const { data } = await http.get(`/courses/${courseId}/versions/${versionId}/curriculum`);
  return data.data;
};

export const getPopularCourses =async (size = 4) => {
  const { data } = await http.get("/courses/popular", {
    params: { size },
  });
  return data.data;
};

export const getPresignedUrl = async (contentType, courseId) => {
    const {data} = await http.post(`/courses/${courseId}/thumbnail/presigned-url`, {contentType: contentType});
    return data;
}
// Gọi GET /courses với từ khóa, bộ lọc, sắp xếp và phân trang.
export const searchCourses = async ({
  keyword,
  categoryIds = [],
  subcategoryIds = [],
  topicId,
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

// Nối mảng thành chuỗi "1,2,3"
    if (categoryIds?.length) params.set("categoryIds", categoryIds.join(","));
    if (subcategoryIds?.length) params.set("subcategoryIds", subcategoryIds.join(","));
    if(topicId) params.set("topicId", topicId)
    if (levels?.length) params.set("levels", levels.join(","));
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



// Gợi ý tìm kiếm liên quan; chưa có từ khóa thì trả danh mục nổi bật.
export const getRelatedQueries = async (keyword) => {
  const { data } = await http.get("/courses/related-queries", {
    params: keyword ? { keyword } : {},
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


export const updateCourseBasicInfo = async (courseId, versionId, payload) => {
    const { data } = await http.put(`/courses/${courseId}/versions/${versionId}/basic-info`, payload);
    return data;
};

 export const createNewSection = async (courseId, versionId, payload ) => {
     const {data} = await  http.post(`/courses/${courseId}/versions/${versionId}/sections`, payload)
     return data;
 }

 export const deleteSection = async (courseId, versionId, sectionId) => {
   const {data} = await http.delete(`/courses/${courseId}/versions/${versionId}/sections/${sectionId}`)
   return data;
 }