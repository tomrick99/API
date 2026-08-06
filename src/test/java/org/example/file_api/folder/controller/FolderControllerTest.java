package org.example.file_api.folder.controller;

import org.example.file_api.folder.dto.FolderCreateReqDTO;
import org.example.file_api.folder.dto.FolderRespDTO;
import org.example.file_api.folder.service.FolderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.example.file_api.common.exception.GlobalExceptionHandler;
import org.example.file_api.common.exception.ResourceNotFoundException;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@ExtendWith(MockitoExtension.class)
class FolderControllerTest {

    // 假对象 不是真的调用ServiceImpl
    @Mock
    private FolderService folderService;

    // mockito会自动把Service注入这个FolderController和spring的autowired类似
    @InjectMocks
    private FolderController folderController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(folderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldCreateFolder() throws Exception {

        // 准备请求对象
        FolderCreateReqDTO request = new FolderCreateReqDTO();
        request.setName("test");
        request.setDescription("desc");
        request.setSort(10);

        FolderRespDTO response = new FolderRespDTO();
        response.setId(1L);
        response.setName("test");
        response.setDescription("desc");
        response.setSort(10);
        response.setStatus(1);

        // Mock Service假设service正常返回了
        when(folderService.createFolder(any(FolderCreateReqDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/folders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.name").value("test"))
                                        .andExpect(jsonPath("$.description").value("desc"))
                                                .andExpect(jsonPath("$.sort").value(10))
                                                        .andExpect(jsonPath("$.status").value(1));
        // 验证Controller有没有调用service
        verify(folderService).createFolder(any(FolderCreateReqDTO.class));
    }

    @Test
    void shouldRejectEmptyFolderName() throws Exception {
        // 创建DTO对象  让ObjectMapper转成JSON
        FolderCreateReqDTO request = new FolderCreateReqDTO();
        request.setName("");

        // mockMvc模拟的是HTTP请求
        mockMvc.perform(post("/api/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("name: folder title cannot be empty"))
        .andExpect(jsonPath("$.path").value("/api/folders"))
        .andExpect(jsonPath("$.timestamp").exists());


        // 参数效验发生在进入service之前
        verifyNoInteractions(folderService);
    }

    @Test
    void shouldReturnNotFoundWhenFolderDoesNotExist() throws Exception {
        when(folderService.getFolder(999L)).thenThrow(new ResourceNotFoundException("Folder is not exist: 999"));

        mockMvc.perform(get("/api/folders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Folder is not exist: 999"))
                .andExpect(jsonPath("$.path").value("/api/folders/999"));

        verify(folderService).getFolder(999L);
    }

    @Test
    void shouldReturnNotContentWhenFolderIsDeleted() throws Exception {
        mockMvc.perform(delete("/api/folders/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(folderService).deleteFolder(1L);
    }
}
