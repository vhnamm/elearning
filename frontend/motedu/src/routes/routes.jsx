import { Routes, Route, Navigate } from "react-router-dom";
import Login from "~pages/Login/Login";
import MainLayout from "~layouts/MainLayout/MainLayout.jsx";
import Register from "~pages/Register/Register.jsx";
import Oauth2CallbackHandler from "~pages/Login/Oauth2CallbackHandler.jsx";
import InstructorLandingPage from "~pages/InstructorLandingPage/InstructorLandingPage.jsx";
import Onboarding from "~pages/Onboarding/Onboarding.jsx";
import HeaderOnly from "~layouts/HeaderOnly/HeaderOnly.jsx";

const AppRoutes = () => (
  <Routes>
      <Route path="/oauth2/callback" element={<Oauth2CallbackHandler/>}></Route>

      <Route path="/" element={<MainLayout/>}>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register/>}></Route>
          <Route path="/teaching" element={<InstructorLandingPage/>}></Route>
      </Route>

      <Route path={"/instructor/onboarding"} element={<HeaderOnly/>}>
        <Route index={true} element={<Onboarding/>}></Route>
      </Route>
  </Routes>
);

export default AppRoutes;
