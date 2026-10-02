package com.hnv.elearning.feature.moderation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Locale;

@Getter
@AllArgsConstructor
public enum RejectReason {
    AUDIO_VIDEO_QUALITY("Chất lượng Video/Âm thanh (Mờ, rung giật, tạp âm, lệch tiếng)"),
    INCOMPLETE_CONTENT("Nội dung chưa đầy đủ (Thiếu bài tập thực hành, thiếu đáp án Quiz)"),
    POLICY_COPYRIGHT("Vi phạm chính sách bản quyền (Chứa watermark ngoài, nhạc vi phạm)"),
    MISLEADING_DESCRIPTION("Mô tả và giáo trình không khớp với nội dung thực tế"),
    TECHNICAL_R2("Lỗi liên kết lưu trữ Presigned R2/S3 không thể streaming");

    private final String label;

    @JsonCreator
    public static RejectReason from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return RejectReason.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
