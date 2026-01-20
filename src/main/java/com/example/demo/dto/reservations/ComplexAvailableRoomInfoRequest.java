package com.example.demo.dto.reservations;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ComplexAvailableRoomInfoRequest {
    private LocalDate reservationDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer capacity;
    private String features;
    private String roomType;
}
