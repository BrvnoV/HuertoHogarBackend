package com.huertohogar.huertohogarbackend.service;

import com.huertohogar.huertohogarbackend.model.Categoria;
import com.huertohogar.huertohogarbackend.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Autowired
    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<Categoria> obtenerTodas() {
        return categoriaRepository.findAll();
    }

    public Optional<Categoria> obtenerPorId(Long id) {
        return categoriaRepository.findById(id);
    }

    public Categoria crearCategoria(Categoria categoria) {
        if (categoriaRepository.existsByNombre(categoria.getNombre())) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre");
        }
        return categoriaRepository.save(categoria);
    }

    public Categoria actualizarCategoria(Long id, Categoria detalles) {
        return categoriaRepository.findById(id)
                .map(categoria -> {
                    // Si cambia el nombre, revisar duplicados
                    if (!categoria.getNombre().equals(detalles.getNombre()) &&
                            categoriaRepository.existsByNombre(detalles.getNombre())) {
                        throw new IllegalArgumentException("Ya existe una categoría con ese nombre");
                    }
                    categoria.setNombre(detalles.getNombre());
                    categoria.setDescripcion(detalles.getDescripcion());
                    return categoriaRepository.save(categoria);
                }).orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
    }

    public void eliminarCategoria(Long id) {
        // En un caso real, validar si hay productos usando esta categoría antes de
        // borrar
        categoriaRepository.deleteById(id);
    }
}
