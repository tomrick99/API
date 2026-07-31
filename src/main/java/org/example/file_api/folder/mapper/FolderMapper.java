package org.example.file_api.folder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.file_api.folder.domain.FolderDO;

@Mapper
public interface FolderMapper extends BaseMapper<FolderDO> {
}
