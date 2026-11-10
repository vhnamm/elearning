import http from "~services/http.js";

export const createQuestion = async (itemId, payload) => {
    const {data} = await http.post(`/curriculum-items/${itemId}/questions`, payload);
    return data.data;
}

// Danh sách câu hỏi của một quiz: { quizId, questions: [{ id, content, position, options: [...] }] }
export const getQuestions = async (itemId) => {
    const {data} = await http.get(`/curriculum-items/${itemId}/questions`);
    return data.data;
}

export const deleteQuestion = async (itemId, questionId) => {
    await http.delete(`/curriculum-items/${itemId}/questions/${questionId}`);
}

export const updateQuestion = async (itemId, questionId, payload) => {
    const {data} = await http.put(`/curriculum-items/${itemId}/questions/${questionId}`, payload)
    return data.data
}