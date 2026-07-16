package org.example.file_api.material.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.file_api.material.domain.MaterialDO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class MaterialMapperTest {

    // 所有测试插入的数据 标题都用mapper-test-开头 目的是方便测试 以及结束后只删除测试数据 手动加入的不删
    private static final String TEST_TITLE_PREFIX = "mapper-test-";

    @Autowired
    private MaterialMapper materialMapper;

    @BeforeEach
    @AfterEach
    void cleanTestData() {
        materialMapper.delete(new LambdaQueryWrapper<MaterialDO>()
                .likeRight(MaterialDO::getTitle, TEST_TITLE_PREFIX));
    } //每一个测试前后都会执行. likeRight的意思是WHERE title LIKE 'mapper-test-%'

    @Test
    void shouldInsertAndSelectWithMyBatisPlusBaseMapper() {
        MaterialDO material = newTestMaterial("base-insert", "note");

        int rows = materialMapper.insert(material);

        assertEquals(1, rows);
        assertNotNull(material.getId());

        MaterialDO selected = materialMapper.selectById(material.getId());
        assertNotNull(selected);
        assertEquals(material.getTitle(), selected.getTitle());
        assertEquals(material.getType(), selected.getType());
        assertEquals(material.getDescription(), selected.getDescription());
        assertNotNull(selected.getCreatedAt());
        assertNotNull(selected.getUpdatedAt());
    }

    @Test
    void shouldUpdateAndDeleteWithMyBatisPlusBaseMapper() {
        MaterialDO material = newTestMaterial("base-update", "note");
        materialMapper.insert(material);

        material.setTitle(TEST_TITLE_PREFIX + "base-updated-" + UUID.randomUUID());
        material.setDescription("updated by BaseMapper");
        material.setUpdatedAt(LocalDateTime.now());

        int updateRows = materialMapper.updateById(material);
        MaterialDO updated = materialMapper.selectById(material.getId());

        assertEquals(1, updateRows);
        assertEquals(material.getTitle(), updated.getTitle());
        assertEquals("updated by BaseMapper", updated.getDescription());

        int deleteRows = materialMapper.deleteById(material.getId());

        assertEquals(1, deleteRows);
        assertNull(materialMapper.selectById(material.getId()));
    }

    @Test
    void shouldRunRawSqlMethods() {
        MaterialDO material = newTestMaterial("raw-sql", "doc");

        int insertRows = materialMapper.insertBySql(material);

        assertEquals(1, insertRows);

        MaterialDO inserted = materialMapper.selectOne(new LambdaQueryWrapper<MaterialDO>()
                .eq(MaterialDO::getTitle, material.getTitle()));
        assertNotNull(inserted);

        inserted.setDescription("updated by raw SQL");
        inserted.setUpdatedAt(LocalDateTime.now());

        int updateRows = materialMapper.updateBySql(inserted);
        MaterialDO updated = materialMapper.selectByIdBySql(inserted.getId());

        assertEquals(1, updateRows);
        assertEquals("updated by raw SQL", updated.getDescription());

        int deleteRows = materialMapper.deleteByIdBySql(inserted.getId());

        assertEquals(1, deleteRows);
        assertNull(materialMapper.selectById(inserted.getId()));
    }

    @Test
    void shouldQueryByTypeWithOrderAndLimitOffset() {
        MaterialDO older = newTestMaterial("older", "video");
        older.setCreatedAt(LocalDateTime.now().minusDays(1));
        older.setUpdatedAt(older.getCreatedAt());
        MaterialDO newer = newTestMaterial("newer", "video");
        newer.setCreatedAt(LocalDateTime.now());
        newer.setUpdatedAt(newer.getCreatedAt());

        materialMapper.insert(older);
        materialMapper.insert(newer);

        List<MaterialDO> firstPage = materialMapper.selectByTypeOrderByCreatedAtDesc("video", 1, 0);
        List<MaterialDO> secondPage = materialMapper.selectByTypeOrderByCreatedAtDesc("video", 1, 1);

        assertEquals(1, firstPage.size());
        assertEquals(1, secondPage.size());
        assertTrue(firstPage.getFirst().getCreatedAt().isAfter(secondPage.getFirst().getCreatedAt()));
    }

    private MaterialDO newTestMaterial(String titlePart, String type) {
        LocalDateTime now = LocalDateTime.now();
        MaterialDO material = new MaterialDO();
        material.setTitle(TEST_TITLE_PREFIX + titlePart + "-" + UUID.randomUUID());
        material.setType(type);
        material.setDescription("created by mapper integration test");
        material.setCreatedAt(now);
        material.setUpdatedAt(now);
        return material;
    }
}
