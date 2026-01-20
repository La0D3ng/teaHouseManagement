package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.dto.reservations.AvailableRoomInfo;
import com.example.demo.dto.reservations.ReservationsInfo;
import com.example.demo.dto.reservations.TimeSlotStatus;
import com.example.demo.entity.Reservations;
import com.example.demo.entity.Rooms;
import com.example.demo.mapper.ReservationsMapper;
import com.example.demo.mapper.RoomsMapper;
import com.example.demo.service.IReservationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReservationsServiceImpl extends ServiceImpl<ReservationsMapper, Reservations> implements IReservationsService {
    @Autowired
    private ReservationsMapper reservationsMapper;
    
    @Autowired
    private RoomsMapper roomsMapper;

    // 返回本月预定总量
    @Override
    public Long getMonthTotal() {
        return reservationsMapper.getCurrentMonthTotalReservations();
    }

    // 返回今日预定总量
    @Override
    public Long getTodayTotal() {
        return reservationsMapper.getTodayTotalReservations();
    }

    // 检查指定时间段某包间状态
    @Override
    public boolean isTimeSlotAvailable(Long roomId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        if (roomId == null || date == null || startTime == null || endTime == null) {
            throw new RuntimeException("参数不能为空");
        }
        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new RuntimeException("结束时间必须晚于开始时间");
        }
        // 检查该时间段是否有预定
        int conflictCount = reservationsMapper.checkTimeSlotConflict(roomId, date, startTime, endTime);
        // 为 0 说明没有，> 0 说明该时间段有预定
        return conflictCount == 0;
    }

    // 获取指定包间在指定日期的所有时间段状态 - 只分可用和不可用
    @Override
    public List<TimeSlotStatus> getRoomTimeSlotStatus(Long roomId, LocalDate date) {
        List<TimeSlotStatus> timeSlotStatusList = new ArrayList<>();
        
        // 定义标准时间段区间(每个时间段之间间隔 30min 进行打扫)
        LocalTime[][] timeSlots = {
            {LocalTime.of(9, 0), LocalTime.of(11, 0)},
            {LocalTime.of(11, 30), LocalTime.of(13, 30)},
            {LocalTime.of(14, 0), LocalTime.of(16, 0)},
            {LocalTime.of(16, 30), LocalTime.of(18, 30)},
            {LocalTime.of(19, 0), LocalTime.of(21, 0)},
            {LocalTime.of(21, 30), LocalTime.of(23, 30)}
        };
        
        for (LocalTime[] slot : timeSlots) {
            // 开始时间
            LocalTime start = slot[0];
            // 结束时间
            LocalTime end = slot[1];
            // 时间段
            String timeSlotStr = start + "-" + end;
            // 判断当前时间段该包间是否有冲突
            boolean isAvailable = isTimeSlotAvailable(roomId, date, start, end);
            String status = isAvailable ? "available" : "occupied";
            
            timeSlotStatusList.add(new TimeSlotStatus(timeSlotStr, isAvailable, status));
        }
        
        return timeSlotStatusList;
    }

    // 新增预定单 -- 设置成事务，要么全部成功，要么全部回滚
    @Override
    @Transactional
    public boolean addReservation(Reservations reservation) {
        // 参数验证
        if (reservation.getRoomId() == null) {
            throw new RuntimeException("包间ID不能为空");
        }
        if (reservation.getUserId() == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        if (reservation.getReservationDate() == null) {
            throw new RuntimeException("预定日期不能为空");
        }
        if (reservation.getStartTime() == null || reservation.getEndTime() == null) {
            throw new RuntimeException("预定时间不能为空");
        }
        if (reservation.getGuestCount() == null || reservation.getGuestCount() <= 0) {
            throw new RuntimeException("宾客人数必须大于0");
        }
        
        // 验证预订时间不能是过去的时间
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        
        if (reservation.getReservationDate().isBefore(today)) {
            throw new RuntimeException("不能预订过去的日期");
        }
        
        // 如果是今天,检查预订开始时间是否已经过去
        if (reservation.getReservationDate().isEqual(today) && 
            reservation.getStartTime().isBefore(currentTime)) {
            throw new RuntimeException("预订时间已过,不能预订已经开始的时间段");
        }
        
        // 验证包间是否存在
        Rooms room = roomsMapper.selectById(reservation.getRoomId());
        if (room == null) {
            throw new RuntimeException("包间不存在");
        }
        
        // 验证宾客人数是否超过包间容量
        if (reservation.getGuestCount() > room.getCapacity()) {
            throw new RuntimeException("宾客人数超过包间容量限制");
        }
        
        // 检查时间段是否有冲突
        if (!isTimeSlotAvailable(reservation.getRoomId(), reservation.getReservationDate(), 
                                 reservation.getStartTime(), reservation.getEndTime())) {
            throw new RuntimeException("该时间段已被预定，请选择其他时间段");
        }
        
        // 计算预定小时数是否满足要求，总金额不必手动计算，会有触发器自动设置
        long hours = ChronoUnit.HOURS.between(reservation.getStartTime(), reservation.getEndTime());
        if (hours < 2) {
            throw new RuntimeException("预定时长必须至少两小时");
        }
        
        // 保存预定记录
        return this.save(reservation);
    }

    // 返回可预定包间的信息
    @Override
    public AvailableRoomInfo getAvailableRoomInfo(Long roomId, LocalDate date) {
        if (roomId == null) {
            throw new RuntimeException("包间ID不能为空");
        }
        if (date == null) {
            date = LocalDate.now(); // 默认查询今日
        }
        
        // 查询包间信息
        Rooms room = roomsMapper.selectById(roomId);
        if (room == null) {
            throw new RuntimeException("包间不存在");
        }
        
        // 获取该包间在指定日期的时间段状态
        List<TimeSlotStatus> timeSlotStatus = getRoomTimeSlotStatus(roomId, date);
        
        return new AvailableRoomInfo(room, timeSlotStatus);
    }

    // 修改预订单信息，@Transactional 声明成事务，此预订单更新，要么全部提交，要么全部回滚！
    @Override
    @Transactional
    public boolean updateReservation(Long reservationId, LocalDate reservationDate, LocalTime startTime, 
                                     LocalTime endTime, Integer guestCount, String specialRequirements) {
        // 参数验证
        if (reservationId == null) {
            throw new RuntimeException("预定ID不能为空");
        }
        
        // 获取原预定记录
        Reservations existingReservation = this.getById(reservationId);
        if (existingReservation == null) {
            throw new RuntimeException("预定记录不存在");
        }
        
        // 检查预定状态，已取消或已完成的不允许修改
        if (existingReservation.getReservationStatus() == Reservations.ReservationStatus.cancelled) {
            throw new RuntimeException("已取消的预定不能修改");
        }
        if (existingReservation.getReservationStatus() == Reservations.ReservationStatus.completed) {
            throw new RuntimeException("已完成的预定不能修改");
        }
        
        // 验证当前时间是否已超过预订开始时间
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        
        // 如果预订日期是今天,且当前时间已经超过预订开始时间,不允许修改
        if (existingReservation.getReservationDate().isEqual(today) && 
            currentTime.isAfter(existingReservation.getStartTime())) {
            throw new RuntimeException("预订已开始,不能修改订单");
        }
        
        // 如果预订日期在今天之前,不允许修改
        if (existingReservation.getReservationDate().isBefore(today)) {
            throw new RuntimeException("预订日期已过,不能修改订单");
        }
        
        // 获取包间信息
        Rooms room = roomsMapper.selectById(existingReservation.getRoomId());
        if (room == null) {
            throw new RuntimeException("包间不存在");
        }
        
        // 如果修改了日期，使用新日期，否则保持原日期
        LocalDate finalDate = reservationDate != null ? reservationDate : existingReservation.getReservationDate();
        
        // 如果修改了时间段
        LocalTime finalStartTime = startTime != null ? startTime : existingReservation.getStartTime();
        LocalTime finalEndTime = endTime != null ? endTime : existingReservation.getEndTime();
        
        // 验证时间合法性
        if (finalStartTime.isAfter(finalEndTime) || finalStartTime.equals(finalEndTime)) {
            throw new RuntimeException("结束时间必须晚于开始时间");
        }
        
        // 如果修改了时间段或日期，需要检查冲突
        if ((reservationDate != null && !reservationDate.equals(existingReservation.getReservationDate())) ||
            (startTime != null && !startTime.equals(existingReservation.getStartTime())) ||
            (endTime != null && !endTime.equals(existingReservation.getEndTime()))) {
            
            // 检查新时间段是否有冲突（排除当前预定）
            int conflictCount = reservationsMapper.checkTimeSlotConflictExcluding(
                existingReservation.getRoomId(), 
                finalDate, 
                finalStartTime, 
                finalEndTime,
                reservationId
            );
            
            if (conflictCount > 0) {
                throw new RuntimeException("该时间段已被预定，请选择其他时间段");
            }
        }
        
        // 如果修改了宾客人数
        Integer finalGuestCount = guestCount != null ? guestCount : existingReservation.getGuestCount();
        if (finalGuestCount <= 0) {
            throw new RuntimeException("宾客人数必须大于0");
        }
        if (finalGuestCount > room.getCapacity()) {
            throw new RuntimeException("宾客人数超过包间容量限制");
        }
        
        // 更新预定信息
        existingReservation.setReservationDate(finalDate);
        existingReservation.setStartTime(finalStartTime);
        existingReservation.setEndTime(finalEndTime);
        existingReservation.setGuestCount(finalGuestCount);
        
        // 如果提供了特殊需求，更新特殊需求
        if (specialRequirements != null) {
            existingReservation.setSpecialRequirements(specialRequirements);
        }
        
        // 保存更新
        return this.updateById(existingReservation);
    }

    // 修改订单状态 -- 修改订单状态为确认或者完成
    @Override
    public boolean updateReservationStatus(Long reservationId, String reservationStatus) {
        // 查询该订单是否存在
        Reservations oldReservations = this.getById(reservationId);
        if (oldReservations == null) {
            throw new RuntimeException("该订单不存在");
        }
        // 修改预订单状态
        oldReservations.setReservationStatus(Reservations.ReservationStatus.valueOf(reservationStatus));
        // 获取该预定单对应的包间
        Long roomId = reservationsMapper.getRoomIdByReservationId(reservationId);
        if (roomId == null) {
            throw new RuntimeException("该预订包间不存在");
        }
        Rooms room = roomsMapper.selectById(roomId);

        // 保存更新
        return this.updateById(oldReservations);
    }

    // 取消订单 -- 返回退款金额,如果取消失败,返回
    @Override
    @Transactional
    public Double cancelReservation(Long reservationId, Long userId) {
        // 参数验证
        if (reservationId == null) {
            throw new RuntimeException("预订ID不能为空");
        }
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        
        // 获取该预订对应的房间ID
        Long roomId = reservationsMapper.getRoomIdByReservationId(reservationId);
        if (roomId == null) {
            throw new RuntimeException("预订记录不存在");
        }
        
        // 验证当前时间是否已超过预订开始时间
        Reservations reservation = this.getById(reservationId);
        if (reservation == null) {
            throw new RuntimeException("预订记录不存在");
        }
        
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        
        // 如果预订日期是今天,且当前时间已经超过预订开始时间,不允许取消
        if (reservation.getReservationDate().isEqual(today) && 
            currentTime.isAfter(reservation.getStartTime())) {
            throw new RuntimeException("预订已开始,不能取消订单");
        }
        
        // 如果预订日期在今天之前,不允许取消
        if (reservation.getReservationDate().isBefore(today)) {
            throw new RuntimeException("预订日期已过,不能取消订单");
        }
        
        // 调用存储过程执行取消操作
        reservationsMapper.callCancelReservation(reservationId, userId);
        
        // 获取存储过程的执行结果
        java.util.Map<String, Object> result = reservationsMapper.getCancelResult();
        // 数据库返回的可能是Long类型,需要先转换为Number再获取intValue
        Object resultObj = result.get("result");
        Integer resultCode = resultObj != null ? ((Number) resultObj).intValue() : null;
        String message = (String) result.get("message"); // 成功 / 失败消息
        
        // 检查存储过程执行结果
        if (resultCode == null || resultCode == 0) {
            throw new RuntimeException(message != null ? message : "取消预订失败");
        }
        
        // 从消息中提取退款金额
        // 消息格式: "取消成功,可退款金额: XXX"
        try {
            String amountStr = message.substring(message.lastIndexOf(":") + 1).trim();
            return Double.parseDouble(amountStr);
        } catch (Exception e) {
            // 如果无法解析退款金额,返回0.00
            return 0.00;
        }
    }

    // 获取某用户的所有订单信息 -- 可自定义状态查询
    @Override
    public List<ReservationsInfo> getReservationsInfo(Long userId, String reservationStatus) {
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        if (reservationStatus == null || reservationStatus.trim().isEmpty()) {
            throw new RuntimeException("预定状态不能为空");
        }
        
        Reservations.ReservationStatus status = Reservations.ReservationStatus.valueOf(reservationStatus);
        return reservationsMapper.getReservationInfo(userId, status);
    }

    // 复合搜索筛选可用包间信息
    @Override
    public List<AvailableRoomInfo> getAviliableRoomInfoByCompoundQuery(LocalDate reservationDate,
                                                                       LocalTime startTime,
                                                                       LocalTime endTime,
                                                                       Integer capacity,
                                                                       String features,
                                                                       String roomType) {
        // 设置默认值
        LocalDate queryDate = reservationDate != null ? reservationDate : LocalDate.now();
        LocalTime queryStartTime = startTime != null ? startTime : LocalTime.of(9, 0);
        LocalTime queryEndTime = endTime != null ? endTime : LocalTime.of(23, 30);
        Rooms.RoomType queryRoomType = (roomType != null && !roomType.trim().isEmpty()) 
            ? Rooms.RoomType.valueOf(roomType) 
            : Rooms.RoomType.standard; // 默认为 standard
        
        // 验证时间合法性
        if (queryStartTime.isAfter(queryEndTime) || queryStartTime.equals(queryEndTime)) {
            throw new RuntimeException("结束时间必须晚于开始时间");
        }
        
        // 根据容量、特色、类型筛选包间
        List<Rooms> filteredRooms = roomsMapper.getRoomsByComplexQuery(capacity, features, queryRoomType);
        
        // 构建返回结果
        List<AvailableRoomInfo> result = new ArrayList<>();
        
        for (Rooms room : filteredRooms) {
            // 检查该包间在指定时间段是否可用
            boolean isAvailable = isTimeSlotAvailable(room.getRoomId(), queryDate, queryStartTime, queryEndTime);
            
            // 只返回可用的包间
            if (isAvailable) {
                // 获取该包间在指定日期的所有时间段状态
                List<TimeSlotStatus> timeSlotStatus = getRoomTimeSlotStatus(room.getRoomId(), queryDate);
                result.add(new AvailableRoomInfo(room, timeSlotStatus));
            }
        }
        
        return result;
    }
}
