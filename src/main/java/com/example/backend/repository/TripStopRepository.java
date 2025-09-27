package com.example.backend.repository;

import com.example.backend.domain.TripStop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TripStopRepository extends JpaRepository<TripStop, Long> {

    /* Buscas do mais antigo para o mais recente (nesse caso será: 1, 2, ...)*/
    List<TripStop> findByTripIdOrderBySeqAsc(Long tripId);
    List<TripStop> findByTripIdAndDeliveredFalseOrderBySeqAsc(Long tripId);

    List<TripStop> findByOrderId(Long orderId);

    /* Busca UMA parada específica pela viagem + sequência. 
     * Retorna Optional porque pode não existir parada com essa combinação:
     * - Optional.of(tripStop) se encontrar a parada
     * - Optional.empty() se não existir tripId=X com seq=Y */
    Optional<TripStop> findByTripIdAndSeq(Long tripId, int seq);

    long countByTripId(Long tripId); // Contador de paradas de uma viagem
}
