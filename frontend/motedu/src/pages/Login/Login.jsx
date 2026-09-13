import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { Form, Input, Checkbox, message } from "antd";
import { MailOutlined, LockOutlined } from "@ant-design/icons";
import Button from "~components/common/Button/Button";
import GoogleIcon from "~components/common/GoogleIcon/GoogleIcon";
import { googleLoginUrl } from "~services/auth.service";
import useAuth from "~hooks/useAuth";
import styles from "./Login.module.scss";

const Login = () => {
  const [submitting, setSubmitting] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const from = location.state?.from?.pathname || "/";

  const handleFinish = async (values) => {
    setSubmitting(true);
    try {
      await login({ email: values.email, password: values.password });
      message.success("Đăng nhập thành công");
      navigate(from, { replace: true });
    } catch (error) {
      const msg =
        error?.response?.data?.message || "Email hoặc mật khẩu không đúng";
      message.error(msg);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <main className={styles.main}>
      <div className={styles.card}>
        <h2>Đăng nhập</h2>
        <p className={styles.subtitle}>
          Chào mừng bạn quay lại! Vui lòng nhập thông tin tài khoản.
        </p>

        <Form
          layout="vertical"
          onFinish={handleFinish}
          disabled={submitting}
          requiredMark={false}
          className={styles.form}
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

          <div className={styles.formRow}>
            <Form.Item name="remember" valuePropName="checked" noStyle>
              <Checkbox>Ghi nhớ đăng nhập</Checkbox>
            </Form.Item>
            <Link to="/forgot-password" className={styles.link}>
              Quên mật khẩu?
            </Link>
          </div>

          <Button
            primary
            rounded
            size="large"
            className={styles.submitBtn}
            loading={submitting}
            type="submit"
          >
            Đăng nhập
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
        >
          <GoogleIcon /> Đăng nhập với Google
        </Button>

        <p className={styles.registerText}>
          Chưa có tài khoản? <Link to="/register">Đăng ký ngay</Link>
        </p>
      </div>
    </main>
  );
};

export default Login;
