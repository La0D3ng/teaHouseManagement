package com.example.demo.dto.reservations;

import lombok.Data;

@Data
public class ReservationsInfoRequest {
    private Long userId;
    private String reservationStatus;
}
