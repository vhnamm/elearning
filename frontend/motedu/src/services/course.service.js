import http from "~services/http.js";

export const getInstructorCourses = async ({keyword, status, min, max, page = 0, size = 10, sort = "createdAt,desc"} = {}) => {
    const {data} = await http.get("/users/me/instructed-courses", {
        params: {keyword, status, min, max, page, size, sort},
    });
    console.log(data.data)
    return data.data;
}

export const saveDraftCourse = async ({title, categoryId}) => {
    const {data} = await http.post("/courses", {title: title, categoryId: categoryId})
    return data.data
}

export const getCourseBasicInfo = async (courseId) => {
    const {data} = await http.get(`/courses/${courseId}/basic-info`);
    return data.data;
}