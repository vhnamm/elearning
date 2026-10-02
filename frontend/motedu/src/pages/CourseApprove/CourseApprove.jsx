import { useCallback, useEffect, useRef, useState } from "react";
import clsx from "clsx";
import { Alert, Empty, Modal, Spin, message } from "antd";
import {
  BookOutlined,
  CheckCircleFilled,
  CheckCircleOutlined,
  ClockCircleOutlined,
  CloseCircleOutlined,
  CloseOutlined,
  DownOutlined,
  FileTextOutlined,
  PictureOutlined,
  ReadOutlined,
  ReloadOutlined,
  SendOutlined,
  ToolOutlined,
  WarningOutlined,
} from "@ant-design/icons";
import {
  approveCourse,
  getCourseForReview,
  getErrorMessage,
  getPendingCourses,
  getRejectReasons,
  notifyPendingCoursesChanged,
  rejectCourse,
} from "~services/moderation.service.js";
import styles from "./CourseApprove.module.scss";

const PAGE_SIZE = 20;
const MIN_REASON_LENGTH = 30;

const LEVEL_LABELS = {
  BEGINNER: "Cơ bản",
  INTERMEDIATE: "Trung cấp",
  ADVANCED: "Nâng cao",
};

const AVATAR_TONES = ["primary", "secondary", "tertiary", "primaryDark", "tint"];

const REJECT_TEMPLATES = [
  {
    label: "Thiếu câu hỏi Quiz",
    category: "INCOMPLETE_CONTENT",
    text: "Bài trắc nghiệm hiện chưa đủ số lượng câu hỏi. Theo quy định phát hành của MótEdu, bài kiểm tra chương yêu cầu tối thiểu 5 câu hỏi để đảm bảo đánh giá năng lực học viên.",
  },
  {
    label: "Video mờ / lệch tiếng",
    category: "AUDIO_VIDEO_QUALITY",
    text: "Một số video có độ phân giải không đồng nhất (có đoạn bị mờ dưới 720p) và âm thanh thu âm bị rè ở dải tần cao. Vui lòng xuất lại video và master lại âm thanh chuẩn -14 LUFS.",
  },
  {
    label: "Link tài liệu hỏng",
    category: "INCOMPLETE_CONTENT",
    text: "Đường link tải tài liệu/project mẫu đính kèm đang trả về lỗi 404 (Not Found). Vui lòng cập nhật lại liên kết hoặc đính kèm file nén trực tiếp.",
  },
];

const formatPrice = (value) => {
  if (value == null) return "Chưa đặt giá";
  const amount = Number(value);
  return amount === 0 ? "Miễn phí" : `${amount.toLocaleString("vi-VN")} ₫`;
};

const formatShortTime = (iso) => {
  if (!iso) return "--";
  const d = new Date(iso);
  const pad = (n) => String(n).padStart(2, "0");
  return `${pad(d.getHours())}:${pad(d.getMinutes())} ${pad(d.getDate())}/${pad(d.getMonth() + 1)}`;
};

const formatDate = (iso) => (iso ? new Date(iso).toLocaleDateString("vi-VN") : "--");

const formatTime = (iso) => (iso ? new Date(iso).toLocaleTimeString("vi-VN", { hour12: false }) : "--");

const getInitial = (name) => (name?.trim() ? name.trim().split(" ").pop().charAt(0).toUpperCase() : "?");

const getAvatarTone = (id) => AVATAR_TONES[Math.abs(Number(id) || 0) % AVATAR_TONES.length];

const Avatar = ({ id, name, avatar, className }) => (
  <span className={clsx(className, styles[`avatar_${getAvatarTone(id)}`])}>
    {avatar ? <img src={avatar} alt={name} /> : getInitial(name)}
  </span>
);

const Thumbnail = ({ src, alt, className }) => (
  <div className={className}>
    {src ? (
      <img src={src} alt={alt} />
    ) : (
      <span className={styles.thumbPlaceholder}>
        <PictureOutlined />
      </span>
    )}
  </div>
);

const QueueCard = ({ course, active, onSelect }) => (
  <div className={clsx(styles.queueCard, active && styles.queueCardActive)} onClick={() => onSelect(course.id)}>
    <span className={clsx(styles.statusBadge, styles.queueStatus)}>
      <span className={styles.statusDot}></span>
      Chờ duyệt
    </span>

    <div className={styles.queueBody}>
      <Thumbnail src={course.thumbnailUrl} alt={course.title} className={styles.queueThumb} />
      <div className={styles.queueInfo}>
        <span className={clsx(styles.queueCategory, active && styles.queueCategoryActive)}>
          {course.categoryName ?? "Chưa phân loại"}
        </span>
        <h4 className={styles.queueTitle}>{course.title ?? "(Chưa đặt tên)"}</h4>
      </div>
    </div>

    <div className={styles.queueFooter}>
      <div className={styles.queueInstructor}>
        <Avatar
          id={course.instructorId}
          name={course.instructorName}
          avatar={course.instructorAvatar}
          className={styles.avatarXs}
        />
        <span className={styles.queueInstructorName}>{course.instructorName ?? "Không rõ"}</span>
      </div>
      <div className={styles.queueMeta}>
        <span className={styles.queuePrice}>{formatPrice(course.price)}</span>
        <span className={styles.queueTime}>
          <ClockCircleOutlined /> {formatShortTime(course.submittedAt)}
        </span>
      </div>
    </div>
  </div>
);

const RejectModal = ({ course, reasons, submitting, onClose, onSubmit }) => {
  const [category, setCategory] = useState("");
  const [reason, setReason] = useState("");
  const [error, setError] = useState("");

  const applyTemplate = (template) => {
    setCategory(template.category);
    setReason(template.text);
    setError("");
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!category) {
      setError("Vui lòng chọn nhóm lý do từ chối.");
      return;
    }
    if (reason.trim().length < MIN_REASON_LENGTH) {
      setError(`Vui lòng nhập lý do cụ thể (tối thiểu ${MIN_REASON_LENGTH} ký tự) để hỗ trợ giảng viên sửa đổi.`);
      return;
    }
    setError("");
    onSubmit({ reasonCategory: category, feedback: reason.trim() }).catch((err) =>
      setError(getErrorMessage(err, "Không thể từ chối khóa học. Vui lòng thử lại."))
    );
  };

  return (
    <div className={styles.modalOverlay} onClick={submitting ? undefined : onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <div className={styles.modalHeader}>
          <div className={styles.modalHeaderTitle}>
            <WarningOutlined />
            <span>Từ chối duyệt khóa học</span>
          </div>
          <button type="button" className={styles.modalClose} onClick={onClose} disabled={submitting}>
            <CloseOutlined />
          </button>
        </div>

        <form className={styles.modalBody} onSubmit={handleSubmit}>
          <div className={styles.modalCourse}>
            <span className={styles.fieldLabelMuted}>Khóa học đang thẩm định:</span>
            <p className={styles.modalCourseTitle}>{course.title}</p>
            {course.instructor && (
              <span className={styles.modalCourseInstructor}>
                Giảng viên nhận phản hồi: {course.instructor.fullName} ({course.instructor.email})
              </span>
            )}
          </div>

          <div className={styles.field}>
            <label className={styles.fieldLabel}>
              Nhóm lý do từ chối <span className={styles.required}>*</span>
            </label>
            <div className={styles.selectWrapper}>
              <select value={category} onChange={(e) => setCategory(e.target.value)}>
                <option value="" disabled>
                  -- Chọn nhóm nguyên nhân cần khắc phục --
                </option>
                {reasons.map((item) => (
                  <option key={item.value} value={item.value}>
                    {item.label}
                  </option>
                ))}
              </select>
              <DownOutlined className={styles.selectIcon} />
            </div>
          </div>

          <div className={styles.field}>
            <div className={styles.fieldLabelRow}>
              <label className={styles.fieldLabel}>
                Chi tiết yêu cầu chỉnh sửa gửi Giảng viên <span className={styles.required}>*</span>
              </label>
              <span className={styles.fieldHint}>
                {reason.trim().length}/{MIN_REASON_LENGTH} ký tự tối thiểu
              </span>
            </div>
            <textarea
              rows={4}
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              placeholder="Ví dụ: Chương 1 - Bài 2: Âm lượng giọng nói bị rè từ phút 03:15. Bài trắc nghiệm Chương 1 mới có 2 câu hỏi, chưa đạt yêu cầu tối thiểu 5 câu theo quy định MótEdu..."
            />
            <p className={styles.fieldHint}>
              Nội dung này sẽ được tự động gửi qua Email và Notification Trung tâm Giảng viên.
            </p>
          </div>

          <div className={styles.templates}>
            <span className={styles.fieldLabelMuted}>Mẫu ghi chú nhanh:</span>
            {REJECT_TEMPLATES.map((template) => (
              <button
                key={template.label}
                type="button"
                className={styles.templateBtn}
                onClick={() => applyTemplate(template)}
              >
                {template.label}
              </button>
            ))}
          </div>

          {error && <p className={styles.formError}>{error}</p>}

          <div className={styles.modalActions}>
            <button type="button" className={styles.btnSecondary} onClick={onClose} disabled={submitting}>
              Hủy thao tác
            </button>
            <button type="submit" className={styles.btnDanger} disabled={submitting}>
              <SendOutlined />
              {submitting ? "Đang gửi..." : "Xác nhận Từ chối & Gửi Email"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

const CourseDetail = ({ course, onClose, onReject, onApprove, acting }) => {
  const instructor = course.instructor;
  const outcomes = course.learningOutcomes ?? [];
  const skills = course.requiredSkills ?? [];
  const topics = course.topics ?? [];

  return (
    <>
      <div className={styles.actionBar}>
        <div className={styles.actionBarLeft}>
          <button
            type="button"
            className={styles.btnBack}
            onClick={onClose}
            aria-label="Đóng chi tiết"
            title="Đóng chi tiết, quay lại hàng đợi"
          >
            <CloseOutlined />
          </button>
          <span className={styles.courseCode}>CRS-ID: #{course.id}</span>
        </div>
        <div className={styles.actionButtons}>
          <button type="button" className={styles.btnReject} onClick={onReject} disabled={acting}>
            <CloseCircleOutlined className={styles.textError} />
            Từ chối duyệt
          </button>
          <button type="button" className={styles.btnApprove} onClick={onApprove} disabled={acting}>
            <CheckCircleOutlined />
            Phê duyệt mở bán
          </button>
        </div>
      </div>

      <section className={styles.card}>
        <div className={styles.overviewHead}>
          <div className={styles.overviewText}>
            <div className={styles.chips}>
              {course.categoryName && (
                <span className={clsx(styles.chip, styles.chipPrimary)}>{course.categoryName}</span>
              )}
              {course.subcategoryName && <span className={styles.chip}>{course.subcategoryName}</span>}
              <span className={styles.chip}>Cấp độ: {LEVEL_LABELS[course.level] ?? "Chưa chọn"}</span>
            </div>
            <h2 className={styles.courseTitle}>{course.title ?? "(Chưa đặt tên)"}</h2>
            {course.shortDescription && <p className={styles.courseSubtitle}>{course.shortDescription}</p>}
          </div>
          <Thumbnail src={course.thumbnailUrl} alt={course.title} className={styles.overviewThumb} />
        </div>

        <div className={styles.infoGrid}>
          <div className={styles.infoItem}>
            <span className={styles.infoLabel}>Giảng viên nộp đơn</span>
            <div className={styles.infoInstructor}>
              <Avatar
                id={instructor?.id}
                name={instructor?.fullName}
                avatar={instructor?.avatar}
                className={styles.avatarSm}
              />
              <span className={styles.infoInstructorName}>{instructor?.fullName ?? "Không rõ"}</span>
            </div>
            {instructor?.email && <span className={styles.infoSub}>{instructor.email}</span>}
          </div>
          <div className={styles.infoItem}>
            <span className={styles.infoLabel}>Giá niêm yết</span>
            <span className={clsx(styles.infoValue, styles.textPrimary)}>{formatPrice(course.price)}</span>
          </div>
          <div className={styles.infoItem}>
            <span className={styles.infoLabel}>Ngày tạo khóa học</span>
            <span className={clsx(styles.infoValue, styles.infoValueSm)}>{formatDate(course.createdAt)}</span>
            <span className={styles.infoSub}>Lúc {formatTime(course.createdAt)}</span>
          </div>
          <div className={styles.infoItem}>
            <span className={styles.infoLabel}>Ngày gửi duyệt</span>
            <span className={clsx(styles.infoValue, styles.infoValueSm)}>{formatDate(course.submittedAt)}</span>
            <span className={styles.infoSub}>Lúc {formatTime(course.submittedAt)} GMT+7</span>
          </div>
        </div>

        <div className={styles.block}>
          <h4 className={styles.blockTitle}>
            <FileTextOutlined className={styles.textPrimary} />
            Mô tả chi tiết nội dung
          </h4>
          <div className={styles.description}>
            {course.description ? (
              course.description
                .split(/\n+/)
                .filter((paragraph) => paragraph.trim())
                .map((paragraph, i) => <p key={i}>{paragraph}</p>)
            ) : (
              <p className={styles.textMuted}>Giảng viên chưa nhập mô tả chi tiết.</p>
            )}
          </div>
        </div>

        <div className={styles.block}>
          <h4 className={styles.blockTitle}>
            <ReadOutlined className={styles.textPrimary} />
            Mục tiêu học tập ("Bạn sẽ học được gì")
          </h4>
          {outcomes.length > 0 ? (
            <div className={styles.outcomes}>
              {outcomes.map((outcome, i) => (
                <div key={i} className={styles.outcome}>
                  <CheckCircleFilled className={styles.textPrimary} />
                  <span>{outcome}</span>
                </div>
              ))}
            </div>
          ) : (
            <p className={styles.emptyText}>Chưa có mục tiêu học tập.</p>
          )}
        </div>

        <div className={styles.block}>
          <h4 className={styles.blockTitle}>
            <ToolOutlined className={styles.textPrimary} />
            Yêu cầu đầu vào
          </h4>
          {skills.length > 0 ? (
            <div className={styles.outcomes}>
              {skills.map((skill, i) => (
                <div key={i} className={styles.outcome}>
                  <CheckCircleFilled className={styles.textMuted} />
                  <span>{skill}</span>
                </div>
              ))}
            </div>
          ) : (
            <p className={styles.emptyText}>Không yêu cầu kỹ năng đầu vào.</p>
          )}
        </div>

        {topics.length > 0 && (
          <div className={styles.block}>
            <h4 className={styles.blockTitle}>
              <BookOutlined className={styles.textPrimary} />
              Chủ đề
            </h4>
            <div className={styles.chips}>
              {topics.map((topic) => (
                <span key={topic} className={styles.chip}>
                  {topic}
                </span>
              ))}
            </div>
          </div>
        )}
      </section>

      <section className={styles.card}>
        <div className={styles.curriculumHead}>
          <div className={styles.curriculumTitle}>
            <BookOutlined className={styles.textPrimary} />
            <h3>Giáo trình chi tiết (Curriculum Review)</h3>
          </div>
        </div>
        <Empty description="Hệ thống chưa lưu dữ liệu giáo trình (chương, bài học) cho khóa học này." />
      </section>
    </>
  );
};

const CourseApprove = () => {
  const [messageApi, messageContext] = message.useMessage();
  const [modalApi, modalContext] = Modal.useModal();

  const [courses, setCourses] = useState([]);
  const [totalElements, setTotalElements] = useState(0);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [queueLoading, setQueueLoading] = useState(true);
  const [queueError, setQueueError] = useState(null);

  const [selectedId, setSelectedId] = useState(null);
  const [detail, setDetail] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState(null);

  const [reasons, setReasons] = useState([]);
  const [rejectOpen, setRejectOpen] = useState(false);
  const [acting, setActing] = useState(false);

  const detailRequestRef = useRef(0);

  const applyQueueResult = useCallback((result, nextPage) => {
    const content = result?.content ?? [];
    setCourses((prev) => (nextPage === 0 ? content : [...prev, ...content]));
    setTotalElements(result?.totalElements ?? content.length);
    setPage(nextPage);
    setHasMore(nextPage + 1 < (result?.totalPages ?? 0));
    setQueueError(null);
  }, []);

  const loadQueue = useCallback(
    async (nextPage = 0) => {
      setQueueLoading(true);
      setQueueError(null);
      try {
        applyQueueResult(await getPendingCourses({ page: nextPage, size: PAGE_SIZE }), nextPage);
      } catch (err) {
        setQueueError(getErrorMessage(err, "Không thể tải hàng đợi kiểm duyệt. Vui lòng thử lại."));
      } finally {
        setQueueLoading(false);
      }
    },
    [applyQueueResult]
  );

  useEffect(() => {
    let cancelled = false;

    getPendingCourses({ page: 0, size: PAGE_SIZE })
      .then((result) => {
        if (!cancelled) applyQueueResult(result, 0);
      })
      .catch((err) => {
        if (!cancelled) setQueueError(getErrorMessage(err, "Không thể tải hàng đợi kiểm duyệt. Vui lòng thử lại."));
      })
      .finally(() => {
        if (!cancelled) setQueueLoading(false);
      });

    getRejectReasons()
      .then((result) => {
        if (!cancelled) setReasons(result);
      })
      .catch(() => {});

    return () => {
      cancelled = true;
    };
  }, [applyQueueResult]);

  const loadDetail = useCallback(async (courseId) => {
    const requestId = ++detailRequestRef.current;
    setDetailLoading(true);
    setDetailError(null);
    setDetail(null);
    try {
      const result = await getCourseForReview(courseId);
      if (requestId === detailRequestRef.current) setDetail(result);
    } catch (err) {
      if (requestId === detailRequestRef.current) {
        setDetailError(getErrorMessage(err, "Không thể tải chi tiết khóa học."));
      }
    } finally {
      if (requestId === detailRequestRef.current) setDetailLoading(false);
    }
  }, []);

  const handleSelect = (courseId) => {
    if (courseId === selectedId) return;
    setSelectedId(courseId);
    loadDetail(courseId);
  };

  const closeDetail = () => {
    detailRequestRef.current += 1;
    setSelectedId(null);
    setDetail(null);
    setDetailError(null);
    setDetailLoading(false);
  };

  const removeFromQueue = (courseId) => {
    setCourses((prev) => prev.filter((c) => c.id !== courseId));
    setTotalElements((prev) => Math.max(prev - 1, 0));
    closeDetail();
    notifyPendingCoursesChanged();
  };

  const handleApprove = () => {
    if (!detail) return;
    modalApi.confirm({
      title: "Xác nhận phê duyệt khóa học",
      content: `Khóa học "${detail.title}" sẽ được chuyển sang trạng thái Published và mở bán công khai ngay lập tức.`,
      okText: "Phê duyệt",
      cancelText: "Hủy",
      onOk: async () => {
        setActing(true);
        try {
          await approveCourse(detail.id);
          messageApi.success(`Khóa học #${detail.id} đã được phê duyệt và công khai.`);
          removeFromQueue(detail.id);
        } catch (err) {
          messageApi.error(getErrorMessage(err, "Không thể phê duyệt khóa học. Vui lòng thử lại."));
          if (err?.response?.status === 409) loadQueue(0);
        } finally {
          setActing(false);
        }
      },
    });
  };

  const handleReject = async (payload) => {
    if (!detail) return;
    setActing(true);
    try {
      await rejectCourse(detail.id, payload);
      messageApi.success(
        `Đã từ chối khóa học #${detail.id}. Email phản hồi đã được gửi đến giảng viên ${detail.instructor?.fullName ?? ""}.`
      );
      setRejectOpen(false);
      removeFromQueue(detail.id);
    } finally {
      setActing(false);
    }
  };

  const showDetailPanel = selectedId !== null;

  return (
    <div className={styles.wrapper}>
      {messageContext}
      {modalContext}

      <div className={clsx(styles.queuePanel, !showDetailPanel && styles.queuePanelFull)}>
        <div className={styles.queueHeader}>
          <div className={styles.queueHeaderTitle}>
            <span>Hàng đợi kiểm duyệt</span>
            <span className={styles.queueCount}>{totalElements}</span>
          </div>
          <div className={styles.queueHeaderRight}>
            <span className={styles.queueSort}>Sắp xếp: Mới nhất trước</span>
            <button
              type="button"
              className={styles.btnIcon}
              onClick={() => loadQueue(0)}
              disabled={queueLoading}
              title="Tải lại hàng đợi"
            >
              <ReloadOutlined spin={queueLoading} />
            </button>
          </div>
        </div>

        {!showDetailPanel && courses.length > 0 && (
          <p className={styles.queueHint}>Chọn một khóa học trong hàng đợi để bắt đầu thẩm định.</p>
        )}

        {queueError && <Alert type="error" showIcon title={queueError} />}

        <Spin spinning={queueLoading && courses.length === 0}>
          <div className={clsx(styles.queueList, !showDetailPanel && styles.queueListGrid)}>
            {courses.map((course) => (
              <QueueCard key={course.id} course={course} active={course.id === selectedId} onSelect={handleSelect} />
            ))}
            {!queueLoading && !queueError && courses.length === 0 && (
              <div className={styles.emptyQueue}>Không còn khóa học nào chờ duyệt.</div>
            )}
          </div>
        </Spin>

        {hasMore && (
          <button
            type="button"
            className={styles.btnLoadMore}
            onClick={() => loadQueue(page + 1)}
            disabled={queueLoading}
          >
            {queueLoading ? "Đang tải..." : "Tải thêm khóa học"}
          </button>
        )}
      </div>

      {showDetailPanel && (
        <div className={styles.detailPanel}>
          {detailLoading && (
            <div className={styles.detailState}>
              <Spin />
            </div>
          )}
          {detailError && (
            <div className={styles.detailState}>
              <Alert type="error" showIcon title={detailError} />
              <div className={styles.detailStateActions}>
                <button type="button" className={styles.btnSecondary} onClick={closeDetail}>
                  Đóng
                </button>
                <button type="button" className={styles.btnSecondary} onClick={() => loadDetail(selectedId)}>
                  Thử lại
                </button>
              </div>
            </div>
          )}
          {detail && (
            <CourseDetail
              course={detail}
              acting={acting}
              onClose={closeDetail}
              onReject={() => setRejectOpen(true)}
              onApprove={handleApprove}
            />
          )}
        </div>
      )}

      {rejectOpen && detail && (
        <RejectModal
          course={detail}
          reasons={reasons}
          submitting={acting}
          onClose={() => setRejectOpen(false)}
          onSubmit={handleReject}
        />
      )}
    </div>
  );
};

export default CourseApprove;
