import React from "react";
import styles from "./Card.module.scss";
import clsx from "clsx";

const Card = ({ className, key, children }) => {
  const classes = clsx(styles.card, className);
  return (
    <div key={key} className={classes}>
      {children}
    </div>
  );
};

export default Card;
