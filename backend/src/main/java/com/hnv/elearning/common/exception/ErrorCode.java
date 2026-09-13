package com.hnv.elearning.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor

public enum ErrorCode {

    UNCATEGORIZED_EXCEPTION(9999, "Lỗi hệ thống không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    BAD_REQUEST(400, "Yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_KEY(401, "Cấu hình mã lỗi không hợp lệ", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(403, "Bạn không có quyền truy cập tài nguyên này", HttpStatus.FORBIDDEN),

    // ==========================================
    // 10xx: AUTHENTICATION & AUTHORIZATION
    // ==========================================
    UNAUTHENTICATED(1001, "Chưa xác thực danh tính", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS(1002, "Tài khoản hoặc mật khẩu không đúng", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED(1003, "Tài khoản đã bị khóa", HttpStatus.FORBIDDEN),
    ROLE_NOT_FOUND(1004, "Không tìm thấy vai trò (Role)", HttpStatus.NOT_FOUND),
    REFRESH_TOKEN_EXPIRED(1005, "Phiên đăng nhập đã hết hạn", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_INVALID(1006, "Invalid refresh token", HttpStatus.UNAUTHORIZED),

    // ==========================================
    // 20xx: USER MANAGEMENT
    // ==========================================
    USER_NOT_FOUND(2001, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(2002, "Người dùng đã tồn tại trên hệ thống", HttpStatus.CONFLICT),
    PASSWORD_INVALID(2003, "Mật khẩu phải từ 3-20 ký tự và chứa ít nhất 1 ký tự đặc biệt", HttpStatus.BAD_REQUEST),
    PENDING_REGISTRATION_NOT_FOUND(2004, "Không tìm thấy thông tin đăng ký hoặc phiên làm việc đã hết hạn", HttpStatus.BAD_REQUEST),

    // ==========================================
    // 30xx: OTP, VERIFICATION & NOTIFICATION
    // ==========================================
    OTP_INVALID(3001, "Mã OTP đã hết hạn hoặc không tồn tại. Vui lòng lấy mã mới!", HttpStatus.BAD_REQUEST),
    OTP_MAX_ATTEMPTS(3002, "Đã vượt quá số lần thử OTP cho phép", HttpStatus.BAD_REQUEST),
    SEND_MAIL_FAIL(3003, "Có lỗi xảy ra trong quá trình gửi email", HttpStatus.INTERNAL_SERVER_ERROR),

    // ==========================================
    // 40xx: CATEGORY MANAGEMENT
    // ==========================================
    CATEGORY_NOT_FOUND(4001, "Không tìm thấy danh mục", HttpStatus.NOT_FOUND),
    CATEGORY_INVALID_PARENT(4002, "Không thể chọn chính danh mục hoặc danh mục con của nó làm danh mục cha", HttpStatus.BAD_REQUEST),
    CATEGORY_NAME_DUPLICATED(4003, "Tên danh mục đã tồn tại", HttpStatus.CONFLICT);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;


}
