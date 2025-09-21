package sit.int204.mobileshop.filters;

import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import sit.int204.mobileshop.dtos.UserResponseDto;
import sit.int204.mobileshop.services.JwtService;
import sit.int204.mobileshop.services.UserService;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String USER_ID_CLAIM = "id";

    private final UserService userService;
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (!hasValidAuthorizationHeader(authHeader)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            String token = extractToken(authHeader);
            JWTClaimsSet claims = jwtService.validateAccessToken(token);
            Long userId = extractUserId(claims);

            UserResponseDto user = getUserAndValidate(userId, response);
            if (user == null) return;

            setAuthenticationContext(user, request);

        } catch (Exception e) {
            writeErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid or expired token");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean hasValidAuthorizationHeader(String authHeader) {
        return authHeader != null && authHeader.startsWith(BEARER_PREFIX);
    }

    private String extractToken(String authHeader) {
        return authHeader.substring(BEARER_PREFIX.length());
    }

    private Long extractUserId(JWTClaimsSet claims) throws IllegalArgumentException {
        Object idClaim = claims.getClaim(USER_ID_CLAIM);

        if (idClaim == null) {
            throw new IllegalArgumentException("Token missing user ID claim");
        }

        if (idClaim instanceof Number) {
            return ((Number) idClaim).longValue();
        }

        if (idClaim instanceof String) {
            try {
                return Long.parseLong((String) idClaim);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid user ID format in token");
            }
        }

        throw new IllegalArgumentException("User ID claim has invalid type");
    }

    private UserResponseDto getUserAndValidate(Long userId, HttpServletResponse response)
            throws IOException {
        try {
            UserResponseDto user = userService.getUserById(userId);
            if (user == null) {
                writeErrorResponse(response, HttpStatus.UNAUTHORIZED, "User not found");
                return null;
            }
            if ("INACTIVE".equals(user.getStatus())) {
                writeErrorResponse(response, HttpStatus.FORBIDDEN, "User account is inactive");
                return null;
            }
            return user;

        } catch (Exception e) {
            writeErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "Authentication service error");
            return null;
        }
    }

    private void setAuthenticationContext(UserResponseDto user, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(user, null, null);
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private void writeErrorResponse(HttpServletResponse response, HttpStatus status, String message)
            throws IOException {

        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format(
                "{\"error\":\"%s\",\"status\":%d,\"timestamp\":\"%s\"}",
                message,
                status.value(),
                java.time.Instant.now().toString()
        ));
        response.getWriter().flush();
    }
    
}