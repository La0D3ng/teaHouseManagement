package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.dto.reservations.AvailableRoomInfo;
import com.example.demo.dto.reservations.ReservationsInfo;
import com.example.demo.dto.reservations.TimeSlotStatus;
import com.example.demo.entity.Reservations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface IReservationsService extends IService<Reservations> {
    // 获取今日某个时间段该包间的预定状态
    boolean isTimeSlotAvailable(Long roomId, LocalDate date, LocalTime startTime, LocalTime endTime);

    // 获取指定包间在指定日期的所有时间段状态
    List<TimeSlotStatus> getRoomTimeSlotStatus(Long roomId, LocalDate date);

    // 新增预定单，需要保证该时间段没有其他预定单冲突，否则无法预定
    boolean addReservation(Reservations reservation);

    // 返回可预定包间的信息，联合查询 Reservations 表和 Rooms 表
    AvailableRoomInfo getAvailableRoomInfo(Long roomId, LocalDate date);

    // 获取本月预定总量
    Long getMonthTotal();

    // 获取今日预定的总量
    Long getTodayTotal();

    // 修改预订单信息 -- 不可修改预定的房间！可以修改预定的时间段（保证该时间段该房间有空），guest_count,special_requirements
    boolean updateReservation(Long reservationId, LocalDate reservationDate, LocalTime startTime, LocalTime endTime, Integer guestCount, String specialRequirements);

    // 提交预订单 -- 修改预订单状态
    boolean updateReservationStatus(Long reservationId, String reservationStatus);

    // 获取某用户某特定状态的所有订单信息
    List<ReservationsInfo> getReservationsInfo(Long userId, String reservationStatus);

    // 复合搜索筛选可用包间信息 -- 查询日期（默认今天）、查询时间（默认全部时间段）、容纳人数（默认不限人数）、包间特色(features)、包间类型(roomType, 默认standard) -- 联合查询 reservations 和 rooms 表
    List<AvailableRoomInfo> getAviliableRoomInfoByCompoundQuery(LocalDate reservationDate, LocalTime startTime, LocalTime endTime, Integer capacity, String features, String roomType);

    // 取消订单
    Double cancelReservation(Long reservationId, Long userId);
}
