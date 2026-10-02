import http from "~services/http.js";

export const getAllCategories = async () => {
    const {data} = await http.get("/categories")
    return data.data
}

export const getCategoryTree = async () => {
    const {data} = await http.get("/categories/tree")
    return data.data;
}

export const searchTopics = async (keyword = "") => {
    const {data} = await http.get("/topics", { params: { keyword } })
    return data.data;
}