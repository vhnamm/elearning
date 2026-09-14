import { Routes, Route, Navigate } from "react-router-dom";
import Login from "~pages/Login/Login";
import MainLayout from "~layouts/MainLayout/MainLayout.jsx";
import Register from "~pages/Register/Register.jsx";
import Oauth2CallbackHandler from "~pages/Login/Oauth2CallbackHandler.jsx";

const AppRoutes = () => (
  <Routes>
      <Route path="/oauth2/callback" element={<Oauth2CallbackHandler/>}></Route>

      <Route path="/" element={<MainLayout/>}>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register/>}></Route>
      </Route>

  </Routes>
);

export default AppRoutes;
