package com.example.demo.dto.rooms;
import lombok.Data;

@Data
public class AddRoomRequest {
    private String roomName;
    private String capacity;
    private String features;
    private String hourlyRate;
    private String roomType;
    private String description;
    private String imageUrls;
    private String status;
}
