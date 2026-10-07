package com.project.eliascphoto.config;

import java.time.Duration;
import java.time.ZoneId;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.appointments")
public class AppointmentProperties {

    private Duration minimumNotice = Duration.ofHours(1);
    private String timeZone = "America/Argentina/Buenos_Aires";

    public ZoneId zoneId() {
        return ZoneId.of(timeZone);
    }
}
