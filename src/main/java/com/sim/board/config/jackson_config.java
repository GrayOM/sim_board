// src/main/java/com/sim/board/config/jackson_config.java 파일 생성

package com.sim.board.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class jackson_config { //java 날짜 출력 타입 정하는 클래스

    @Bean
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperBuilder.json()
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // 날짜를 ISO 형식으로 직렬화
                .featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS) // 빈 객체 직렬화 오류 방지
                .modules(new JavaTimeModule()) // Java 8 날짜/시간 타입 지원
                .build();
    }
}