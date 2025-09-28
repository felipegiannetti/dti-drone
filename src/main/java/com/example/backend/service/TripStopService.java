package com.example.backend.service;

import com.example.backend.domain.Order;
import com.example.backend.domain.Trip;
import com.example.backend.domain.TripStop;
import com.example.backend.repository.OrderRepository;
import com.example.backend.repository.TripRepository;
import com.example.backend.repository.TripStopRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class TripStopService {

    private final TripRepository tripRepo;
    private final TripStopRepository stopRepo;
    private final OrderRepository orderRepo;
    private final EntityManager entityManager;

    public TripStopService(TripRepository tripRepo, TripStopRepository stopRepo, OrderRepository orderRepo, EntityManager entityManager) {
        this.tripRepo = tripRepo;
        this.stopRepo = stopRepo;
        this.orderRepo = orderRepo;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public List<TripStop> listByTrip(Long tripId) {
        ensureTripExists(tripId);
        return stopRepo.findByTripIdOrderBySeqAsc(tripId);
    }

    @Transactional(readOnly = true)
    public TripStop getByTripAndSeq(Long tripId, int seq) {
        return stopRepo.findByTripIdAndSeq(tripId, seq)
                .orElseThrow(() -> new EntityNotFoundException("TripStop não encontrado: trip=" + tripId + " seq=" + seq));
    }

    @Transactional
    public TripStop create(Long tripId, Long orderId, Integer x, Integer y, Integer seq) {
        Trip trip = getTrip(tripId);
        ensurePlanned(trip);

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order não encontrado: " + orderId));

        TripStop stop = new TripStop();
        stop.setTrip(trip);
        stop.setOrder(order);
        stop.setX(x != null ? x : 0);
        stop.setY(y != null ? y : 0);

        int nextSeq = (int) stopRepo.countByTripId(tripId) + 1;
        stop.setSeq(seq != null && seq > 0 ? seq : nextSeq);

        stop.setEstimatedArrivalAt(null);
        stop.setEstimatedDepartureAt(null);
        stop.setDelivered(false);

        normalizeSequenceGaps(tripId, stop.getSeq());

        return stopRepo.save(stop);
    }

    @Transactional
    public void deleteIfPlanned(Long tripId, int seq) {
        Trip trip = getTrip(tripId);
        ensurePlanned(trip);

        TripStop stop = getByTripAndSeq(tripId, seq);
        stopRepo.delete(stop);

        List<TripStop> remaining = stopRepo.findByTripIdOrderBySeqAsc(tripId);
        int s = 1;
        for (TripStop ts : remaining) {
            if (ts.getSeq() != s) {
                ts.setSeq(s);
                stopRepo.save(ts);
            }

            s++;
        }
    }

    @Transactional
    public void move(Long tripId, int fromSeq, int toSeq) {
        Trip trip = getTrip(tripId);
        ensurePlanned(trip);

        List<TripStop> stops = stopRepo.findByTripIdOrderBySeqAsc(tripId);
        if (stops.isEmpty()) return;

        if (toSeq < 1) toSeq = 1;
        if (toSeq > stops.size()) toSeq = stops.size();

        TripStop moving = getByTripAndSeq(tripId, fromSeq);

        for (int i = 0; i < stops.size(); i++) {
            stops.get(i).setSeq(-(i + 1));
            stopRepo.save(stops.get(i));
        }
        
        entityManager.flush();

        stops.removeIf(s -> s.getId().equals(moving.getId()));
        stops.add(toSeq - 1, moving);

        for (int i = 0; i < stops.size(); i++) {
            stops.get(i).setSeq(i + 1);
            stopRepo.save(stops.get(i));
        }
    }

    @Transactional
    public TripStop markDelivered(Long tripId, int seq) {
        TripStop stop = getByTripAndSeq(tripId, seq);

        stop.setDelivered(true);
        stopRepo.save(stop);

        Order o = stop.getOrder();
        o.setStatus(Order.Status.DELIVERED);
        orderRepo.save(o);

        List<TripStop> pendentes = stopRepo.findByTripIdAndDeliveredFalseOrderBySeqAsc(tripId);
        if (pendentes.isEmpty()) {
            Trip trip = stop.getTrip();

            trip.setStatus(Trip.Status.FINISHED);
            trip.setFinishAt(Instant.now());

            tripRepo.save(trip);
        }

        return stop;
    }

    @Transactional
    public TripStop updateEstimates(Long tripId, int seq, Instant estimatedArrivalAt, Instant estimatedDepartureAt) {
        TripStop stop = getByTripAndSeq(tripId, seq);

        stop.setEstimatedArrivalAt(estimatedArrivalAt);
        stop.setEstimatedDepartureAt(estimatedDepartureAt);

        return stopRepo.save(stop);
    }

    private Trip getTrip(Long tripId) {
        return tripRepo.findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException("Trip não encontrada: " + tripId));
    }

    private void ensureTripExists(Long tripId) {
        if (!tripRepo.existsById(tripId)) {
            throw new EntityNotFoundException("Trip não encontrada: " + tripId);
        }
    }

    private void ensurePlanned(Trip trip) {
        if (trip.getStatus() != Trip.Status.PLANNED) {
            throw new IllegalStateException("Operação permitida apenas quando Trip está PLANNED");
        }
    }

    // abre espaço para inserir uma parada em 'seq' sem quebrar a unique
    private void normalizeSequenceGaps(Long tripId, int seqToInsert) {
        List<TripStop> stops = stopRepo.findByTripIdOrderBySeqAsc(tripId);

        for (int i = stops.size() - 1; i >= 0; i--) {
            TripStop ts = stops.get(i);

            if (ts.getSeq() >= seqToInsert) {
                ts.setSeq(ts.getSeq() + 1);
                stopRepo.save(ts);
            }
        }
    }
}