package com.puffbytes.puffbytes.common.util;

import com.puffbytes.puffbytes.common.exception.UnauthenticatedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthenticatedException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (principal == null) {
            throw new UnauthenticatedException("User not authenticated");
        }

        return principal.toString();
    }
}
