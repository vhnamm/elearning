import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { Row, Col, Tabs, Input, Select, Pagination, Spin, Empty, Alert } from "antd";
import {
  SearchOutlined,
} from "@ant-design/icons";
import {getInstructorCourses} from "~services/course.service.js";
import styles from "./InstructorCourses.module.scss";
import useDebounce from "~hooks/useDebounce.js";
import CreateCourseCard from "~pages/InstructorCourses/CreateCourseCard.jsx";
import CourseCard from "~pages/InstructorCourses/CourseCard.jsx";

const PAGE_SIZE = 2;

const SORT_OPTIONS = [
  { value: "createdAt,desc", label: "Ngày tạo: Mới nhất" },
  { value: "createdAt,asc", label: "Ngày tạo: Cũ nhất" },
  { value: "price,asc", label: "Giá: Thấp đến cao" },
  { value: "price,desc", label: "Giá: Cao đến thấp" },
];

const TABS = [
    { key: "all", label: "Tất cả khoá học" },
    { key: "DRAFT", label: "Bản nháp" },
    { key: "PUBLISHED", label: "Đã xuất bản" },
    { key: "PENDING_REVIEW", label: "Đang chờ duyệt" },
];

const InstructorCourses = () => {
  // Nguồn sự thật cho filter/sort/page là URL, không phải useState,
  // để bookmark/share link, refresh (F5) và nút Back/Forward của trình duyệt hoạt động đúng.
  const [searchParams, setSearchParams] = useSearchParams();

  const activeTab = searchParams.get("status") ?? "all";
  const sortBy = searchParams.get("sort") ?? "createdAt,desc";
  const page = Number(searchParams.get("page") ?? 0);

  // searchText vẫn là useState riêng: cần phản hồi tức thì khi gõ,
  // chỉ đồng bộ vào URL sau khi debounce (tránh mỗi keystroke tạo 1 entry lịch sử).
  const [searchText, setSearchText] = useState(searchParams.get("q") ?? "");
  const debouncedKeyword = useDebounce(searchText, 500);

  const [coursePage, setCoursePage] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Đồng bộ debouncedKeyword vào URL (?q=...) và reset về trang 1.
  useEffect(() => {
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev);
      if (debouncedKeyword) next.set("q", debouncedKeyword);
      else next.delete("q");
      next.set("page", "0");
      return next;
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debouncedKeyword]);

  const handleTabChange = (key) => {
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev);
      if (key === "all") next.delete("status");
      else next.set("status", key);
      next.set("page", "0");
      return next;
    });
  };

  const handleSortChange = (value) => {
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev);
      next.set("sort", value);
      next.set("page", "0");
      return next;
    });
  };

  const handlePageChange = (nextPage) => {
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev);
      next.set("page", String(nextPage - 1));
      return next;
    });
  };

  useEffect(() => {
    let cancelled = false;

    const fetchCourses = async () => {
      setLoading(true);
      setError(null);
      try {
        const result = await getInstructorCourses({
          keyword: debouncedKeyword || undefined,
          status: activeTab === "all" ? undefined : activeTab,
          sort: sortBy,
          page,
          size: PAGE_SIZE,
        });
        if (!cancelled) setCoursePage(result);
      } catch (err) {
        if (!cancelled) setError(err);
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    fetchCourses();
    return () => {
      cancelled = true;
    };
  }, [activeTab, debouncedKeyword, sortBy, page]);


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
          onChange={handleSortChange}
          options={SORT_OPTIONS}
        />
      </div>

      <Tabs
        className={styles.tabs}
        activeKey={activeTab}
        onChange={handleTabChange}
        items={TABS}
      />

      {error && (
        <Alert
          type="error"
          showIcon
          title="Không thể tải danh sách khoá học. Vui lòng thử lại."
          style={{ marginBottom: 16 }}
        />
      )}

        <Spin spinning={loading}>
            <div style={{ minHeight: "350px" }}> {/* Giữ chiều cao tối thiểu cho hiệu ứng loading không bị giật */}
                <Row className={styles.coursesItemRow} gutter={[20, 20]}>
                    {activeTab === "all" && (
                        <Col xs={24} sm={12} lg={8} className={styles["col-5-items"]}>
                            <CreateCourseCard />
                        </Col>
                    )}

                    {coursePage?.content?.map((course) => (
                        <Col key={course.id} xs={24} sm={12} lg={8} className={styles["col-5-items"]}>
                            <CourseCard course={course} />
                        </Col>
                    ))}
                </Row>


                {!loading && !error && coursePage?.content?.length === 0 && activeTab !== "all" && (
                    <Empty description="Chưa có khoá học nào" style={{ margin: "40px 0" }} />
                )}
            </div>
        </Spin>

      {coursePage?.totalElements > PAGE_SIZE && (
        <div className={styles.pagination}>
          <Pagination
            current={page + 1}
            pageSize={PAGE_SIZE}
            total={coursePage.totalElements}
            onChange={handlePageChange}
            showSizeChanger={false}
          />
        </div>
      )}
    </div>
  );
};

export default InstructorCourses;
