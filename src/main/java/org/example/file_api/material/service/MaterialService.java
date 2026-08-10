package org.example.file_api.material.service;

import org.example.file_api.material.dto.MaterialCreateReqDTO;
import org.example.file_api.material.dto.MaterialPageRespDTO;
import org.example.file_api.material.dto.MaterialRespDTO;
import org.example.file_api.material.dto.MaterialUpdateReqDTO;

// Service层的接口 不写具体实现只定义这个业务模块可以做什么
public interface MaterialService {
    // 传入请求DTO 响应返回DTO
    MaterialRespDTO createMaterial(MaterialCreateReqDTO request);

    MaterialRespDTO getMaterial(Long id);

    MaterialRespDTO updateMaterial(Long id, MaterialUpdateReqDTO request);

    void deleteMaterial(Long id);

    MaterialPageRespDTO listMaterials(String type, long page, long pageSize);
}
