import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { SearchOutlined } from "@ant-design/icons";
import styles from "./Header.module.scss";

const HeaderSearch = () => {
  const [keyword, setKeyword] = useState("");
  const navigate = useNavigate();

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!keyword.trim()) return;
    navigate(`/search?keyword=${encodeURIComponent(keyword.trim())}`);
  };

  return (
    <form className={styles.search} onSubmit={handleSubmit}>
      <SearchOutlined className={styles.searchIcon} />
      <input
        value={keyword}
        onChange={(e) => setKeyword(e.target.value)}
        placeholder="Tìm kiếm khóa học..."
      />
    </form>
  );
};

export default HeaderSearch;
