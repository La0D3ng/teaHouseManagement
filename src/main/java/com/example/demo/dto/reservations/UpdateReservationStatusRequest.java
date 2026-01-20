package com.example.demo.dto.reservations;

import lombok.Data;

@Data
public class UpdateReservationStatusRequest {
    private Long reservationId;
    private String reservationStatus;
}
