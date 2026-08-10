package org.example.file_api.practice.jpa.material.repository;

import org.example.file_api.practice.jpa.material.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialRepository extends JpaRepository<Material, Integer> {
}
