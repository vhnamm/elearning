import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import styles from './CourseDetail.module.scss';
import {
    getCourseDetailPublic,
    checkUserEnrollmentApi,
    enrollFreeCourseApi
} from '~services/course.service.js';

const CourseDetail = () => {
    const { courseId } = useParams();
    const navigate = useNavigate();

    const [course, setCourse] = useState(null);
    const [loading, setLoading] = useState(true);
    const [videoModal, setVideoModal] = useState({ isOpen: false, url: '' });

    const [isEnrolled, setIsEnrolled] = useState(false);
    const [isProcessing, setIsProcessing] = useState(false);

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
            setIsEnrolled(true);
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
                const [courseData, enrollmentStatus] = await Promise.all([
                    getCourseDetailPublic(courseId),
                    checkUserEnrollmentApi(courseId).catch(() => false)
                ]);

                setCourse(courseData);
                setIsEnrolled(enrollmentStatus === true);
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

    if (loading) {
        return (
            <div className={styles.loadingContainer}>
                <div className={styles.spinner}></div>
                <h2>Đang giải nén dữ liệu khóa học...</h2>
            </div>
        );
    }

    if (!course) {
        return (
            <div className={styles.container}>
                <h2>Không tìm thấy khóa học này!</h2>
            </div>
        );
    }

    return (
        <div className={styles.container}>
            <div className={`${styles.breadcrumb} ${styles.fadeIn}`}>
                Khám phá &gt; Khóa học &gt; <span>{course.title}</span>
            </div>

            <div className={styles.layout}>
                <div className={styles.mainColumn}>
                    <h1 className={`${styles.title} ${styles.slideUp}`}>{course.title}</h1>

                    <div className={`${styles.metaInfo} ${styles.slideUp}`}>
                        <span className={styles.rating}>⭐ {course.averageStar || 0} ({course.reviewCount || 0} đánh giá)</span>
                        <span className={styles.students}>👥 {course.studentCount || 0} học viên</span>
                        <div className={styles.instructorBlock}>
                            <img src={course.instructor?.avatar || 'https://placehold.co/150'} alt="Avatar" className={styles.instructorAvatar} />
                            <span>Giảng viên: <strong>{course.instructor?.fullName || 'Đang cập nhật'}</strong></span>
                        </div>
                    </div>

                    <div className={`${styles.thumbnailContainer} ${styles.zoomIn}`}>
                        <img src={course.thumbnailUrl || 'https://placehold.co/800x450?text=No+Thumbnail'} alt={course.title} className={styles.thumbnailImg} />
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
                        <p className={styles.summary}>
                            {course.sections?.length || 0} chương • {course.totalQuizzes || 0} bài kiểm tra •
                            Tổng thời lượng: {course.totalDurationSeconds ? `${Math.floor(course.totalDurationSeconds / 3600)}h ${Math.floor((course.totalDurationSeconds % 3600) / 60)}m` : '0m'}
                        </p>

                        <div className={styles.accordion}>
                            {course.sections?.sort((a, b) => a.position - b.position).map((section, sIdx) => (
                                <div key={section.id} className={styles.chapter} style={{ animationDelay: `${sIdx * 0.15}s` }}>
                                    <div className={styles.chapterHeader}>
                                        <span>Chương {section.position}: {section.title}</span>
                                        <span>{section.curriculumItems?.length || 0} mục</span>
                                    </div>
                                    <div className={styles.chapterBody}>
                                        {section.curriculumItems?.sort((a, b) => a.position - b.position).map(item => {
                                            const isLocked = !item.preview && !isEnrolled;

                                            return (
                                                <div
                                                    key={item.id}
                                                    className={`${styles.lesson} ${isLocked ? styles.locked : styles.previewable}`}
                                                    onClick={() => {
                                                        if (!isLocked && item.videoKey) {
                                                            setVideoModal({ isOpen: true, url: item.videoKey });
                                                        }
                                                    }}
                                                >
                                                    <div className={styles.lessonTitle}>
                                                        <span className={styles.icon}>
                                                          {isLocked ? '🔒' : (item.type === 'VIDEO' || item.type === 'LECTURE' ? '📺' : item.type === 'QUIZ' ? '📝' : '📄')}
                                                        </span>
                                                        <span className={styles.text}>{item.title}</span>
                                                    </div>
                                                    {(item.type === 'VIDEO' || item.type === 'LECTURE') && item.videoDurationSeconds && (
                                                        <span className={styles.duration}>
                                                          {Math.floor(item.videoDurationSeconds / 60)}:{String(item.videoDurationSeconds % 60).padStart(2, '0')}
                                                        </span>
                                                    )}
                                                </div>
                                            );
                                        })}
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>

                <div className={`${styles.sidebarColumn} ${styles.slideLeft}`}>
                    <div className={styles.purchaseCard}>

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
                                        {course.price === 0 || !course.price
                                            ? 'Miễn phí'
                                            : new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(course.price)}
                                    </span>
                                </div>

                                {course.price === 0 || !course.price ? (
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
                                <li>📺 Truy cập trọn đời các bài giảng</li>
                                <li>🏆 {course.hasCertificate ? 'Chứng chỉ hoàn thành' : 'Không kèm chứng chỉ'}</li>
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