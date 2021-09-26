package com.roc.malltiny.modules.ums.service;

import com.roc.malltiny.modules.ums.dto.UmsAdminParam;
import com.roc.malltiny.modules.ums.dto.UpdateAdminPasswordParam;
import com.roc.malltiny.modules.ums.model.UmsAdmin;
import com.baomidou.mybatisplus.extension.service.IService;
import com.roc.malltiny.modules.ums.model.UmsResource;
import com.roc.malltiny.modules.ums.model.UmsRole;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * <p>
 * 后台用户表 服务类
 * </p>
 *
 * @author roc
 * @since 2021-09-24
 */

/**
 * 后台管理员service
 */
public interface UmsAdminService extends IService<UmsAdmin> {

    // 根据用户名获取后台管理员
    UmsAdmin getAdminByUsername(String username);

    // 注册功能
    UmsAdmin register(UmsAdminParam umsAdminParam);

    // 登录功能
    String login(String username, String password);

    // 刷新token的功能
    String refreshToken(String oldToken);

    // 根据用户名或昵称分页查询用户
    Page<UmsAdmin> list(String keyword, Integer pageSize, Integer pageNum);

    // 修改指定用户信息
    boolean update(Long id, UmsAdmin admin);

    // 删除指定用户
    boolean delete(Long id);

    // 修改用户角色关系
    int updateRole(Long adminId, List<Long> roleIds);

    // 获取用户角色
    List<UmsRole> getRoleList(Long adminId);

    // 获取指定用户可访问的资源
    List<UmsResource> getResourceList(Long adminId);

    // 修改密码
    int updatePassword(UpdateAdminPasswordParam passwordParam);
}
