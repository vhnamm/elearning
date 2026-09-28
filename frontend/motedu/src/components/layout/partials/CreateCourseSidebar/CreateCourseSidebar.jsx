import React from "react";
import { Button, Progress } from "antd";
import {
    FileTextOutlined,
    ProfileOutlined,
    TagOutlined,
    EyeOutlined,
    SendOutlined,
} from "@ant-design/icons";
import styles from "./CreateCourseSidebar.module.scss";

const STEPS = [
    { key: "overview", label: "Tổng quan khóa học", icon: <FileTextOutlined />, status: "active" },
    { key: "curriculum", label: "Đề cương khóa học", icon: <ProfileOutlined />, status: "Chưa xong" },
    { key: "pricing", label: "Cài đặt giá & Khuyến mãi", icon: <TagOutlined />, status: "Chờ" },
    { key: "preview", label: "Xem trước & Cài đặt", icon: <EyeOutlined />, status: "Chờ" },
];

const CreateCourseSidebar = ({ progressPercent = 25 }) => {
    return (
        <aside className={styles.wrapper}>
            <div className={styles.top}>
                <span className={styles.groupLabel}>Quy trình tạo khóa học</span>
                <h2 className={styles.groupTitle}>Thiết lập nội dung</h2>

                <ul className={styles.stepList}>
                    {STEPS.map((step) => {
                        const isActive = step.status === "active";
                        return (
                            <li
                                key={step.key}
                                className={`${styles.stepItem} ${isActive ? styles.active : ""}`}
                            >
                                <span className={styles.stepIcon}>{step.icon}</span>
                                <span className={styles.stepLabel}>{step.label}</span>
                                {isActive ? (
                                    <span className={styles.activeDot} aria-hidden="true" />
                                ) : (
                                    <span className={styles.stepStatus}>{step.status}</span>
                                )}
                            </li>
                        );
                    })}
                </ul>
            </div>

            <div className={styles.bottom}>
                <div className={styles.progressBox}>
                    <div className={styles.progressHeader}>
                        <span>Tiến độ hoàn thiện</span>
                        <span className={styles.progressPercent}>{progressPercent}%</span>
                    </div>
                    <Progress
                        percent={progressPercent}
                        showInfo={false}
                        strokeColor="#2563eb"
                        size="small"
                    />
                    <p className={styles.progressNote}>1/4 bước đã hoàn tất một phần</p>
                </div>

                <Button block disabled icon={<SendOutlined />} className={styles.submitBtn}>
                    Gửi duyệt khóa học
                </Button>
                <p className={styles.submitNote}>
                    Cần hoàn thành tối thiểu Tổng quan &amp; Đề cương để gửi kiểm duyệt
                </p>
            </div>
        </aside>
    );
};

export default CreateCourseSidebar;
