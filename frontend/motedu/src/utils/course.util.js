// Giá 0 hiển thị "Miễn phí", giống thẻ khóa học ở trang quản lý khóa học của giảng viên.
export const formatPrice = (price) => {
  if (price === null || price === undefined || price === "") return "";
  const numPrice = Number(price);
  if (numPrice === 0) return "Miễn phí";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(numPrice);
};

// Chuẩn hóa số sao về 1 chữ số thập phân; chưa có đánh giá thì hiển thị "--".
export const formatRating = (rating) => {
  const numRating = Number(rating) || 0;
  if (numRating === 0) return "--";
  return numRating.toFixed(1);
};

export const formatReviewCount = (count) => {
  const n = Number(count) || 0;
  if (n >= 1000) {
    return `${(n / 1000).toFixed(1).replace(/\.0$/, "")}k đánh giá`;
  }
  return `${n} đánh giá`;
};

// Chuyển PublicCourseCardDto từ API thành dữ liệu hiển thị của thẻ khóa học.
export const mapCourseCard = (course) => ({
  id: course.id,
  title: course.title,
  instructor: course.instructorName || "Giảng viên MótEdu",
  rating: formatRating(course.rating),
  reviews: formatReviewCount(course.reviewCount),
  price: formatPrice(course.price),
  image: course.thumbnailUrl,
});
