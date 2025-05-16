// src/main/java/com/sim/board/dto/CommentDto.java
package com.sim.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentDto {

    @NotBlank(message = "댓글 내용은 필수 입력 항목입니다.")
    @Size(min = 2, max = 1000, message = "댓글은 2자 이상 1000자 이하여야 합니다.")
    private String content;
}