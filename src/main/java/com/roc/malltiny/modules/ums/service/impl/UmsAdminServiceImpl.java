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
 * ?????????????????????service?????????
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
        // ????????????????????????????????????
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
        // ???????????????????????????????????????
        QueryWrapper<UmsAdmin> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(UmsAdmin::getUsername, umsAdmin.getUsername());
        List<UmsAdmin> umsAdminList = list(wrapper);
        // ?????????????????????????????????????????????null
        if (umsAdminList.size() > 0) {
            return null;
        }
        // ???????????????????????????
        String encodePassword = passwordEncoder.encode(umsAdmin.getPassword());
        umsAdmin.setPassword(encodePassword);
        baseMapper.insert(umsAdmin);
        return umsAdmin;
    }

    /**
     * ????????????????????????????????????????????????->????????????->???????????????->??????token??????????????????->??????????????????->??????token
     * @param username
     * @param password
     * @return
     */
    @Override
    public String login(String username, String password) {
        String token = null;
        try {
            UserDetails userDetails = loadUserByUsername(username);
            // password??????????????????????????????????????????userDetails.getPassword()??????????????????????????????????????????
            if (!passwordEncoder.matches(password, userDetails.getPassword())) {
                Asserts.fail("???????????????");
            }
            if (!userDetails.isEnabled()) {
                Asserts.fail("?????????????????????");
            }
            // ????????????
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            token = jwtTokenUtil.generateToken(userDetails);
            // updateLoginTimeByUsername(username);
            insertLoginLog(username);
        } catch (AuthenticationException e) {
            LOGGER.warn("???????????????{}", e.getMessage());
        }
        return token;
    }

    /**
     * ??????????????????
     * @param username
     */
    private void insertLoginLog(String username) {
        UmsAdmin admin = getAdminByUsername(username);
        if (admin == null) return;
        UmsAdminLoginLog loginLog = new UmsAdminLoginLog();
        loginLog.setAdminId(admin.getId());
        loginLog.setCreateTime(new Date());
        // ??????request
        ServletRequestAttributes attributes =(ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        loginLog.setIp(request.getRemoteAddr());
        loginLogMapper.insert(loginLog);
    }

    /**
     * ?????????????????????????????????
     * @param username
     */
    private void updateLoginTimeByUsername(String username) {
        UmsAdmin record = new UmsAdmin();
        record.setLoginTime(new Date());
        QueryWrapper<UmsAdmin> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(UmsAdmin::getUsername, username);
        // ?????????????????????wrapper
        update(record, wrapper);
    }

    @Override
    public String refreshToken(String oldToken) {
        return jwtTokenUtil.refreshHeadToken(oldToken);
    }

    @Override
    public Page<UmsAdmin> list(String keyword, Integer pageSize, Integer pageNum) {
        // ????????????
        Page<UmsAdmin> page = new Page<>(pageNum, pageSize);
        // ?????????????????????keyword????????????????????????
        QueryWrapper<UmsAdmin> wrapper = new QueryWrapper<>();
        LambdaQueryWrapper<UmsAdmin> lambda = wrapper.lambda();
        if (StrUtil.isNotEmpty(keyword)) {
            lambda.like(UmsAdmin::getUsername, keyword);
            lambda.or().like(UmsAdmin::getNickName, keyword);
        }
        // ????????????????????????
        return page(page, wrapper);
    }

    @Override
    public boolean update(Long id, UmsAdmin admin) {
        admin.setId(id);
        UmsAdmin rawAdmin = getById(id);
        if (rawAdmin.getPassword().equals(admin.getPassword())) {
            // ????????????????????????????????????
            admin.setPassword(null);
        } else {
            // ???????????????????????????????????????
            if (StrUtil.isEmpty(admin.getPassword())) {
                // ??????????????????????????????
                admin.setPassword(null);
            } else {
                admin.setPassword(passwordEncoder.encode(admin.getPassword()));
            }
        }
        boolean success = updateById(admin);
        // ???????????????????????????????????????
        adminCacheService.delAdmin(id);
        return success;
    }

    @Override
    public boolean delete(Long id) {
        // ???????????????????????????
        adminCacheService.delAdmin(id);
        // ????????????????????????
        boolean success = removeById(id);
        // ???????????????????????????
        adminCacheService.delResourceList(id);
        return success;
    }

    @Override
    public int updateRole(Long adminId, List<Long> roleIds) {
        // ??????ids?????????
        int count = roleIds == null ? 0 : roleIds.size();
        // ????????????????????????
        QueryWrapper<UmsAdminRoleRelation> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(UmsAdminRoleRelation::getAdminId, adminId);
        // ???????????????IService?????????
        roleRelationService.remove(wrapper);
        // ????????????
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
        // ?????????????????????????????????????????????????????????
        List<UmsResource> resourceList = adminCacheService.getResourceList(adminId);
        if (CollUtil.isNotEmpty(resourceList)) {
            return resourceList;
        }
        // ??????adminId??????????????????resourceList
        resourceList = resourceMapper.getResourceList(adminId);
        // ??????????????????
        if (CollUtil.isNotEmpty(resourceList)) {
            // ???????????????
            adminCacheService.setResourceList(adminId, resourceList);
        }
        return resourceList;
    }

    @Override
    public int updatePassword(UpdateAdminPasswordParam passwordParam) {
        // ????????????????????????????????????????????????
        if (StrUtil.isEmpty(passwordParam.getUsername())
            || StrUtil.isEmpty(passwordParam.getOldPassword())
            || StrUtil.isEmpty(passwordParam.getNewPassword())) {
            return -1;
        }
        QueryWrapper<UmsAdmin> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(UmsAdmin::getUsername, passwordParam.getUsername());
        // ???????????????????????????????????????
        List<UmsAdmin> adminList = list(wrapper);
        if (CollUtil.isEmpty(adminList)) {
            return -2;
        }
        UmsAdmin umsAdmin = adminList.get(0);
        // ???????????????????????????????????????????????????
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
        throw new UsernameNotFoundException("????????????????????????");
    }
}
