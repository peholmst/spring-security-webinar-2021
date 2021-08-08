package org.vaadin.webinar.security.sampleapp.security.config;

import com.vaadin.flow.spring.security.VaadinSavedRequestAwareAuthenticationSuccessHandler;
import com.vaadin.flow.spring.security.VaadinWebSecurityConfigurerAdapter;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;

/**
 * This class sets up Spring Security to protect our application.
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
class SecurityConfig extends VaadinWebSecurityConfigurerAdapter {

    final GrantedAuthoritiesMapper authoritiesMapper;

    SecurityConfig(GrantedAuthoritiesMapper authoritiesMapper) {
        this.authoritiesMapper = authoritiesMapper;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http
                // Enable OAuth2 login
                .oauth2Login()
                // Use a custom authorities mapper to get the roles from the identity provider into the Authentication token
                .userInfoEndpoint().userAuthoritiesMapper(authoritiesMapper)
                .and()
                // Use a Vaadin aware authentication success handler
                .successHandler(new VaadinSavedRequestAwareAuthenticationSuccessHandler())
                .and()
                // Require HTTPS
                .requiresChannel().anyRequest().requiresSecure();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        super.configure(web);
        web.ignoring().antMatchers("/h2-console/**");
    }

    @Bean
    public PolicyFactory htmlSanitizer() {
        // This is the policy we will be using to sanitize HTML input
        return Sanitizers.FORMATTING.and(Sanitizers.BLOCKS).and(Sanitizers.STYLES).and(Sanitizers.LINKS);
    }
}
