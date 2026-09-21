import {Link} from "react-router-dom";
import {PlusOutlined} from "@ant-design/icons";
import styles from "./InstructorCourses.module.scss"

const CreateCourseCard = () => (
    <Link to="/instructor/courses/new" className={styles.createCard}>
    <span className={styles.createIcon}>
      <PlusOutlined />
    </span>
        <h3>Tạo khoá học mới</h3>
        <p>Bắt đầu chia sẻ kiến thức của bạn.</p>
    </Link>
);

export default CreateCourseCard