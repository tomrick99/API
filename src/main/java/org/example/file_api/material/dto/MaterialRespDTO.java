package org.example.file_api.material.dto;

import java.time.LocalDateTime;

public class MaterialRespDTO {

    private Long id;

    private String title;

    private String type;

    private String description;

    private LocalDateTime creationDateAt;

    private LocalDateTime updateDateAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public LocalDateTime getCreationDateAt() {
        return creationDateAt;
    }
    public void setCreationDateAt(LocalDateTime creationDateAt) {
        this.creationDateAt = creationDateAt;
    }
    public LocalDateTime getUpdateDateAt() {
        return updateDateAt;
    }
    public void setUpdateDateAt(LocalDateTime updateDateAt) {
        this.updateDateAt = updateDateAt;
    }

}
