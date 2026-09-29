package com.example.sandboxspei.repository;

import com.example.sandboxspei.entity.Operacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repositorio JPA para el agregado {@link Operacion}.
 */
public interface OperacionRepository extends JpaRepository<Operacion, String> {

    boolean existsByReferenciaSeguimiento(String referenciaSeguimiento);

    Optional<Operacion> findByReferenciaSeguimiento(String referenciaSeguimiento);

    Page<Operacion> findAllByOrderByFechaRegistroDesc(Pageable pageable);

    /**
     * Carga la operación con bloqueo pesimista de escritura (SELECT ... FOR
     * UPDATE). Se usa en TODO cambio de estado para que el avance asíncrono
     * y una solicitud manual de transición no se pisen entre sí ni dupliquen
     * transiciones. Debe invocarse dentro de una transacción.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Operacion o where o.id = :id")
    Optional<Operacion> findByIdForUpdate(@Param("id") String id);
}
