package com.example.backend.service;

import com.example.backend.domain.Drone;
import com.example.backend.domain.Order;
import com.example.backend.domain.Trip;
import com.example.backend.repository.DroneRepository;
import com.example.backend.repository.OrderRepository;
import com.example.backend.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class PlanningServiceTest {

    @Autowired PlanningService planning;
    @Autowired DroneService droneService;
    @Autowired OrderService orderService;
    @Autowired DroneRepository droneRepo;
    @Autowired OrderRepository orderRepo;
    @Autowired TripRepository tripRepo;

    @BeforeEach
    void setup() {
        tripRepo.deleteAll();
        orderRepo.deleteAll();
        droneRepo.deleteAll();
    }

    @Test
    void planAll_respeitaCapacidadeEAlcance_eGeraTrips() {
        Drone d = new Drone();
        d.setName("D-TST");
        d.setCapacityKg(5.0);
        d.setRangeKm(20.0);
        d.setSpeedKmh(40.0);
        d.setBatteryPct(100);
        d = droneService.create(d);

        Order o1 = new Order();
        o1.setCustomerX(2);
        o1.setCustomerY(2);
        o1.setWeightKg(3.0);
        o1.setPriority(Order.Priority.HIGH);
        orderService.create(o1);
        
        Order o2 = new Order();
        o2.setCustomerX(3);
        o2.setCustomerY(0);
        o2.setWeightKg(3.0);
        o2.setPriority(Order.Priority.LOW);
        orderService.create(o2);
        
        Order o3 = new Order();
        o3.setCustomerX(5);
        o3.setCustomerY(5);
        o3.setWeightKg(1.5);
        o3.setPriority(Order.Priority.HIGH);
        orderService.create(o3);
        
        Order o4 = new Order();
        o4.setCustomerX(8);
        o4.setCustomerY(8);
        o4.setWeightKg(0.5);
        o4.setPriority(Order.Priority.MEDIUM);
        orderService.create(o4);

        List<Trip> trips = planning.planAll();
        assertThat(trips).isNotEmpty();

        for (Trip t : trips) {
            assertThat(t.getTotalWeight()).isLessThanOrEqualTo(5.0 + 1e-6);
            assertThat(t.getTotalDistanceKm()).isLessThanOrEqualTo(20.0 + 1e-6);
            assertThat(t.getFinishAt()).isNotNull();
        }

        List<Order> planned = orderService.listByStatus(Order.Status.PLANNED);
        assertThat(planned).isNotEmpty();
    }
}
