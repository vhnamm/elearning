import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import useAuth from "~hooks/useAuth.js";
import LoadingScreen from "~components/common/Loading/LoadingScreen.jsx";

const Oauth2CallbackHandler = () => {
    const { loginWithAccessToken } = useAuth();
    const navigate = useNavigate();

    useEffect(() => {

        const handleOAuth2 = async () => {
            try {
                // 1. Lấy phần hash sau dấu # (ví dụ: "accessToken=eyJhbG...")
                const hashString = window.location.hash.startsWith("#")
                    ? window.location.hash.substring(1)
                    : window.location.hash;

                const params = new URLSearchParams(hashString);
                const accessToken = params.get("accessToken");

                if (!accessToken) {
                    throw new Error("Không tìm thấy accessToken trong URL hash");
                }

                // 2. Gọi login bằng token thuần (chỉ chứa JWT)
                await loginWithAccessToken(accessToken);

                // 3. Lấy redirect destination đúng cú pháp: getItem() || fallback
                const from = sessionStorage.getItem("redirectTo") || "/";
                sessionStorage.removeItem("redirectTo");

                navigate(from, { replace: true });
            } catch (err) {
                console.error("OAuth2 authentication error:", err);
                // 4. Nếu có lỗi (401, mất mạng, token sai) -> Điều hướng thoát khỏi Loading
                sessionStorage.removeItem("redirectTo");
                navigate("/login", {
                    replace: true,
                    state: { error: "Đăng nhập bằng Google thất bại. Vui lòng thử lại!" }
                });
            }
        };

        handleOAuth2();
    }, []);

    return (
        <LoadingScreen></LoadingScreen>
    );
};

export default Oauth2CallbackHandler;