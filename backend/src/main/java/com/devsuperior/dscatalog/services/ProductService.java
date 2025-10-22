package com.devsuperior.dscatalog.services;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.DataBaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository repository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Page<ProductDTO> findAllPaged(Pageable pageable){
        Page<Product> list = repository.findAll(pageable);
        return list.map(ProductDTO::new);
    }

    @Transactional(readOnly = true)
    public ProductDTO findById(Long id){
        Product entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Id: " + id + " not found!"));
        return new ProductDTO(entity, entity.getCategories());
    }

    @Transactional
    public ProductDTO insert(ProductDTO dto){
        Product entity = new Product();
        copyDtoToEntity(dto,entity);
        entity = repository.save(entity);
        return new ProductDTO(entity);
    }

    @Transactional
    public ProductDTO update(Long id, ProductDTO dto){
        try {
            Product entity = repository.getReferenceById(id);
            copyDtoToEntity(dto ,entity );
            entity = repository.save(entity);
            return new ProductDTO(entity);
        }
        catch (EntityNotFoundException e){
            throw new ResourceNotFoundException("Id Not found: " + id);
        }

    }

    @Transactional
    public void delete(Long id){
        if (!repository.existsById(id)){
            throw new ResourceNotFoundException("Id: " + id + " not found!");
        }
        try {
            repository.deleteById(id);
        }
        catch (DataIntegrityViolationException e){
            throw new DataBaseException("Integrity failure reference!");
        }
    }


    public  void copyDtoToEntity( ProductDTO dto,Product entity){
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setImgUrl(dto.getImgUrl());
        entity.setDate(dto.getDate());

        entity.getCategories().clear();
        for (CategoryDTO catDTO : dto.getCategories()){
            Category category = categoryRepository.getReferenceById(catDTO.getId());
            entity.getCategories().add(category);
        }
    }
}
