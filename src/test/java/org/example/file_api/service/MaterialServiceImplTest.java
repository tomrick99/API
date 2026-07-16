package org.example.file_api.service;

import org.example.file_api.dto.MaterialCreateReqDTO;
import org.example.file_api.dto.MaterialRespDTO;
import org.example.file_api.dto.MaterialUpdateReqDTO;
import org.example.file_api.material.domain.MaterialDO;
import org.example.file_api.material.mapper.MaterialMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;



// 这里只测一个类 MaterialServiceImpl 不用启动SpringBoot 也不用连接数据库
public class MaterialServiceImplTest {
    private MaterialMapper materialMapper;

    private MaterialServiceImpl materialService;

    @BeforeEach
    void setUp() {

        // 创建假的Mapper 不会真查Mysql
        materialMapper = mock(MaterialMapper.class);
        // new一个真的Service
        materialService = new MaterialServiceImpl(materialMapper);
    }

    @Test
    void shouldCreateMaterial() {

        // 准备请求DTO 假装Collerctor收到了JSON 而且转成了DTO
        MaterialCreateReqDTO request = new MaterialCreateReqDTO();
        request.setTitle("Java 基础笔记");
        request.setType("note");
        request.setDescription("Java 学习资料");

        // 规定假的Mapper行为 因为这里不连接数据库 所有把自增id回填到对象里
        when(materialMapper.insert(any(MaterialDO.class))).thenAnswer(
                invocation -> {
                    MaterialDO material = invocation.getArgument(0);
                    material.setId(1L);
                    return 1;
                }
        );

        // 调用真的Service
        MaterialRespDTO response = materialService.createMaterial(request);

        // 断言返回结果
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Java 基础笔记",  response.getTitle());
        assertEquals("note", response.getType());
        assertEquals("Java 学习资料", response.getDescription());
        assertNotNull(response.getCreationDateAt());
        assertNotNull(response.getUpdateDateAt());

        // 验证mapper被调用
        verify(materialMapper).insert(any(MaterialDO.class));
    }

    @Test
    void shouldGetMaterialById() {
        // 只是Service->假Mapper

        // 造一个假的MaterialDO
        MaterialDO material = new MaterialDO();
        material.setId(1L);
        material.setTitle("Java 基础笔记");
        material.setType("note");
        material.setDescription("Java 学习指南");
        material.setCreatedAt(LocalDateTime.now());
        material.setUpdatedAt(LocalDateTime.now());

        // 规定mapper.selectById(1L)时返回这个对象
        when(materialMapper.selectById(1L)).thenReturn(material);

        // 调用真的materialService.getMaterial(1L);
        MaterialRespDTO response = materialService.getMaterial(1L);

        // 断言检查
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Java 基础笔记", response.getTitle());
        assertEquals("note", response.getType());
        assertEquals("Java 学习指南", response.getDescription());
        assertNotNull(response.getCreationDateAt());
        assertNotNull(response.getUpdateDateAt());

        verify(materialMapper).selectById(1L);
    }

    @Test
    void shouldThrowExceptionWhenMaterialNotFound() {

        // 假装mapper去查id=999时查不到 返回Null
        when(materialMapper.selectById(999L)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> materialService.getMaterial(999L)
        );

        assertEquals("资料不存在", exception.getMessage());

        verify(materialMapper).selectById(999L);

    }

    @Test
    void shouldUpdateMaterial() {

        // 更新成功
        MaterialDO oldMaterial = new MaterialDO();
        oldMaterial.setId(1L);
        oldMaterial.setTitle("旧标题");
        oldMaterial.setType("note");
        oldMaterial.setDescription("旧描述");
        oldMaterial.setCreatedAt(LocalDateTime.now());
        oldMaterial.setUpdatedAt(LocalDateTime.now());

        MaterialUpdateReqDTO request = new MaterialUpdateReqDTO();
        request.setTitle("新标题");
        request.setType("doc");
        request.setDescription("新描述");

        when(materialMapper.selectById(1L)).thenReturn(oldMaterial);
        when(materialMapper.updateById(any(MaterialDO.class))).thenReturn(1);

        MaterialRespDTO response = materialService.updateMaterial(1L, request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("新标题", response.getTitle());
        assertEquals("doc", response.getType());
        assertEquals("新描述", response.getDescription());
        assertNotNull(response.getCreationDateAt());
        assertNotNull(response.getUpdateDateAt());

        verify(materialMapper).selectById(1L);
        verify(materialMapper).updateById(any(MaterialDO.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdateMaterialNotFound() {

        // 更新时资料不存在
        MaterialUpdateReqDTO request = new MaterialUpdateReqDTO();
        request.setTitle("新标题");
        request.setType("doc");
        request.setDescription("新描述");

        when(materialMapper.selectById(999L)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> materialService.updateMaterial(999L, request)
        );

        assertEquals("资料不存在", exception.getMessage());

        verify(materialMapper).selectById(999L);
        verify(materialMapper, never()).updateById(any(MaterialDO.class));
    }

    @Test
    void shouldDeleteMaterial() {

        // 删除成功
        MaterialDO material = new MaterialDO();
        material.setId(1L);
        material.setTitle("Java 基础笔记");
        material.setType("note");
        material.setDescription("Java 学习资料");
        material.setCreatedAt(LocalDateTime.now());
        material.setUpdatedAt(LocalDateTime.now());

        when(materialMapper.selectById(1L)).thenReturn(material);
        when(materialMapper.deleteById(1L)).thenReturn(1);

        materialService.deleteMaterial(1L);

        verify(materialMapper).selectById(1L);
        verify(materialMapper).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeleteMaterialNotFound() {

        // 删除时资料不存在
        when(materialMapper.selectById(999L)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> materialService.deleteMaterial(999L)
        );

        assertEquals("资料不存在", exception.getMessage());

        verify(materialMapper).selectById(999L);
        verify(materialMapper, never()).deleteById(999L);
    }

//    @Test
//    void shouldThrowExceptionWhenCreateMaterial() {
//
//        // 在创建资料的时候抛出异常
//        MaterialCreateReqDTO request = new MaterialCreateReqDTO();
//        request.setTitle("Java 基础笔记");
//        request.setType("note");
//        request.setDescription("Java 学习资料");
//
//        when(materialMapper.insert(any(MaterialDO.class))).thenReturn(0);
//
//        IllegalStateException exception = assertThrows(
//                IllegalStateException.class,
//                () -> materialService.createMaterial(request)
//        );
//
//        assertEquals("资料库创建失败", exception.getMessage());
//
//        verify(materialMapper).insert(any(MaterialDO.class));
//    }
}
