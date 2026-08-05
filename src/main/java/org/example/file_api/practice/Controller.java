package org.example.file_api.practice;

import org.example.file_api.dto.MaterialRequest;
import org.example.file_api.entity.Material;
import org.example.file_api.repository.MaterialRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController     //告诉Spring这是接受http请求的一个类 类中所有方法的返回值自动写入HTTP响应体
@RequestMapping("/api/test/materials")
public class Controller {

    private final MaterialRepository materialRepository;

    public Controller(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    @GetMapping
    public List<Material> getAllMaterials() {
        return materialRepository.findAll();
    }

    @GetMapping("/{id}")
    public Material getMaterialById(@PathVariable Integer id) {
        return materialRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Material createMaterial(@RequestBody MaterialRequest request) {
        Material material = new Material(
                request.getTitle(),
                request.getDescription()
        );

        return materialRepository.save(material);
    }

    @PutMapping("/{id}")
    public Material updateMaterial(@PathVariable Integer id,
                                   @RequestBody MaterialRequest request) {
        Material oldMaterial = materialRepository.findById(id).orElse(null);

        if (oldMaterial == null) {
            return null;
        }

        oldMaterial.setTitle(request.getTitle());
        oldMaterial.setDescription(request.getDescription());

        return materialRepository.save(oldMaterial);
    }

    @DeleteMapping("/{id}")
    public String deleteMaterial(@PathVariable Integer id) {
        materialRepository.deleteById(id);
        return "material " + id + " is deleted";
    }
}