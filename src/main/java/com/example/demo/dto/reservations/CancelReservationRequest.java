package com.example.demo.dto.reservations;

import lombok.Data;

@Data
public class CancelReservationRequest {
    private Long reservationId;
    private Long userId;
}
