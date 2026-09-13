import { Link } from "react-router-dom";
import { Badge } from "antd";
import { ShoppingCartOutlined, UserOutlined } from "@ant-design/icons";
import Button from "~components/common/Button/Button";
import useAuth from "~hooks/useAuth";
import styles from "./Header.module.scss";

const HeaderActions = () => {
  const { isAuthenticated } = useAuth();

  return (
    <div className={styles.actions}>
      <Link to="/cart" className={styles.cartIcon}>
        <Badge count={2} size="small" color="#f59e0b">
          <ShoppingCartOutlined />
        </Badge>
      </Link>

      {isAuthenticated ? (
        <Link to="/profile" className={styles.avatar}>
          <UserOutlined />
        </Link>
      ) : (
        <>
          <Button outlined size="medium" to="/login">
            Đăng nhập
          </Button>
          <Button primary size="medium" to="/register">
            Đăng ký
          </Button>
        </>
      )}
    </div>
  );
};

export default HeaderActions;
