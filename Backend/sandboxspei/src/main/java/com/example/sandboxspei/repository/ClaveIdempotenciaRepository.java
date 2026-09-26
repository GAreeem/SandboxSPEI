package com.example.sandboxspei.repository;

import com.example.sandboxspei.entity.ClaveIdempotencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio JPA para {@link ClaveIdempotencia}.
 */
public interface ClaveIdempotenciaRepository extends JpaRepository<ClaveIdempotencia, String> {

    Optional<ClaveIdempotencia> findByClave(String clave);
}
