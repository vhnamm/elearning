import Button from "~components/common/Button/Button.jsx";
import styles from "./NotFound.module.scss";

const NotFound = () => {
    return (
        <main className={styles.main}>
            <img src="/404.png" alt="Không tìm thấy trang" className={styles.image} />
            <h1>404</h1>
            <p>
                Không tìm thấy trang bạn yêu cầu. Trang có thể đã bị di chuyển hoặc bạn không có quyền truy cập.
            </p>
            <Button primary rounded to="/">
                Về trang chủ
            </Button>
        </main>
    );
};

export default NotFound;
