package com.hnv.elearning.feature.course.service.impl;

import com.hnv.elearning.common.exception.AppException;
import com.hnv.elearning.common.exception.ErrorCode;
import com.hnv.elearning.feature.course.enums.LectureType;
import com.hnv.elearning.feature.course.service.LectureContentHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LectureContentHandlerRegistry {
    private Map<LectureType, LectureContentHandler> handlers;

    public LectureContentHandlerRegistry(List<LectureContentHandler> handlerList) {
        this.handlers = handlerList.stream().collect(
                Collectors.toMap(h -> h.type(), h->h)
        );

    }

    public  LectureContentHandler getHandler(LectureType lectureType) {
        LectureContentHandler handler = lectureType == null ? null : handlers.get(lectureType);
        if (handler == null) {
            throw new AppException(ErrorCode.LECTURE_CONTENT_TYPE_INVALID);
        }

        return handler;
    }
}
