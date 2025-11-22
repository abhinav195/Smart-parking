package com.example.smartparking.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dummy")
public class DummyController {
    @PostMapping("/check-in")
    ResponseEntity<String> checkIn(@Valid @RequestBody CheckInRequest request){
        return ResponseEntity.ok().build();
    }
}
