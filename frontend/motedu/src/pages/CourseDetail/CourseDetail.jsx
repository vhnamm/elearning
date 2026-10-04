import React, { useState, useEffect } from 'react';
import { Navigate, useParams } from 'react-router-dom';
import {
    PlayCircleOutlined,
    QuestionCircleOutlined,
    FileTextOutlined,
    DownOutlined,
    ClockCircleOutlined,
    ReadOutlined,
    FieldTimeOutlined,
    SafetyCertificateOutlined,
} from '@ant-design/icons';
import useAuth from '~hooks/useAuth';
import { formatPrice } from '~/utils/course.util.js';
import styles from './CourseDetail.module.scss';
import {
    getCourseDetailPublic,
    getCourseCurriculumPublic,
    checkUserEnrollmentApi,
    enrollFreeCourseApi
} from '~services/course.service.js';

const CourseDetail = () => {
    const { courseId } = useParams();
    const { isAuthenticated, isInitializing } = useAuth();

    const [course, setCourse] = useState(null);
    const [curriculum, setCurriculum] = useState({ sections: [], totalDurationSeconds: 0, totalQuizzes: 0 });
    const [loading, setLoading] = useState(true);
    const [videoModal, setVideoModal] = useState({ isOpen: false, url: '' });

    const [enrolled, setEnrolled] = useState(false);
    // Chưa đăng nhập thì mặc định chưa đăng ký, không gọi API.
    const isEnrolled = isAuthenticated && enrolled;
    const [isProcessing, setIsProcessing] = useState(false);

    // Id các chương đang mở; mặc định thu gọn hết.
    const [openSections, setOpenSections] = useState(() => new Set());
    const [descExpanded, setDescExpanded] = useState(false);

    const toggleSection = (id) => {
        setOpenSections((prev) => {
            const next = new Set(prev);
            if (next.has(id)) next.delete(id);
            else next.add(id);
            return next;
        });
    };

    const isVideo = (type) => type === 'VIDEO' || type === 'LECTURE';
    const formatDuration = (seconds) =>
        `${Math.floor(seconds / 60)}:${String(seconds % 60).padStart(2, '0')}`;

    const formatTotalDuration = (seconds) => {
        const hours = Math.floor((seconds || 0) / 3600);
        const minutes = Math.floor(((seconds || 0) % 3600) / 60);
        return `${String(hours).padStart(2, '0')} giờ ${String(minutes).padStart(2, '0')} phút`;
    };

    const renderLessonIcon = (type) => {
        if (isVideo(type)) return <PlayCircleOutlined />;
        if (type === 'QUIZ' || type === 'QUIZZ') return <QuestionCircleOutlined />;
        return <FileTextOutlined />;
    };

    const getEmbedUrl = (url) => {
        if (!url) return '';
        const videoIdMatch = url.match(/[?&]v=([^&]+)/);
        if (videoIdMatch && videoIdMatch[1]) {
            return `https://www.youtube.com/embed/${videoIdMatch[1]}?autoplay=1`;
        }
        return url;
    };

    const handleGoToLearn = () => {
        alert("Đang chuyển bạn đến phòng học VIP...");
    };

    const handleEnrollFree = async () => {
        try {
            setIsProcessing(true);
            await enrollFreeCourseApi(courseId);
            alert("Đăng ký khóa học miễn phí thành công!");
            setEnrolled(true);
        } catch (error) {
            console.error(error);
            alert("Đăng ký thất bại, vui lòng thử lại!");
        } finally {
            setIsProcessing(false);
        }
    };

    const handleAddToCart = () => {
        alert("Đã quăng khóa học vào giỏ hàng!");
    };

    useEffect(() => {
        const fetchInitialData = async () => {
            try {
                setLoading(true);
                const [courseData, curriculumData] = await Promise.all([
                    getCourseDetailPublic(courseId),
                    getCourseCurriculumPublic(courseId),
                ]);

                setCourse(courseData);
                setCurriculum(curriculumData);
            } catch (error) {
                console.error("Lỗi kéo dữ liệu khóa học:", error);
            } finally {
                setLoading(false);
            }
        };

        if (courseId) {
            fetchInitialData();
        }
    }, [courseId]);

    useEffect(() => {
        if (isInitializing || !isAuthenticated) return;
        checkUserEnrollmentApi(courseId)
            .then(setEnrolled)
            .catch(() => setEnrolled(false));
    }, [courseId, isAuthenticated, isInitializing]);

    if (loading) {
        return (
            <div className={styles.loadingContainer}>
                <div className={styles.spinner}></div>
                <h2>Đang giải nén dữ liệu khóa học...</h2>
            </div>
        );
    }

    if (!course) {
        return <Navigate to="/404" replace />;
    }

    const sortedSections = [...(curriculum.sections || [])].sort((a, b) => a.position - b.position);
    const allExpanded = sortedSections.length > 0 && openSections.size === sortedSections.length;
    const toggleAll = () =>
        setOpenSections(allExpanded ? new Set() : new Set(sortedSections.map((sec) => sec.id)));

    const lectureCount = sortedSections.reduce(
        (sum, sec) => sum + (sec.curriculumItems || []).filter((it) => isVideo(it.type)).length,
        0
    );

    const isFree = course.price === 0 || !course.price;
    const DESC_LIMIT = 600;
    const description = course.description || '';
    const isLongDesc = description.length > DESC_LIMIT;

    return (
        <div className={styles.container}>
            <div className={`${styles.breadcrumb} ${styles.fadeIn}`}>
                Khám phá &gt; Khóa học &gt; <span>{course.title}</span>
            </div>

            <div className={styles.layout}>
                <div className={styles.mainColumn}>
                    <h1 className={`${styles.title} ${styles.slideUp}`}>{course.title}</h1>
                    {course.shortDescription && (
                        <p className={`${styles.subtitle} ${styles.slideUp}`}>{course.shortDescription}</p>
                    )}

                    <div className={`${styles.metaInfo} ${styles.slideUp}`}>
                        <span className={styles.rating}>⭐ {course.averageStar || 0} ({course.reviewCount || 0} đánh giá)</span>
                        <span className={styles.students}>👥 {course.studentCount || 0} học viên</span>
                        <div className={styles.instructorBlock}>
                            <img src={course.instructor?.avatar || 'https://placehold.co/150'} alt="Avatar" className={styles.instructorAvatar} />
                            <span>Giảng viên: <strong>{course.instructor?.fullName || 'Đang cập nhật'}</strong></span>
                        </div>
                    </div>

                    <div className={`${styles.whatYouWillLearn} ${styles.slideUp}`}>
                        <h2>Bạn sẽ học được gì</h2>
                        <ul className={styles.learningList}>
                            {course.learningOutcomes?.length > 0 ? (
                                course.learningOutcomes.map((item, index) => (
                                    <li key={item.id || index} style={{ animationDelay: `${index * 0.1}s` }} className={styles.staggerItem}>
                                        {item.content}
                                    </li>
                                ))
                            ) : (
                                <li>Chưa có thông tin mục tiêu học tập.</li>
                            )}
                        </ul>
                    </div>

                    <div className={`${styles.courseContent} ${styles.slideUp}`}>
                        <h2>Nội dung khóa học</h2>
                        <div className={styles.contentHeader}>
                            <p className={styles.summary}>
                                {sortedSections.length} chương • {curriculum.totalQuizzes || 0} bài kiểm tra •
                                Tổng thời lượng: {curriculum.totalDurationSeconds ? `${Math.floor(curriculum.totalDurationSeconds / 3600)}h ${Math.floor((curriculum.totalDurationSeconds % 3600) / 60)}m` : '0m'}
                            </p>
                            {sortedSections.length > 0 && (
                                <button type="button" className={styles.toggleAll} onClick={toggleAll}>
                                    {allExpanded ? 'Thu gọn tất cả' : 'Mở rộng tất cả'}
                                </button>
                            )}
                        </div>

                        <div className={styles.accordion}>
                            {sortedSections.map((section, sIdx) => {
                                const isOpen = openSections.has(section.id);
                                const items = [...(section.curriculumItems || [])].sort((a, b) => a.position - b.position);

                                return (
                                    <div key={section.id} className={styles.chapter} style={{ animationDelay: `${sIdx * 0.15}s` }}>
                                        <div
                                            className={styles.chapterHeader}
                                            role="button"
                                            tabIndex={0}
                                            aria-expanded={isOpen}
                                            onClick={() => toggleSection(section.id)}
                                            onKeyDown={(e) => {
                                                if (e.key === 'Enter' || e.key === ' ') {
                                                    e.preventDefault();
                                                    toggleSection(section.id);
                                                }
                                            }}
                                        >
                                            <span className={styles.chapterTitle}>
                                                <DownOutlined className={`${styles.chevron} ${isOpen ? styles.chevronOpen : ''}`} />
                                                Chương {section.position}: {section.title}
                                            </span>
                                            <span className={styles.chapterCount}>{items.length} bài học</span>
                                        </div>
                                        {isOpen && (
                                            <div className={styles.chapterBody}>
                                                {items.map((item) => (
                                                    <div key={item.id} className={styles.lesson}>
                                                        <div className={styles.lessonTitle}>
                                                            <span className={styles.icon}>{renderLessonIcon(item.type)}</span>
                                                            <span className={styles.text}>{item.title}</span>
                                                        </div>
                                                        <div className={styles.lessonMeta}>
                                                            {item.preview && (
                                                                <button
                                                                    type="button"
                                                                    className={styles.previewLink}
                                                                    onClick={() => item.videoKey && setVideoModal({ isOpen: true, url: item.videoKey })}
                                                                >
                                                                    Xem trước
                                                                </button>
                                                            )}
                                                            {isVideo(item.type) && item.videoDurationSeconds && (
                                                                <span className={styles.duration}>{formatDuration(item.videoDurationSeconds)}</span>
                                                            )}
                                                        </div>
                                                    </div>
                                                ))}
                                            </div>
                                        )}
                                    </div>
                                );
                            })}
                        </div>
                    </div>

                    <div className={`${styles.requirements} ${styles.slideUp}`}>
                        <h2>Yêu cầu khóa học</h2>
                        {course.requiredSkills?.length > 0 ? (
                            <ul className={styles.requirementList}>
                                {course.requiredSkills.map((item, index) => (
                                    <li key={item.id || index}>{item.content}</li>
                                ))}
                            </ul>
                        ) : (
                            <p className={styles.muted}>Không có yêu cầu đặc biệt.</p>
                        )}
                    </div>

                    <div className={`${styles.description} ${styles.slideUp}`}>
                        <h2>Mô tả chi tiết</h2>
                        {description ? (
                            <>
                                <div className={`${styles.descriptionBody} ${isLongDesc && !descExpanded ? styles.descriptionCollapsed : ''}`}>
                                    {description}
                                </div>
                                {isLongDesc && (
                                    <button type="button" className={styles.toggleAll} onClick={() => setDescExpanded((v) => !v)}>
                                        {descExpanded ? 'Thu gọn' : 'Xem thêm'}
                                    </button>
                                )}
                            </>
                        ) : (
                            <p className={styles.muted}>Chưa có mô tả chi tiết.</p>
                        )}
                    </div>
                </div>

                <div className={`${styles.sidebarColumn} ${styles.slideLeft}`}>
                    <div className={styles.purchaseCard}>
                        <div className={styles.thumbnailContainer}>
                            <img src={course.thumbnailUrl || 'https://placehold.co/800x450?text=No+Thumbnail'} alt={course.title} className={styles.thumbnailImg} />
                        </div>

                        {isEnrolled ? (
                            <div className={styles.purchasedSection}>
                                <div className={styles.successBadge}>✓ Bạn đã sở hữu khóa học này</div>
                                <button className={styles.continueBtn} onClick={handleGoToLearn}>
                                    Vào học ngay
                                </button>
                            </div>
                        ) : (
                            <>
                                <div className={styles.priceSection}>
                                    <span className={styles.currentPrice}>
                                        {isFree ? 'Miễn phí' : formatPrice(course.price)}
                                    </span>
                                </div>

                                {isFree ? (
                                    <button
                                        className={styles.enrollBtn}
                                        onClick={handleEnrollFree}
                                        disabled={isProcessing}
                                    >
                                        {isProcessing ? 'Đang xử lý...' : 'Đăng ký ngay'}
                                    </button>
                                ) : (
                                    <>
                                        <button className={styles.buyBtn} onClick={() => alert('Chuyển sang trang thanh toán')}>
                                            Mua ngay
                                        </button>
                                        <button className={styles.cartBtn} onClick={handleAddToCart}>
                                            Thêm vào giỏ hàng
                                        </button>
                                    </>
                                )}
                            </>
                        )}

                        <div className={styles.includes}>
                            <p>Khóa học này bao gồm:</p>
                            <ul>
                                <li><ClockCircleOutlined /> Thời lượng: {formatTotalDuration(curriculum.totalDurationSeconds)}</li>
                                <li><ReadOutlined /> Giáo trình: {lectureCount} bài giảng</li>
                                <li><FieldTimeOutlined /> Truy cập trọn đời</li>
                                {course.hasCertificate && (
                                    <li><SafetyCertificateOutlined /> Chứng nhận hoàn thành</li>
                                )}
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
            {videoModal.isOpen && (
                <div className={styles.modalOverlay} onClick={() => setVideoModal({ isOpen: false, url: '' })}>
                    <div className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
                        <button className={styles.closeBtn} onClick={() => setVideoModal({ isOpen: false, url: '' })}>✕</button>
                        <div className={styles.iframeContainer}>
                            <iframe
                                src={getEmbedUrl(videoModal.url)}
                                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                                allowFullScreen
                                title="Video Học Thử"
                            ></iframe>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default CourseDetail;