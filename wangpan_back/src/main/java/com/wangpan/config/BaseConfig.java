package com.wangpan.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author fangyixin
 * @date 2023/11/14 16:01
 */
@Data
@Configuration
public class BaseConfig {
    @Value("${spring.mail.username}")
    private String mailUsername;

    @Value("${admin.emails}")
    private String adminEmails;

    @Value("${project.folder}")
    private String projectFolder;

}
