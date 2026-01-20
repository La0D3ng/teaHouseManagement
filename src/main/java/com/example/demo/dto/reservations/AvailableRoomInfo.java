package com.example.demo.dto.reservations;

import com.example.demo.entity.Rooms;
import lombok.Data;
import java.util.List;

@Data
public class AvailableRoomInfo {
    private Rooms room;                          // 包间基本信息
    private List<TimeSlotStatus> timeSlotStatus; // 时间段状态列表
    
    public AvailableRoomInfo(Rooms room, List<TimeSlotStatus> timeSlotStatus) {
        this.room = room;
        this.timeSlotStatus = timeSlotStatus;
    }
}
