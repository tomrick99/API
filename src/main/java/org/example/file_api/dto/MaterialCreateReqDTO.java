package org.example.file_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 这个类表示 增资料的时候前端传过来的数据
public class MaterialCreateReqDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过两百")
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
