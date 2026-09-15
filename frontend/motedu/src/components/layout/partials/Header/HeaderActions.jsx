import { Link, useNavigate } from "react-router-dom";
import { Badge, Popover } from "antd";
import { ShoppingCartOutlined, UserOutlined } from "@ant-design/icons";
import Button from "~components/common/Button/Button";
import useAuth from "~hooks/useAuth";
import styles from "./Header.module.scss";

const items = [
    {name: "Khoá học của tôi", link: "/my-courses"}
]

const HeaderActions = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate("/");
  };


    const content = (
        <div className={styles.popupContainer}>
            <Link className={styles.popupHeader} to="/instructor/profile">

                <div className={styles.popupAvatar} >

                    {user?.avatar === null ?  <UserOutlined /> : <img src={user?.avatar} alt="anh ava"/>}
                </div>

                <div className={styles.popupInfo}>
                    <h3>{user?.fullName}</h3>
                    <h4>{user?.email}</h4>
                </div>
            </Link>

            <div className={styles.popupOptions}>
                {items.map(item => (
                    <Link key={item.link} to={item.link} className={styles.popupOptionItem}>
                        {item.name}
                    </Link>
                ))}
            </div>

            <div className={styles.popupOptions}>
                <button type="button" className={styles.popupOptionItem} onClick={handleLogout}>
                    Đăng xuất
                </button>
            </div>
        </div>
    )


  return (
    <div className={styles.actions}>
      <Link to="/cart" className={styles.cartIcon}>
        <Badge count={2} size="small" color="#f59e0b">
          <ShoppingCartOutlined />
        </Badge>
      </Link>

      {isAuthenticated ? (
            <Popover
                content={content}
                trigger={"hover"}
            >
                <Link to="/profile" className={styles.avatar}>
                    {user?.avatar === null ?  <UserOutlined /> : <img src={user.avatar} alt="anh ava"/>}

                </Link>
            </Popover>



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
