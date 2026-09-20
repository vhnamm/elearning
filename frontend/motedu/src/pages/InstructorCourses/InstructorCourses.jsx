import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { Row, Col, Tabs, Input, Select } from "antd";
import {
  StarFilled,
  TeamOutlined,
  PictureOutlined,
  DeleteOutlined,
  MoreOutlined,
  PlusOutlined,
  SearchOutlined,
} from "@ant-design/icons";
import styles from "./InstructorCourses.module.scss";

// TODO: thay bằng dữ liệu thật từ API (GET /instructor/courses)
// priceValue/createdAt là dữ liệu thô để sort, price là chuỗi đã format để hiển thị
const DUMMY_COURSES = [
  {
    id: 1,
    title: "Lập trình Web Cơ bản với HTML, CSS & JS",
    thumbnail: null,
    status: "published",
    students: 850,
    rating: 4.8,
    price: "1.200.000đ",
    priceValue: 1200000,
    createdAt: "2026-06-10",
  },
  {
    id: 2,
    title: "ReactJS Thực chiến cho Người mới",
    thumbnail: null,
    status: "draft",
    students: null,
    rating: null,
    price: "0đ",
    priceValue: 0,
    createdAt: "2026-08-02",
  },
  {
    id: 3,
    title: "NodeJS & Express: Xây dựng API từ A-Z",
    thumbnail: null,
    status: "pending",
    students: null,
    rating: null,
    price: "890.000đ",
    priceValue: 890000,
    createdAt: "2026-07-18",
  },
  {
    id: 4,
    title: "Spring Boot cho Người mới bắt đầu",
    thumbnail: null,
    status: "published",
    students: 412,
    rating: 4.6,
    price: "1.500.000đ",
    priceValue: 1500000,
    createdAt: "2026-05-01",
  },
  {
    id: 5,
    title: "Thiết kế UI/UX với Figma",
    thumbnail: null,
    status: "draft",
    students: null,
    rating: null,
    price: "0đ",
    priceValue: 0,
    createdAt: "2026-09-05",
  },
  {
    id: 6,
    title: "Docker & Kubernetes cho Developer",
    thumbnail: null,
    status: "pending",
    students: null,
    rating: null,
    price: "990.000đ",
    priceValue: 990000,
    createdAt: "2026-04-20",
  },
];

const SORT_OPTIONS = [
  { value: "newest", label: "Ngày tạo: Mới nhất" },
  { value: "oldest", label: "Ngày tạo: Cũ nhất" },
  { value: "price_asc", label: "Giá: Thấp đến cao" },
  { value: "price_desc", label: "Giá: Cao đến thấp" },
];

const SORTERS = {
  newest: (a, b) => new Date(b.createdAt) - new Date(a.createdAt),
  oldest: (a, b) => new Date(a.createdAt) - new Date(b.createdAt),
  price_asc: (a, b) => a.priceValue - b.priceValue,
  price_desc: (a, b) => b.priceValue - a.priceValue,
};

const STATUS_META = {
  published: { label: "Đã xuất bản", className: "statusPublished" },
  draft: { label: "Bản nháp", className: "statusDraft" },
  pending: { label: "Đang chờ duyệt", className: "statusPending" },
};

const TABS = [
  { key: "all", label: "Tất cả khoá học" },
  { key: "draft", label: "Bản nháp" },
  { key: "published", label: "Đã xuất bản" },
  { key: "pending", label: "Đang chờ duyệt" },
];

const CourseCard = ({ course }) => {
  const status = STATUS_META[course.status];

  return (
    <div className={styles.card}>
      <div className={styles.thumb}>
        {course.thumbnail ? (
          <img src={course.thumbnail} alt={course.title} />
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
          <span>
            <TeamOutlined /> {course.students ?? "--"}
          </span>
          <span className={styles.dot}>•</span>
          <span>
            <StarFilled /> {course.rating ?? "--"}
          </span>
          <span className={styles.dot}>•</span>
          <span>{course.price}</span>
        </div>

        <div className={styles.actions}>
          <Link to={`/instructor/courses/${course.id}/edit`} className={styles.editBtn}>
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

const CreateCourseCard = () => (
  <Link to="/instructor/courses/new" className={styles.createCard}>
    <span className={styles.createIcon}>
      <PlusOutlined />
    </span>
    <h3>Tạo khoá học mới</h3>
    <p>Bắt đầu chia sẻ kiến thức của bạn.</p>
  </Link>
);

const InstructorCourses = () => {
  const [activeTab, setActiveTab] = useState("all");
  const [searchText, setSearchText] = useState("");
  const [sortBy, setSortBy] = useState("newest");

  const filteredCourses = useMemo(() => {
    let result = DUMMY_COURSES;

    if (activeTab !== "all") {
      result = result.filter((course) => course.status === activeTab);
    }

    const keyword = searchText.trim().toLowerCase();
    if (keyword) {
      result = result.filter((course) =>
        course.title.toLowerCase().includes(keyword)
      );
    }

    return [...result].sort(SORTERS[sortBy]);
  }, [activeTab, searchText, sortBy]);

  return (
    <div className={styles.wrapper}>
      <div className={styles.header}>
        <h1>Khoá học của tôi</h1>
      </div>

      <div className={styles.toolbar}>
        <Input
          className={styles.searchInput}
          placeholder="Tìm kiếm khoá học theo tên..."
          prefix={<SearchOutlined />}
          value={searchText}
          onChange={(e) => setSearchText(e.target.value)}
          allowClear
        />
        <Select
          className={styles.sortSelect}
          value={sortBy}
          onChange={setSortBy}
          options={SORT_OPTIONS}
        />
      </div>

      <Tabs
        className={styles.tabs}
        activeKey={activeTab}
        onChange={setActiveTab}
        items={TABS}
      />

      <Row gutter={[20, 20]}>

          {activeTab === "all" && (
              <Col xs={24} sm={12} lg={8}
                   className={styles["col-5-items"]}
              >
                  <CreateCourseCard />
              </Col>
          )}

        {filteredCourses.map((course) => (
          <Col key={course.id} xs={24} sm={12} lg={8}
               className={styles["col-5-items"]}
          >
            <CourseCard course={course} />
          </Col>
        ))}


      </Row>
    </div>
  );
};

export default InstructorCourses;
