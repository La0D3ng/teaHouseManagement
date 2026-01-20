package com.example.demo.dto.roomReviews;

import lombok.Data;

@Data
public class UpdateReviewRequest {
    private Long reviewId;       // 评价ID
    private Integer rating;      // 评分（1-5）
    private String reviewContent; // 评价内容
}
