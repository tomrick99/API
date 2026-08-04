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

        mockMvc = MockMvcBuilders.standaloneSetup(folderController)
                .setValidator(validator)
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldCreateFolder() {

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
        when(folderService.createFolder(request)).thenReturn(response);

        // 直接调用Controller方法
        FolderRespDTO result = folderController.createFolder(request);

        assertSame(response, result);
        assertEquals(1L, result.getId());
        assertEquals("test", result.getName());
        assertEquals("desc", result.getDescription());
        assertEquals(10, result.getSort());
        assertEquals(1, result.getStatus());

        // 验证Controller有没有调用service
        verify(folderService).createFolder(request);
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
                .andExpect(status().isBadRequest());

        // 参数效验发生在进入service之前
        verifyNoInteractions(folderService);
    }
}
