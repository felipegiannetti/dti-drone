package com.example.backend.service;

import com.example.backend.domain.Drone;
import com.example.backend.domain.Trip;
import com.example.backend.repository.DroneRepository;
import com.example.backend.repository.OrderRepository;
import com.example.backend.repository.TripRepository;
import com.example.backend.repository.TripStopRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TripServiceTest {

    @Autowired TripService tripService;
    @Autowired DroneService droneService;
    @Autowired TripRepository tripRepo;
    @Autowired TripStopRepository stopRepo;
    @Autowired DroneRepository droneRepo;
    @Autowired OrderRepository orderRepo;

    @BeforeEach
    void setup() {
        stopRepo.deleteAll();
        tripRepo.deleteAll();
        orderRepo.deleteAll();
        droneRepo.deleteAll();
    }

    private Drone newDrone() {
        Drone d = new Drone();
        d.setName("D-Trip");
        d.setCapacityKg(5.0);
        d.setRangeKm(20.0);
        d.setSpeedKmh(40.0);
        d.setBatteryPct(100);
        return droneService.create(d);
    }

    @Test
    void create_ignoraFinishAt_eRequerDroneId() {
        Drone d = newDrone();

        Trip t = new Trip();
        t.setDrone(d);
        t.setTotalWeight(0.0);
        t.setTotalDistanceKm(0.0);
        t.setStartAt(Instant.now());
        t.setStatus(Trip.Status.PLANNED);
        t.setFinishAt(Instant.now());

        Trip saved = tripService.create(t);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFinishAt()).isNull();
        assertThat(saved.getStatus()).isEqualTo(Trip.Status.PLANNED);
    }

    @Test
    void deleteIfPlanned_soPermiteQuandoPlanned() {
        Drone d = newDrone();

        Trip t = new Trip();
        t.setDrone(d);
        Trip saved = tripService.create(t);

        tripService.deleteIfPlanned(saved.getId());
        assertThat(tripRepo.findById(saved.getId())).isEmpty();

        Trip t2 = new Trip();
        t2.setDrone(d);
        Trip saved2 = tripService.create(t2);
        tripService.updateStatus(saved2.getId(), Trip.Status.IN_PROGRESS);

        assertThatThrownBy(() -> tripService.deleteIfPlanned(saved2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void updateStartTime_recalculaFinishAtComVelocidade() {
        Drone d = newDrone();
        Trip t = new Trip();
        t.setDrone(d);
        t.setTotalDistanceKm(10.0);
        t = tripService.create(t);

        Instant start = Instant.now();
        Trip upd = tripService.updateStartTime(t.getId(), start);
        
        assertThat(upd.getStartAt()).isEqualTo(start);
        assertThat(upd.getFinishAt()).isNotNull();
    }

    @Test
    void updateTotals_naoPermiteNegativos() {
        Drone d = newDrone();
        Trip t = new Trip();
        t.setDrone(d);
        t = tripService.create(t);

        Trip upd = tripService.updateTotals(t.getId(), -5.0, -1.0);
        assertThat(upd.getTotalWeight()).isEqualTo(0.0);
        assertThat(upd.getTotalDistanceKm()).isEqualTo(0.0);
    }
}