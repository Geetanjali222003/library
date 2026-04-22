package com.authorbooksystem.crud.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    @Autowired CustomerUserDetailsService userDetailsService;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain chain)
//        throws ServletException, IOException{
//        String header=request.getHeader("Authorization");
//        if(header!=null && header.startsWith("Bearer ")){
//            String token=header.substring(7);
//            Claims claims=JwtUtil.validateToken(token);
//            String username=claims.getSubject();
//            UserDetails userDetails=userDetailsService.loadUserByUsername(username);
//
//            UsernamePasswordAuthenticationToken auth=new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
//            SecurityContextHolder.getContext().setAuthentication(auth);
//        }
//        chain.doFilter(request,response);
//    }
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // ✅ SKIP JWT CHECK FOR LOGIN & SWAGGER
        if (path.startsWith("/auth") || 
            path.startsWith("/swagger") || 
            path.startsWith("/v3/api-docs") || 
            path.equals("/swagger-ui.html") ||
            path.startsWith("/api-docs")) {
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            try {
                String token = header.substring(7);
                Claims claims = JwtUtil.validateToken(token);
                String username = claims.getSubject();
                
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                // Log the error and continue without setting authentication
                // This will result in 401 Unauthorized for protected endpoints
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(request, response);
    }

}
