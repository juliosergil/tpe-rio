package org.tperio.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.tperio.service.JwtUtil;
import org.tperio.service.impl.UsuarioServiceImpl;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;

import java.util.ArrayList;
import java.util.List;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UsuarioServiceImpl usuarioService;

    @Autowired
    private JwtUtil jwtUtil;

    private static final String[] PUBLIC_MATCHERS = {
            "/h2-console/**",
            "/api/usuarios/**"
    };

    @Bean
    public OncePerRequestFilter jwtFilter(){
        return new JwtAuthFilter(jwtUtil, usuarioService);
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
        return source;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(usuarioService)
                .passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure( HttpSecurity http ) throws Exception {
        http
                .cors().and().csrf().disable()
                .authorizeRequests()
                .antMatchers(PUBLIC_MATCHERS)
                    .permitAll()
                .antMatchers("/api/hello")
                    .hasAnyRole("USER", "ADMIN")
                .antMatchers("/api/hello/adm")
                     .hasRole("ADMIN")
                .anyRequest().authenticated()
                    .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                .addFilterBefore( jwtFilter(), UsernamePasswordAuthenticationFilter.class);
        ;
        http.headers().frameOptions().disable();
    }

    private List<SecurityReference> defaultAuth(){
        AuthorizationScope authorizationScope = new AuthorizationScope(
                "global", "accessEverything");
        AuthorizationScope[] scopes = new AuthorizationScope[1];
        scopes[0] = authorizationScope;
        SecurityReference reference = new SecurityReference("JWT", scopes);
        List<SecurityReference> auths = new ArrayList<>();
        auths.add(reference);
        return auths;
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(
                "/v2/api-docs",
                "/configuration/ui",
                "/swagger-resources/**",
                "/configuration/security",
                "/swagger-ui.html",
                "/webjars/**");
    }
}
