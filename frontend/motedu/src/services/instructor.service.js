import {BASE_URL} from "~services/http.js";
import http from "~services/http.js";

export const onboarding = async ({email, experience, headline, bio}) => {
    const {data} = await http.post("/instructors/onboarding", {email: email, experience: experience, headline: headline, bio: bio});
    return data;
}