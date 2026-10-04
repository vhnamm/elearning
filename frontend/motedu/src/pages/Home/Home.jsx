import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faArrowRight,
  faChevronLeft,
  faChevronRight,
  faStar,
} from "@fortawesome/free-solid-svg-icons";
import { Skeleton } from "antd";
import clsx from "clsx";
import {
  getPopularCourses,
} from "~services/course.service.js";
import { getTrendingTopics } from "~services/category.service.js";
import { mapCourseCard } from "~/utils/course.util.js";
import styles from "./Home.module.scss";
import {searchCourses} from "~services/course.service.js";


const POPULAR_SIZE = 4;

const HERO_IMAGE =
  "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?auto=format&fit=crop&w=1200&q=80";

// Thẻ khóa học dùng chung cho lưới phổ biến và carousel theo topic.
const CourseCard = ({ course, className }) => (
  <Link
    to={`/course/${course.id}`}
    className={clsx(styles.courseCard, className)}
  >
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
  </Link>
);

// Khung chờ cùng bố cục với thẻ khóa học để trang không bị nhảy khi dữ liệu về.
const CourseCardSkeleton = ({ className }) => (
  <div className={clsx(styles.courseCard, className)} aria-hidden>
    <div className={styles.courseThumb}>
      <Skeleton.Node active style={{ width: "100%", height: "100%" }}>
        <span />
      </Skeleton.Node>
    </div>
    <div className={styles.courseBody}>
      <Skeleton active title={{ width: "90%" }} paragraph={{ rows: 2 }} />
    </div>
  </div>
);

const Home = () => {
  const [activeTab, setActiveTab] = useState(null);
  const [popularCourses, setPopularCourses] = useState([]);
  const [topicCourses, setTopicCourses] = useState([]);
  const [popularLoading, setPopularLoading] = useState(true);
  const [skillLoading, setSkillLoading] = useState(true);

  const [popularError, setPopularError] = useState(null);
  const [skillError, setSkillError] = useState(null);
  const carouselRef = useRef(null);
  const [trendingTopics, setTrendingTopics] = useState([])

    const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));


  useEffect(() => {
    let cancelled = false;

    const loadPopular = async () => {
      setPopularLoading(true);
      setPopularError(null);

      await sleep(1000)
      try {
        const data = await getPopularCourses(POPULAR_SIZE);
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
    // Khi có topic thì loading của carousel do effect tải khóa học theo topic quản lý.
    const loadTrendingTopic = async () => {
        try {
            const data = await getTrendingTopics()
            if (cancelled) return
            setTrendingTopics(data)
            setActiveTab(data[0] ?? null)
            if (data.length === 0) setSkillLoading(false)
        }
        catch {
            if(!cancelled){
                setSkillError("Không tải được topic hot")
                setTrendingTopics([])
                setSkillLoading(false)
            }
        }
    }

    loadTrendingTopic();
    return () => {
      cancelled = true;
    };
  }, []);

    useEffect(() => {
        if (!activeTab) return;
        let cancelled = false;
        const fetchTopicCourses = async () => {
            setSkillLoading(true)
            setSkillError(null)
            await sleep(1000)
            try {
                const data = await searchCourses({topicId: activeTab.id})
                if (!cancelled) setTopicCourses((data?.content || []).map(mapCourseCard))
            }catch{
                if (!cancelled) {
                    setSkillError("Không tải được khóa học theo chủ đề này.")
                    setTopicCourses([])
                }
            }finally {
                if(!cancelled) setSkillLoading(false)
            }
        }

        fetchTopicCourses()
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
        {popularError && <p>{popularError}</p>}
        {!popularLoading && !popularError && popularCourses.length === 0 && (
          <p>Chưa có khóa học phổ biến.</p>
        )}
        <div className={styles.courseGrid}>
          {popularLoading &&
            Array.from({ length: POPULAR_SIZE }, (_, i) => (
              <CourseCardSkeleton key={i} />
            ))}
          {popularCourses.map((course) => (
            <CourseCard key={course.id} course={course} />
          ))}
        </div>
      </section>

      <section className={styles.section}>
        <div className={styles.skillsIntro}>
          <h2>Kỹ năng xu hướng được học nhiều nhất</h2>
          <p>
              Bỏ túi các kỹ năng thực chiến của doanh nghiệp trên MótEdu
          </p>
        </div>

        <div className={styles.tabs}>
          {trendingTopics.map((topic) => (
            <button
              key={topic.id}
              type="button"
              className={clsx(
                styles.tab,
                activeTab?.id === topic.id && styles.tabActive
              )}
              onClick={() => setActiveTab(topic)}
            >
              {topic.name}
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
            {skillError && <p>{skillError}</p>}
            {!skillLoading && !skillError && topicCourses.length === 0 && (
              <p>Chưa có khóa học cho chủ đề này.</p>
            )}
            {skillLoading &&
              Array.from({ length: POPULAR_SIZE }, (_, i) => (
                <CourseCardSkeleton key={i} className={styles.carouselItem} />
              ))}
            {!skillLoading &&
              topicCourses.map((course) => (
                <CourseCard
                  key={course.id}
                  course={course}
                  className={styles.carouselItem}
                />
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
          Xem tất cả khóa học {activeTab?.name}
          <FontAwesomeIcon icon={faArrowRight} />
        </a>
      </section>
    </main>
  );
};

export default Home;
