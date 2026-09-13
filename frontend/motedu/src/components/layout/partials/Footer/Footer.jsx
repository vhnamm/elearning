import clsx from "clsx";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faFacebook,
  faInstagram,
  faYoutube,
} from "@fortawesome/free-brands-svg-icons";
import Logo from "~components/common/Logo/Logo";
import styles from "./Footer.module.scss";

const LINK_GROUPS = [
  {
    title: "Về MótEdu",
    links: ["Giới thiệu", "Tuyển dụng", "Điều khoản sử dụng", "Chính sách bảo mật"],
  },
  {
    title: "Khóa học",
    links: ["Khám phá khóa học", "Giảng viên", "Danh mục", "Ưu đãi"],
  },
  {
    title: "Hỗ trợ",
    links: ["Trung tâm trợ giúp", "Câu hỏi thường gặp", "Liên hệ hỗ trợ"],
  },
];

const Footer = () => {
  return (
    <footer className={clsx(styles.wrapper)}>
      <div className={clsx(styles.footer)}>
        <div className={clsx(styles.brand_wrap)}>
          <Logo variant="light" className={styles.logo} />
          <p>Học mọi lúc, mọi nơi cùng MótEdu.</p>
          <div className={clsx(styles.social_wrap)}>
            <a href="#" aria-label="Facebook" className={styles.icon}>
              <FontAwesomeIcon icon={faFacebook} />
            </a>
            <a href="#" aria-label="Instagram" className={styles.icon}>
              <FontAwesomeIcon icon={faInstagram} />
            </a>
            <a href="#" aria-label="YouTube" className={styles.icon}>
              <FontAwesomeIcon icon={faYoutube} />
            </a>
          </div>
        </div>

        <div className={clsx(styles.links_wrap)}>
          {LINK_GROUPS.map((group) => (
            <div key={group.title} className={clsx(styles.item_wrap)}>
              <h3>{group.title}</h3>
              <ul>
                {group.links.map((link) => (
                  <li key={link}>
                    <a href="#">{link}</a>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>
      </div>

      <div className={clsx(styles.bottom_bar)}>
        <span>© {new Date().getFullYear()} MótEdu. All rights reserved.</span>
      </div>
    </footer>
  );
};

export default Footer;
