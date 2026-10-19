package com.hnv.elearning.feature.course.dto.instructor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CurriculumItemInstructorResponse {
    private Long id;
    private String type; //LECTURE | QUIZ
    private String title;
    private int position;
    private boolean isPreview;
    //lecture
    private String contentType; //VIDEO | DOCUMENT
    private String videoStatus;
    private String fileName;
    private Long fileSizeBytes;
    private String thumbnailUrl;
    private Integer videoDurationSeconds;
    //quiz
    Long questionCount;
    Integer passingScore;
}
