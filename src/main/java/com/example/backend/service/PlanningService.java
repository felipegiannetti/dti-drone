package com.example.backend.service;

import com.example.backend.domain.Drone;
import com.example.backend.domain.Order;
import com.example.backend.domain.Trip;
import com.example.backend.domain.TripStop;
import com.example.backend.repository.DroneRepository;
import com.example.backend.repository.OrderRepository;
import com.example.backend.repository.TripRepository;
import com.example.backend.repository.TripStopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlanningService {

    private final DroneRepository droneRepo;
    private final OrderRepository orderRepo;
    private final TripRepository tripRepo;
    private final TripStopRepository stopRepo;

    private static final int HUB_ORIGEM_X = 0;
    private static final int HUB_ORIGEM_Y = 0;

    public PlanningService(DroneRepository droneRepo, OrderRepository orderRepo, TripRepository tripRepo, TripStopRepository stopRepo) {
        this.droneRepo = droneRepo;
        this.orderRepo = orderRepo;
        this.tripRepo = tripRepo;
        this.stopRepo = stopRepo;
    }

    @Transactional
    public List<Trip> planAll() {
        List<Order> pending = new ArrayList<>(orderRepo.findByStatus(Order.Status.PENDING));
        
        pending.sort(Comparator
                .comparing(Order::getPriority, Comparator.comparingInt(this::priorityRank))
                .thenComparing(o -> distance(HUB_ORIGEM_X, HUB_ORIGEM_Y, o.getCustomerX(), o.getCustomerY())));

        if (pending.isEmpty()) return List.of();

        List<Trip> result = new ArrayList<>();
        List<Drone> drones = droneRepo.findAll();

        for (Drone drone : drones) {
            while (true) {
                List<Order> pack = pickByKnapsack(pending, drone.getCapacityKg());
                if (pack.isEmpty()) {
                    break;
                }

                List<Order> delivery = sequenceByNearestNeighbor(pack);

                double dist = totalPathDistance(delivery);
                
                while (dist > drone.getRangeKm() && !delivery.isEmpty()) {
                    dist = totalPathDistance(delivery);
                }
                
                if (delivery.isEmpty()) {
                    break;
                }

                Trip trip = new Trip();
                trip.setDrone(drone);
                trip.setTotalWeight(delivery.stream().mapToDouble(Order::getWeightKg).sum());
                trip.setTotalDistanceKm(dist);
                trip.setStartAt(Instant.now());
                trip.setStatus(Trip.Status.PLANNED);
                trip = tripRepo.save(trip);

                double speedKmh = Math.max(1.0, drone.getSpeedKmh());
                Instant cursor = trip.getStartAt();
                int cx = HUB_ORIGEM_X, cy = HUB_ORIGEM_Y;
                int seq = 1;

                for (Order o : delivery) {
                    double legKm = distance(cx, cy, o.getCustomerX(), o.getCustomerY());
                    Duration travel = Duration.ofSeconds((long) ((legKm / speedKmh) * 3600.0));

                    TripStop stop = new TripStop();
                    stop.setTrip(trip);
                    stop.setOrder(o);
                    stop.setSeq(seq++);
                    stop.setX(o.getCustomerX());
                    stop.setY(o.getCustomerY());
                    stop.setEstimatedArrivalAt(cursor.plus(travel));
                    stop.setEstimatedDepartureAt(cursor.plus(travel));
                    stop.setDelivered(false);
                    stopRepo.save(stop);

                    cursor = stop.getEstimatedDepartureAt();
                    cx = o.getCustomerX(); cy = o.getCustomerY();

                    o.setStatus(Order.Status.PLANNED);
                }

                double backKm = distance(cx, cy, HUB_ORIGEM_X, HUB_ORIGEM_Y);
                Duration backTravel = Duration.ofSeconds((long) ((backKm / speedKmh) * 3600.0));
                trip.setFinishAt(cursor.plus(backTravel));
                tripRepo.save(trip);

                orderRepo.saveAll(delivery);
                pending.removeAll(delivery);

                result.add(trip);

                if (pending.isEmpty()) break;
            }
        }
        return result;
    }

    private int priorityRank(Order.Priority p) {
        return switch (p) {
            case HIGH -> 0;
            case MEDIUM -> 1;
            case LOW -> 2;
        };
    }

    private static double distance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private static double totalPathDistance(List<Order> path) {
        if (path.isEmpty()) return 0.0;

        double distance = 0.0;
        int cx = HUB_ORIGEM_X, cy = HUB_ORIGEM_Y;

        for (Order o : path) {
            distance += distance(cx, cy, o.getCustomerX(), o.getCustomerY());
            cx = o.getCustomerX();
            cy = o.getCustomerY();
        }

        distance += distance(cx, cy, HUB_ORIGEM_X, HUB_ORIGEM_Y);
        return distance;
    }

    private List<Order> pickByKnapsack(List<Order> pool, double capacityKg) {
        if (pool.isEmpty()) return List.of();
        List<Order> sorted = pool.stream()
                .sorted(Comparator
                        .comparing(Order::getPriority, Comparator.comparingInt(this::priorityRank))
                        .thenComparing((Order o) -> -o.getWeightKg()))
                .collect(Collectors.toList());

        List<Order> chosen = new ArrayList<>();
        double sum = 0.0;
        for (Order o : sorted) {
            if (sum + o.getWeightKg() <= capacityKg + 1e-9) {
                chosen.add(o);
                sum += o.getWeightKg();
            }
        }
        return chosen;
    }

    private List<Order> sequenceByNearestNeighbor(List<Order> orders) {
        if (orders.isEmpty()) return List.of();
        
        Map<Order.Priority, List<Order>> byPriority = orders.stream()
            .collect(Collectors.groupingBy(Order::getPriority));
        
        List<Order> route = new ArrayList<>();
        final int[] currentPos = {HUB_ORIGEM_X, HUB_ORIGEM_Y};
        
        for (Order.Priority priority : List.of(Order.Priority.HIGH, Order.Priority.MEDIUM, Order.Priority.LOW)) {
            List<Order> priorityGroup = byPriority.getOrDefault(priority, List.of());
            if (priorityGroup.isEmpty()) continue;
            
            List<Order> remaining = new ArrayList<>(priorityGroup);
            
            while (!remaining.isEmpty()) {
                Order nearest = remaining.stream()
                        .min(Comparator.comparingDouble(o -> distance(currentPos[0], currentPos[1], o.getCustomerX(), o.getCustomerY())))
                        .orElseThrow(() -> new IllegalStateException("Unexpected empty list"));

                route.add(nearest);
                
                currentPos[0] = nearest.getCustomerX();
                currentPos[1] = nearest.getCustomerY();
                remaining.remove(nearest);
            }
        }
        
        return route;
    }
}