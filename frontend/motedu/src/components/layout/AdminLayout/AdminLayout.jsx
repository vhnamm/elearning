import { Outlet } from "react-router-dom";
import AdminSidebar from "../partials/AdminSidebar/AdminSidebar";
import HeaderAdmin from "../partials/HeaderAdmin/HeaderAdmin";
import styles from "./AdminLayout.module.scss";

const AdminLayout = () => {
  return (
    <div className={styles.layout}>
      <AdminSidebar />
      <div className={styles.main}>
        <HeaderAdmin />
        <main className={styles.content}>
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default AdminLayout;
