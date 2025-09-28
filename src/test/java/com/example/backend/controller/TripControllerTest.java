package com.example.backend.controller;

import com.example.backend.domain.Drone;
import com.example.backend.domain.Trip;
import com.example.backend.exception.GlobalExceptionHandler;
import com.example.backend.service.TripService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TripController.class)
@Import(GlobalExceptionHandler.class)
class TripControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockitoBean TripService trips;

    @Test
    void create_ok() throws Exception {
        Trip saved = new Trip();
        saved.setId(5L);
        Drone d = new Drone(); d.setId(1L);
        saved.setDrone(d);
        saved.setStatus(Trip.Status.PLANNED);

        Mockito.when(trips.create(any(Trip.class))).thenReturn(saved);

        var req = Map.of("droneId", 1, "totalWeight", 1.2, "totalDistanceKm", 3.4);
        mvc.perform(post("/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req)))
           .andExpect(status().isCreated())
           .andExpect(header().string("Location", "/trips/5"))
           .andExpect(jsonPath("$.id", is(5)))
           .andExpect(jsonPath("$.status", is("PLANNED")));
    }

    @Test
    void delete_conflictWhenNotPlanned() throws Exception {
        Mockito.doThrow(new IllegalStateException("Só PLANNED pode excluir"))
               .when(trips).deleteIfPlanned(7L);

        mvc.perform(delete("/trips/7"))
           .andExpect(status().isConflict())
           .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    void patch_times_ok() throws Exception {
        Trip t = new Trip(); t.setId(9L);
        t.setStartAt(Instant.parse("2025-01-01T00:00:00Z"));
        t.setFinishAt(Instant.parse("2025-01-01T00:10:00Z"));

        Mockito.when(trips.updateStartTime(eq(9L), any(Instant.class))).thenReturn(t);

        var req = Map.of("startAt", "2025-01-01T00:00:00Z");
        mvc.perform(patch("/trips/9/times")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req)))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.startAt", is("2025-01-01T00:00:00Z")));
    }

    @Test
    void list_filtersInMemory_ok() throws Exception {
        Trip a = new Trip(); a.setId(1L); a.setStatus(Trip.Status.PLANNED);
        Trip b = new Trip(); b.setId(2L); b.setStatus(Trip.Status.FINISHED);
        Mockito.when(trips.listAll()).thenReturn(List.of(a,b));

        mvc.perform(get("/trips").param("status","PLANNED"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$", hasSize(1)))
           .andExpect(jsonPath("$[0].status", is("PLANNED")));
    }
}