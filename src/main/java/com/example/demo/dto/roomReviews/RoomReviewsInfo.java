package com.example.demo.dto.roomReviews;

import lombok.Data;

@Data
public class RoomReviewsInfo {
    private Long reviewId;
    private Long reservationId;
    private Long userId;
    private Long roomId;
    private String roomName;
    private String username;
    private Integer rating;
    private String reviewContent;
}
