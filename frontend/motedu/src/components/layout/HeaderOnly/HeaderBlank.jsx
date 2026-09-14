import styles from "~layouts/partials/Header/Header.module.scss";
import Logo from "~components/common/Logo/Logo.jsx";
import HeaderNav from "~layouts/partials/Header/HeaderNav.jsx";
import HeaderSearch from "~layouts/partials/Header/HeaderSearch.jsx";
import HeaderActions from "~layouts/partials/Header/HeaderActions.jsx";

const HeaderBlank = () => (
    <header className={styles.wrapper}>
        <div className={styles.inner}>
            <Logo />

        </div>
    </header>
);

export default HeaderBlank;