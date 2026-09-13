import clsx from "clsx";
import { Link } from "react-router-dom";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faGraduationCap } from "@fortawesome/free-solid-svg-icons";
import styles from "./Logo.module.scss";

const Logo = ({ className, to = "/", variant = "dark" }) => (
  <Link to={to} className={clsx(styles.logo, className)}>
    <span className={styles.icon}>
      <FontAwesomeIcon icon={faGraduationCap} />
    </span>
    <span className={clsx(styles.text, { [styles.light]: variant === "light" })}>
      Mót<span className={styles.accent}>Edu</span>
      <span className={styles.accent}>.</span>
    </span>
  </Link>
);

export default Logo;
