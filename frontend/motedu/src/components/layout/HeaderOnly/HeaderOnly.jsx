import React from "react";
import { Outlet } from "react-router-dom";
import Header from "../partials/Header/Header";

const HeaderOnly = () => {
  return (
    <div>
      <Header />
      <Outlet />
    </div>
  );
};
export default HeaderOnly;



