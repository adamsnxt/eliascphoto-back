package com.project.eliascphoto.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class RegistrationIpGuard {

    private final String allowedIp;
    private final boolean production;

    public RegistrationIpGuard(
            Environment environment,
            @Value("${app.security.registration.allowed-ip:}") String allowedIp) {
        this.allowedIp = allowedIp == null ? "" : allowedIp.trim();
        this.production = environment.acceptsProfiles(Profiles.of("prod"));
    }

    public void requireAllowed(HttpServletRequest request) {
        if (!production) {
            return;
        }

        String clientIp = getClientIp(request);
        if (!StringUtils.hasText(allowedIp) || !allowedIp.equals(clientIp)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Registro no permitido desde esta IP");
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            String[] addresses = forwardedFor.split(",");
            return addresses[addresses.length - 1].trim();
        }
        return request.getRemoteAddr();
    }
}
