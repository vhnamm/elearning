import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faArrowRight,
  faChevronLeft,
  faChevronRight,
  faRocket,
  faStar,
} from "@fortawesome/free-solid-svg-icons";
import clsx from "clsx";
import {
  getPopularCourses,
  getPublishedCourses,
} from "~services/course.service.js";
import styles from "./Home.module.scss";

const SKILL_TABS = [
  { label: "Trí tuệ nhân tạo (AI)", keyword: "AI" },
  { label: "Python", keyword: "Python" },
  { label: "Microsoft Excel", keyword: "Excel" },
  { label: "AI Agents & Agentic AI", keyword: "AI Agent" },
  { label: "Marketing số", keyword: "Marketing" },
  { label: "Amazon AWS", keyword: "AWS" },
];

const HERO_IMAGE =
  "https://lh3.googleusercontent.com/aida-public/AB6AXuCC2GrKowWrjof4iATC92m-Iu5xvUwTml7YchwsvfQwnbbai5Sb6fVIa-cxbI7tPCHn7yd_b5hbrB7sO2y2ZxILBTTNTYCsd2GAOCSpVcnvKjwM1aupfrw12HDTmdANEuiarbtQTCW1VndUh4Gf_agp_vNxVKjcVUnLfSouFtNIp5cnXHL54YVvlk9coEp9GFAP4uc3rAdZ1wMs-gs-Nu0NWrh5oTaleD0a2rC5JNZFUT2MQGmjNfcN";

const formatPrice = (price) => {
  if (price == null) return "";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(Number(price));
};

const formatReviewCount = (count) => {
  const n = Number(count) || 0;
  if (n >= 1000) {
    return `${(n / 1000).toFixed(1).replace(/\.0$/, "")}k đánh giá`;
  }
  return `${n} đánh giá`;
};

const mapCourseCard = (course) => ({
  id: course.id,
  title: course.title,
  instructor: course.instructorName || "Giảng viên MótEdu",
  rating: course.rating ?? 0,
  reviews: formatReviewCount(course.reviewCount),
  price: formatPrice(course.price),
  image: course.thumbnailUrl,
});

const Home = () => {
  const [activeTab, setActiveTab] = useState(SKILL_TABS[0]);
  const [popularCourses, setPopularCourses] = useState([]);
  const [skillCourses, setSkillCourses] = useState([]);
  const [popularLoading, setPopularLoading] = useState(true);
  const [skillLoading, setSkillLoading] = useState(true);
  const [popularError, setPopularError] = useState(null);
  const [skillError, setSkillError] = useState(null);
  const carouselRef = useRef(null);

  useEffect(() => {
    let cancelled = false;

    const loadPopular = async () => {
      setPopularLoading(true);
      setPopularError(null);
      try {
        const data = await getPopularCourses(4);
        if (!cancelled) {
          setPopularCourses((data || []).map(mapCourseCard));
        }
      } catch {
        if (!cancelled) {
          setPopularError("Không tải được khóa học phổ biến.");
          setPopularCourses([]);
        }
      } finally {
        if (!cancelled) setPopularLoading(false);
      }
    };

    loadPopular();
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    let cancelled = false;

    const loadSkillCourses = async () => {
      setSkillLoading(true);
      setSkillError(null);
      try {
        const page = await getPublishedCourses({
          keyword: activeTab.keyword,
          page: 0,
          size: 8,
        });
        if (!cancelled) {
          setSkillCourses((page?.content || []).map(mapCourseCard));
        }
      } catch {
        if (!cancelled) {
          setSkillError("Không tải được khóa học theo kỹ năng.");
          setSkillCourses([]);
        }
      } finally {
        if (!cancelled) setSkillLoading(false);
      }
    };

    loadSkillCourses();
    return () => {
      cancelled = true;
    };
  }, [activeTab]);

  const scrollCarousel = (direction) => {
    const el = carouselRef.current;
    if (!el) return;
    el.scrollBy({ left: direction * 300, behavior: "smooth" });
  };

  return (
    <main className={styles.page}>
      <section className={styles.hero}>
        <div className={styles.heroCopy}>
          <div className={styles.heroBadge}>
            <FontAwesomeIcon icon={faRocket} />
            Khám phá tiềm năng của bạn
          </div>
          <h1 className={styles.heroTitle}>
            Nâng cao sự nghiệp của bạn với{" "}
            <span>kỹ năng mới</span>
          </h1>
          <p className={styles.heroDesc}>
            Học tập qua các bài giảng tương tác, thực hành dự án thực tế và xây
            dựng hồ sơ năng lực ấn tượng. Trải nghiệm học tập thú vị như chơi
            game!
          </p>
          <div className={styles.heroActions}>
            <Link to="/register" className={styles.btnPrimary}>
              Bắt đầu ngay
              <FontAwesomeIcon icon={faArrowRight} />
            </Link>
            <button type="button" className={styles.btnGhost}>
              Làm bài test năng lực
            </button>
          </div>
        </div>
        <div className={styles.heroMedia}>
          <div className={styles.heroDecor} aria-hidden />
          <img src={HERO_IMAGE} alt="Học viên học tập trên MótEdu" />
        </div>
      </section>

      <section className={styles.section}>
        <div className={styles.sectionHead}>
          <h2>Khóa học phổ biến</h2>
          <a href="#" className={styles.seeAll}>
            Xem tất cả
          </a>
        </div>
        {popularLoading && <p>Đang tải khóa học...</p>}
        {popularError && <p>{popularError}</p>}
        {!popularLoading && !popularError && popularCourses.length === 0 && (
          <p>Chưa có khóa học phổ biến.</p>
        )}
        <div className={styles.courseGrid}>
          {popularCourses.map((course) => (
            <article key={course.id} className={styles.courseCard}>
              <div className={styles.courseThumb}>
                <img src={course.image} alt={course.title} />
              </div>
              <div className={styles.courseBody}>
                <h3>{course.title}</h3>
                <p>{course.instructor}</p>
                <div className={styles.courseMeta}>
                  <div className={styles.rating}>
                    <FontAwesomeIcon icon={faStar} />
                    <span>{course.rating}</span>
                    <span className={styles.reviews}>({course.reviews})</span>
                  </div>
                  <div className={styles.price}>{course.price}</div>
                </div>
              </div>
            </article>
          ))}
        </div>
      </section>

      <section className={styles.section}>
        <div className={styles.skillsIntro}>
          <h2>Kỹ năng thay đổi sự nghiệp và cuộc sống</h2>
          <p>
            Từ kỹ năng thiết yếu đến chủ đề chuyên sâu, MótEdu đồng hành cùng
            bạn phát triển nghề nghiệp.
          </p>
        </div>

        <div className={styles.tabs}>
          {SKILL_TABS.map((tab) => (
            <button
              key={tab.label}
              type="button"
              className={clsx(
                styles.tab,
                activeTab.label === tab.label && styles.tabActive
              )}
              onClick={() => setActiveTab(tab)}
            >
              {tab.label}
            </button>
          ))}
        </div>

        <div className={styles.carouselWrap}>
          <button
            type="button"
            className={clsx(styles.carouselBtn, styles.carouselPrev)}
            onClick={() => scrollCarousel(-1)}
            aria-label="Trước"
          >
            <FontAwesomeIcon icon={faChevronLeft} />
          </button>

          <div className={styles.carousel} ref={carouselRef}>
            {skillLoading && <p>Đang tải...</p>}
            {skillError && <p>{skillError}</p>}
            {!skillLoading && !skillError && skillCourses.length === 0 && (
              <p>Chưa có khóa học cho chủ đề này.</p>
            )}
            {skillCourses.map((course) => (
              <article key={course.id} className={styles.skillCard}>
                <img src={course.image} alt={course.title} />
                <div className={styles.skillCardBody}>
                  <h3>{course.title}</h3>
                  <p>{course.instructor}</p>
                  <div className={styles.skillCardMeta}>
                    <div className={styles.ratingSm}>
                      <span>{course.rating}</span>
                      <FontAwesomeIcon icon={faStar} />
                      <span className={styles.reviews}>
                        ({course.reviews})
                      </span>
                    </div>
                  </div>
                  <div className={styles.priceRow}>
                    <span>{course.price}</span>
                  </div>
                </div>
              </article>
            ))}
          </div>

          <button
            type="button"
            className={clsx(styles.carouselBtn, styles.carouselNext)}
            onClick={() => scrollCarousel(1)}
            aria-label="Sau"
          >
            <FontAwesomeIcon icon={faChevronRight} />
          </button>
        </div>

        <a href="#" className={styles.showAll}>
          Xem tất cả khóa học {activeTab.label}
          <FontAwesomeIcon icon={faArrowRight} />
        </a>
      </section>
    </main>
  );
};

export default Home;
