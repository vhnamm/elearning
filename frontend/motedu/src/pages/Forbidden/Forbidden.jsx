import { Link } from "react-router-dom";
import Button from "~components/common/Button/Button.jsx";
import styles from "./Forbidden.module.scss";

const Forbidden = () => {
    return (
        <main className={styles.main}>
            <div className={styles.card}>
                <h1>403</h1>
                <h2>Bạn không có quyền truy cập trang này</h2>
                <p>Tài khoản của bạn không đủ quyền để xem nội dung này.</p>
                <Button primary rounded to="/">
                    Về trang chủ
                </Button>
            </div>
        </main>
    );
};

export default Forbidden;
