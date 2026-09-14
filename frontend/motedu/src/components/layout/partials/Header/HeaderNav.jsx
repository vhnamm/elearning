import { useState } from "react";
import clsx from "clsx";
import { NavLink } from "react-router-dom";
import useAuth from "~hooks/useAuth";
import styles from "./Header.module.scss";

// TODO: thay bằng danh mục khóa học thật (gọi API danh mục) khi có API.
const PLACEHOLDER_CATEGORIES = ["Lập trình", "Thiết kế", "Kinh doanh", "Ngoại ngữ"];

// Mỗi mục có hành vi riêng (dropdown, điều hướng theo trạng thái đăng nhập...)
// nên viết tường minh từng mục, không gom chung thành 1 mảng {label, to}.
const HeaderNav = () => {
  const { isAuthenticated, user } = useAuth();
  const [exploreOpen, setExploreOpen] = useState(false);

  return (
    <nav className={styles.nav}>
      <div
        className={styles.exploreWrap}
        onMouseEnter={() => setExploreOpen(true)}
        onMouseLeave={() => setExploreOpen(false)}
      >
        <span className={styles.navItem}>Khám phá</span>

        {exploreOpen && (
          <div className={styles.exploreDropdown}>
            {PLACEHOLDER_CATEGORIES.map((category) => (
              <a key={category} href="#" className={styles.exploreItem}>
                {category}
              </a>
            ))}
          </div>
        )}
      </div>

      {isAuthenticated && (
        <NavLink
          to="/my-courses"
          className={clsx(styles.navItem)}
        >
          Khóa học của tôi
        </NavLink>
      )}

      <NavLink
        to={user?.roles.includes('INSTRUCTOR') ? "/instructor/courses" : "/teaching"}
        className={clsx(styles.navItem)}
      >
        Giảng viên
      </NavLink>
    </nav>
  );
};

export default HeaderNav;
