import { BellOutlined, SearchOutlined, UserOutlined } from "@ant-design/icons";
import useAuth from "~hooks/useAuth";
import styles from "./HeaderAdmin.module.scss";

const HeaderAdmin = () => {
  const { user } = useAuth();

  return (
    <header className={styles.wrapper}>
      <div className={styles.search}>
        <SearchOutlined className={styles.searchIcon} />
        <input type="text" placeholder="Tìm kiếm khóa học, giảng viên, đơn duyệt..." />
      </div>

      <div className={styles.actions}>
        <button type="button" className={styles.iconBtn} aria-label="Thông báo">
          <BellOutlined />
          <span className={styles.notifyDot}></span>
        </button>

        <div className={styles.divider}></div>

        <div className={styles.profile}>
          <div className={styles.profileInfo}>
            <span className={styles.profileName}>{user?.fullName ?? "Admin Quản trị viên"}</span>
            <div className={styles.profileRole}>
              <span className={styles.roleDot}></span>
              <span>Super Admin</span>
            </div>
          </div>
          <div className={styles.avatar}>
            {user?.avatar ? <img src={user.avatar} alt={user?.fullName} /> : <UserOutlined />}
          </div>
        </div>
      </div>
    </header>
  );
};

export default HeaderAdmin;
