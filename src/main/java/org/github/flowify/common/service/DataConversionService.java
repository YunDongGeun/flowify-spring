package org.github.flowify.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.github.flowify.common.exception.BusinessException;
import org.github.flowify.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataConversionService {

    private final ObjectMapper objectMapper;

    public <T> T convert(Object source, Class<T> targetType) {
        try {
            return objectMapper.convertValue(source, targetType);
        } catch (IllegalArgumentException e) {
            log.error("데이터 변환 실패: {} -> {}", source.getClass().getSimpleName(), targetType.getSimpleName(), e);
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "데이터 형식 변환에 실패했습니다.");
        }
    }

    public Map<String, Object> toMap(Object source) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.convertValue(source, Map.class);
            return result;
        } catch (IllegalArgumentException e) {
            log.error("Map 변환 실패: {}", source.getClass().getSimpleName(), e);
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "데이터 형식 변환에 실패했습니다.");
        }
    }
}
