import React from 'react';
import ReactDOM from 'react-dom'
import styles from './Modal.module.scss'
import clsx from 'clsx';
import Button from '../Button/Button';
const Modal = ({ children, className, type= null,  color="", closeBtn=false, onClose}) => {
  const classes = clsx(styles.modal, className, styles[color]);
  

  return ReactDOM.createPortal(
    <div
     onClick={() => onClose()}
     className={clsx(styles.overlay, {
      [styles.left] : type === "left",
      [styles.center] : type === "center"
    })}
    >
      
      <div 
       onClick={(e) => e.stopPropagation()}
        className={classes}
      >
        {children}
      </div>
      {console.log(onClose)}
      {closeBtn && <Button onClick={() => onClose()} className={styles.close_btn} primary size="medium">Close</Button>}
    </div>,
    document.querySelector("#portal")
  );
};


export default Modal;