package com.fixflow.security;

import com.fixflow.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import javax.crypto.SecretKey;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    private static final String SECRET_STRING = System.getenv("JWT_SECRET");
    private static final SecretKey SECRET_KEY;

    static {
        if (SECRET_STRING != null && !SECRET_STRING.isEmpty()) {
            SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));
        } else {
            // Provide a default for local testing if env is missing, but WARN heavily.
            System.err.println("WARNING: JWT_SECRET environment variable is missing. Using insecure fallback.");
            SECRET_KEY = Keys.hmacShaKeyFor("a-very-long-fallback-secret-key-that-should-never-be-used-in-production".getBytes(StandardCharsets.UTF_8));
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String authorizationHeader = requestContext.getHeaderString("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"Authentication is required\"}").build());
            return;
        }

        String token = authorizationHeader.substring("Bearer".length()).trim();

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Integer userId = Integer.valueOf(claims.getSubject());
            Role role = Role.valueOf(claims.get("role", String.class));

            requestContext.setSecurityContext(new UserSecurityContext(userId, role));

            // Check authorization rules
            Method resourceMethod = resourceInfo.getResourceMethod();
            Class<?> resourceClass = resourceInfo.getResourceClass();
            
            checkPermissions(resourceClass, role, requestContext);
            checkPermissions(resourceMethod, role, requestContext);
            
        } catch (Exception e) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"Invalid or expired token\"}").build());
        }
    }

    private void checkPermissions(AnnotatedElement annotatedElement, Role userRole, ContainerRequestContext requestContext) {
        if (annotatedElement == null) return;
        
        Secured secured = annotatedElement.getAnnotation(Secured.class);
        if (secured != null) {
            Role[] allowedRoles = secured.value();
            if (allowedRoles.length > 0) {
                List<Role> allowedList = Arrays.asList(allowedRoles);
                if (!allowedList.contains(userRole)) {
                    requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).entity("{\"status\": 403, \"error\": \"Forbidden\", \"message\": \"You do not have permission to perform this operation\"}").build());
                }
            }
        }
    }
}
