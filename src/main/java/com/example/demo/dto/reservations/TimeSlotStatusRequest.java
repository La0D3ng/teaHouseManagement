package com.example.demo.dto.reservations;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class TimeSlotStatusRequest {
    private Long roomId;              // 包间ID
    private LocalDate reservationDate; // 预定日期
    private LocalTime startTime;      // 开始时间
    private LocalTime endTime;        // 结束时间
}
