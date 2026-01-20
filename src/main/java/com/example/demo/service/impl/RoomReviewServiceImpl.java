package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.dto.roomReviews.AddReviewRequest;
import com.example.demo.dto.roomReviews.RoomReviewsInfo;
import com.example.demo.entity.Reservations;
import com.example.demo.entity.RoomReviews;
import com.example.demo.mapper.RoomReviewsMapper;
import com.example.demo.service.IReservationsService;
import com.example.demo.service.IRoomReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoomReviewServiceImpl extends ServiceImpl<RoomReviewsMapper, RoomReviews> implements IRoomReviewService {
    @Autowired
    private RoomReviewsMapper roomReviewsMapper;
    
    @Autowired
    private IReservationsService reservationsService;

    // 新增评价 - 只有订单状态为已完成时才能进行评论
    @Override
    @Transactional
    public boolean addReviews(AddReviewRequest reviewsInfo) {
        // 参数验证
        if (reviewsInfo.getReservationId() == null) {
            throw new RuntimeException("预定ID不能为空");
        }
        if (reviewsInfo.getUserId() == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        if (reviewsInfo.getRating() == null || reviewsInfo.getRating() < 1 || reviewsInfo.getRating() > 5) {
            throw new RuntimeException("评分必须在1-5之间");
        }
        
        // 验证预定单是否存在
        Reservations reservation = reservationsService.getById(reviewsInfo.getReservationId());
        if (reservation == null) {
            throw new RuntimeException("预定单不存在");
        }
        
        // 验证预定单是否属于该用户
        if (!reservation.getUserId().equals(reviewsInfo.getUserId())) {
            throw new RuntimeException("您无权对此预定单进行评价");
        }
        
        // 验证预定单状态是否为已完成
        if (reservation.getReservationStatus() != Reservations.ReservationStatus.completed) {
            throw new RuntimeException("只有已完成的预定才能进行评价");
        }
        
        // 检查是否已经评价过 -- 只准修改，不可以重复评价
        int existingReviewCount = roomReviewsMapper.checkReviewExists(reviewsInfo.getReservationId());
        if (existingReviewCount > 0) {
            throw new RuntimeException("该预定已经评价过，不能重复评价");
        }
        
        // 创建评价对象
        RoomReviews review = new RoomReviews();
        review.setReservationId(reviewsInfo.getReservationId());
        review.setRoomId(reservation.getRoomId());
        review.setUserId(reviewsInfo.getUserId());
        review.setRating(reviewsInfo.getRating());
        review.setReviewContent(reviewsInfo.getReviewContent());
        
        return this.save(review);
    }

    // 删除评价
    @Override
    @Transactional
    public boolean deleteReviews(Long reviewId) {
        if (reviewId == null) {
            throw new RuntimeException("评价ID不能为空");
        }
        
        // 验证评价是否存在
        RoomReviews review = this.getById(reviewId);
        if (review == null) {
            throw new RuntimeException("评价不存在");
        }
        
        return this.removeById(reviewId);
    }

    // 修改评价
    @Override
    @Transactional
    public boolean updateReviews(Long reviewId, Integer rating, String reviewContent) {
        if (reviewId == null) {
            throw new RuntimeException("评价ID不能为空");
        }
        
        // 验证评价是否存在
        RoomReviews existingReview = this.getById(reviewId);
        if (existingReview == null) {
            throw new RuntimeException("评价不存在");
        }
        
        // 至少需要修改一项
        if (rating == null && reviewContent == null) {
            throw new RuntimeException("请至少修改一项信息");
        }
        
        // 更新评分
        if (rating != null) {
            if (rating < 1 || rating > 5) {
                throw new RuntimeException("评分必须在1-5之间");
            }
            existingReview.setRating(rating);
        }
        
        // 更新评价内容
        if (reviewContent != null) {
            existingReview.setReviewContent(reviewContent);
        }
        // 更新评价信息
        return this.updateById(existingReview);
    }

    // 显示某用户的所有评价
    @Override
    public List<RoomReviewsInfo> showAllReviews(Long userId) {
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        
        return roomReviewsMapper.showAllReviews(userId);
    }
}
