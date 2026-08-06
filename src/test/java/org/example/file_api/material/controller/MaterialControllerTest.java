package org.example.file_api.material.controller;

import org.example.file_api.material.dto.*;
import org.example.file_api.material.service.MaterialService;
import org.example.file_api.common.exception.ResourceNotFoundException;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MaterialController.class)
class MaterialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MaterialService materialService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnCreatedWhenMaterialIsCreated() throws Exception{
        MaterialCreateReqDTO request = new MaterialCreateReqDTO();
        request.setTitle("Java 学习资料");
        request.setType("PDF");
        request.setDescription("MyBatis学习笔记");

        MaterialRespDTO response = buildResponse();

        when(materialService.createMaterial(
                any(MaterialCreateReqDTO.class)
        )).thenReturn(response);

        mockMvc.perform(
                post("/api/materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Java 学习资料"))
                .andExpect(jsonPath("$.type").value("PDF"))
                .andExpect(jsonPath("$.description").value("MyBatis学习笔记"));

        verify(materialService).createMaterial(any(MaterialCreateReqDTO.class));
    }

    // 查询ID为1的资料, 然后测试Controller是否把它正确转换成JSON返回
    @Test
    void shouldGetMaterial() throws Exception {
        MaterialRespDTO response = buildResponse();

        when(materialService.getMaterial(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/materials/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Java 学习资料"));

        verify(materialService).getMaterial(1L);
    }

    @Test
    void shouldListMaterialsWithPaginationAndType() throws Exception {
        MaterialPageRespDTO response = new MaterialPageRespDTO();
        response.setRecords(List.of(buildResponse()));
        response.setTotal(11L);
        response.setPage(2L);
        response.setPageSize(10L);

        when(materialService.listMaterials("PDF", 2L, 10L))
                .thenReturn(response);

        mockMvc.perform(get("/api/materials")
                        .param("type", "PDF")
                        .param("page", "2")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(11))
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.records[0].id").value(1));

        verify(materialService).listMaterials("PDF", 2L, 10L);
    }

    @Test
    void shouldUpdateMaterial() throws Exception {
        MaterialUpdateReqDTO request = new MaterialUpdateReqDTO();
        request.setTitle("更新后的资料");
        request.setType("DOC");
        request.setDescription("更新后的描述");

        MaterialRespDTO response = buildResponse();
        response.setTitle("更新后的资料");
        response.setType("DOC");
        response.setDescription("更新后的描述");
        when(materialService.updateMaterial(eq(1L), any(MaterialUpdateReqDTO.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/materials/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("更新后的资料"))
                .andExpect(jsonPath("$.type").value("DOC"))
                .andExpect(jsonPath("$.description").value("更新后的描述"));

        verify(materialService).updateMaterial(eq(1L), any(MaterialUpdateReqDTO.class));
    }

    @Test
    void shouldReturnNoContentWhenMaterialIsDeleted() throws Exception {
        mockMvc.perform(delete("/api/materials/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(materialService).deleteMaterial(1L);
    }

    @Test
    void shouldRejectEmptyTitle() throws Exception {
        MaterialCreateReqDTO request = new MaterialCreateReqDTO();
        request.setTitle("");
        request.setType("PDF");
        request.setDescription("没有标题");

        mockMvc.perform(post("/api/materials")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("title: 标题不能为空"))
        .andExpect(jsonPath("$.path").value("/api/materials"))
        .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(materialService);
    }

    private MaterialRespDTO buildResponse() {
        MaterialRespDTO response = new MaterialRespDTO();
        response.setId(1L);
        response.setTitle("Java 学习资料");
        response.setType("PDF");
        response.setDescription("MyBatis学习笔记");
        response.setCreationDateAt(LocalDateTime.of(2026, 7, 20, 10, 0));
        response.setUpdateDateAt(LocalDateTime.of(2026, 7, 20, 10, 0));
        return response;
    }

    @Test
    void shouldReturnNotFoundWhenMaterialDoesNotExist() throws Exception {
        when(materialService.getMaterial(999L))
                .thenThrow(new ResourceNotFoundException("资料不存在: 999"));

        mockMvc.perform(get("/api/materials/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("资料不存在: 999"))
                .andExpect(jsonPath("$.path").value("/api/materials/999"));

        verify(materialService).getMaterial(999L);
    }

    @Test
    void shouldReturnInternalServerErrorWhenMaterialCreationFails() throws Exception {
        MaterialCreateReqDTO request = new MaterialCreateReqDTO();
        request.setTitle("Java 学习资料");
        request.setType("PDF");
        request.setDescription("MyBatis 学习笔记");

        when(materialService.createMaterial(any()))
                .thenThrow(new IllegalStateException("资料创建失败"));

        mockMvc.perform(post("/api/materials")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("资料创建失败"))
                .andExpect(jsonPath("$.path").value("/api/materials"));

        verify(materialService).createMaterial(any());
    }
}

