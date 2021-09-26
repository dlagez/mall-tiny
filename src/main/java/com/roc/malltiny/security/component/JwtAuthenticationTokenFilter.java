package com.roc.malltiny.security.component;

import com.roc.malltiny.security.util.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationTokenFilter.class);

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Value("${jwt.tokenHeader}")
    private String tokenHeader;  // Authorization

    @Value("${jwt.tokenHead}")
    private String tokenHead;  // Bearer

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        //  大概是这个样子的： Authorization：Bearer *************
        // 根据properties定义的tokenHeader拿到请求的authHeader。
        // authHeader的值是Bearer
        String authHeader = request.getHeader(this.tokenHeader);
        // 判断用户请求的authHeader是不是咱们定义的tokenHead，是就继续执行。
        if (authHeader != null && authHeader.startsWith(this.tokenHead)) {
            // substring传入一个值，表示获取这个数组索引之后的字符串，比如substring(5)，会取(6:)的字符串
            // 这里的authToken是是JWT生成，传给用户的 这个的加密形式header.payload.signature
            String authToken = authHeader.substring(this.tokenHead.length());

            // 我们在jwtTokenUtil定义了根据token获取用户名的方法
            String username = jwtTokenUtil.getUserNameFromToken(authToken);
            LOGGER.info("checking username:{}", username);

            // 这里是判断有了用户名，但是没有认证的情况，就会对用户进行认证
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 从数据库加载用户
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                // 判断token是否还有效
                if (jwtTokenUtil.validateToken(authToken, userDetails)) {
                    // 使用用户的信息新建认证信息
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    LOGGER.info("authenticated user: {}", username);
                    // 设置认证信息到SecurityContextHolder,鉴权成功
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
