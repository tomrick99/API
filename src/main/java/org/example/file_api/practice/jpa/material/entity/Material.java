package org.example.file_api.practice.jpa.material.entity;

import jakarta.persistence.*;

@Entity     //这个类对应数据库表
@Table(name = "material")   //对应material表
public class Material {

    @Id     //主键
    @GeneratedValue(strategy = GenerationType.IDENTITY)     //id由MySQL自动 递增 接数据库后不用再写nextID++
    private Integer id;

    private String title;

    private String description;

    public Material() {
    }

    public Material(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}