package com.project.eliascphoto.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationIpGuard.class);

    private final String allowedIp;
    private final boolean production;

    public RegistrationIpGuard(
            Environment environment,
            @Value("${app.security.registration.allowed-ip:}") String allowedIp) {
        this.allowedIp = allowedIp == null ? "" : allowedIp.trim();
        this.production = environment.acceptsProfiles(Profiles.of("prod"));
    }

    public void requireAllowed(HttpServletRequest request, String registrationId) {
        if (!production) {
            LOGGER.info("Registration id={} stage=ip_check result=bypassed profile=non-prod", registrationId);
            return;
        }

        String clientIp = getClientIp(request);
        boolean allowed = StringUtils.hasText(allowedIp) && allowedIp.equals(clientIp);
        LOGGER.info(
                "Registration id={} stage=ip_check profile=prod remote_addr={} selected_ip={} allowlist_configured={} match={}",
                registrationId,
                request.getRemoteAddr(),
                clientIp,
                StringUtils.hasText(allowedIp),
                allowed);
        if (!allowed) {
            LOGGER.warn("Registration id={} stage=ip_check result=denied", registrationId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Registro no permitido desde esta IP");
        }
        LOGGER.info("Registration id={} stage=ip_check result=allowed", registrationId);
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
