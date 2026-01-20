package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.dto.roomReviews.RoomReviewsInfo;
import com.example.demo.entity.RoomReviews;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RoomReviewsMapper extends BaseMapper<RoomReviews> {
    // 显示某用户的所有评价
    @Select("SELECT review_id, RR.reservation_id, room_name, username, rating, review_content " +
            "FROM room_reviews RR " +
            "INNER JOIN rooms R ON RR.room_id = R.room_id " +
            "INNER JOIN users U ON RR.user_id = U.user_id " +
            "INNER JOIN reservations RE ON RR.reservation_id = RE.reservation_id " +
            "WHERE RR.user_id = #{userId}")
    List<RoomReviewsInfo> showAllReviews(@Param("userId") Long userId);

    // 检查某预定是否已经评价
    @Select("SELECT COUNT(*) FROM room_reviews WHERE reservation_id = #{reservationId}")
    int checkReviewExists(@Param("reservationId") Long reservationId);
}
