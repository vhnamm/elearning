import { Routes, Route, Navigate } from "react-router-dom";
import Login from "~pages/Login/Login";
import MainLayout from "~layouts/MainLayout/MainLayout.jsx";
import Register from "~pages/Register/Register.jsx";
import Oauth2CallbackHandler from "~pages/Login/Oauth2CallbackHandler.jsx";
import InstructorLandingPage from "~pages/InstructorLandingPage/InstructorLandingPage.jsx";
import Onboarding from "~pages/Onboarding/Onboarding.jsx";
import HeaderOnly from "~layouts/HeaderOnly/HeaderOnly.jsx";
import Forbidden from "~pages/Forbidden/Forbidden.jsx";
import RouteGuard from "./RouteGuard.jsx";

const AppRoutes = () => (
  <Routes>
      <Route path="/oauth2/callback" element={<Oauth2CallbackHandler/>}></Route>

      <Route path="/" element={<MainLayout/>}>
          {/* Nhóm 2: Auth/Guest-only -> đã login thì đá về "/" */}
          <Route element={<RouteGuard guestOnly redirectTo="/" />}>
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register/>}></Route>
          </Route>

          {/* Nhóm 3: Non-Role Landing -> Instructor/Admin đã có role thì đá sang trang của họ */}
          {/* TODO: đổi redirectTo="/instructor/courses" khi trang đó đã có route thật, hiện chưa tồn tại */}
          <Route element={<RouteGuard forbiddenRoles={["INSTRUCTOR", "ADMIN"]} redirectTo="/" />}>
              <Route path="/teaching" element={<InstructorLandingPage/>}></Route>
          </Route>

          {/* Public: trang báo không đủ quyền, dùng làm redirectTo cho các RouteGuard có allowedRoles */}
          <Route path="/403" element={<Forbidden/>}></Route>
      </Route>

      {/* Nhóm 4: Onboarding/Upgrade -> bắt buộc login; đã là Instructor/Admin thì đá sang trang của họ */}
      {/* TODO: đổi redirectTo="/instructor/courses" khi trang đó đã có route thật, hiện chưa tồn tại */}
      <Route element={<RouteGuard requireAuth forbiddenRoles={["INSTRUCTOR", "ADMIN"]} redirectTo="/instructor/courses" />}>
        <Route path="/instructor/onboarding" element={<HeaderOnly/>}>
          <Route index={true} element={<Onboarding/>}></Route>
        </Route>
      </Route>

      {/* Nhóm 5: Protected/Role-specific -> chưa login -> /login; login rồi mà sai role -> /403 */}
      {/* <Route element={<RouteGuard requireAuth allowedRoles={["INSTRUCTOR"]} redirectTo="/403" />}>
        <Route path="/instructor/dashboard" element={<InstructorDashboard/>} />
      </Route>
      <Route element={<RouteGuard requireAuth allowedRoles={["ADMIN"]} redirectTo="/403" />}>
        <Route path="/admin" element={<AdminPage/>} />
      </Route> */}
  </Routes>
);

export default AppRoutes;
