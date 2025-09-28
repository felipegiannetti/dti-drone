package com.example.backend.controller;

import com.example.backend.domain.Drone;
import com.example.backend.exception.GlobalExceptionHandler;
import com.example.backend.service.DroneService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DroneController.class)
@Import(GlobalExceptionHandler.class)
class DroneControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockitoBean DroneService drones;

    @Test
    void create_ok() throws Exception {
        Drone req = new Drone();
        req.setName("D1"); req.setCapacityKg(5); req.setRangeKm(20); req.setSpeedKmh(40); req.setBatteryPct(100);
        Drone saved = new Drone();
        saved.setId(1L); saved.setName("D1"); saved.setCapacityKg(5); saved.setRangeKm(20); saved.setSpeedKmh(40); saved.setBatteryPct(100);
        saved.setStatus(Drone.Status.IDLE);

        Mockito.when(drones.create(any(Drone.class))).thenReturn(saved);

        mvc.perform(post("/drones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req)))
           .andExpect(status().isCreated())
           .andExpect(header().string("Location", "/drones/1"))
           .andExpect(jsonPath("$.id", is(1)))
           .andExpect(jsonPath("$.status", is("IDLE")));
    }

    @Test
    void list_ok() throws Exception {
        Drone d = new Drone(); d.setId(1L); d.setName("D1"); d.setCapacityKg(5); d.setRangeKm(20); d.setSpeedKmh(40); d.setBatteryPct(100);
        Mockito.when(drones.listAll()).thenReturn(List.of(d));
        mvc.perform(get("/drones"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void updateBattery_badRequestMaps400() throws Exception {
        Mockito.when(drones.updateBattery(eq(1L), anyInt())).thenThrow(new IllegalArgumentException("batteryPct must be between 0 and 100"));
        var req = Map.of("batteryPct", 200);
        mvc.perform(patch("/drones/1/battery")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req)))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.status", is(400)));
    }
}