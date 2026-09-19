import { Link, useLocation, useNavigate } from "react-router-dom";
import { Menu } from "antd";
import {
  AppstoreOutlined,
  ReadOutlined,
  TeamOutlined,
  StarOutlined,
  CreditCardOutlined,
  SettingOutlined,
  UserOutlined,
  LogoutOutlined,
} from "@ant-design/icons";
import Logo from "~components/common/Logo/Logo";
import useAuth from "~hooks/useAuth";
import styles from "./InstructorSidebar.module.scss";

const NAV_ITEMS = [
  { key: "/instructor/dashboard", label: "Dashboard", icon: <AppstoreOutlined /> },
  { key: "/instructor/courses", label: "My Courses", icon: <ReadOutlined /> },
  { key: "/instructor/students", label: "Students", icon: <TeamOutlined /> },
  { key: "/instructor/reviews", label: "Reviews", icon: <StarOutlined /> },
  { key: "/instructor/payouts", label: "Payouts", icon: <CreditCardOutlined /> },
];

const InstructorSidebar = () => {
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const activeKey =
    NAV_ITEMS.find((item) => location.pathname.startsWith(item.key))?.key ??
    "/instructor/dashboard";

  const menuItems = NAV_ITEMS.map((item) => ({
    key: item.key,
    icon: item.icon,
    label: <Link to={item.key}>{item.label}</Link>,
  }));

  const handleLogout = async () => {
    await logout();
    navigate("/");
  };

  return (
    <div className={styles.wrapper}>
      <div className={styles.brand}>
        <Logo className={styles.logo} to="/instructor/dashboard" />
        <span className={styles.brandCaption}>Instructor Panel</span>
      </div>

      <span className={styles.groupLabel}>Menu</span>

      <Menu
        mode="inline"
        className={styles.menu}
        selectedKeys={[activeKey]}
        items={menuItems}
      />

      <Link to="/instructor/settings" className={styles.settingsLink}>
        <SettingOutlined />
        Settings
      </Link>

      <div className={styles.profile}>
        <div className={styles.avatar}>
          {user?.avatar ? <img src={user.avatar} alt={user?.fullName} /> : <UserOutlined />}
        </div>
        <div className={styles.profileInfo}>
          <h3>{user?.fullName ?? "Instructor"}</h3>
          <p>Giảng viên</p>
        </div>
        <button
          type="button"
          className={styles.logoutBtn}
          onClick={handleLogout}
          aria-label="Đăng xuất"
        >
          <LogoutOutlined />
        </button>
      </div>
    </div>
  );
};

export default InstructorSidebar;
