package sumdu.edu.ua.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class SecurityAuditEventListener {

    private static final Logger log = LoggerFactory.getLogger(SecurityAuditEventListener.class);

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        log.info("SECURITY AUDIT: User '{}' successfully logged in", event.getAuthentication().getName());
    }

    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        log.warn("SECURITY AUDIT: Failed login attempt for user '{}': {}",
                event.getAuthentication().getName(), event.getException().getMessage());
    }

    @EventListener
    public void onLogoutSuccess(LogoutSuccessEvent event) {
        log.info("SECURITY AUDIT: User '{}' logged out successfully",
                event.getAuthentication() != null ? event.getAuthentication().getName() : "anonymous");
    }
}
