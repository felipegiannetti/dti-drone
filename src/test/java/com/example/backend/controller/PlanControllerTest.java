package com.example.backend.controller;

import com.example.backend.domain.Trip;
import com.example.backend.exception.GlobalExceptionHandler;
import com.example.backend.service.PlanningService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PlanController.class)
@Import(GlobalExceptionHandler.class)
class PlanControllerTest {

    @Autowired MockMvc mvc;

    @MockitoBean PlanningService planning;

    @Test
    void plan_ok_returnsTrips() throws Exception {
        Trip t = new Trip(); t.setId(1L);
        Mockito.when(planning.planAll()).thenReturn(List.of(t));

        mvc.perform(post("/plan"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$", hasSize(1)));
    }
}