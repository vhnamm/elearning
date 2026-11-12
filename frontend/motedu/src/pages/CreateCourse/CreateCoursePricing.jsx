import { useEffect, useState } from "react";
import { useNavigate, useOutletContext, useParams } from "react-router-dom";
import {
    Alert,
    Button,
    InputNumber,
    Radio,
    Spin,
    Statistic,
    Switch,
    Tag,
    message,
} from "antd";
import {
    ArrowRightOutlined,
    CheckCircleFilled,
    DollarOutlined,
    SaveOutlined,
    WalletOutlined,
} from "@ant-design/icons";
import styles from "./CreateCoursePricing.module.scss";

const formatVND = (val) => {
    if (val == null || Number.isNaN(Number(val))) return "0 ₫";
    return `${Number(val).toLocaleString("vi-VN")} ₫`;
};

export default function CreateCoursePricing() {
    const { setPageTitle } = useOutletContext() || {};
    const { courseId, versionId } = useParams();
    const navigate = useNavigate();

    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);

    // State Giá khóa học: null = chưa đặt giá, 0 = miễn phí, > 0 = có phí
    const [savedPrice, setSavedPrice] = useState(null);
    const [priceMode, setPriceMode] = useState("PAID"); // "FREE" | "PAID"
    const [priceInput, setPriceInput] = useState(499000);
    const [limits, setLimits] = useState({
        minPaidPrice: 50000,
        maxPrice: 50000000,
    });

    // State Ví số dư nhận tiền
    const [walletBalance, setWalletBalance] = useState(0);
    const [useWallet, setUseWallet] = useState(true);

    useEffect(() => {
        setPageTitle?.("Cài đặt giá khóa học");
    }, [setPageTitle]);

    useEffect(() => {
        if (!courseId) return;
        let cancelled = false;

        const fetchPricing = async () => {
            setLoading(true);
            try {
                const data = await getCoursePricing(courseId);
                if (cancelled || !data) return;

                if (data.minPaidPrice || data.maxPrice) {
                    setLimits({
                        minPaidPrice: data.minPaidPrice ?? 50000,
                        maxPrice: data.maxPrice ?? 50000000,
                    });
                }

                setSavedPrice(data.price ?? null);
                if (data.price === 0) {
                    setPriceMode("FREE");
                } else if (data.price > 0) {
                    setPriceMode("PAID");
                    setPriceInput(data.price);
                } else {
                    setPriceMode("PAID");
                    setPriceInput(data.minPaidPrice ? Math.max(499000, data.minPaidPrice) : 499000);
                }

                if (data.walletBalance != null) {
                    setWalletBalance(data.walletBalance);
                }
            } catch {
                // Giữ giá trị mặc định nếu API chưa sẵn sàng
            } finally {
                if (!cancelled) setLoading(false);
            }
        };

        fetchPricing();
        return () => {
            cancelled = true;
        };
    }, [courseId]);

    // const handleSavePrice = async () => {
    //     const targetPrice = priceMode === "FREE" ? 0 : Number(priceInput);
    //
    //     if (priceMode === "PAID") {
    //         if (!targetPrice || targetPrice < limits.minPaidPrice || targetPrice > limits.maxPrice) {
    //             message.warning(
    //                 `Giá bán phải từ ${formatVND(limits.minPaidPrice)} đến ${formatVND(limits.maxPrice)}`
    //             );
    //             return;
    //         }
    //         if (!useWallet) {
    //             message.warning("Vui lòng bật nhận doanh thu vào Ví số dư!");
    //             return;
    //         }
    //     }
    //
    //     setSaving(true);
    //     try {
    //         await updateCoursePrice(courseId, { price: targetPrice });
    //         setSavedPrice(targetPrice);
    //         message.success("Đã lưu giá khóa học thành công!");
    //     } catch {
    //         message.error("Không thể lưu giá khóa học, vui lòng thử lại!");
    //     } finally {
    //         setSaving(false);
    //     }
    // };

    return (
        <div className={styles.container}>
            <div className={styles.wrapper}>
                {/* Sticky Header */}
                <div className={styles.stickyHeader}>
                    <div className={styles.headerLeft}>
                        <span className={styles.stepBadge}>BƯỚC 3 TRÊN 4</span>
                        <h1 className={styles.pageTitle}>Bước 3: Cài đặt giá khóa học</h1>
                    </div>
                    <Button
                        type="primary"
                        size="large"
                        className={styles.submitBtn}
                        icon={<ArrowRightOutlined />}
                        iconPosition="end"
                        onClick={() =>
                            navigate(`/instructor/course/${courseId}/version/${versionId}/manage/submit`)
                        }
                    >
                        Tiếp tục
                    </Button>
                </div>

                <p className={styles.pageSubtitle}>
                    Thiết lập mức học phí và ví nhận doanh thu cho khóa học. Thay đổi giá có hiệu lực ngay lập tức mà
                    không cần chờ duyệt lại phiên bản.
                </p>

                <Spin spinning={loading} tip="Đang tải thông tin giá...">
                    <div className={styles.sectionList}>
                        {/* 1. GIÁ KHÓA HỌC */}
                        <section className={styles.section}>
                            <div className={styles.sectionHeader}>
                                <DollarOutlined className={styles.sectionIcon} />
                                <span className={styles.sectionTitle}>Giá khóa học</span>
                                {savedPrice === null && <Tag color="warning">Chưa đặt giá</Tag>}
                                {savedPrice === 0 && <Tag color="success">Miễn phí</Tag>}
                                {savedPrice > 0 && (
                                    <Tag color="blue">Giá hiện tại: {formatVND(savedPrice)}</Tag>
                                )}
                            </div>

                            <div className={styles.sectionBody}>
                                <Radio.Group
                                    value={priceMode}
                                    onChange={(e) => setPriceMode(e.target.value)}
                                    className={styles.radioGroup}
                                >
                                    <Radio value="FREE" className={styles.radioItem}>
                                        <span className={styles.radioTitle}>Miễn phí</span>
                                        <span className={styles.radioSub}>
                                            Học viên đăng ký vào học ngay không cần thanh toán.
                                        </span>
                                    </Radio>
                                    <Radio value="PAID" className={styles.radioItem}>
                                        <span className={styles.radioTitle}>Có phí</span>
                                        <span className={styles.radioSub}>
                                            Học viên thanh toán học phí để mở khóa toàn bộ nội dung.
                                        </span>
                                    </Radio>
                                </Radio.Group>

                                {priceMode === "PAID" && (
                                    <div className={styles.priceInputBox}>
                                        <span className={styles.fieldLabel}>Giá bán:</span>
                                        <InputNumber
                                            size="large"
                                            className={styles.priceInput}
                                            value={priceInput}
                                            min={limits.minPaidPrice}
                                            max={limits.maxPrice}
                                            step={10000}
                                            addonAfter="₫"
                                            formatter={(value) =>
                                                `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ".")
                                            }
                                            parser={(value) => value?.replace(/\.\s?|(\.*)/g, "")}
                                            onChange={(val) => setPriceInput(val)}
                                            placeholder="499.000"
                                        />
                                        <span className={styles.priceHint}>
                                            Từ <strong>{formatVND(limits.minPaidPrice)}</strong> đến{" "}
                                            <strong>{formatVND(limits.maxPrice)}</strong>
                                        </span>
                                    </div>
                                )}
                            </div>

                            <div className={styles.sectionFooter}>
                                <Button
                                    type="primary"
                                    icon={<SaveOutlined />}
                                    loading={saving}
                                    className={styles.savePriceBtn}
                                    onClick={handleSavePrice}
                                >
                                    Lưu giá
                                </Button>
                            </div>
                        </section>

                        {/* 2. TÀI KHOẢN VÍ NHẬN TIỀN */}
                        <section className={styles.section}>
                            <div className={styles.sectionHeader}>
                                <WalletOutlined className={styles.sectionIcon} />
                                <span className={styles.sectionTitle}>Tài khoản nhận tiền (Ví số dư)</span>
                                {priceMode === "PAID" && useWallet && (
                                    <Tag icon={<CheckCircleFilled />} color="processing">
                                        Đang kích hoạt
                                    </Tag>
                                )}
                            </div>

                            <div className={styles.sectionBody}>
                                {priceMode === "FREE" ? (
                                    <Alert
                                        type="info"
                                        showIcon
                                        message="Khóa học miễn phí không phát sinh doanh thu nên không cần chọn ví nhận tiền."
                                    />
                                ) : (
                                    <div className={styles.walletCard}>
                                        <div className={styles.walletLeft}>
                                            <div className={styles.walletIconBox}>
                                                <WalletOutlined />
                                            </div>
                                            <div className={styles.walletInfo}>
                                                <div className={styles.walletTitle}>Ví số dư giảng viên</div>
                                                <div className={styles.walletDesc}>
                                                    Doanh thu từ mỗi lượt mua khóa học sẽ được cộng trực tiếp vào số dư
                                                    ví của bạn trên hệ thống.
                                                </div>
                                            </div>
                                        </div>

                                        <div className={styles.walletRight}>
                                            <Statistic
                                                title="Số dư khả dụng"
                                                value={walletBalance}
                                                suffix="₫"
                                                groupSeparator="."
                                                valueStyle={{
                                                    fontSize: 18,
                                                    fontWeight: 700,
                                                    color: "#2563eb",
                                                }}
                                            />
                                            <Switch
                                                checked={useWallet}
                                                onChange={(checked) => setUseWallet(checked)}
                                            />
                                        </div>
                                    </div>
                                )}
                            </div>
                        </section>
                    </div>
                </Spin>
            </div>
        </div>
    );
}