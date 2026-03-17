package task1.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import task1.entity.User;
import task1.repository.UserRepository;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements Filter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain filterChain) throws ServletException, IOException {

        HttpServletRequest req = (HttpServletRequest) request; //
        HttpServletResponse res = (HttpServletResponse) response; //

        String authHeader = req.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                String username = jwtService.validateTokenAndGetUsername(token);
                log.info("Validated JWT token and extracted username {}", username);

                if (username != null) {
                    User user = userRepository.findByUsername(username).orElse(null);
                    if (user != null) {
                        List<SimpleGrantedAuthority> authorities = Arrays.stream(user.getRoles().name().split(","))
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.trim()))
                                .collect(Collectors.toList());

                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(username, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.info("Authentication for URI {} successful for user {}", ((HttpServletRequest) request).getRequestURI(), username);
                    }
                }
            } catch (IllegalArgumentException e) {  // контроллер эдвайс
                log.error("Token validation failed: {}", e.getMessage());
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.getWriter().write("{\"error\": \"Invalid token\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}