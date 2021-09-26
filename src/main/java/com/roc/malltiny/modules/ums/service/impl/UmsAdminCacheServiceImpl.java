package com.roc.malltiny.modules.ums.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.roc.malltiny.common.service.RedisService;
import com.roc.malltiny.modules.ums.mapper.UmsAdminMapper;
import com.roc.malltiny.modules.ums.model.UmsAdmin;
import com.roc.malltiny.modules.ums.model.UmsAdminRoleRelation;
import com.roc.malltiny.modules.ums.model.UmsResource;
import com.roc.malltiny.modules.ums.service.UmsAdminCacheService;
import com.roc.malltiny.modules.ums.service.UmsAdminRoleRelationService;
import com.roc.malltiny.modules.ums.service.UmsAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作缓存的类
 */
@Service
public class UmsAdminCacheServiceImpl implements UmsAdminCacheService {

    @Autowired
    private UmsAdminService adminService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private UmsAdminMapper adminMapper;

    @Autowired
    private UmsAdminRoleRelationService adminRoleRelationService;

    @Value("${redis.database}")
    private String REDIS_DATABASE;
    @Value("${redis.expire.common}")
    private Long REDIS_EXPIRE;
    @Value("${redis.key.admin}")
    private String REDIS_KEY_ADMIN;
    @Value("${redis.key.resourceList}")
    private String REDIS_KEY_RESOURCE_LIST;


    @Override
    public void delAdmin(Long adminId) {
        UmsAdmin admin = adminService.getById(adminId);
        if (admin != null) {
            String key = REDIS_DATABASE + ":" + REDIS_KEY_ADMIN + ":" + admin.getUsername();
            redisService.del(key);
        }
    }

    @Override
    public void delResourceList(Long adminId) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_RESOURCE_LIST + ":" + adminId;
        redisService.del(key);
    }

    @Override
    public void delResourceListByRole(Long roleId) {
        // 根据RoleRelation查询adminId，其实就是role和admin的多对多查询。
        QueryWrapper<UmsAdminRoleRelation> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(UmsAdminRoleRelation::getRoleId, roleId);
        List<UmsAdminRoleRelation> relationList = adminRoleRelationService.list(wrapper);
        if (CollUtil.isNotEmpty(relationList)) {
            String keyPrefix = REDIS_DATABASE + ":" + REDIS_KEY_RESOURCE_LIST + ":";
            // 这里使用到了流 stream
            List<String> keys = relationList.stream().map(relation -> keyPrefix + relation.getAdminId()).collect(Collectors.toList());
            redisService.del(keys);
        }
    }

    @Override
    public void delResourceListByRoleIds(List<Long> roleIds) {
        // 根据RoleRelation查询adminId，其实就是role和admin的多对多查询。
        QueryWrapper<UmsAdminRoleRelation> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(UmsAdminRoleRelation::getRoleId, roleIds);
        List<UmsAdminRoleRelation> relationList = adminRoleRelationService.list(wrapper);
        if (CollUtil.isNotEmpty(relationList)) {
            String keyPrefix = REDIS_DATABASE + ":" + REDIS_KEY_RESOURCE_LIST + ":";
            // 这里使用到了流 stream
            List<String> keys = relationList.stream().map(relation -> keyPrefix + relation.getAdminId()).collect(Collectors.toList());
            redisService.del(keys);
        }
    }

    @Override
    public void delResourceListByResource(Long resourceId) {
        // 根据resourceId找出adminId即可删除
        List<Long> adminIdList = adminMapper.getAdminIdList(resourceId);
        if (CollUtil.isNotEmpty(adminIdList)) {
           String keyPrefix = REDIS_DATABASE + ":" + REDIS_KEY_RESOURCE_LIST + ":";
            List<String> keys = adminIdList.stream().map(adminId -> keyPrefix + adminId).collect(Collectors.toList());
            redisService.del(keys);
        }
    }

    @Override
    public UmsAdmin getAdmin(String username) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_ADMIN + ":" + username;
        return (UmsAdmin) redisService.get(key);
    }

    @Override
    public void setAdmin(UmsAdmin admin) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_ADMIN + ":" + admin.getUsername();
        redisService.set(key, admin, REDIS_EXPIRE);
    }

    @Override
    public List<UmsResource> getResourceList(Long adminId) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_RESOURCE_LIST + ":" + adminId;
        return (List<UmsResource>) redisService.get(key);
    }

    @Override
    public void setResourceList(Long adminId, List<UmsResource> resourcesList) {
        String key = REDIS_DATABASE + ":" + REDIS_KEY_RESOURCE_LIST + ":" + adminId;
        redisService.set(key, resourcesList, REDIS_EXPIRE);
    }
}
