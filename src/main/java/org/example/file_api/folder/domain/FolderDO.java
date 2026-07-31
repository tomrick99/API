package org.example.file_api.folder.domain;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


@Data
@TableName("material_folder")
public class FolderDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private Integer sort;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
