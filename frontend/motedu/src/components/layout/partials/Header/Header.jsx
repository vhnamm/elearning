import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { MenuOutlined, CloseOutlined } from "@ant-design/icons";
import Logo from "~components/common/Logo/Logo";
import HeaderNav from "./HeaderNav";
import HeaderSearch from "./HeaderSearch";
import HeaderActions from "./HeaderActions";
import useAuth from "~hooks/useAuth";
import styles from "./Header.module.scss";

const Header = () => {
  const [mobileOpen, setMobileOpen] = useState(false);
  const { isAuthenticated, user } = useAuth();

  useEffect(() => {
    document.body.style.overflow = mobileOpen ? "hidden" : "";
    return () => {
      document.body.style.overflow = "";
    };
  }, [mobileOpen]);

  useEffect(() => {
    const onResize = () => {
      if (window.innerWidth > 900) setMobileOpen(false);
    };
    window.addEventListener("resize", onResize);
    return () => window.removeEventListener("resize", onResize);
  }, []);

  const closeMobile = () => setMobileOpen(false);

  const teachingTo = user?.roles?.includes("INSTRUCTOR")
    ? "/instructor/courses"
    : "/teaching";

  return (
    <header className={styles.wrapper}>
      <div className={styles.inner}>
        <Logo />

        <div className={styles.desktopNav}>
          <HeaderNav />
        </div>

        <div className={styles.desktopSearch}>
          <HeaderSearch />
        </div>

        <div className={styles.rightCluster}>
          <HeaderActions hideAuthOnMobile onNavigate={closeMobile} />

          <button
            type="button"
            className={styles.menuToggle}
            aria-label={mobileOpen ? "Đóng menu" : "Mở menu"}
            aria-expanded={mobileOpen}
            onClick={() => setMobileOpen((open) => !open)}
          >
            {mobileOpen ? <CloseOutlined /> : <MenuOutlined />}
          </button>
        </div>
      </div>

      {mobileOpen && (
        <>
          <button
            type="button"
            className={styles.backdrop}
            aria-label="Đóng menu"
            onClick={closeMobile}
          />
          <div className={styles.mobilePanel}>
            <HeaderSearch />

            <nav className={styles.mobileNav}>
              <p className={styles.mobileLabel}>Khám phá</p>
              <Link to="/" className={styles.mobileLink} onClick={closeMobile}>
                Lập trình
              </Link>
              <Link to="/" className={styles.mobileLink} onClick={closeMobile}>
                Thiết kế
              </Link>
              <Link to="/" className={styles.mobileLink} onClick={closeMobile}>
                Kinh doanh
              </Link>
              <Link to="/" className={styles.mobileLink} onClick={closeMobile}>
                Ngoại ngữ
              </Link>

              {isAuthenticated && (
                <Link
                  to="/my-courses"
                  className={styles.mobileLink}
                  onClick={closeMobile}
                >
                  Khóa học của tôi
                </Link>
              )}

              <Link
                to={teachingTo}
                className={styles.mobileLink}
                onClick={closeMobile}
              >
                Giảng viên
              </Link>
            </nav>

            {!isAuthenticated && (
              <div className={styles.mobileAuth}>
                <Link
                  to="/login"
                  className={styles.mobileAuthGhost}
                  onClick={closeMobile}
                >
                  Đăng nhập
                </Link>
                <Link
                  to="/register"
                  className={styles.mobileAuthPrimary}
                  onClick={closeMobile}
                >
                  Đăng ký
                </Link>
              </div>
            )}
          </div>
        </>
      )}
    </header>
  );
};

export default Header;
