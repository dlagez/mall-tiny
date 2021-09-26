package com.roc.malltiny.modules.ums.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.roc.malltiny.common.exception.Asserts;
import com.roc.malltiny.domain.AdminUserDetails;
import com.roc.malltiny.modules.ums.dto.UmsAdminParam;
import com.roc.malltiny.modules.ums.dto.UpdateAdminPasswordParam;
import com.roc.malltiny.modules.ums.mapper.UmsAdminLoginLogMapper;
import com.roc.malltiny.modules.ums.mapper.UmsResourceMapper;
import com.roc.malltiny.modules.ums.mapper.UmsRoleMapper;
import com.roc.malltiny.modules.ums.model.*;
import com.roc.malltiny.modules.ums.mapper.UmsAdminMapper;
import com.roc.malltiny.modules.ums.service.UmsAdminCacheService;
import com.roc.malltiny.modules.ums.service.UmsAdminRoleRelationService;
import com.roc.malltiny.modules.ums.service.UmsAdminService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roc.malltiny.security.util.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 后台管理员管理service实现类
 * </p>
 *
 * @author roc
 * @since 2021-09-24
 */
@Service
public class UmsAdminServiceImpl extends ServiceImpl<UmsAdminMapper, UmsAdmin> implements UmsAdminService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UmsAdminServiceImpl.class);

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UmsAdminLoginLogMapper loginLogMapper;

    @Autowired
    private UmsAdminCacheService adminCacheService;

    @Autowired
    private UmsAdminRoleRelationService roleRelationService;

    @Autowired
    private UmsRoleMapper roleMapper;

    @Autowired
    private UmsResourceMapper resourceMapper;


    @Override
    public UmsAdmin getAdminByUsername(String username) {
        UmsAdmin admin = adminCacheService.getAdmin(username);
        if (admin != null) return admin;
        // 如果查询出来的用户有多个
        QueryWrapper<UmsAdmin> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(UmsAdmin::getUsername, username);
        List<UmsAdmin> list = list(wrapper);
        if (list != null && list.size() > 0) {
            UmsAdmin admin1 = list.get(0);
            adminCacheService.setAdmin(admin1);
            return admin1;
        }
        return null;
    }

    @Override
    public UmsAdmin register(UmsAdminParam umsAdminParam) {
        UmsAdmin umsAdmin = new UmsAdmin();
        BeanUtils.copyProperties(umsAdminParam, umsAdmin);
        umsAdmin.setCreateTime(new Date());
        umsAdmin.setStatus(1);
        // 查询是否有想用用户名的用户
        QueryWrapper<UmsAdmin> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(UmsAdmin::getUsername, umsAdmin.getUsername());
        List<UmsAdmin> umsAdminList = list(wrapper);
        // 如果用户名已经存在了，直接返回null
        if (umsAdminList.size() > 0) {
            return null;
        }
        // 将密码进行加密操作
        String encodePassword = passwordEncoder.encode(umsAdmin.getPassword());
        umsAdmin.setPassword(encodePassword);
        baseMapper.insert(umsAdmin);
        return umsAdmin;
    }

    /**
     * 总体的流程是：根据用户名查询用户->比较密码->是否被封禁->创建token并添加到认证->记录登录用户->返回token
     * @param username
     * @param password
     * @return
     */
    @Override
    public String login(String username, String password) {
        String token = null;
        try {
            UserDetails userDetails = loadUserByUsername(username);
            // password是用户传递过来的，没有加密，userDetails.getPassword()是数据库查询出来的，加密了。
            if (!passwordEncoder.matches(password, userDetails.getPassword())) {
                Asserts.fail("密码不正确");
            }
            if (!userDetails.isEnabled()) {
                Asserts.fail("账号已经被禁用");
            }
            // 创建验证
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            token = jwtTokenUtil.generateToken(userDetails);
            // updateLoginTimeByUsername(username);
            insertLoginLog(username);
        } catch (AuthenticationException e) {
            LOGGER.warn("登录异常：{}", e.getMessage());
        }
        return token;
    }

    /**
     * 添加登录记录
     * @param username
     */
    private void insertLoginLog(String username) {
        UmsAdmin admin = getAdminByUsername(username);
        if (admin == null) return;
        UmsAdminLoginLog loginLog = new UmsAdminLoginLog();
        loginLog.setAdminId(admin.getId());
        loginLog.setCreateTime(new Date());
        // 拿到request
        ServletRequestAttributes attributes =(ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        loginLog.setIp(request.getRemoteAddr());
        loginLogMapper.insert(loginLog);
    }

    /**
     * 根据用户名修改登录时间
     * @param username
     */
    private void updateLoginTimeByUsername(String username) {
        UmsAdmin record = new UmsAdmin();
        record.setLoginTime(new Date());
        QueryWrapper<UmsAdmin> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(UmsAdmin::getUsername, username);
        // 使用到了更新的wrapper
        update(record, wrapper);
    }

    @Override
    public String refreshToken(String oldToken) {
        return jwtTokenUtil.refreshHeadToken(oldToken);
    }

    @Override
    public Page<UmsAdmin> list(String keyword, Integer pageSize, Integer pageNum) {
        // 分页查询
        Page<UmsAdmin> page = new Page<>(pageNum, pageSize);
        // 查询条件，使用keyword查询用户名和昵称
        QueryWrapper<UmsAdmin> wrapper = new QueryWrapper<>();
        LambdaQueryWrapper<UmsAdmin> lambda = wrapper.lambda();
        if (StrUtil.isNotEmpty(keyword)) {
            lambda.like(UmsAdmin::getUsername, keyword);
            lambda.or().like(UmsAdmin::getNickName, keyword);
        }
        // 返回分页查询结果
        return page(page, wrapper);
    }

    @Override
    public boolean update(Long id, UmsAdmin admin) {
        admin.setId(id);
        UmsAdmin rawAdmin = getById(id);
        if (rawAdmin.getPassword().equals(admin.getPassword())) {
            // 与原密码相同的不需要修改
            admin.setPassword(null);
        } else {
            // 与原加密密码不同的需要修改
            if (StrUtil.isEmpty(admin.getPassword())) {
                // 密码为空也不需要修改
                admin.setPassword(null);
            } else {
                admin.setPassword(passwordEncoder.encode(admin.getPassword()));
            }
        }
        boolean success = updateById(admin);
        // 修改了之后删除缓存的用户。
        adminCacheService.delAdmin(id);
        return success;
    }

    @Override
    public boolean delete(Long id) {
        // 首先删除缓存的用户
        adminCacheService.delAdmin(id);
        // 删除数据库的用户
        boolean success = removeById(id);
        // 删除用户的缓存资源
        adminCacheService.delResourceList(id);
        return success;
    }

    @Override
    public int updateRole(Long adminId, List<Long> roleIds) {
        // 计算ids的长度
        int count = roleIds == null ? 0 : roleIds.size();
        // 先删除原来的关系
        QueryWrapper<UmsAdminRoleRelation> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(UmsAdminRoleRelation::getAdminId, adminId);
        // 这个方法是IService的方法
        roleRelationService.remove(wrapper);
        // 建立关系
        if (!CollectionUtils.isEmpty(roleIds)) {
            ArrayList<UmsAdminRoleRelation> list = new ArrayList<>();
            for (Long roleId : roleIds) {
                UmsAdminRoleRelation roleRelation = new UmsAdminRoleRelation();
                roleRelation.setAdminId(adminId);
                roleRelation.setRoleId(roleId);
                list.add(roleRelation);
            }
            roleRelationService.saveBatch(list);
        }
        adminCacheService.delResourceList(adminId);
        return count;
    }

    @Override
    public List<UmsRole> getRoleList(Long adminId) {
        return roleMapper.getRoleList(adminId);
    }

    @Override
    public List<UmsResource> getResourceList(Long adminId) {
        // 缓存中有值就直接返回，减轻数据库的压力
        List<UmsResource> resourceList = adminCacheService.getResourceList(adminId);
        if (CollUtil.isNotEmpty(resourceList)) {
            return resourceList;
        }
        // 根据adminId从数据库查询resourceList
        resourceList = resourceMapper.getResourceList(adminId);
        // 查询出来有值
        if (CollUtil.isNotEmpty(resourceList)) {
            // 放入缓存中
            adminCacheService.setResourceList(adminId, resourceList);
        }
        return resourceList;
    }

    @Override
    public int updatePassword(UpdateAdminPasswordParam passwordParam) {
        // 检测用户传来的数据，有空值就返回
        if (StrUtil.isEmpty(passwordParam.getUsername())
            || StrUtil.isEmpty(passwordParam.getOldPassword())
            || StrUtil.isEmpty(passwordParam.getNewPassword())) {
            return -1;
        }
        QueryWrapper<UmsAdmin> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(UmsAdmin::getUsername, passwordParam.getUsername());
        // 从数据库查询出来的用户列表
        List<UmsAdmin> adminList = list(wrapper);
        if (CollUtil.isEmpty(adminList)) {
            return -2;
        }
        UmsAdmin umsAdmin = adminList.get(0);
        // 用户传来的密码和数据库的密码不一致
        if (!passwordEncoder.matches(passwordParam.getOldPassword(), umsAdmin.getPassword())) {
            return -3;
        }
        umsAdmin.setPassword(passwordEncoder.encode(passwordParam.getNewPassword()));
        updateById(umsAdmin);
        return 1;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        UmsAdmin admin = getAdminByUsername(username);
        if (admin != null) {
            List<UmsResource> resourceList = getResourceList(admin.getId());
            return new AdminUserDetails(admin, resourceList);
        }
        throw new UsernameNotFoundException("用户名或密码错误");
    }
}
