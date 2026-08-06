package org.example.file_api.material.controller;

import jakarta.validation.Valid;
import org.example.file_api.material.dto.MaterialCreateReqDTO;
import org.example.file_api.material.dto.MaterialPageRespDTO;
import org.example.file_api.material.dto.MaterialRespDTO;
import org.example.file_api.material.dto.MaterialUpdateReqDTO;
import org.example.file_api.material.service.MaterialService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// 核心链路 HTTP JSON -> MaterialController -> MaterialService
// -> MaterialMapper -> material_mybatis
@RestController
@RequestMapping("/api/materials")
public class MaterialController {
    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    // 新增
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MaterialRespDTO createMaterial(
            @Valid @RequestBody MaterialCreateReqDTO request
    ) {
        return materialService.createMaterial(request);
    }

    // 查询
    @GetMapping("/{id}")
    public MaterialRespDTO getMaterial(@PathVariable Long id) {
        return materialService.getMaterial(id);
    }


    @GetMapping
    public MaterialPageRespDTO listMaterials(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize) {
        return materialService.listMaterials(type, page, pageSize);
    }

    // 更新
    @PutMapping("/{id}")
    public MaterialRespDTO updateMaterial(@PathVariable Long id,
                                          @Valid @RequestBody MaterialUpdateReqDTO request) {
        return materialService.updateMaterial(id, request);
    }

    // 删除
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMaterial(@PathVariable Long id) {
        materialService.deleteMaterial(id);
    }

}
