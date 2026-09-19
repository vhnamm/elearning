import { Outlet } from "react-router-dom";
import { Row, Col } from "antd";
import InstructorSidebar from "../partials/InstructorSidebar/InstructorSidebar";
import HeaderInstructor from "../partials/HeaderInstructor/HeaderInstructor";
import styles from "./InstructorLayout.module.scss";

const InstructorLayout = () => {
  return (
    <Row className={styles.layout} gutter={0}>
      <Col xs={24} md={7} lg={5} xl={3} className={styles.sider}>
        <InstructorSidebar />
      </Col>

      <Col xs={24} md={17} lg={19} xl={21} className={styles.main}>
        <HeaderInstructor />
        <div className={styles.content}>
          <Outlet />
        </div>
      </Col>
    </Row>
  );
};

export default InstructorLayout;
