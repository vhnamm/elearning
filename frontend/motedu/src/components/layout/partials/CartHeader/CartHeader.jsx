import React from "react";
import { Link } from "react-router-dom";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faBagShopping } from "@fortawesome/free-solid-svg-icons";
import styles from "./CartHeader.module.scss";
import clsx from "clsx";

const CartHeader = () => {
  return (
    <div className={clsx(styles.wrapper)}>
      <Link to={"/cart"} className={clsx(styles.cart_icon)}>
        <FontAwesomeIcon
          className={clsx(styles["cart-icon"])}
          icon={faBagShopping}
        />
      </Link>

      <div className={clsx(styles.quantity_wrap)}>
        <span>10</span>
      </div>
    </div>
  );
};

export default CartHeader;
