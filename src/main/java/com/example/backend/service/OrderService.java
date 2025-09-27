package com.example.backend.service;

import com.example.backend.domain.Order;
import com.example.backend.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepo;

    public OrderService(OrderRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    @Transactional
    public Order create(Order order) {
        validate(order);
        order.setId(null);

        if (order.getPriority() == null) {
            order.setPriority(Order.Priority.LOW);
        }
        
        order.setStatus(Order.Status.PENDING);

        return orderRepo.save(order);
    }

    @Transactional(readOnly = true)
    public List<Order> listAll() {
        return orderRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Order getById(Long id) {
        return orderRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + id));
    }

    @Transactional
    public Order updateBasicFields(Long id, Integer customerX, Integer customerY, Double weightKg, Order.Priority priority) {
        Order o = getById(id);

        if (customerX != null) o.setCustomerX(customerX);
        if (customerY != null) o.setCustomerY(customerY);
        if (weightKg != null)  o.setWeightKg(weightKg);
        if (priority != null)  o.setPriority(priority);

        validate(o);

        return orderRepo.save(o);
    }

    @Transactional
    public void deleteIfPending(Long id) {
        Order o = getById(id);

        if (o.getStatus() != Order.Status.PENDING) { //so pode excluir se ainda não foi planejado
            throw new IllegalStateException("Cannot delete order with status " + o.getStatus());
        }

        orderRepo.delete(o);
    }

    @Transactional(readOnly = true)
    public List<Order> listByStatus(Order.Status status) {
        return orderRepo.findByStatus(status);
    }

    @Transactional
    public Order updateStatus(Long id, Order.Status newStatus) {
        Order o = getById(id);

        o.setStatus(newStatus);

        return orderRepo.save(o);
    }

    private void validate(Order o) {
        if (o.getWeightKg() <= 0) {
            throw new IllegalArgumentException("weightKg must be > 0");
        }
    }
}