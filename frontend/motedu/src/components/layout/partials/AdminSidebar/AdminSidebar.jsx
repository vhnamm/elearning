import { useEffect, useState } from "react";
import clsx from "clsx";
import { Link, NavLink, useNavigate } from "react-router-dom";
import {
  ApartmentOutlined,
  AppstoreOutlined,
  ExportOutlined,
  LogoutOutlined,
  SafetyCertificateOutlined,
  SettingOutlined,
  TagOutlined,
  TeamOutlined,
} from "@ant-design/icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faGraduationCap } from "@fortawesome/free-solid-svg-icons";
import useAuth from "~hooks/useAuth";
import { countPendingCourses, PENDING_COURSES_CHANGED_EVENT } from "~services/moderation.service.js";
import styles from "./AdminSidebar.module.scss";

const NAV_ITEMS = [
  { to: "/admin/dashboard", label: "Tổng quan hệ thống", icon: <AppstoreOutlined /> },
  {
    to: "/admin/courses/approve",
    label: "Kiểm duyệt khóa học",
    icon: <SafetyCertificateOutlined />,
    badgeKey: "pendingCourses",
    badgeTone: "error",
  },
  { to: "/admin/topics/approve", label: "Kiểm duyệt chủ đề", icon: <TagOutlined /> },
  { to: "/admin/categories", label: "Quản lý danh mục", icon: <ApartmentOutlined /> },
  { to: "/admin/users", label: "Người dùng & Giảng viên", icon: <TeamOutlined /> },
  { to: "/admin/settings", label: "Cài đặt hệ thống", icon: <SettingOutlined /> },
];

const AdminSidebar = () => {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const [badges, setBadges] = useState({});

  useEffect(() => {
    const loadCounts = () => {
      countPendingCourses()
        .then((count) => setBadges((prev) => ({ ...prev, pendingCourses: count })))
        .catch(() => {});
    };

    loadCounts();
    window.addEventListener(PENDING_COURSES_CHANGED_EVENT, loadCounts);
    return () => window.removeEventListener(PENDING_COURSES_CHANGED_EVENT, loadCounts);
  }, []);

  const handleLogout = async () => {
    await logout();
    navigate("/");
  };

  return (
    <aside className={styles.wrapper}>
      <div className={styles.top}>
        <div className={styles.brand}>
          <Link to="/admin/dashboard" className={styles.brandLink}>
            <span className={styles.brandIcon}>
              <FontAwesomeIcon icon={faGraduationCap} />
            </span>
            <span className={styles.brandText}>
              <span className={styles.brandName}>MótEdu</span>
              <span className={styles.brandCaption}>Hệ Thống Quản Trị</span>
            </span>
          </Link>
          <span className={styles.roleTag}>ADMIN</span>
        </div>

        <span className={styles.groupLabel}>Điều hướng quản trị</span>

        <nav className={styles.nav}>
          {NAV_ITEMS.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => clsx(styles.navItem, isActive && styles.navItemActive)}
            >
              <span className={styles.navItemMain}>
                {item.icon}
                <span>{item.label}</span>
              </span>
              {badges[item.badgeKey] > 0 && (
                <span className={clsx(styles.badge, styles[`badge_${item.badgeTone}`])}>{badges[item.badgeKey]}</span>
              )}
            </NavLink>
          ))}
        </nav>
      </div>

      <div className={styles.bottom}>
        <Link to="/" className={styles.bottomLink}>
          <ExportOutlined />
          <span>Về giao diện học viên/giảng viên</span>
        </Link>
        <button type="button" className={clsx(styles.bottomLink, styles.logout)} onClick={handleLogout}>
          <LogoutOutlined />
          <span>Đăng xuất</span>
        </button>
      </div>
    </aside>
  );
};

export default AdminSidebar;
