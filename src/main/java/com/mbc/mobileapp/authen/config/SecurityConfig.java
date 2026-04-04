package com.mbc.mobileapp.authen.config;

import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import com.mbc.common.entity.ComPartnerUser;
import com.mbc.common.repository.ComPartnerUserRepo;

/**
 * 
 * @author dongvt
 *
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthenticationEntryPoint authEntryPoint;

    @Autowired
    private ComPartnerUserRepo partnerUserRepo;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        // (authenticated).
        http.authorizeRequests().antMatchers("/swagger-ui.html/**", "/swagger-resources/**", "/webjars/**",
            "/swagger-ui.html#!/**", "/v2/**", "/actuator/**", "/css/**", "/js/**", "/").permitAll().anyRequest()
            .authenticated();
        // Sử dụng AuthenticationEntryPoint để xác thực user/password
        http.httpBasic().authenticationEntryPoint(authEntryPoint);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        return bCryptPasswordEncoder;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(inMemoryUserDetailsManager());
    }

    @Bean
    public InMemoryUserDetailsManager inMemoryUserDetailsManager() {
        final Properties users = new Properties();

        List<ComPartnerUser> listUser = partnerUserRepo.findByStatusAndType(new BigDecimal(1), "SDK.RETAIL");

        for (ComPartnerUser partnerUser : listUser) {
            users.put(partnerUser.getUserId(),
                this.passwordEncoder().encode(partnerUser.getPassword()) + "," + partnerUser.getRole() + ",enabled");
        }

        return new InMemoryUserDetailsManager(users);
    }
}
