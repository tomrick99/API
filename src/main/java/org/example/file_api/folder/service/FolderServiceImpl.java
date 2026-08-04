package org.example.file_api.folder.service;

import org.springframework.transaction.annotation.Transactional;
import org.example.file_api.folder.domain.FolderDO;
import org.example.file_api.folder.dto.FolderCreateReqDTO;
import org.example.file_api.folder.dto.FolderRespDTO;
import org.example.file_api.folder.dto.FolderUpdateReqDTO;
import org.example.file_api.folder.mapper.FolderMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FolderServiceImpl implements FolderService {
    private final FolderMapper folderMapper;
    public FolderServiceImpl(FolderMapper folderMapper) {
        this.folderMapper = folderMapper;
    }

    @Transactional
    @Override
    public FolderRespDTO createFolder(FolderCreateReqDTO request) {

        if(request==null){
            throw new IllegalArgumentException();
        }

        FolderDO folder = new FolderDO();

        folder.setName(request.getName());
        folder.setDescription(request.getDescription());
        if (request.getSort() ==null) {
            folder.setSort(100);
        }else folder.setSort(request.getSort());

        folder.setStatus(1);

        folder.setCreatedAt(LocalDateTime.now());
        folder.setUpdatedAt(LocalDateTime.now());

        folderMapper.insert(folder);

        return toRespDTO(folder);

    }

    // 查询的时候要看数据是否存在
    // id不为null 也不代表数据库里面就有这个查询对象了
    @Override
    public FolderRespDTO getFolder(Long id) {

        if(id==null){
            throw new IllegalArgumentException();
        }

        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }

        FolderDO folder = folderMapper.selectById(id);

        if (folder == null) {
            throw new IllegalArgumentException("folder is null");
        }

        return toRespDTO(folder);



    }

    // 更新文件的时候不止是看id是否为空 还要看request和更改对象是否在数据库里面
    @Transactional
    @Override
    public FolderRespDTO updateFolder(Long id, FolderUpdateReqDTO request) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }

        if (request == null) {
            throw new IllegalArgumentException("request is null");
        }

        FolderDO folder = folderMapper.selectById(id);

        if (folder == null) {
            throw new IllegalArgumentException("folder is null");
        }
        else {
            folder.setName(request.getName());
            folder.setDescription(request.getDescription());

            // sort前端不传 不能直接设置, sort是非空字段
            if (request.getSort() != null) {
                folder.setSort(request.getSort());
            }
            folder.setUpdatedAt(LocalDateTime.now());
            folderMapper.updateById(folder);
            return toRespDTO(folder);
        }


    }

    @Transactional
    @Override
    public void deleteFolder(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }

        FolderDO folder = folderMapper.selectById(id);

        if (folder == null) {
            throw new IllegalArgumentException("folder is null");
        }

        folderMapper.deleteById(id);

    }

    // 转换方法只是复制数据 不应该直接生成数据
    private FolderRespDTO toRespDTO(FolderDO folder) {
        FolderRespDTO response = new FolderRespDTO();
        response.setId(folder.getId());
        response.setName(folder.getName());
        response.setDescription(folder.getDescription());
        response.setSort(folder.getSort());
        // 时间不是直接获取当前时间
        response.setCreatedAt(folder.getCreatedAt());
        response.setUpdatedAt(folder.getUpdatedAt());
        response.setStatus(folder.getStatus());

        return response;
    }
}
