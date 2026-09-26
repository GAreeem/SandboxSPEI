package com.example.sandboxspei.repository;

import com.example.sandboxspei.entity.Operacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio JPA para el agregado {@link Operacion}.
 */
public interface OperacionRepository extends JpaRepository<Operacion, String> {

    boolean existsByReferenciaSeguimiento(String referenciaSeguimiento);

    Optional<Operacion> findByReferenciaSeguimiento(String referenciaSeguimiento);

    Page<Operacion> findAllByOrderByFechaRegistroDesc(Pageable pageable);
}
