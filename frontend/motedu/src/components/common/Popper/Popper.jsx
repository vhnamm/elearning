import React from "react";
import clsx from "clsx";
import styles from "./Popper.module.scss";
import ReactDOM from 'react-dom'
const Popper = ({ children, className, hide=false}) => {
  return <div className={clsx(styles.wrapper, className, hide? "hide" : "")}>{children}</div>;
};

export default Popper;
