import { Outlet } from "react-router-dom";
import Header from "../partials/Header/Header";
import Footer from "../partials/Footer/Footer";
import styles from "./MainLayout.module.scss";

const MainLayout = () => {
  return (
    <div className={styles.layout}>
      <Header />
      <div className={styles.content}>
        <Outlet />
      </div>
      <Footer />
    </div>
  );
};

export default MainLayout;
