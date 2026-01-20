package com.example.demo.dto.reservations;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class ReservationsInfo {
    private Long reservationId;
    private String roomName;
    private String username;
    private LocalDate reservationDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer guestCount;
    private Double totalAmount;
    private String reservationStatus;
    private String specialRequirements;
    private String contactPhone;
    private String contactName;
    private LocalDateTime createdAt;
}
