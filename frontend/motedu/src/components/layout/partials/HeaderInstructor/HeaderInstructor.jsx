import { Link } from "react-router-dom";
import { Badge, Input, Popover } from "antd";
import { BellOutlined, PlusOutlined, SearchOutlined, UserOutlined } from "@ant-design/icons";
import useAuth from "~hooks/useAuth";
import styles from "./HeaderInstructor.module.scss";

const HeaderInstructor = () => {
  const { user } = useAuth();

  const popoverContent = (
    <div className={styles.popupContainer}>
      <Link className={styles.popupHeader} to="/instructor/profile">
        <div className={styles.popupAvatar}>
          {user?.avatar ? <img src={user.avatar} alt={user?.fullName} /> : <UserOutlined />}
        </div>
        <div className={styles.popupInfo}>
          <h3>{user?.fullName}</h3>
          <h4>{user?.email}</h4>
        </div>
      </Link>

      <div className={styles.popupOptions}>
        <Link className={styles.popupOptionItem} to="/">
          Xem trang học viên
        </Link>
        <Link className={styles.popupOptionItem} to="/instructor/profile">
          Hồ sơ giảng viên
        </Link>
      </div>
    </div>
  );

  return (
    <header className={styles.wrapper}>


      <div className={styles.actions}>
        <Link to="/instructor/courses/new" className={styles.createBtn}>
          <PlusOutlined />
          Tạo khoá học
        </Link>

        <button type="button" className={styles.iconBtn} aria-label="Thông báo">
          <Badge dot offset={[-2, 2]}>
            <BellOutlined />
          </Badge>
        </button>

        <Popover content={popoverContent} trigger="hover" placement="bottomRight">
          <Link to="/instructor/profile" className={styles.avatar}>
            {user?.avatar ? <img src={user.avatar} alt={user?.fullName} /> : <UserOutlined />}
          </Link>
        </Popover>
      </div>
    </header>
  );
};

export default HeaderInstructor;
