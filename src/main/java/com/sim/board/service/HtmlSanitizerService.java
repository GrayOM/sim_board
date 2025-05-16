// src/main/java/com/sim/board/service/HtmlSanitizerService.java
package com.sim.board.service;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Service;

@Service
public class HtmlSanitizerService {

    // 게시글 컨텐츠용 정책 (기본 서식 허용)
    private final PolicyFactory contentPolicy = new HtmlPolicyBuilder()
            .allowElements("b", "i", "u", "strong", "em", "p", "br", "ul", "ol", "li", "h1", "h2", "h3", "h4", "h5", "h6")
            .allowCommonBlockElements()
            .allowCommonInlineFormattingElements()
            .toFactory();

    // 댓글용 정책 (최소한의 서식만 허용)
    private final PolicyFactory commentPolicy = new HtmlPolicyBuilder()
            .allowElements("b", "i", "u", "strong", "em", "br")
            .toFactory();

    // 사용자 입력용 정책 (모든 HTML 태그 제거)
    private final PolicyFactory strictPolicy = new HtmlPolicyBuilder()
            .toFactory();

    // 게시글 내용 살균 (기본 서식 허용)
    public String sanitizeContent(String html) {
        if (html == null) {
            return null;
        }
        return contentPolicy.sanitize(html);
    }

    // 댓글 내용 살균 (최소한의 서식만 허용)
    public String sanitizeComment(String html) {
        if (html == null) {
            return null;
        }
        return commentPolicy.sanitize(html);
    }

    // 모든 HTML 제거 (사용자 이름, 이메일 등에 사용)
    public String sanitizeStrict(String html) {
        if (html == null) {
            return null;
        }
        return strictPolicy.sanitize(html);
    }
}