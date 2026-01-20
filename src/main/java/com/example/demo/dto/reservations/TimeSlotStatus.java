package com.example.demo.dto.reservations;

import lombok.Data;

@Data
public class TimeSlotStatus {
    private String timeSlot; // 时间段
    private Boolean isAvailable; // 是否可预定
    private String status; // 预定状态
    
    public TimeSlotStatus(String timeSlot, Boolean isAvailable, String status) {
        this.timeSlot = timeSlot;
        this.isAvailable = isAvailable;
        this.status = status;
    }
}
