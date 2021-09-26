package com.roc.malltiny.modules.ums.service.impl;

import com.roc.malltiny.modules.ums.dto.UmsAdminParam;
import com.roc.malltiny.modules.ums.dto.UpdateAdminPasswordParam;
import com.roc.malltiny.modules.ums.mapper.UmsAdminLoginLogMapper;
import com.roc.malltiny.modules.ums.model.UmsAdmin;
import com.roc.malltiny.modules.ums.mapper.UmsAdminMapper;
import com.roc.malltiny.modules.ums.model.UmsResource;
import com.roc.malltiny.modules.ums.model.UmsRole;
import com.roc.malltiny.modules.ums.service.UmsAdminCacheService;
import com.roc.malltiny.modules.ums.service.UmsAdminService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roc.malltiny.security.util.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 后台用户表 服务实现类
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

    private UmsAdminCacheService adminCacheService;
    @Override
    public UmsAdmin getAdminByUsername(String username) {
        return null;
    }

    @Override
    public UmsAdmin register(UmsAdminParam umsAdminParam) {
        return null;
    }

    @Override
    public String login(String username, String password) {
        return null;
    }

    @Override
    public String refreshToken(String oldToken) {
        return null;
    }

    @Override
    public Page<UmsAdmin> list(String keyword, Integer pageSize, Integer pageNum) {
        return null;
    }

    @Override
    public boolean update(Long id, UmsAdmin admin) {
        return false;
    }

    @Override
    public boolean delete(Long id) {
        return false;
    }

    @Override
    public int updateRole(Long adminId, List<Long> roleIds) {
        return 0;
    }

    @Override
    public List<UmsRole> getRoleList(Long adminId) {
        return null;
    }

    @Override
    public List<UmsResource> getResourceList(Long adminId) {
        return null;
    }

    @Override
    public int updatePassword(UpdateAdminPasswordParam passwordParam) {
        return 0;
    }
}
