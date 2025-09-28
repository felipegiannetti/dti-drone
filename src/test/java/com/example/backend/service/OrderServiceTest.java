package com.example.backend.service;

import com.example.backend.domain.Order;
import com.example.backend.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class OrderServiceTest {

    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepo;

    @BeforeEach
    void setup() {
        orderRepo.deleteAll();
    }

    @Test
    void create_deveIniciarComoPending_ePrioridadeDefaultLow() {
        Order o = new Order();
        o.setCustomerX(2);
        o.setCustomerY(3);
        o.setWeightKg(2.0);
        o.setPriority(null);

        Order saved = orderService.create(o);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(Order.Status.PENDING);
        assertThat(saved.getPriority()).isEqualTo(Order.Priority.LOW);
    }

    @Test
    void updateBasicFields_alteraDadosPrincipais() {
        Order o = new Order();
        o.setCustomerX(1);
        o.setCustomerY(1);
        o.setWeightKg(1.0);
        o.setPriority(Order.Priority.LOW);
        o = orderService.create(o);

        Order updated = orderService.updateBasicFields(o.getId(), 5, 6, 3.5, Order.Priority.HIGH);

        assertThat(updated.getCustomerX()).isEqualTo(5);
        assertThat(updated.getCustomerY()).isEqualTo(6);
        assertThat(updated.getWeightKg()).isEqualTo(3.5);
        assertThat(updated.getPriority()).isEqualTo(Order.Priority.HIGH);
    }

    @Test
    void deleteIfPending_soPermiteQuandoStatusPending() {
        Order o = new Order();
        o.setCustomerX(0);
        o.setCustomerY(0);
        o.setWeightKg(1.0);
        o.setPriority(Order.Priority.MEDIUM);
        o = orderService.create(o);
        
        orderService.deleteIfPending(o.getId());
        assertThat(orderRepo.findById(o.getId())).isEmpty();

        final Order o2 = new Order();
        o2.setCustomerX(0);
        o2.setCustomerY(0);
        o2.setWeightKg(1.0);
        o2.setPriority(Order.Priority.MEDIUM);
        Order createdO2 = orderService.create(o2);
        orderService.updateStatus(createdO2.getId(), Order.Status.PLANNED);
        assertThatThrownBy(() -> orderService.deleteIfPending(createdO2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void listByStatus_filtraCorretamente() {
        Order o1 = new Order();
        o1.setCustomerX(1);
        o1.setCustomerY(1);
        o1.setWeightKg(1.0);
        o1.setPriority(Order.Priority.LOW);
        orderService.create(o1);
        
        Order planned = new Order();
        planned.setCustomerX(2);
        planned.setCustomerY(2);
        planned.setWeightKg(1.0);
        planned.setPriority(Order.Priority.LOW);
        planned = orderService.create(planned);
        orderService.updateStatus(planned.getId(), Order.Status.PLANNED);

        List<Order> pendings = orderService.listByStatus(Order.Status.PENDING);
        List<Order> plannedList = orderService.listByStatus(Order.Status.PLANNED);

        assertThat(pendings).hasSize(1);
        assertThat(plannedList).hasSize(1);
    }
}
