package com.example.backend.controller;

import com.example.backend.domain.Order;
import com.example.backend.domain.TripStop;
import com.example.backend.exception.GlobalExceptionHandler;
import com.example.backend.service.TripStopService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TripStopController.class)
@Import(GlobalExceptionHandler.class)
class TripStopControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockitoBean TripStopService stops;

    @Test
    void create_ok() throws Exception {
        TripStop saved = new TripStop();
        saved.setId(1L); saved.setSeq(1); saved.setX(2); saved.setY(3);
        Order o = new Order(); o.setId(11L);
        saved.setOrder(o);

        Mockito.when(stops.create(eq(5L), eq(11L), eq(2), eq(3), isNull()))
               .thenReturn(saved);

        var req = Map.of("orderId", 11, "x", 2, "y", 3);
        mvc.perform(post("/trips/5/stops")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req)))
           .andExpect(status().isCreated())
           .andExpect(header().string("Location", "/trips/5/stops/1"))
           .andExpect(jsonPath("$.seq", is(1)))
           .andExpect(jsonPath("$.x", is(2)));
    }

    @Test
    void list_ok() throws Exception {
        TripStop s = new TripStop(); s.setId(1L); s.setSeq(1);
        Mockito.when(stops.listByTrip(5L)).thenReturn(List.of(s));
        mvc.perform(get("/trips/5/stops"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void markDelivered_ok() throws Exception {
        TripStop s = new TripStop(); s.setId(1L); s.setSeq(1); s.setDelivered(true);
        Mockito.when(stops.markDelivered(5L, 1)).thenReturn(s);

        mvc.perform(patch("/trips/5/stops/1/delivered"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.delivered", is(true)));
    }

    @Test
    void updateEstimates_ok() throws Exception {
        TripStop s = new TripStop(); s.setId(1L); s.setSeq(1);
        s.setEstimatedArrivalAt(Instant.parse("2025-01-01T00:05:00Z"));
        s.setEstimatedDepartureAt(Instant.parse("2025-01-01T00:06:00Z"));

        Mockito.when(stops.updateEstimates(eq(5L), eq(1),
                any(Instant.class), any(Instant.class))).thenReturn(s);

        var req = Map.of("estimatedArrivalAt","2025-01-01T00:05:00Z","estimatedDepartureAt","2025-01-01T00:06:00Z");
        mvc.perform(patch("/trips/5/stops/1/estimates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req)))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.estimatedArrivalAt", is("2025-01-01T00:05:00Z")));
    }

    @Test
    void deleteIfPlanned_ok204() throws Exception {
        mvc.perform(delete("/trips/5/stops/2"))
           .andExpect(status().isNoContent());
        Mockito.verify(stops).deleteIfPlanned(5L, 2);
    }

    @Test
    void get_notFound_mapsTo404() throws Exception {
        Mockito.when(stops.getByTripAndSeq(5L, 9))
               .thenThrow(new EntityNotFoundException("not found"));

        mvc.perform(get("/trips/5/stops/9"))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void move_ok204() throws Exception {
        var req = Map.of("toSeq", 1);
        mvc.perform(patch("/trips/5/stops/3/move")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req)))
           .andExpect(status().isNoContent());
        Mockito.verify(stops).move(5L, 3, 1);
    }
}