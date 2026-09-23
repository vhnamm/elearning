import React from "react";
import styles from "./HeaderBlank.module.scss";
import LogoNoLink from "~components/common/Logo/LogoNoLink.jsx";

const HeaderBlank = ({ title = "Tạo khóa học mới" }) => (
    <header className={styles.wrapper}>
        <div className={styles.inner}>
            <LogoNoLink />
            {title && (
                <>
                    <span className={styles.divider} aria-hidden="true" />
                    <h1 className={styles.pageTitle}>{title}</h1>
                </>
            )}
        </div>
    </header>
);

export default HeaderBlank;