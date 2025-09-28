package com.example.backend.service;

import com.example.backend.domain.Drone;
import com.example.backend.repository.DroneRepository;
import com.example.backend.repository.OrderRepository;
import com.example.backend.repository.TripRepository;
import com.example.backend.repository.TripStopRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DroneServiceTest {

    @Autowired DroneService droneService;
    @Autowired DroneRepository droneRepo;
    @Autowired TripStopRepository stopRepo;
    @Autowired TripRepository tripRepo;
    @Autowired OrderRepository orderRepo;

    @BeforeEach
    void setup() {
        stopRepo.deleteAll();
        tripRepo.deleteAll();
        orderRepo.deleteAll();
        droneRepo.deleteAll();
    }

    @Test
    void create_ok_devePreencherStatusDefaultIdle() {
        Drone d = new Drone();
        d.setName("D1");
        d.setCapacityKg(5.0);
        d.setRangeKm(20.0);
        d.setSpeedKmh(40.0);
        d.setBatteryPct(100);
        d.setStatus(null);

        Drone saved = droneService.create(d);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(Drone.Status.IDLE);
    }

    @Test
    void create_invalido_deveFalharPorValidacao() {
        Drone d = new Drone();
        d.setName("");
        d.setCapacityKg(-1.0);
        d.setRangeKm(0.0);
        d.setSpeedKmh(0.0);
        d.setBatteryPct(101);

        assertThatThrownBy(() -> droneService.create(d)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deleteIfIdle_soPermiteQuandoIdle() {
        Drone d = new Drone();
        d.setName("D2");
        d.setCapacityKg(5.0);
        d.setRangeKm(20.0);
        d.setSpeedKmh(40.0);
        d.setBatteryPct(100);
        d = droneService.create(d);

        droneService.deleteIfIdle(d.getId());
        assertThat(droneRepo.findById(d.getId())).isEmpty();

        Drone d2 = new Drone();
        d2.setName("D3");
        d2.setCapacityKg(5.0);
        d2.setRangeKm(20.0);
        d2.setSpeedKmh(40.0);
        d2.setBatteryPct(100);
        d2 = droneService.create(d2);
        droneService.updateStatus(d2.getId(), Drone.Status.EM_VOO);

        Long id = d2.getId();
        assertThatThrownBy(() -> droneService.deleteIfIdle(id)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void updateBattery_validaFaixa_0a100() {
        Drone d = new Drone();
        d.setName("D4");
        d.setCapacityKg(5.0);
        d.setRangeKm(20.0);
        d.setSpeedKmh(40.0);
        d.setBatteryPct(50);
        Drone saved = droneService.create(d);

        Drone upd = droneService.updateBattery(saved.getId(), 80);
        assertThat(upd.getBatteryPct()).isEqualTo(80);

        Long droneId = saved.getId();
        assertThatThrownBy(() -> droneService.updateBattery(droneId, -1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> droneService.updateBattery(droneId, 101)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateLocation_atualizaXY() {
        Drone d = new Drone();
        d.setName("D5");
        d.setCapacityKg(5.0);
        d.setRangeKm(20.0);
        d.setSpeedKmh(40.0);
        d.setBatteryPct(100);
        Drone saved = droneService.create(d);

        Drone upd = droneService.updateLocation(saved.getId(), 7, 9);
        assertThat(upd.getLocationX()).isEqualTo(7);
        assertThat(upd.getLocationY()).isEqualTo(9);
    }
}