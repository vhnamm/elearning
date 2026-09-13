import clsx from "clsx";
import { faSpinner } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import styles from "./Button.module.scss";
import { Link } from "react-router-dom";

function Button({
  children,
  href,
  primary,
  rounded,
  outlined,
  loading = false,
  disabled = false,
  className,
  to=null,
  size,
  ...props
}) {
  let Component = "button";

  if (href) {
    Component = "a";
  } else if (to) {
    Component = Link; 
    props.to = to;    
  }
  const classes = clsx(styles.btn, className, styles[size], {
    [styles.primary]: primary,
    [styles.rounded]: rounded,
    [styles.outlined]: outlined,
    [styles.disabled]: disabled,
    [styles.loading]: loading,
  });

  if (disabled || loading) {
    delete props.onClick;
  }
  return (
    <Component {...props} href={href} className={classes}>
      <span className={styles.btn_content}>{children}</span>
      {loading && (
        <span className={styles.spinner}>
          <FontAwesomeIcon icon={faSpinner} />
        </span>
      )}
    </Component>
  );
}

export default Button;
