package com.example.sandboxspei.repository;

import com.example.sandboxspei.entity.TransicionEstado;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA para {@link TransicionEstado}.
 */
public interface TransicionEstadoRepository extends JpaRepository<TransicionEstado, Long> {
}
