import http from "~services/http.js";

export const getInstructorCourses = async ({keyword, status, min, max, page = 0, size = 10, sort = "createdAt,desc"} = {}) => {
    const {data} = await http.get("/instructor/courses", {
        params: {keyword, status, min, max, page, size, sort},
    });
    console.log(data.data)
    return data.data;
}