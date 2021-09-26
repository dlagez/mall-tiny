package com.roc.malltiny.modules.ums.mapper;

import com.roc.malltiny.modules.ums.model.UmsResource;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lombok.extern.java.Log;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 后台资源表 Mapper 接口
 * </p>
 *
 * @author roc
 * @since 2021-09-24
 */
public interface UmsResourceMapper extends BaseMapper<UmsResource> {
    /**
     * 获取用户所有可访问资源
     * @param adminId
     * @return
     */
    List<UmsResource> getResourceList(@Param("adminId") Long adminId);

    /**
     * 根据角色id获取资源
     * @param roleId
     * @return
     */
    List<UmsResource> getResourceListByRoleId(@Param("roleId") Long roleId);


}
