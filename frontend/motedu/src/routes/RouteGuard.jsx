import { Navigate, Outlet, useLocation } from "react-router-dom";
import useAuth from "~hooks/useAuth.js";
import LoadingScreen from "~components/common/Loading/LoadingScreen.jsx";

const hasAnyRole = (user, roles) => roles.some((role) => user?.roles?.includes(role));

const RouteGuard = ({
    guestOnly = false,
    requireAuth = false,
    allowedRoles = [],
    forbiddenRoles = [],
    redirectTo = "/",
}) => {
    const { user, isAuthenticated, isInitializing } = useAuth();
    const location = useLocation();

    // Chưa xác định xong trạng thái auth (đang gọi /auth/me) -> chưa quyết định redirect vội, tránh flash sai trang
    if (isInitializing) return <LoadingScreen></LoadingScreen>;

    // Đã đăng nhập rồi thì không cho vào lại /login, /register...
    // Ưu tiên nơi user định vào trước đó (đã lưu khi bị chặn bởi requireAuth), rồi mới tới redirectTo mặc định.
    if (guestOnly && isAuthenticated) {
        const from = sessionStorage.getItem("redirectTo");
        sessionStorage.removeItem("redirectTo");
        return <Navigate to={from || redirectTo} replace />;
    }

    // Cần đăng nhập mới được vào
    if (requireAuth && !isAuthenticated) {
        sessionStorage.setItem("redirectTo", location.pathname);
        return <Navigate to="/login" replace state={{ from: location }} />;
    }

    // Blacklist: đã đăng nhập và có role bị cấm (VD: Instructor vào lại /teaching, /instructor/onboarding)
    if (isAuthenticated && forbiddenRoles.length > 0 && hasAnyRole(user, forbiddenRoles)) {
        return <Navigate to={redirectTo} replace />;
    }

    // Whitelist: đã đăng nhập nhưng không có role được phép
    if (isAuthenticated && allowedRoles.length > 0 && !hasAnyRole(user, allowedRoles)) {
        return <Navigate to={redirectTo} replace />;
    }

    return <Outlet />;
};

export default RouteGuard;
