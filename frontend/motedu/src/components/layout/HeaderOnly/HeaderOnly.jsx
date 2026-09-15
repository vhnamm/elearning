import React from "react";
import { Outlet } from "react-router-dom";
import Header from "../partials/Header/Header";
import Footer from "~layouts/partials/Footer/Footer.jsx";
import HeaderBlank from "~layouts/HeaderOnly/HeaderBlank.jsx";

const HeaderOnly = () => {
  return (
    <div>
      <HeaderBlank />
      <Outlet />
    </div>
  );
};
export default HeaderOnly;



