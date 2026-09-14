import React from "react";
import { Row, Col } from "antd";
import styles from "./InstrcutorLandingPage.module.scss";
import Button from "~components/common/Button/Button.jsx";
import {CheckOutlined, CreditCardFilled, TeamOutlined} from "@ant-design/icons";
import {useNavigate} from "react-router-dom";

export default function InstructorLandingPage() {
    const navigate = useNavigate();
    const handleCta = () => {
        navigate("/instructor/onboarding")
    }
    return (
        <div className={styles.pageWrapper}>
            {/* 1. Hero Section */}
            <section className={styles.heroSection}>
                <div className={styles.container}>
                    <Row gutter={[40, 40]} align="middle">
                        {/* Cột trái: Giới thiệu & CTA — 7/12 cột */}
                        <Col xs={24} md={14} className={styles.heroLeft}>
                            <h1 className={styles.heroTitle}>
                                Lan tỏa tri thức của bạn,
                                <br />
                                <span style={{color: "#2563eb"}}>kiến tạo thu nhập </span>
                                không giới hạn.
                                <br />

                            </h1>

                            <p className={styles.heroDesc}>
                                Trở thành giảng viên trên nền tảng MótEdu để kết nối với hơn
                                250,000+ học viên trên cả nước. Chúng tôi cung cấp công cụ xây
                                dựng khóa học chuyên nghiệp, bài tập coding, quiz và hệ thống thanh
                                toán tự động.
                            </p>

                            <div className={styles.ctaRow}>
                                <Button
                                    className={styles.btnPrimary}
                                    size={"large"}
                                    primary={true}
                                    onClick={handleCta}
                                >
                                    Trở thành giảng viên ngay hôm nay <span>→</span>
                                </Button>
                                <div className={styles.guaranteeBadge}>
                                    <CheckOutlined className={styles.checkIcon}/>
                                    <span>Miễn phí đăng ký</span>
                                </div>
                            </div>

                            {/* Các con số thống kê */}
                            <div className={styles.statsRow}>
                                <div className={styles.statItem}>
                                    <span className={styles.statNumber}>250K+</span>
                                    <span className={styles.statLabel}>Học viên tích cực</span>
                                </div>
                                <div className={styles.statItem}>
                                    <span className={styles.statNumber}>85%</span>
                                    <span className={styles.statLabel}>Chia sẻ doanh thu</span>
                                </div>
                                <div className={styles.statItem}>
                                    <span className={styles.statNumber}>1,200+</span>
                                    <span className={styles.statLabel}>Khóa học xuất bản</span>
                                </div>
                            </div>
                        </Col>

                        {/* Cột phải: Card demo doanh thu — 5/12 cột */}
                        <Col xs={24} md={10} className={styles.heroRight}>
                            <div className={styles.instructorCard}>
                                <div className={styles.cardHeader}>
                                    <img className={styles.cardAvatar} src={"https://studiochupanhdep.com/Upload/Images/Album/Anh-profile-ca-nhan-nam-19.jpg"}/>
                                    <div className={styles.cardInfo}>
                                        <h4 className={styles.cardName}>TS. Nguyễn Minh Khoa</h4>
                                        <p className={styles.cardRole}>Chuyên gia tư vấn tình cảm</p>
                                        <p className={styles.cardRating}>⭐ 4.9/5.0 (12,400 học viên)</p>
                                    </div>
                                </div>

                                <div className={styles.earningsBox}>
                                    <div className={styles.earningsHeader}>
                                        <span className={styles.earningsLabel}>Doanh thu tháng này</span>
                                        <span className={styles.earningsGrowth}>+28.4%</span>
                                    </div>
                                    <div className={styles.earningsValue}>48,500,000 đ</div>
                                    <div className={styles.progressBar}>
                                        <div className={styles.progressFill} />
                                    </div>
                                </div>

                                <blockquote className={styles.quoteBox}>
                                    "MótEdu giúp tôi tập trung 100% vào việc giảng dạy. Toàn bộ hạ tầng lưu trữ
                                    video, hệ thống chấm bài tự động và thanh toán đã có MótEdu lo trọn vẹn."
                                </blockquote>
                            </div>
                        </Col>
                    </Row>
                </div>
            </section>

            {/* 2. Features Section */}
            <section className={styles.featuresSection}>
                <div className={styles.container}>
                    <div className={styles.sectionHeader}>
                        <h2>Vì sao nên giảng dạy tại MótEdu?</h2>
                        <p>
                            Chúng tôi trang bị mọi công cụ hiện đại và chính sách bảo vệ giảng viên toàn diện nhất Việt Nam.
                        </p>
                    </div>

                    <Row gutter={[24, 24]}>
                        <Col xs={24} sm={12} lg={8}>
                            <div className={styles.featureCard}>
                                <CreditCardFilled className={styles.featureIconBox}/>
                                <h3>Thu nhập hấp dẫn & Minh bạch</h3>
                                <p>
                                    Hưởng tỷ lệ chia sẻ doanh thu lên tới 85%. Báo cáo doanh thu thời gian thực (Real-time analytics),
                                    đối soát và rút tiền tự động về tài khoản ngân hàng hàng tháng.
                                </p>
                            </div>
                        </Col>

                        <Col xs={24} sm={12} lg={8}>
                            <div className={styles.featureCard}>
                                <div className={styles.featureIconBox}>✨</div>
                                <h3>Bộ công cụ Course Builder tối tân</h3>
                                <p>
                                    Hỗ trợ video 4K chống tải lậu DRM, hệ sinh thái bài tập trắc nghiệm thông minh, tạo Quiz tính giờ, và
                                    phòng thực hành tương tác trực quan.
                                </p>
                            </div>
                        </Col>

                        <Col xs={24} sm={12} lg={8}>
                            <div className={styles.featureCard}>
                                <TeamOutlined className={styles.featureIconBox}/>
                                <h3>Mạng lưới học viên sẵn có</h3>
                                <p>
                                    Tiếp cận ngay cộng đồng 250,000+ sinh viên và người đi làm. MótEdu hỗ trợ chạy chiến dịch
                                    marketing đa kênh giúp khóa học của bạn tiếp cận đúng đối tượng mục tiêu.
                                </p>
                            </div>
                        </Col>
                    </Row>
                </div>
            </section>

            {/* 3. Steps Section */}
            <section className={styles.stepsSection}>
                <div className={styles.container}>
                    <div className={styles.sectionHeader}>
                        <span className={styles.stepTag}>LỘ TRÌNH ĐƠN GIẢN</span>
                        <h2>Bắt đầu chỉ trong 3 bước </h2>
                    </div>

                    <Row gutter={[20, 20]}>
                        <Col xs={24} sm={12} lg={8}>
                            <div className={styles.stepCard}>
                                <div className={styles.stepNumber}>1</div>
                                <div className={styles.stepContent}>
                                    <h4>Đăng ký thông tin</h4>
                                    <p>Điền kinh nghiệm và lĩnh vực chuyên môn</p>
                                </div>
                            </div>
                        </Col>

                        <Col xs={24} sm={12} lg={8}>
                            <div className={styles.stepCard}>
                                <div className={styles.stepNumber}>2</div>
                                <div className={styles.stepContent}>
                                    <h4>Xây dựng bài giảng</h4>
                                    <p>Sử dụng Course Builder 4 bước để tải video, tài liệu và tạo Quiz</p>
                                </div>
                            </div>
                        </Col>

                        <Col xs={24} sm={12} lg={8}>
                            <div className={styles.stepCard}>
                                <div className={styles.stepNumber}>3</div>
                                <div className={styles.stepContent}>
                                    <h4>Xuất bản & Thu nhập</h4>
                                    <p>Đội ngũ MótEdu duyệt khóa học trong 24h và bắt đầu mở bán</p>
                                </div>
                            </div>
                        </Col>
                    </Row>
                </div>
            </section>

            {/* 4. Bottom Banner CTA */}
            <section className={styles.bannerSection}>
                <div className={styles.container}>
                    <Row
                        className={styles.bannerCard}
                        align="middle"
                        justify="space-between"
                        gutter={[16, 16]}
                    >
                        <Col xs={24} md={16} className={styles.bannerText}>
                            <h3>Sẵn sàng đồng hành cùng thế hệ nhân tài mới?</h3>
                            <p>Mở tài khoản giảng viên hoàn toàn miễn phí và bắt đầu tạo khóa học ngay hôm nay</p>
                        </Col>
                        <Col xs={24} md={8} className={styles.bannerBtnCol}>
                            <Button primary className={styles.btnBanner}>Đăng ký giảng dạy ngay</Button>
                        </Col>
                    </Row>
                </div>
            </section>
        </div>
    );
}
