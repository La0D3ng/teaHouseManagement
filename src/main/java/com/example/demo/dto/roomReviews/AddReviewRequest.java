package com.example.demo.dto.roomReviews;

import lombok.Data;

@Data
public class AddReviewRequest {
    private Long reservationId;  // 预定ID
    private Long userId;         // 用户ID
    private Integer rating;      // 评分（1-5）
    private String reviewContent; // 评价内容
}
