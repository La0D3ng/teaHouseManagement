package com.example.demo.dto.reservations;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class UpdateReservationRequest {
    private Long reservationId;        // 预定ID（必填）
    private LocalDate reservationDate; // 预定日期
    private LocalTime startTime;       // 开始时间
    private LocalTime endTime;         // 结束时间
    private Integer guestCount;        // 宾客人数
    private String specialRequirements; // 特殊需求
}
