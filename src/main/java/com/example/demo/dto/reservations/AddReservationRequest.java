package com.example.demo.dto.reservations;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AddReservationRequest {
    private Long roomId;              // 包间ID
    private Long userId;              // 用户ID
    private LocalDate reservationDate; // 预定日期
    private LocalTime startTime;      // 开始时间
    private LocalTime endTime;        // 结束时间
    private Integer guestCount;       // 宾客人数
    private String specialRequirements; // 特殊需求
    private String contactPhone;      // 联系电话
    private String contactName;       // 联系人姓名
}
