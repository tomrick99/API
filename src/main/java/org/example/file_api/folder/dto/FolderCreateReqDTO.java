package org.example.file_api.folder.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FolderCreateReqDTO {

    @NotBlank(message = "folder title cannot be empty")
    @Size(max = 100, message = "folder name length cannot exceed 100 characters")
    private String name;

    @Size(max = 500, message = "folder description length cannot exceed 500 characters")
    private String description;

    @Min(value = 0, message = "sort value cannot less than 0")
    private Integer sort;
}
