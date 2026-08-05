package org.example.file_api.practice.jpa.material.dto;
//dto 把前端传过来的JSON数据封装成一个JAVA对象
public class MaterialRequest {

    private String title;
    private String description;

    public MaterialRequest() {}

    public MaterialRequest(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
