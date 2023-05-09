package com.ll.gramgram.boundedContext.notification.service;

import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.likeablePerson.service.LikeablePersonService;
import com.ll.gramgram.boundedContext.member.entity.Member;
import com.ll.gramgram.boundedContext.member.service.MemberService;
import com.ll.gramgram.boundedContext.notification.entity.Notification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.MethodName.class)
public class NotificationServiceTests {

    @Autowired
    private MemberService memberService;
    @Autowired
    private LikeablePersonService likeablePersonService;
    @Autowired
    private NotificationService notificationService;

    @Test
    @DisplayName("호감 표시를 하면 그에 따른 아림이 생성된다.")
    void t001() throws Exception{
        Member memberUser3 = memberService.findByUsername("user3").orElseThrow();
        Member memberUser5 = memberService.findByUsername("user5").orElseThrow();

        LikeablePerson likeablePersonToInstaUser4 = likeablePersonService.like(memberUser3,"insta_user5",1).getData();

        List<Notification> notifications = notificationService.findByToInstaMember(memberUser5.getInstaMember());

        Notification lastNotification = notifications.get(notifications.size()-1);

        assertThat(lastNotification.getFromInstaMember().getUsername()).isEqualTo("insta_user3");
        assertThat(lastNotification.getTypeCode()).isEqualTo("LIKE");
        assertThat(lastNotification.getNewAttractiveTypeCode()).isEqualTo(1);
    }
}
