package com.bachhoa.workmanagement.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@RequiredArgsConstructor
public class NotificationSchemaInitializer {

    private final JdbcTemplate jdbcTemplate;

    @Bean
    CommandLineRunner updateNotificationTypeConstraint() {
        return args -> {
            jdbcTemplate.execute(
                    "ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_type_check");
            jdbcTemplate.execute(
                    "ALTER TABLE notifications ADD CONSTRAINT notifications_type_check "
                            + "CHECK (type IN ("
                            + "'TASK_ASSIGNED',"
                            + "'TASK_STATUS_CHANGED',"
                            + "'TASK_DUE_SOON',"
                            + "'TASK_OVERDUE',"
                            + "'ROOM_INVITATION',"
                            + "'ROOM_MEMBER_ADDED'"
                            + "))");
        };
    }
}
