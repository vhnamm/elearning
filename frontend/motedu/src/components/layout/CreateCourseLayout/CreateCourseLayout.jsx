import React from "react";
import { Outlet, Link } from "react-router-dom";
import { ArrowLeftOutlined } from "@ant-design/icons";
import HeaderBlank from "~layouts/HeaderBlankLayout/HeaderBlank.jsx";
import CreateCourseSidebar from "~layouts/partials/CreateCourseSidebar/CreateCourseSidebar.jsx";
import styles from "./CreateCourseLayout.module.scss";

const CreateCourseLayout = () => {
    return (
        <div className={styles.layout}>
            <HeaderBlank
                title="Tổng quan khóa học"
                extra={
                    <Link to="/instructor/courses" className={styles.dashboardLink}>
                        <ArrowLeftOutlined />
                        Về Dashboard
                        <span className={styles.dot} aria-hidden="true" />
                    </Link>
                }
            />
            <div className={styles.body}>
                <CreateCourseSidebar />
                <main className={styles.content}>
                    <Outlet />
                </main>
            </div>
        </div>
    );
};

export default CreateCourseLayout;
