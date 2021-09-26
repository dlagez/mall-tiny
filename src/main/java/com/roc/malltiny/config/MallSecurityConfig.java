package com.roc.malltiny.config;

import com.roc.malltiny.modules.ums.model.UmsResource;
import com.roc.malltiny.modules.ums.service.UmsAdminService;
import com.roc.malltiny.modules.ums.service.UmsResourceService;
import com.roc.malltiny.security.component.DynamicSecurityService;
import com.roc.malltiny.security.config.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * mall-security模块相关配置
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MallSecurityConfig extends SecurityConfig {

    @Autowired
    private UmsAdminService adminService;

    @Autowired
    private UmsResourceService resourceService;

    @Bean
    public UserDetailsService userDetailsService () {
        // 获取登录用户信息
        return username -> adminService.loadUserByUsername(username);
    }

    @Bean
    public DynamicSecurityService dynamicSecurityService() {
        return new DynamicSecurityService() {
            @Override
            public Map<String, ConfigAttribute> loadDataSource() {
                Map<String, ConfigAttribute> map = new ConcurrentHashMap<>();
                List<UmsResource> resourceList = resourceService.list();
                for (UmsResource umsResource : resourceList) {
                    map.put(umsResource.getUrl(), new org.springframework.security.access.SecurityConfig(umsResource.getId() + ":" + umsResource.getName()));
                }
                return map;
            }
        };
    }
}
