import http from "~services/http.js";

export const getAllCategories = async () => {
    const {data} = await http.get("/categories")
    return data.data
}