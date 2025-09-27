package com.example.backend.controller;

import com.example.backend.domain.Order;
import com.example.backend.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orders;

    public OrderController(OrderService orders) {
        this.orders = orders;
    }

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody Order body) {
        Order saved = orders.create(body);
        return ResponseEntity
                .created(URI.create("/orders/" + saved.getId()))
                .body(saved);
    }

    @GetMapping
    public List<Order> list(@RequestParam(value = "status", required = false) Order.Status status) {
        if (status != null) {
            return orders.listByStatus(status);
        }

        return orders.listAll();
    }

    @GetMapping("/{id}")
    public Order get(@PathVariable Long id) {
        return orders.getById(id);
    }

    @PatchMapping("/{id}")
    public Order updateBasic(@PathVariable Long id, @RequestBody UpdateOrderRequest req) {
        return orders.updateBasicFields(id, req.customerX, req.customerY, req.weightKg, req.priority);
    }

    @PatchMapping("/{id}/status")
    public Order updateStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest req) {
        return orders.updateStatus(id, req.status);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIfPending(@PathVariable Long id) {
        orders.deleteIfPending(id);
    }

    public static class UpdateOrderRequest {
        public Integer customerX;
        public Integer customerY;
        public Double weightKg;
        public Order.Priority priority;
    }

    public static class UpdateStatusRequest {
        public Order.Status status;
    }
}
