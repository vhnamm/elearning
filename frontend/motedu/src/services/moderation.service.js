import http from "~services/http.js";

export const PENDING_COURSES_CHANGED_EVENT = "admin:pending-courses-changed";

export const notifyPendingCoursesChanged = () => {
  window.dispatchEvent(new Event(PENDING_COURSES_CHANGED_EVENT));
};

export const getPendingCourses = async ({ page = 0, size = 20, sort = "updatedAt,desc" } = {}) => {
  const { data } = await http.get("/admin/courses/pending", {
    params: { page, size, sort },
  });
  return data.data;
};

export const countPendingCourses = async () => {
  const { data } = await http.get("/admin/courses/pending/count");
  return data.data?.count ?? 0;
};

export const getRejectReasons = async () => {
  const { data } = await http.get("/admin/courses/reject-reasons");
  return data.data ?? [];
};

export const getCourseForReview = async (courseId) => {
  const { data } = await http.get(`/admin/courses/${courseId}/review`);
  return data.data;
};

export const approveCourse = async (courseId, feedback) => {
  const { data } = await http.post(`/admin/courses/${courseId}/approve`, feedback ? { feedback } : {});
  return data.data;
};

export const rejectCourse = async (courseId, { reasonCategory, feedback }) => {
  const { data } = await http.post(`/admin/courses/${courseId}/reject`, { reasonCategory, feedback });
  return data.data;
};

export const getErrorMessage = (error, fallback) => error?.response?.data?.message || fallback;
