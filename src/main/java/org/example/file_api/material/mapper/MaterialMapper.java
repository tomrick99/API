package org.example.file_api.material.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.file_api.material.domain.MaterialDO;

import java.util.List;

@Mapper
public interface MaterialMapper extends BaseMapper<MaterialDO> {

    @Insert("""
            INSERT INTO material_mybatis (title, type, description, created_at, updated_at)
            VALUES (#{title}, #{type}, #{description}, #{createdAt}, #{updatedAt})
            """)
    int insertBySql(MaterialDO material);

    @Select("""
            SELECT id, title, type, description, created_at, updated_at
            FROM material_mybatis
            WHERE id = #{id}
            """)
    MaterialDO selectByIdBySql(Long id);

    @Update("""
            UPDATE material_mybatis
            SET title = #{title},
                type = #{type},
                description = #{description},
                updated_at = #{updatedAt}
            WHERE id = #{id}
            """)
    int updateBySql(MaterialDO material);

    @Delete("""
            DELETE FROM material_mybatis
            WHERE id = #{id}
            """)
    int deleteByIdBySql(Long id);

    @Select("""
            <script>
            SELECT id, title, type, description, created_at, updated_at
            FROM material_mybatis
            <where>
                <if test="type != null and type != ''">
                    type = #{type}
                </if>
            </where>
            ORDER BY created_at DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<MaterialDO> selectByTypeOrderByCreatedAtDesc(
            @Param("type") String type,
            @Param("limit") long limit,
            @Param("offset") long offset);

    // 查询符合条件的总记录数 添加@Param 明确SQL参数名称
    @Select("""
            <script>
            SELECT COUNT(*)
            FROM material_mybatis
            <where>
                <if test="type != null and type != ''">
                    type = #{type}
                </if>
            </where>
            </script>
            """)
    long countByType(@Param("type") String type);
}
