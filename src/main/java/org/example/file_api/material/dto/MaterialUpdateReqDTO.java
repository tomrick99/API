package org.example.file_api.material.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 更新资料的时候用 之后可能会修改 更新的时候可能允许只改description title不一定传
public class MaterialUpdateReqDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200")
    private String title;

    @Size(max = 50, message = "类型长度不能超过50")
    private String type;

    @Size(max = 500, message = "描述长度不能超过500")
    private String description;

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
}
