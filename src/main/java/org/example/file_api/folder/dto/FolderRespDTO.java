package org.example.file_api.folder.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FolderRespDTO {
    private Long id;
    private String name;
    private String description;
    private Integer sort;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
