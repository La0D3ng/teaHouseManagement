package com.example.demo.dto.rooms;

import com.example.demo.entity.Rooms;
import lombok.Data;

@Data
public class UpdateRoomsRequest {
    private Long roomId;
    private String roomName = null;
    private Integer capacity = -10086;
    private String features = null;
    private String hourlyRate = null;
    private String roomType = null;
    private String description = null;
    private String imageUrls = null;
    private Rooms.RoomStatus roomStatus;
}
