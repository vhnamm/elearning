import Logo from "~components/common/Logo/Logo";
import HeaderNav from "./HeaderNav";
import HeaderSearch from "./HeaderSearch";
import HeaderActions from "./HeaderActions";
import styles from "./Header.module.scss";

const Header = () => (
  <header className={styles.wrapper}>
    <div className={styles.inner}>
      <Logo />

        <HeaderNav />
      <HeaderSearch />

      <HeaderActions />
    </div>
  </header>
);

export default Header;
