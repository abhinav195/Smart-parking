package com.example.smartparking.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DummyController.class)
@Import(GlobalExceptionHandler.class)
public class GlobalExceptionHandlerWebTest {
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    org.springframework.test.web.servlet.MockMvc mockMvc;

    @Test
    void validation_errors_return_problem_detail() throws Exception {
        var bad = new CheckInRequest("abc-###", "");
        mockMvc.perform(post("/dummy/check-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.status").value("400"))
                .andExpect(jsonPath("$.errorCode").value("validation_failed"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }
}
