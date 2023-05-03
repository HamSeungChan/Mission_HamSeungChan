package com.ll.gramgram.base.initData;

import com.ll.gramgram.boundedContext.instaMember.service.InstaMemberService;
import com.ll.gramgram.boundedContext.likeablePerson.service.LikeablePersonService;
import com.ll.gramgram.boundedContext.member.entity.Member;
import com.ll.gramgram.boundedContext.member.service.MemberService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"dev", "test"})
public class NotProd {
    @Bean
    CommandLineRunner initData(
            MemberService memberService,
            InstaMemberService instaMemberService,
            LikeablePersonService likeablePersonService
    ) {
        return args -> {
            Member memberAdmin = memberService.join("admin", "1234").getData();
            Member memberUser1 = memberService.join("user1", "1234").getData();
            Member memberUser2 = memberService.join("user2", "1234").getData();
            Member memberUser3 = memberService.join("user3", "1234").getData();
            Member memberUser4 = memberService.join("user4", "1234").getData();
            Member memberUser5 = memberService.join("user5", "1234").getData();

            //ddl-auto: create 이기 때문에 조금만 수정하여도 카카오 로그인한 정보가 사라짐을 방지
            Member memberUser5ByKakao = memberService.whenSocialLogin("KAKAO", "KAKAO__2735880781").getData();
            Member memberUser6ByGoogle = memberService.whenSocialLogin("GOOGLE", "GOOGLE__117979796166034494966").getData();

            instaMemberService.connect(memberUser2, "insta_user2", "M");
            instaMemberService.connect(memberUser3, "insta_user3", "W");
            instaMemberService.connect(memberUser4, "insta_user4", "M");
            instaMemberService.connect(memberUser5, "insta_user5", "M");

            likeablePersonService.like(memberUser3, "insta_user4", 1);
            likeablePersonService.like(memberUser3, "insta_user100", 2);

            likeablePersonService.like(memberUser5, "insta_user101", 2);
            likeablePersonService.like(memberUser5, "insta_user102", 2);
            likeablePersonService.like(memberUser5, "insta_user103", 2);
            likeablePersonService.like(memberUser5, "insta_user104", 2);
            likeablePersonService.like(memberUser5, "insta_user105", 2);
            likeablePersonService.like(memberUser5, "insta_user106", 2);
            likeablePersonService.like(memberUser5, "insta_user107", 2);
            likeablePersonService.like(memberUser5, "insta_user108", 2);
            likeablePersonService.like(memberUser5, "insta_user109", 2);
            likeablePersonService.like(memberUser5, "insta_user110", 2);
        };
    }
}
