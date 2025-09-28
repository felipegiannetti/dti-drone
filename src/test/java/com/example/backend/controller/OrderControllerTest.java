package com.example.backend.controller;

import com.example.backend.domain.Order;
import com.example.backend.exception.GlobalExceptionHandler;
import com.example.backend.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrderController.class)
@Import(GlobalExceptionHandler.class)
class OrderControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockitoBean OrderService orders;

    @Test
    void create_ok_returns201AndBody() throws Exception {
        Order body = new Order();
        body.setCustomerX(2); body.setCustomerY(3); body.setWeightKg(2.0); body.setPriority(Order.Priority.HIGH);
        Order saved = new Order();
        saved.setCustomerX(2); saved.setCustomerY(3); saved.setWeightKg(2.0); saved.setPriority(Order.Priority.HIGH);
        saved.setId(10L);
        saved.setStatus(Order.Status.PENDING);

        Mockito.when(orders.create(any(Order.class))).thenReturn(saved);

        mvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
           .andExpect(status().isCreated())
           .andExpect(header().string("Location", "/orders/10"))
           .andExpect(jsonPath("$.id", is(10)))
           .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    void get_notFound_mapsTo404Json() throws Exception {
        Mockito.when(orders.getById(99L)).thenThrow(new EntityNotFoundException("Order not found"));

        mvc.perform(get("/orders/99"))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.status", is(404)))
           .andExpect(jsonPath("$.error", containsString("Not Found")));
    }

    @Test
    void list_withStatusFilter_ok() throws Exception {
        Order o = new Order();
        o.setCustomerX(1); o.setCustomerY(1); o.setWeightKg(1.0); o.setPriority(Order.Priority.LOW);
        o.setId(1L); o.setStatus(Order.Status.PENDING);
        Mockito.when(orders.listByStatus(Order.Status.PENDING)).thenReturn(List.of(o));

        mvc.perform(get("/orders").param("status", "PENDING"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$", hasSize(1)))
           .andExpect(jsonPath("$[0].status", is("PENDING")));
    }

    @Test
    void delete_conflictMapsTo409() throws Exception {
        Mockito.doThrow(new IllegalStateException("Cannot delete"))
               .when(orders).deleteIfPending(1L);

        mvc.perform(delete("/orders/1"))
           .andExpect(status().isConflict())
           .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    void patch_updateBasic_ok() throws Exception {
        Order updated = new Order();
        updated.setCustomerX(5); updated.setCustomerY(6); updated.setWeightKg(3.5); updated.setPriority(Order.Priority.HIGH);
        updated.setId(7L); updated.setStatus(Order.Status.PENDING);
        Mockito.when(orders.updateBasicFields(eq(7L), any(), any(), any(), any())).thenReturn(updated);

        var req = Map.of("customerX", 5, "customerY", 6, "weightKg", 3.5, "priority", "HIGH");

        mvc.perform(patch("/orders/7")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req)))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.customerX", is(5)))
           .andExpect(jsonPath("$.priority", is("HIGH")));
    }
}