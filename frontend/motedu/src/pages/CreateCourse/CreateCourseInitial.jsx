import React, { useState, useEffect } from 'react';
import {useOutletContext} from "react-router-dom";
import {
    Form,
    Input,
    Select,
    Button,
    Card,
    Typography
} from 'antd';
import {
    ArrowLeftOutlined,
    ArrowRightOutlined,
    AppstoreOutlined,
    InfoCircleOutlined
} from '@ant-design/icons';
import styles from './CreateCourseInitial.module.scss';

const { Title, Paragraph } = Typography;

export default function CreateCourseInitial() {
    const { setPageTitle } = useOutletContext() || {};
    useEffect(() => {
        // Cập nhật text cho HeaderBlank khi vào trang này
        setPageTitle("Tạo khóa học mới");

        // (Tùy chọn) Reset khi rời khỏi trang
        return () => setHeaderTitle("");
    }, [setPageTitle]);

    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);

    const categories = [
        { value: 'it-dev', label: 'Lập trình & Công nghệ thông tin' },
        { value: 'design', label: 'Thiết kế đồ họa & Multimedia' },
        { value: 'business', label: 'Kinh doanh & Khởi nghiệp' },
        { value: 'marketing', label: 'Marketing số & Truyền thông' },
        { value: 'language', label: 'Ngoại ngữ' },
    ];

    const onFinish = (values) => {
        setLoading(true);
        console.log('Khởi tạo khóa học nháp:', values);
        setTimeout(() => {
            setLoading(false);
        }, 800);
    };

    return (
        <div className={styles.container}>
            <div className={styles.wrapper}>
                {/* Nút điều hướng */}
                <div className={styles.navRow}>
                    <Button
                        type="link"
                        icon={<ArrowLeftOutlined />}
                        className={styles.backBtn}
                    >
                        Quay lại Bảng điều khiển
                    </Button>
                </div>

                {/* Khung Form chính */}
                <Card className={styles.courseCard}>
                    <div className={styles.cardHeader}>
                        <Title level={2} className={styles.title}>
                            Đầu tiên, chúng tôi cần thông tin tổng quan về khóa học của bạn
                        </Title>
                        <Paragraph className={styles.subtitle}>
                            Trước khi đi vào cụ thể, bạn vui lòng điền tiêu đề và danh mục.
                        </Paragraph>
                    </div>

                    <Form form={form} layout="vertical" onFinish={onFinish}>
                        {/* Tiêu đề khóa học */}
                        <Form.Item
                            name="title"
                            className={styles.formItem}
                            label={
                                <span className={styles.fieldLabel}>
                                    Tiêu đề khóa học <span className={styles.requiredStar}>*</span>
                                </span>
                            }
                            rules={[
                                { required: true, message: 'Vui lòng nhập tiêu đề khóa học!' },
                                { max: 100, message: 'Tiêu đề không được vượt quá 100 ký tự!' }
                            ]}
                            extra={
                                <span className={styles.fieldNote}>
                                  <InfoCircleOutlined />
                                  Tiêu đề nên súc tích, nêu bật kết quả học tập (tối đa 100 ký tự). Có thể cập nhật lại trước khi xuất bản.
                                </span>
                            }
                        >
                            <Input
                                size="large"
                                maxLength={100}
                                showCount
                                placeholder="VD: Lập trình Spring Boot & Microservices..."
                                className={styles.inputField}
                            />
                        </Form.Item>

                        {/* Box Danh mục */}
                        <div className={styles.categoryBox}>
                            <div className={styles.categoryInfo}>
                                <div className={styles.categoryIcon}>
                                    <AppstoreOutlined />
                                </div>
                                <div>
                                    <div className={styles.categoryTitle}>Danh mục dự kiến ban đầu</div>
                                    <div className={styles.categoryDesc}>
                                        Giúp phân bổ gợi ý cấu trúc giảng dạy tối ưu theo lĩnh vực của bạn.
                                    </div>
                                </div>
                            </div>

                            <Form.Item
                                name="category"
                                noStyle
                                rules={[{ required: true, message: 'Vui lòng chọn danh mục!' }]}
                            >
                                <Select
                                    size="large"
                                    options={categories}
                                    className={styles.categorySelect}
                                    placeholder="Chọn danh mục phù hợp"
                                />
                            </Form.Item>
                        </div>

                        {/* Nút nộp / lưu nháp */}
                        <div className={styles.submitWrapper}>
                            <Button
                                type="primary"
                                htmlType="submit"
                                size="large"
                                loading={loading}
                                icon={<ArrowRightOutlined />}
                                className={styles.submitBtn}
                            >
                                Tiếp tục tạo nội dung & Lưu nháp
                            </Button>
                        </div>
                    </Form>
                </Card>
            </div>
        </div>
    );
}