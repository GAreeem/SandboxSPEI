package com.example.sandboxspei.repository;

import com.example.sandboxspei.entity.Institucion;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA para el catálogo de {@link Institucion}. La llave
 * primaria es el propio código de 3 dígitos (ej. "801"), por lo que
 * {@code existsById} y {@code findById} sirven directamente para las
 * validaciones V04/V05.
 */
public interface InstitucionRepository extends JpaRepository<Institucion, String> {
}
