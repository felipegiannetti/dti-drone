package com.example.backend.service;

import com.example.backend.domain.Drone;
import com.example.backend.domain.Order;
import com.example.backend.domain.Trip;
import com.example.backend.domain.TripStop;
import com.example.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TripStopServiceTest {

    @Autowired TripStopService stopService;
    @Autowired TripService tripService;
    @Autowired OrderService orderService;
    @Autowired DroneService droneService;

    @Autowired TripStopRepository stopRepo;
    @Autowired TripRepository tripRepo;
    @Autowired OrderRepository orderRepo;
    @Autowired DroneRepository droneRepo;

    @BeforeEach
    void setup() {
        stopRepo.deleteAll();
        tripRepo.deleteAll();
        orderRepo.deleteAll();
        droneRepo.deleteAll();
    }

    private Drone newDrone() {
        Drone d = new Drone();
        d.setName("D-Stop");
        d.setCapacityKg(5.0);
        d.setRangeKm(20.0);
        d.setSpeedKmh(40.0);
        d.setBatteryPct(100);
        return droneService.create(d);
    }

    private Trip newPlannedTrip(Drone d) {
        Trip t = new Trip();
        t.setDrone(d);
        return tripService.create(t);
    }

    private Order newOrder(int x, int y, double w) {
        Order o = new Order();
        o.setCustomerX(x);
        o.setCustomerY(y);
        o.setWeightKg(w);
        o.setPriority(Order.Priority.MEDIUM);
        return orderService.create(o);
    }

    @Test
    void create_e_listByTrip_comSeqAutomatico() {
        Drone d = newDrone();
        Trip trip = newPlannedTrip(d);
        Order o1 = newOrder(2, 2, 1.0);
        Order o2 = newOrder(3, 3, 1.0);

        stopService.create(trip.getId(), o1.getId(), 2, 2, null);
        stopService.create(trip.getId(), o2.getId(), 3, 3, null);

        List<TripStop> list = stopService.listByTrip(trip.getId());
        assertThat(list).hasSize(2);
        assertThat(list.get(0).getSeq()).isEqualTo(1);
        assertThat(list.get(1).getSeq()).isEqualTo(2);
    }

    @Test
    void move_reordenaSequencia() {
        Drone d = newDrone();
        Trip trip = newPlannedTrip(d);
        Order o1 = newOrder(1, 1, 1.0);
        Order o2 = newOrder(2, 2, 1.0);
        Order o3 = newOrder(3, 3, 1.0);

        stopService.create(trip.getId(), o1.getId(), 1, 1, null);
        stopService.create(trip.getId(), o2.getId(), 2, 2, null);
        stopService.create(trip.getId(), o3.getId(), 3, 3, null);

        stopService.move(trip.getId(), 3, 1);
        List<TripStop> after = stopService.listByTrip(trip.getId());
        assertThat(after).extracting(TripStop::getSeq).containsExactly(1,2,3);
        assertThat(after.get(0).getOrder().getId()).isEqualTo(o3.getId());
        assertThat(after.get(1).getOrder().getId()).isEqualTo(o1.getId());
        assertThat(after.get(2).getOrder().getId()).isEqualTo(o2.getId());
    }

    @Test
    void deleteIfPlanned_removeECompactaSequencias() {
        Drone d = newDrone();
        Trip trip = newPlannedTrip(d);
        Order o1 = newOrder(1, 1, 1.0);
        Order o2 = newOrder(2, 2, 1.0);
        Order o3 = newOrder(3, 3, 1.0);

        stopService.create(trip.getId(), o1.getId(), 1, 1, null);
        stopService.create(trip.getId(), o2.getId(), 2, 2, null);
        stopService.create(trip.getId(), o3.getId(), 3, 3, null);

        stopService.deleteIfPlanned(trip.getId(), 2);

        List<TripStop> after = stopService.listByTrip(trip.getId());
        assertThat(after).hasSize(2);
        assertThat(after.get(0).getSeq()).isEqualTo(1);
        assertThat(after.get(1).getSeq()).isEqualTo(2);
        assertThat(after.get(0).getOrder().getId()).isEqualTo(o1.getId());
        assertThat(after.get(1).getOrder().getId()).isEqualTo(o3.getId());
    }

    @Test
    void markDelivered_atualizaOrderEFinalizaTripQuandoTodasEntregues() {
        Drone d = newDrone();
        Trip trip = newPlannedTrip(d);
        Order o1 = newOrder(1, 1, 1.0);
        Order o2 = newOrder(2, 2, 1.0);

        stopService.create(trip.getId(), o1.getId(), 1, 1, null);
        stopService.create(trip.getId(), o2.getId(), 2, 2, null);

        TripStop s1 = stopService.markDelivered(trip.getId(), 1);
        assertThat(s1.isDelivered()).isTrue();
        assertThat(tripService.getById(trip.getId()).getStatus()).isEqualTo(Trip.Status.PLANNED);

        TripStop s2 = stopService.markDelivered(trip.getId(), 2);
        assertThat(s2.isDelivered()).isTrue();

        Trip finished = tripService.getById(trip.getId());
        assertThat(finished.getStatus()).isEqualTo(Trip.Status.FINISHED);
        assertThat(finished.getFinishAt()).isNotNull();

        Order so1 = orderService.getById(o1.getId());
        Order so2 = orderService.getById(o2.getId());
        assertThat(so1.getStatus()).isEqualTo(Order.Status.DELIVERED);
        assertThat(so2.getStatus()).isEqualTo(Order.Status.DELIVERED);
    }

    @Test
    void updateEstimates_defineHorariosEstimados() {
        Drone d = newDrone();
        Trip trip = newPlannedTrip(d);
        Order o = newOrder(4, 4, 1.0);
        stopService.create(trip.getId(), o.getId(), 4, 4, null);

        Instant arr = Instant.now().plusSeconds(300);
        Instant dep = arr.plusSeconds(60);

        TripStop updated = stopService.updateEstimates(trip.getId(), 1, arr, dep);
        assertThat(updated.getEstimatedArrivalAt()).isEqualTo(arr);
        assertThat(updated.getEstimatedDepartureAt()).isEqualTo(dep);
    }
}