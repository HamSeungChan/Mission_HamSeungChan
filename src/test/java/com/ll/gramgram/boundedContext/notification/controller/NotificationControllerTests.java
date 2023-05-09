package com.ll.gramgram.boundedContext.notification.controller;

import com.ll.gramgram.boundedContext.notification.entity.Notification;
import com.ll.gramgram.boundedContext.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import static org.hamcrest.Matchers.not;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.MethodName.class)
public class NotificationControllerTests {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private NotificationService notificationService;

    @Test
    @DisplayName("알림 목록에 접속했을 때 아직 읽음 처리되지 않은 것들을 읽음 처리 한다.")
    @WithUserDetails("user4")
    void t001() throws Exception {
        List<Notification> notifications = notificationService.findByToInstaMember_username("insta_user4");

        long unreadCount = notifications
                .stream()
                .filter(notification -> !notification.isRead())
                .count();

        assertThat(unreadCount).isEqualTo(1);

        ResultActions resultActions = mvc
                .perform(get("/usr/notification/list"))
                .andDo(print());

        unreadCount = notifications
                .stream()
                .filter(notification -> !notification.isRead())
                .count();
        assertThat(unreadCount).isEqualTo(0);
    }

    @Test
    @DisplayName("아직 읽지 않은 알림이 있을 대 상단바에 인디케이터 표시")
    @WithUserDetails("user4")
    void t002() throws Exception {
        ResultActions resultActions = mvc
                .perform(get("/usr/home/about"))
                .andDo(print());

        resultActions
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(containsString("""
                        data-test="hasUnreadNotifications"
                        """.stripIndent().trim())));
    }
    @Test
    @DisplayName("아직 읽지 않은 알림이 있을 때, 알림리스트에 접속하면 상단바에 인디케이터 표시가 더 이상 안됨")
    @WithUserDetails("user4")
    void t003() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(get("/usr/notification/list"))
                .andDo(print());

        // THEN
        resultActions
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(not(containsString("""
                        data-test="hasUnreadNotifications"
                        """.stripIndent().trim()))));
    }
}


