package org.example.file_api.material.service;

import jakarta.transaction.Transactional;
import org.example.file_api.material.dto.MaterialCreateReqDTO;
import org.example.file_api.material.dto.MaterialPageRespDTO;
import org.example.file_api.material.dto.MaterialRespDTO;
import org.example.file_api.material.dto.MaterialUpdateReqDTO;
import org.example.file_api.material.domain.MaterialDO;
import org.example.file_api.material.mapper.MaterialMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MaterialServiceImpl implements MaterialService {

    // 构造器注入 Service要调用Mapper 所以需要一个MaterialMapper
    private final MaterialMapper materialMapper;

    public MaterialServiceImpl(MaterialMapper materialMapper) {
        this.materialMapper = materialMapper;
    }

    @Override
    @Transactional
    public MaterialRespDTO createMaterial(MaterialCreateReqDTO request) {
        LocalDateTime now = LocalDateTime.now();

        MaterialDO material = new MaterialDO();
        material.setTitle(request.getTitle());
        material.setType(request.getType());
        material.setDescription(request.getDescription());
        material.setCreatedAt(now);
        material.setUpdatedAt(now);

        materialMapper.insert(material);

        return toRespDTO(material);
    }

    // get/update/delete都先查存不存在

    @Override
    public MaterialRespDTO getMaterial(Long id) {
        MaterialDO material = materialMapper.selectById(id);

        if (material == null) {
            throw new IllegalArgumentException("资料不存在");
        }


        return toRespDTO(material);
    }

    @Override
    @Transactional
    public MaterialRespDTO updateMaterial(Long id, MaterialUpdateReqDTO request) {
        MaterialDO material = materialMapper.selectById(id);

        if (material == null) {
            throw new IllegalArgumentException("资料不存在");
        }

        material.setTitle(request.getTitle());
        material.setType(request.getType());
        material.setDescription(request.getDescription());
        material.setUpdatedAt(LocalDateTime.now());

        materialMapper.updateById(material);
        return toRespDTO(material);
    }

    @Override
    @Transactional
    public void deleteMaterial(Long id) {
        MaterialDO material = materialMapper.selectById(id);

        if (material == null) {
            throw new IllegalArgumentException("资料不存在");
        }

        materialMapper.deleteById(id);
    }

    // 新增分页业务 效验页码不小于1 效验每页数量必须是1到100
    @Override
    public MaterialPageRespDTO listMaterials(String type, long page, long pageSize) {
        if (page < 1) {
            throw new IllegalArgumentException("page必须大于等于1");
        }
        if (pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("pageSize必须在1到100之间");
        }

        long offset = (page - 1) * pageSize;
        List<MaterialRespDTO> records = materialMapper
                .selectByTypeOrderByCreatedAtDesc(type, pageSize, offset)
                .stream()
                .map(this::toRespDTO)
                .toList();

        MaterialPageRespDTO response = new MaterialPageRespDTO();
        response.setRecords(records);
        response.setTotal(materialMapper.countByType(type));
        response.setPage(page);
        response.setPageSize(pageSize);
        return response;
    }

    private MaterialRespDTO toRespDTO(MaterialDO material) {
        MaterialRespDTO response = new MaterialRespDTO();
        response.setId(material.getId());
        response.setTitle(material.getTitle());
        response.setType(material.getType());
        response.setDescription(material.getDescription());
        response.setCreationDateAt(material.getCreatedAt());
        response.setUpdateDateAt(material.getUpdatedAt());
        return response;
    }
}
