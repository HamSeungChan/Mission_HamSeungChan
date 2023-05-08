package com.ll.gramgram.boundedContext.notification.eventListener;

import com.ll.gramgram.base.event.EventAfterModifyAttractiveType;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;
    @EventListener
    public void listen(EventAfterModifyAttractiveType event) {

        LikeablePerson likeablePerson = event.getLikeablePerson();
        notificationService.makeLike(likeablePerson);
    }

    @EventListener
    public void listen(EventListener event) {
        log.debug("EventAfterLike event : {}", event);
    }
}

