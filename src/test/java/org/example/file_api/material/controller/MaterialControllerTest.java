package org.example.file_api.material.controller;

import org.example.file_api.material.dto.MaterialCreateReqDTO;
import org.example.file_api.material.dto.MaterialRespDTO;
import org.example.file_api.material.dto.MaterialPageRespDTO;
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
    void shouldCreateMaterial() throws Exception {

        // 先创建一个对象
        MaterialRespDTO response = buildResponse();

        // 然后Mock的MaterialService被调用时 就返回这个对象
        when(materialService.createMaterial(any())).thenReturn(response);

        // Controller再把这个Java对象自动转换成JSON 测试再检查返回JSON里的id title等等是否符合预期
        mockMvc.perform(post("/api/materials")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "Java 学习资料",
                            "type": "PDF",
                            "description": "MyBatis学习笔记"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Java 学习资料"))
                .andExpect(jsonPath("$.type").value("PDF"))
                .andExpect(jsonPath("$.description").value("MyBatis学习笔记"));

        verify(materialService).createMaterial(any());

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
        MaterialRespDTO response = buildResponse();
        response.setTitle("更新后的资料");
        response.setType("DOC");
        response.setDescription("更新后的描述");

        when(materialService.updateMaterial(eq(1L), any()))
                .thenReturn(response);

        mockMvc.perform(put("/api/materials/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "更新后的资料",
                                    "type": "DOC",
                                    "description": "更新后的描述"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("更新后的资料"))
                .andExpect(jsonPath("$.type").value("DOC"))
                .andExpect(jsonPath("$.description").value("更新后的描述"));

        verify(materialService).updateMaterial(eq(1L), any());
    }

    @Test
    void shouldDeleteMaterial() throws Exception {
        mockMvc.perform(delete("/api/materials/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("material 1 is deleted"));

        verify(materialService).deleteMaterial(1L);
    }

    @Test
    void shouldRejectEmptyTitle() throws Exception {
        mockMvc.perform(post("/api/materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "",
                                    "type": "PDF",
                                    "description": "没有标题"
                                }
                                """))
                .andExpect(status().isBadRequest());

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
    void shouldReturnInternalServerErrorWhenMaterialCreationFalse() throws Exception {
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

