import { NavLink } from "react-router-dom";
import { Button, Modal } from "antd";
import {
    FileTextOutlined,
    ProfileOutlined,
    TagOutlined,
    EyeOutlined,
    SendOutlined,
} from "@ant-design/icons";
import styles from "./CreateCourseSidebar.module.scss";

// ready: false = trang của bước này chưa có route nên chưa cho bấm
const STEPS = [
    { key: "overview", label: "Tổng quan khóa học", icon: <FileTextOutlined />, ready: true },
    { key: "curriculum", label: "Đề cương khóa học", icon: <ProfileOutlined />, ready: true },
    { key: "pricing", label: "Cài đặt giá & Khuyến mãi", icon: <TagOutlined />, ready: false },
    { key: "preview", label: "Xem trước & Cài đặt", icon: <EyeOutlined />, ready: false },
];

const CreateCourseSidebar = () => {
    // TODO: khi có API kiểm tra điều kiện gửi duyệt thì gọi ở đây, hiện luôn báo chưa đủ.
    const handleSubmitReview = () => {
        Modal.warning({
            title: "Chưa thể gửi duyệt",
            content: "Bạn chưa thiết lập đầy đủ khóa học. Vui lòng hoàn thành các bước còn thiếu rồi gửi duyệt lại.",
            okText: "Đã hiểu",
        });
    };

    return (
        <aside className={styles.wrapper}>
            <div className={styles.top}>
                <span className={styles.groupLabel}>Quy trình tạo khóa học</span>
                <h2 className={styles.groupTitle}>Thiết lập nội dung</h2>

                <ul className={styles.stepList}>
                    {STEPS.map((step) => (
                        <li key={step.key}>
                            {step.ready ? (
                                // to là đường dẫn tương đối, nối vào .../manage nên giữ nguyên courseId/versionId
                                <NavLink
                                    to={step.key}
                                    className={({ isActive }) =>
                                        `${styles.stepItem} ${styles.stepLink} ${isActive ? styles.active : ""}`
                                    }
                                >
                                    {({ isActive }) => (
                                        <>
                                            <span className={styles.stepIcon}>{step.icon}</span>
                                            <span className={styles.stepLabel}>{step.label}</span>
                                            {isActive && <span className={styles.activeDot} aria-hidden="true" />}
                                        </>
                                    )}
                                </NavLink>
                            ) : (
                                <div className={`${styles.stepItem} ${styles.stepDisabled}`}>
                                    <span className={styles.stepIcon}>{step.icon}</span>
                                    <span className={styles.stepLabel}>{step.label}</span>
                                </div>
                            )}
                        </li>
                    ))}
                </ul>
            </div>

            <div className={styles.bottom}>
                <Button
                    block
                    type="primary"
                    icon={<SendOutlined />}
                    className={styles.submitBtn}
                    onClick={handleSubmitReview}
                >
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
