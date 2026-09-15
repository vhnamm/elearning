import React, { useState } from "react";
import {Row, Col, Input, Button, message} from "antd";
import { ArrowLeftOutlined, ArrowRightOutlined } from "@ant-design/icons";
import styles from "./Onboarding.module.scss";
import useAuth from "~hooks/useAuth.js";
import {onboarding} from "~services/instructor.service.js";
import {useNavigate} from "react-router-dom";

const { TextArea } = Input;

const EXPERIENCE_OPTIONS = [
    {
        key: "OFFLINE",
        title: "Trực tiếp",
        desc: "Dạy tại trường, trung tâm hoặc gia sư truyền thống.",
    },
    {
        key: "ONLINE",
        title: "Trực tuyến",
        desc: "Đã từng quay video khóa học hoặc dạy qua Zoom / Meet.",
    },
    {
        key: "BOTH",
        title: "Cả hai hình thức",
        desc: "Đã có kinh nghiệm giảng dạy đa kênh chuyên nghiệp.",
    },
    {
        key: "BEGINNER",
        title: "Người mới bắt đầu",
        desc: "Chưa có kinh nghiệm, muốn bắt đầu chia sẻ kiến thức.",
    },
];

export default function Onboarding() {
    const [teachingExp, setTeachingExp] = useState("ONLINE");
    const [headline, setHeadline] = useState("");
    const [bio, setBio] = useState("");
    const {user, loginWithAccessToken} = useAuth();
    const navigate = useNavigate()

    const handleSubmit = async () => {
        const payload = {email: user?.email, experience: teachingExp, headline, bio };
        try {
            const data = await onboarding(payload);
            await loginWithAccessToken(data.data.accessToken);
            navigate("/instructor/courses")
        }catch (err){
            message.error(err.response.data?.message)
        }


    };

    return (
        <div className={styles.onboardingPage}>
            {/* 1. Nội dung Form chính */}
            <main className={styles.mainContent}>
                <div className={styles.titleSection}>
                    <h1>Thiết lập hồ sơ giảng dạy của bạn</h1>
                    <p>
                        Chỉ mất 1 phút để hoàn tất thông tin cơ bản trước khi bắt đầu tạo khóa học đầu tiên trên MótEdu.
                    </p>
                </div>

                <div className={styles.formContainer}>
                    {/* Câu 1: Kinh nghiệm (Ant Design Row 24 cột -> 2 thẻ span={12}) */}
                    <div className={styles.questionBlock}>
                        <label className={styles.questionLabel}>
                            1. Bạn đã từng có kinh nghiệm dạy học ở hình thức nào trước đây chưa?
                        </label>
                        <Row gutter={[16, 16]}>
                            {EXPERIENCE_OPTIONS.map((item) => {
                                const isSelected = teachingExp === item.key;
                                return (
                                    <Col span={12} key={item.key}>
                                        <div
                                            className={`${styles.radioCard} ${isSelected ? styles.selected : ""}`}
                                            onClick={() => setTeachingExp(item.key)}
                                        >
                                            <div className={styles.radioCircle}>
                                                {isSelected && <div className={styles.innerDot} />}
                                            </div>
                                            <div className={styles.radioInfo}>
                                                <h4>{item.title}</h4>
                                                <p>{item.desc}</p>
                                            </div>
                                        </div>
                                    </Col>
                                );
                            })}
                        </Row>
                    </div>

                    {/* Câu 2: Headline */}
                    <div className={styles.questionBlock}>
                        <div className={styles.labelHeader}>
                            <label className={styles.questionLabel}>2. Tiêu đề chuyên môn vắn tắt (Headline)</label>
                            <span className={styles.hintTag}>Hiển thị dưới tên của bạn</span>
                        </div>
                        <Input
                            className={styles.customInput}
                            size="large"
                            placeholder="Senior Fullstack Developer & Chuyên gia AI Solutions"
                            value={headline}
                            onChange={(e) => setHeadline(e.target.value)}
                        />
                        <p className={styles.fieldNote}>
                            Dòng này sẽ xuất hiện trên trang chi tiết khóa học
                            và thẻ giảng viên (vd: <em>"Chuyên gia AI & Machine Learning"</em>)
                        </p>
                    </div>

                    {/* Câu 3: Bio */}
                    <div className={styles.questionBlock}>
                        <div className={styles.labelHeader}>
                            <label className={styles.questionLabel}>3. Giới thiệu đôi chút về bạn (Bio)</label>
                            <span className={styles.hintTag}>Hiển thị trên trang cá nhân giảng viên & thông tin khóa học</span>
                        </div>
                        <TextArea
                            className={styles.customTextArea}
                            rows={4}
                            maxLength={500}
                            showCount
                            placeholder="Chia sẻ ngắn về số năm kinh nghiệm, thế mạnh công việc hoặc phương châm giảng dạy của bạn..."
                            value={bio}
                            onChange={(e) => setBio(e.target.value)}
                        />
                    </div>
                </div>
            </main>

            {/* 2. Thanh điều hướng đáy */}
            <footer className={styles.bottomBar}>
                <div className={styles.bottomContainer}>
                    <Button type="text" icon={<ArrowLeftOutlined />} className={styles.btnBack}>
                        Quay lại
                    </Button>

                    <div className={styles.rightActions}>
                        <Button type="text" className={styles.btnSkip}>
                            Để sau
                        </Button>
                        <Button
                            type="primary"
                            className={styles.btnSubmit}
                            onClick={handleSubmit}
                        >
                            Hoàn tất <ArrowRightOutlined />
                        </Button>
                    </div>
                </div>
            </footer>
        </div>
    );
}