package org.example.file_api.folder.service;

import org.example.file_api.folder.domain.FolderDO;
import org.example.file_api.folder.dto.FolderCreateReqDTO;
import org.example.file_api.folder.dto.FolderRespDTO;
import org.example.file_api.folder.dto.FolderUpdateReqDTO;
import org.example.file_api.folder.mapper.FolderMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

@SpringBootTest
public class FolderServiceTest {
    @Autowired
    private FolderService folderService;

    @Autowired
    private FolderMapper folderMapper;

    @Test
    void createFolderTest(){
        FolderCreateReqDTO request = new FolderCreateReqDTO();

        request.setName("test");
        request.setDescription("desc");

        // result是service处理之后返回的DTO
        FolderRespDTO result = folderService.createFolder(request);

        Assertions.assertNotNull(result);

        Assertions.assertEquals("test", result.getName());
        Assertions.assertEquals("desc", result.getDescription());

        // dbFolder是从数据库查出来的数据
        FolderDO dbFolder = folderMapper.selectById(result.getId());
        Assertions.assertNotNull(dbFolder);

        Assertions.assertEquals("test", dbFolder.getName());
    }

    @Test
    void getFolderTest(){
        FolderDO folder = new FolderDO();

        folder.setName("test G");
        folder.setDescription("desc G");
        folder.setSort(100);
        folder.setStatus(1);
        folder.setCreatedAt(LocalDateTime.now());
        folder.setUpdatedAt(LocalDateTime.now());

        folderMapper.insert(folder);

        // 测试查询
        FolderRespDTO result = folderService.getFolder(folder.getId());

        Assertions.assertNotNull(result);
        Assertions.assertEquals("test G", result.getName());
        Assertions.assertEquals("desc G", result.getDescription());
        Assertions.assertEquals(100, result.getSort());
        Assertions.assertEquals(1, result.getStatus());

    }

    @Test
    void updateFolderTest(){
        FolderDO folder = new FolderDO();

        folder.setName("test U");
        folder.setDescription("desc U");
        folder.setSort(200);
        folder.setStatus(2);
        folder.setCreatedAt(LocalDateTime.now());
        folder.setUpdatedAt(LocalDateTime.now());

        folderMapper.insert(folder);

        folder.setName("test P");
        folder.setDescription("desc P");

        folderMapper.updateById(folder);

        

        FolderRespDTO result = folderService.getFolder(folder.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals("test P", result.getName());
        Assertions.assertEquals("desc P", result.getDescription());
        Assertions.assertEquals(200, result.getSort());
        Assertions.assertEquals(2, result.getStatus());

    }

    @Test
    void deleteFolderTest(){
        // 创建一条数据
        FolderDO folder = new FolderDO();

        folder.setName("test D");
        folder.setDescription("desc D");
        folder.setSort(300);
        folder.setStatus(3);
        folder.setCreatedAt(LocalDateTime.now());
        folder.setUpdatedAt(LocalDateTime.now());

        folderMapper.insert(folder);

        // 调用删除
        folderService.deleteFolder(folder.getId());

        // 数据库里面找不到它
        FolderDO dbFolder = folderMapper.selectById(folder.getId());
        Assertions.assertNull(dbFolder);
    }

    @Test
    void createNullRequestTest(){

        // 期待这里会抛一个异常
        assertThrows(
                // 期待这里的异常类型是IllegalArgumentException 而不是NPE
                IllegalArgumentException.class,
                () -> folderService.createFolder(null)
                // lambda就是把这一段代码给包了起来延迟执行 有人调用 JUnit就会自己去执行它
        );

    }

    @Test
    void getNullIdTest(){
        assertThrows(
                IllegalArgumentException.class,
                () -> folderService.getFolder(null));
    }

    @Test
    void getNotExistIdTest(){
        assertThrows(IllegalArgumentException.class,
                () -> folderService.getFolder(null)
        );
    }

    @Test
    void updateNotExistIdTest(){
        assertThrows(IllegalArgumentException.class,() -> folderService.updateFolder(null, null));

    }

    @Test
    void deleteNotExistIdTest(){
        assertThrows(IllegalArgumentException.class,() -> folderService.deleteFolder(null));
    }






}
