import React from 'react';
import { ReadOutlined } from '@ant-design/icons';
import styles from './Logo.module.scss';
import {faGraduationCap} from "@fortawesome/free-solid-svg-icons";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome"; // hoặc import file chứa class của bạn

export default function LogoNoLink({ light = false }) {
    return (
        <div className={styles.logo}>
            <div className={styles.icon}>
                <FontAwesomeIcon icon={faGraduationCap} />
            </div>
            <span className={`${styles.text} ${light ? styles.light : ''}`}>
        Mót<span className={styles.accent}>Edu.</span>
      </span>
        </div>
    );
}