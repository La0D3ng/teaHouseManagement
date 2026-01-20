package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.dto.roomReviews.AddReviewRequest;
import com.example.demo.dto.roomReviews.RoomReviewsInfo;
import com.example.demo.entity.RoomReviews;

import java.util.List;

public interface IRoomReviewService extends IService<RoomReviews> {
    // 新增评价，只有在订单状态为已完成时才能进行评论
    boolean addReviews(AddReviewRequest reviewsInfo);

    // 删除评价
    boolean deleteReviews(Long reviewId);

    // 修改评价，评论表中必须有这条评论才能进行修改
    boolean updateReviews(Long reviewId, Integer rating, String reviewContent);

    // 显示某用户的所有评价
    List<RoomReviewsInfo> showAllReviews(Long userId);
}
