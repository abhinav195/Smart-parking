package com.example.smartparking.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CheckInRequest(
        @NotBlank @Pattern(regexp = "^[A-Z0-9-]+$") String vehicleNumber,
        @NotBlank String spotSize
) { }
