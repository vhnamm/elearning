import {useRef, useState} from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { Form, Input, Checkbox, message } from "antd";
import {MailOutlined, LockOutlined, AccountBookOutlined, UserOutlined} from "@ant-design/icons";
import Button from "~components/common/Button/Button";
import GoogleIcon from "~components/common/GoogleIcon/GoogleIcon";
import { googleLoginUrl } from "~services/auth.service";
import {register} from "~services/auth.service";
import styles from "./Register.module.scss";
import OtpModal from "~components/OtpModal/OtpModal.jsx";

const Register = () => {
    const navigate = useNavigate();
    const [openModal, setOpenModal] = useState(false)
    const [resendLoading, setResendLoading] = useState(false)
    const [confirmLoading, setConfirmLoading] = useState(false)

    const formValuesRef = useRef(null);

    const handleSubmit = async (values) => {
        try {
            const res = await register({email: values.email, password: values.password, fullName: values.fullName})
            formValuesRef.current = values;
            setOpenModal(true)
            message.success(res.message || "Vui lòng kiểm tra mã OTP được gửi hòm thư của bạn")
        }catch (err){

        }
    }
    return (
        <>
            <OtpModal open={openModal} email={formValuesRef.current?.email} loading={confirmLoading} resendLoading={resendLoading}></OtpModal>
            <main className={styles.main}>
                <div className={styles.card}>
                    <h2>Đăng ký</h2>
                    <p className={styles.subtitle}>
                        Đăng ký để tiếp tục hành trình học tập của bạn.
                    </p>

                    <Form
                        layout="vertical"
                        requiredMark={false}
                        className={styles.form}
                        onFinish={handleSubmit}
                    >
                        <Form.Item
                            label="Email"
                            name="email"
                            rules={[
                                { required: true, message: "Vui lòng nhập email" },
                                { type: "email", message: "Email không hợp lệ" },
                            ]}
                        >
                            <Input
                                size="large"
                                prefix={<MailOutlined />}
                                placeholder="you@example.com"
                                autoComplete="email"
                            />
                        </Form.Item>

                        <Form.Item
                            label="Họ và tên"
                            name="fullName"
                            rules={[
                                { required: true, message: "Vui lòng nhập họ và tên" }
                            ]}
                        >
                            <Input
                                size="large"
                                prefix={<UserOutlined/>}
                                placeholder="you@example.com"
                                autoComplete="email"
                            />
                        </Form.Item>
                        <Form.Item
                            label="Mật khẩu"
                            name="password"
                            rules={[{ required: true, message: "Vui lòng nhập mật khẩu" }]}
                        >
                            <Input.Password
                                size="large"
                                prefix={<LockOutlined />}
                                placeholder="Nhập mật khẩu"
                                autoComplete="current-password"
                            />
                        </Form.Item>

                        <Form.Item
                            label="Nhập lại mật khẩu"
                            name="repassword"
                            rules={[{ required: true, message: "Vui lòng nhập mật khẩu" }]}
                        >
                            <Input.Password
                                size="large"
                                prefix={<LockOutlined />}
                                placeholder="Nhập mật khẩu"
                                autoComplete="current-password"
                            />
                        </Form.Item>


                        <Button
                            primary
                            rounded
                            size="large"
                            className={styles.submitBtn}
                            type="submit"
                            disabled={openModal}
                        >
                            Đăng ký
                        </Button>
                    </Form>

                    <div className={styles.divider}>
                        <span>hoặc</span>
                    </div>

                    <Button
                        outlined
                        rounded
                        size="large"
                        href={googleLoginUrl}
                        className={styles.googleBtn}
                        disabled={openModal}
                    >
                        <GoogleIcon /> Đăng nhập với Google
                    </Button>

                    <p className={styles.registerText}>
                        Đã có tài khoản? <Link to="/login">Đăng nhập</Link>
                    </p>
                </div>
            </main>

        </>
    );
};

export default Register;
