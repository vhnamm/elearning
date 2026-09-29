import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faChevronDown,
  faChevronLeft,
  faChevronRight,
  faStar,
} from "@fortawesome/free-solid-svg-icons";
import clsx from "clsx";
import { searchCourses } from "~services/course.service.js";
import { getAllCategories } from "~services/category.service.js";
import styles from "./Search.module.scss";

const SORT_OPTIONS = [
  { value: "popular", label: "Phổ biến nhất" },
  { value: "rating", label: "Đánh giá cao nhất" },
  { value: "newest", label: "Mới nhất" },
  { value: "price_asc", label: "Giá: Thấp đến cao" },
  { value: "price_desc", label: "Giá: Cao đến thấp" },
];

const RATING_OPTIONS = [
  { value: "4.5", stars: 4, label: "Từ 4.5 trở lên" },
  { value: "4", stars: 4, label: "Từ 4.0 trở lên" },
  { value: "3.5", stars: 3, label: "Từ 3.5 trở lên" },
  { value: "3", stars: 1, label: "Từ 3.0 trở lên" },
];

const LEVEL_LABELS = {
  BEGINNER: "Người mới bắt đầu",
  INTERMEDIATE: "Trung cấp",
  ADVANCED: "Chuyên sâu / Nâng cao",
};

const LEVEL_OPTIONS = Object.entries(LEVEL_LABELS).map(([key, label]) => ({
  key,
  label,
}));

// Hiển thị giá VND, giá 0 thì ghi Miễn phí.
const formatPrice = (price) => {
  const value = Number(price);
  if (!value) return "Miễn phí";
  return `${new Intl.NumberFormat("vi-VN").format(value)} đ`;
};

// Định dạng số lượng theo kiểu Việt Nam, ví dụ 1.240.
const formatCount = (count) =>
  new Intl.NumberFormat("vi-VN").format(Number(count) || 0);

// Hiện số đã nhập trong ô giá kèm hậu tố "đ".
const formatMoneyInput = (value) => {
  if (!value) return "";
  return `${new Intl.NumberFormat("vi-VN").format(Number(value))} đ`;
};

// Chỉ giữ chữ số khi người dùng gõ vào ô giá.
const digitsOnly = (value) => String(value).replace(/[^\d]/g, "");

// Tạo dãy số trang, rút gọn bằng dấu ... khi có nhiều trang.
const buildPages = (current, total) => {
  if (total <= 1) return [1];
  if (total <= 7) {
    return Array.from({ length: total }, (_, index) => index + 1);
  }
  const pages = [1];
  if (current > 3) pages.push("ellipsis-left");
  for (let page = Math.max(2, current - 1); page <= Math.min(total - 1, current + 1); page += 1) {
    pages.push(page);
  }
  if (current < total - 2) pages.push("ellipsis-right");
  pages.push(total);
  return pages;
};

const Search = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const keyword = searchParams.get("keyword") || "";
  const sort = searchParams.get("sort") || "popular";
  const priceType = searchParams.get("priceType") || "all";
  const minRating = searchParams.get("minRating") || "";
  const categoryId = searchParams.get("categoryId") || "";
  const levels = searchParams.getAll("levels");
  const min = searchParams.get("min") || "";
  const max = searchParams.get("max") || "";
  const page = Math.max(Number(searchParams.get("page") || "1"), 1);

  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [categories, setCategories] = useState([]);
  const [filtersOpen, setFiltersOpen] = useState(false);
  const [priceDraft, setPriceDraft] = useState({ min, max });
  const [priceSource, setPriceSource] = useState(`${min}|${max}`);
  const queryString = searchParams.toString();

  if (priceSource !== `${min}|${max}`) {
    setPriceSource(`${min}|${max}`);
    setPriceDraft({ min, max });
  }

  // Danh mục sidebar lấy từ API danh mục tĩnh, không phụ thuộc kết quả tìm kiếm.
  useEffect(() => {
    let cancelled = false;
    getAllCategories()
      .then((data) => {
        if (!cancelled) setCategories(data || []);
      })
      .catch(() => {
        if (!cancelled) setCategories([]);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    let cancelled = false;
    const params = new URLSearchParams(queryString);

    // Gọi API tìm kiếm mỗi khi từ khóa, bộ lọc, sắp xếp hoặc trang đổi.
    const load = async () => {
      setLoading(true);
      setError(null);
      try {
        const data = await searchCourses({
          keyword: params.get("keyword") || "",
          categoryId: params.get("categoryId") || "",
          levels: params.getAll("levels"),
          minRating: params.get("minRating") || "",
          priceType: params.get("priceType") || "all",
          min: params.get("min") || "",
          max: params.get("max") || "",
          sort: params.get("sort") || "popular",
          page: Math.max(Number(params.get("page") || "1"), 1) - 1,
          size: 5,
        });
        if (!cancelled) setResult(data);
      } catch {
        if (!cancelled) {
          setError("Không tải được kết quả tìm kiếm.");
          setResult(null);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    load();
    return () => {
      cancelled = true;
    };
  }, [queryString]);

  // Cập nhật query trên URL. Đổi bộ lọc thì quay về trang 1.
  const updateParams = (patch, resetPage = true) => {
    const next = new URLSearchParams(searchParams);
    Object.entries(patch).forEach(([key, value]) => {
      next.delete(key);
      if (Array.isArray(value)) {
        value
          .filter((item) => item != null && item !== "")
          .forEach((item) => next.append(key, String(item)));
      } else if (value != null && value !== "") {
        next.set(key, String(value));
      }
    });
    if (resetPage) next.delete("page");
    setSearchParams(next);
  };

  // Bật hoặc tắt một giá trị trong bộ lọc nhiều lựa chọn, như danh mục và cấp độ.
  const toggleListValue = (key, value, current) => {
    const next = current.includes(value)
      ? current.filter((item) => item !== value)
      : [...current, value];
    updateParams({ [key]: next });
  };

  // Xóa bộ lọc, giữ lại từ khóa và cách sắp xếp.
  const clearFilters = () => {
    updateParams({
      categoryId: "",
      levels: [],
      minRating: "",
      priceType: "",
      min: "",
      max: "",
    });
    setFiltersOpen(false);
  };

  // Ghi khoảng giá lên URL. Nếu giá từ lớn hơn giá đến thì đổi chỗ hai đầu.
  const applyPrice = () => {
    let nextMin = priceDraft.min;
    let nextMax = priceDraft.max;
    if (nextMin && nextMax && Number(nextMin) > Number(nextMax)) {
      [nextMin, nextMax] = [nextMax, nextMin];
    }
    updateParams({
      min: nextMin,
      max: nextMax,
      priceType: priceType === "free" ? "paid" : priceType,
    });
  };

  // Chuyển trang và cuộn lên đầu danh sách.
  const goToPage = (nextPage) => {
    updateParams({ page: nextPage <= 1 ? "" : String(nextPage) }, false);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const courses = result?.content || [];
  const total = result?.totalElements || 0;
  const totalPages = result?.totalPages || 0;
  const relatedQueries = result?.relatedQueries || [];
  const hasFilters =
    Boolean(categoryId) ||
    levels.length > 0 ||
    Boolean(minRating) ||
    priceType !== "all" ||
    Boolean(min) ||
    Boolean(max);
  const activeFilterCount =
    (categoryId ? 1 : 0) +
    levels.length +
    (minRating ? 1 : 0) +
    (priceType !== "all" ? 1 : 0) +
    (min || max ? 1 : 0);

  return (
    <div className={styles.page}>
      <section className={styles.meta}>
        <div className={styles.inner}>
          <nav className={styles.breadcrumb} aria-label="Breadcrumb">
            <Link to="/">Trang chủ</Link>
            <span>/</span>
            <span>Tìm kiếm</span>
            <span>/</span>
            <span className={styles.crumbCurrent}>
              {keyword ? `"${keyword}"` : "Tất cả khóa học"}
            </span>
          </nav>

          <h1>
            {loading && !result ? (
              "Đang tìm kiếm..."
            ) : error ? (
              keyword ? (
                <>
                  Tìm kiếm <span>"{keyword}"</span>
                </>
              ) : (
                "Tìm kiếm khóa học"
              )
            ) : keyword ? (
              <>
                {formatCount(total)} kết quả cho <span>"{keyword}"</span>
              </>
            ) : (
              <>{formatCount(total)} khóa học</>
            )}
          </h1>

          {relatedQueries.length > 0 && (
            <div className={styles.related}>
              <span>{keyword ? "Tìm kiếm liên quan:" : "Danh mục nổi bật:"}</span>
              {relatedQueries.map((query) => (
                <button
                  key={query}
                  type="button"
                  onClick={() =>
                    updateParams({
                      keyword: query,
                      categoryId: "",
                      levels: [],
                      minRating: "",
                      priceType: "",
                      min: "",
                      max: "",
                    })
                  }
                >
                  {query}
                </button>
              ))}
            </div>
          )}

          <div className={styles.toolbar}>
            <button
              type="button"
              className={styles.filterToggle}
              onClick={() => setFiltersOpen((open) => !open)}
            >
              Bộ lọc{activeFilterCount > 0 ? ` (${activeFilterCount})` : ""}
            </button>
            <label className={styles.sort} htmlFor="sort-select">
              <span>Sắp xếp theo:</span>
              <select
                id="sort-select"
                value={sort}
                onChange={(event) => updateParams({ sort: event.target.value })}
              >
                {SORT_OPTIONS.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </label>
          </div>
        </div>
      </section>

      <main className={styles.main}>
        <div className={styles.inner}>
          <div className={styles.layout}>
            <aside className={clsx(styles.sidebar, filtersOpen && styles.sidebarOpen)}>
              <div className={styles.sidebarHead}>
                <h2>Bộ lọc tìm kiếm</h2>
                <button type="button" onClick={clearFilters} disabled={!hasFilters}>
                  Xóa tất cả bộ lọc
                </button>
              </div>

              <section className={styles.filterBlock}>
                <h3>
                  Danh mục
                  <FontAwesomeIcon icon={faChevronDown} />
                </h3>
                <div className={styles.options}>
                  {categories.length === 0 && !loading && !error && (
                    <p className={styles.emptyFilter}>Chưa có danh mục.</p>
                  )}
                  {categories.map((category) => {
                    const id = String(category.id);
                    return (
                      <label key={id} className={styles.checkRow}>
                        <span>
                          <input
                            type="radio"
                            name="category-filter"
                            checked={categoryId === id}
                            onChange={() => updateParams({ categoryId: id })}
                            onClick={() => {
                              if (categoryId === id) {
                                updateParams({ categoryId: "" });
                              }
                            }}
                          />
                          {category.name}
                        </span>
                      </label>
                    );
                  })}
                </div>
              </section>

              <section className={styles.filterBlock}>
                <h3>
                  Đánh giá
                  <FontAwesomeIcon icon={faChevronDown} />
                </h3>
                <div className={styles.options}>
                  {RATING_OPTIONS.map((option) => (
                    <label key={option.value} className={styles.checkRow}>
                      <span>
                        <input
                          type="radio"
                          name="rating-filter"
                          checked={minRating === option.value}
                          onChange={() => updateParams({ minRating: option.value })}
                          onClick={() => {
                            if (minRating === option.value) {
                              updateParams({ minRating: "" });
                            }
                          }}
                        />
                        <span className={styles.stars} aria-hidden>
                          {Array.from({ length: option.stars }, (_, index) => (
                            <FontAwesomeIcon key={index} icon={faStar} />
                          ))}
                        </span>
                        <span className={styles.ratingLabel}>{option.label}</span>
                      </span>
                    </label>
                  ))}
                </div>
              </section>

              <section className={styles.filterBlock}>
                <h3>
                  Khoảng giá
                  <FontAwesomeIcon icon={faChevronDown} />
                </h3>
                <div className={styles.options}>
                  {[
                    { value: "all", label: "Tất cả" },
                    { value: "paid", label: "Trả phí" },
                    { value: "free", label: "Miễn phí" },
                  ].map((option) => (
                    <label key={option.value} className={styles.checkRow}>
                      <span>
                        <input
                          type="radio"
                          name="price-type"
                          checked={priceType === option.value}
                          onChange={() =>
                            updateParams({
                              priceType: option.value === "all" ? "" : option.value,
                              ...(option.value === "free" ? { min: "", max: "" } : {}),
                            })
                          }
                        />
                        {option.label}
                      </span>
                    </label>
                  ))}
                </div>
                {priceType !== "free" && (
                  <div className={styles.priceBox}>
                    <div className={styles.priceGrid}>
                      <label>
                        Từ
                        <input
                          value={formatMoneyInput(priceDraft.min)}
                          onChange={(event) =>
                            setPriceDraft((draft) => ({
                              ...draft,
                              min: digitsOnly(event.target.value),
                            }))
                          }
                        />
                      </label>
                      <label>
                        Đến
                        <input
                          value={formatMoneyInput(priceDraft.max)}
                          onChange={(event) =>
                            setPriceDraft((draft) => ({
                              ...draft,
                              max: digitsOnly(event.target.value),
                            }))
                          }
                        />
                      </label>
                    </div>
                    <button type="button" onClick={applyPrice}>
                      Áp dụng
                    </button>
                  </div>
                )}
              </section>

              <section className={clsx(styles.filterBlock, styles.filterBlockLast)}>
                <h3>
                  Cấp độ
                  <FontAwesomeIcon icon={faChevronDown} />
                </h3>
                <div className={styles.options}>
                  {LEVEL_OPTIONS.map((level) => (
                    <label key={level.key} className={styles.checkRow}>
                      <span>
                        <input
                          type="checkbox"
                          checked={levels.includes(level.key)}
                          onChange={() => toggleListValue("levels", level.key, levels)}
                        />
                        {level.label}
                      </span>
                    </label>
                  ))}
                </div>
              </section>
            </aside>

            <section className={styles.results}>
              {loading && <p className={styles.status}>Đang tải khóa học...</p>}
              {error && <p className={styles.status}>{error}</p>}
              {!loading && !error && courses.length === 0 && (
                <div className={styles.empty}>
                  <h2>Không tìm thấy khóa học phù hợp</h2>
                  <p>Thử từ khóa khác hoặc bỏ bớt bộ lọc.</p>
                  {hasFilters && (
                    <button type="button" onClick={clearFilters}>
                      Xóa bộ lọc
                    </button>
                  )}
                </div>
              )}

              {courses.map((course) => {
                const isFree = Number(course.price) === 0;
                return (
                  <article key={course.id} className={styles.card}>
                    <div className={styles.thumb}>
                      <img src={course.thumbnailUrl} alt={course.title} />
                    </div>
                    <div className={styles.cardBody}>
                      <div>
                        <h3>{course.title}</h3>
                        <p className={styles.instructor}>
                          Giảng viên: <span>{course.instructorName || "Giảng viên MótEdu"}</span>
                        </p>
                        {course.shortDescription && (
                          <p className={styles.desc}>{course.shortDescription}</p>
                        )}
                        <div className={styles.metaRow}>
                          <div className={styles.rating}>
                            <strong>{Number(course.rating || 0).toFixed(1)}</strong>
                            <FontAwesomeIcon icon={faStar} />
                            <span>({formatCount(course.reviewCount)} đánh giá)</span>
                          </div>
                          {course.subcategoryName && (
                            <>
                              <span className={styles.dot}>•</span>
                              <span>{course.subcategoryName}</span>
                            </>
                          )}
                          {course.level && (
                            <>
                              <span className={styles.dot}>•</span>
                              <span className={styles.level}>
                                {LEVEL_LABELS[course.level] || course.level}
                              </span>
                            </>
                          )}
                        </div>
                      </div>
                      <div className={styles.priceRow}>
                        <strong className={clsx(isFree && styles.freePrice)}>
                          {formatPrice(course.price)}
                        </strong>
                        <button type="button" className={clsx(isFree && styles.freeAction)}>
                          {isFree ? "Học ngay" : "Thêm vào giỏ"}
                        </button>
                      </div>
                    </div>
                  </article>
                );
              })}

              {!loading && !error && totalPages > 1 && (
                <div className={styles.pagination}>
                  <button
                    type="button"
                    aria-label="Trang trước"
                    disabled={page <= 1}
                    onClick={() => goToPage(page - 1)}
                  >
                    <FontAwesomeIcon icon={faChevronLeft} />
                  </button>
                  {buildPages(page, totalPages).map((item) =>
                    typeof item === "string" ? (
                      <span key={item} className={styles.ellipsis}>
                        ...
                      </span>
                    ) : (
                      <button
                        key={item}
                        type="button"
                        className={clsx(item === page && styles.pageActive)}
                        onClick={() => goToPage(item)}
                      >
                        {item}
                      </button>
                    )
                  )}
                  <button
                    type="button"
                    aria-label="Trang tiếp"
                    disabled={page >= totalPages}
                    onClick={() => goToPage(page + 1)}
                  >
                    <FontAwesomeIcon icon={faChevronRight} />
                  </button>
                </div>
              )}
            </section>
          </div>
        </div>
      </main>
    </div>
  );
};

export default Search;
