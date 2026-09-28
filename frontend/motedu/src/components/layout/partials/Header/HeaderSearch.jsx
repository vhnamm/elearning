import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { CloseOutlined, SearchOutlined } from "@ant-design/icons";
import styles from "./Header.module.scss";

const HeaderSearch = () => {
  const [searchParams] = useSearchParams();
  const urlKeyword = searchParams.get("keyword") ?? "";
  const [keyword, setKeyword] = useState(urlKeyword);
  const navigate = useNavigate();

  useEffect(() => {
    setKeyword(urlKeyword);
  }, [urlKeyword]);

  const handleSubmit = (e) => {
    e.preventDefault();
    const value = keyword.trim();
    navigate(value ? `/search?keyword=${encodeURIComponent(value)}` : "/search");
  };

  const clearKeyword = () => {
    setKeyword("");
    if (urlKeyword) {
      navigate("/search");
    }
  };

  return (
    <form className={styles.search} onSubmit={handleSubmit}>
      <SearchOutlined className={styles.searchIcon} />
      <input
        value={keyword}
        onChange={(e) => setKeyword(e.target.value)}
        placeholder="Tìm kiếm khóa học, giảng viên, kỹ năng..."
        aria-label="Tìm kiếm khóa học"
      />
      {keyword && (
        <button
          type="button"
          className={styles.searchClear}
          aria-label="Xóa từ khóa"
          onClick={clearKeyword}
        >
          <CloseOutlined />
        </button>
      )}
    </form>
  );
};

export default HeaderSearch;
