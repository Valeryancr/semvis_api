package gt.skynet.semvis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Configuration
@EnableJpaAuditing(
        auditorAwareRef = "auditorProvider",
        dateTimeProviderRef = "auditingDateTimeProvider",
        modifyOnCreate = true
)
public class AuditConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            try {
                SecurityContext ctx = SecurityContextHolder.getContext();
                Authentication auth = ctx != null ? ctx.getAuthentication() : null;
                if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null) {
                    if (auth.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails ud) {
                        return Optional.ofNullable(ud.getUsername());
                    }
                    return Optional.ofNullable(auth.getName());
                }
            } catch (Exception ignored) {
            }
            return Optional.of("system");
        };
    }

    @Bean
    DateTimeProvider auditingDateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now(ZoneOffset.UTC));
    }
}