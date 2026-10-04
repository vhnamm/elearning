import {DeleteOutlined, MoreOutlined, PictureOutlined, StarFilled, TeamOutlined} from "@ant-design/icons"
import {Link} from "react-router-dom";
import {formatPrice} from "~/utils/course.util.js";
import styles from "./InstructorCourses.module.scss"



const STATUS_META = {
    PUBLISHED: { label: "Đã xuất bản", className: "statusPublished" },
    DRAFT: { label: "Bản nháp", className: "statusDraft" },
    PENDING_REVIEW: { label: "Đang chờ duyệt", className: "statusPending" },
    REJECTED: { label: "Bị từ chối", className: "statusRejected" },
};


const formatRating = (rating) => {
    if(rating === 0) return "--";

    return rating.toFixed(1);
}

const CourseCard = ({ course }) => {
    const status = STATUS_META[course.status] ?? STATUS_META.draft;

    return (
        <div className={styles.card}>
            <div className={styles.thumb}>
                {course.thumbnailUrl ? (
                    <img src={course.thumbnailUrl} alt={course.title} />
                ) : (
                    <PictureOutlined className={styles.thumbPlaceholder} />
                )}
                <span className={`${styles.statusBadge} ${styles[status.className]}`}>
                  {status.label}
                </span>
            </div>

            <div className={styles.body}>
                <h3 className={styles.title}>{course.title}</h3>

                <div className={styles.stats}>
                    <div className={styles.statWrap}>
                        <span>
                            <TeamOutlined /> {course.students ?? "--"}
                        </span>
                        <span className={styles.dot}>•</span>
                        <span>
                            <StarFilled /> {formatRating(course.rating)}
                        </span>
                    </div>


                    {/* Bản nháp chưa đặt giá thì formatPrice trả chuỗi rỗng, hiển thị "--" */}
                    <h3 className={styles.price}>{formatPrice(course?.price) || "--"}</h3>
                </div>

                <div className={styles.actions}>
                    <Link to={`/instructor/course/${course.id}/manage/overview`} className={styles.editBtn}>
                        Sửa
                    </Link>
                    <button type="button" className={styles.iconBtn} aria-label="Xoá khoá học">
                        <DeleteOutlined />
                    </button>
                    <button type="button" className={styles.iconBtn} aria-label="Thêm tuỳ chọn">
                        <MoreOutlined />
                    </button>
                </div>
            </div>
        </div>
    );
};

export default CourseCard