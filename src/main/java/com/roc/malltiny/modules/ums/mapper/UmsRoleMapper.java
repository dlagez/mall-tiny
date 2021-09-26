package com.roc.malltiny.modules.ums.mapper;

import com.roc.malltiny.modules.ums.model.UmsRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 后台用户角色表 Mapper 接口
 * </p>
 *
 * @author roc
 * @since 2021-09-24
 */
public interface UmsRoleMapper extends BaseMapper<UmsRole> {
    List<UmsRole> getRoleList(@Param("adminId") Long adminId);
}
