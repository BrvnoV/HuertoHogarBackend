package com.huertohogar.huertohogarbackend.repository; // Ajusta el paquete

import com.huertohogar.huertohogarbackend.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Ejemplo de un método personalizado:
    // List<Producto> findByCategory(String category);
}