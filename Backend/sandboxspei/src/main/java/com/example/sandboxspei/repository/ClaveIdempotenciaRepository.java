package com.example.sandboxspei.repository;

import com.example.sandboxspei.entity.ClaveIdempotencia;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA para {@link ClaveIdempotencia}. La llave primaria es la
 * propia clave, por lo que {@code findById(clave)} es la consulta de
 * idempotencia.
 */
public interface ClaveIdempotenciaRepository extends JpaRepository<ClaveIdempotencia, String> {
}
