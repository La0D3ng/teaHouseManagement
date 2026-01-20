package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.dto.reservations.ReservationsInfo;
import com.example.demo.entity.Reservations;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Mapper
public interface ReservationsMapper extends BaseMapper<Reservations> {
    // ------- 包间预定页面 -------
    // 获取本月预定的总量
    @Select("SELECT COUNT(reservation_id) FROM reservations WHERE (reservation_status = 'confirmed' OR reservation_status = 'completed') AND created_at >= DATE_FORMAT(CURDATE(), '%Y-%m-01') AND created_at < DATE_FORMAT(CURDATE() + INTERVAL 1 MONTH, '%Y-%m-01')")
    Long getCurrentMonthTotalReservations();

    // 获取今日预定的总量
    @Select("SELECT COUNT(reservation_id) FROM reservations WHERE (reservation_status = 'confirmed' OR reservation_status = 'completed') AND created_at >= CURDATE() AND created_at < CURDATE() + INTERVAL 1 DAY")
    Long getTodayTotalReservations();

    // 检查指定时间段是否有预定冲突（排除已取消的预定）
    @Select("SELECT COUNT(*) FROM reservations WHERE room_id = #{roomId} AND reservation_date = #{date} " +
            "AND reservation_status != 'cancelled' " +
            "AND ((start_time < #{endTime} AND end_time > #{startTime}))")
    int checkTimeSlotConflict(@Param("roomId") Long roomId, 
                              @Param("date") LocalDate date, 
                              @Param("startTime") LocalTime startTime, 
                              @Param("endTime") LocalTime endTime);

//    // 获取指定日期的所有预定记录（用于判断时间段状态）
//    @Select("SELECT * FROM reservations WHERE room_id = #{roomId} AND reservation_date = #{date} " +
//            "AND reservation_status != 'cancelled' ORDER BY start_time")
//    List<Reservations> getReservationsByRoomAndDate(@Param("roomId") Long roomId, @Param("date") LocalDate date);

    // 检查时间段冲突（排除当前预定ID）
    @Select("SELECT COUNT(*) FROM reservations WHERE room_id = #{roomId} AND reservation_date = #{date} " +
            "AND reservation_status != 'cancelled' AND reservation_id != #{excludeReservationId} " +
            "AND ((start_time < #{endTime} AND end_time > #{startTime}))")
    int checkTimeSlotConflictExcluding(@Param("roomId") Long roomId,
                                        @Param("date") LocalDate date,
                                        @Param("startTime") LocalTime startTime,
                                        @Param("endTime") LocalTime endTime,
                                        @Param("excludeReservationId") Long excludeReservationId);

    // 获取某用户特定预定状态的所有预定信息
    @Select("SELECT reservation_id, room_name, username, reservation_date, start_time, end_time, guest_count, total_amount, reservation_status, special_requirements, contact_phone, contact_name, RE.created_at " +
            "FROM reservations RE " +
            "INNER JOIN rooms R ON RE.room_id = R.room_id " +
            "INNER JOIN users U ON RE.user_id = U.user_id " +
            "WHERE RE.user_id = #{userId} " +
            "AND reservation_status = #{reservationStatus}"
    )
    List<ReservationsInfo> getReservationInfo(@Param("userId") Long userId, @Param("reservationStatus") Reservations.ReservationStatus reservationStatus);

    // 调用取消预订的存储过程
    @Select("CALL cancel_reservation(#{reservationId}, #{userId}, @result, @message)")
    void callCancelReservation(@Param("reservationId") Long reservationId, @Param("userId") Long userId);

    // 获取存储过程的输出结果
    @Select("SELECT @result AS result, @message AS message")
    @Results({
        @Result(property = "result", column = "result"),
        @Result(property = "message", column = "message")
    })
    java.util.Map<String, Object> getCancelResult();

    // 根据预订ID获取房间ID
    @Select("SELECT room_id FROM reservations WHERE reservation_id = #{reservationId}")
    Long getRoomIdByReservationId(@Param("reservationId") Long reservationId);
}
