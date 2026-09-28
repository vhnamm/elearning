import React, { useState } from "react";
import { Outlet } from "react-router-dom";
import HeaderBlank from "~layouts/HeaderBlankLayout/HeaderBlank.jsx";
import Footer from "~layouts/partials/Footer/Footer.jsx";
import styles from "./HeaderBlankLayout.module.scss";
const HeaderBlankLayout = () => {
    const [pageTitle, setPageTitle] = useState("");

    return (
        <div className={styles.layout}>
            <HeaderBlank title={pageTitle} />
            <main className={styles.main}>
                {/* Truyền setTitle qua context để các trang con tự đổi tiêu đề */}
                <Outlet context={{ setPageTitle }} />
            </main>
            <Footer></Footer>
        </div>
    );
};

export default HeaderBlankLayout;
