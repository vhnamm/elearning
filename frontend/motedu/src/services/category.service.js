import http from "~services/http.js";
import {BASE_URL} from "~services/http.js";

export const getAllCategories = async () => {
    const {data} = await http.get(BASE_URL + "/categories")
    return data.data
}