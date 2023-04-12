package com.ll.gramgram.boundedContext.likeablePerson.service;

import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.instaMember.service.InstaMemberService;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.likeablePerson.repository.LikeablePersonRepository;
import com.ll.gramgram.boundedContext.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeablePersonService {
    private final LikeablePersonRepository likeablePersonRepository;
    private final InstaMemberService instaMemberService;
    @Value("${constant.max.likeable}")
    private String likeableMax;

    @Transactional
    public RsData<LikeablePerson> like(Member member, String username, int attractiveTypeCode) {

        InstaMember fromInstaMember = member.getInstaMember();
        InstaMember toInstaMember = instaMemberService.findByUsernameOrCreate(username).getData();

        List<LikeablePerson> likeablePeople = fromInstaMember.getFromLikeablePeople();

        if (likeablePeople.size() == Integer.parseInt(likeableMax)) {
            return RsData.of("F-4", "호감등록은 10명까지 가능합니다.");
        }

        for (LikeablePerson likeablePerson : likeablePeople) {
            if (isSameToInstaMember(likeablePerson, toInstaMember)) {
                if (isSameAttractiveTypeCode(likeablePerson, attractiveTypeCode)) {
                    return RsData.of("F-3", "이미 등록된 호감상대입니다. 중복해서 호감상대로 등록할 수 없습니다");
                }
                changeAttractiveTypeCode(fromInstaMember, toInstaMember, likeablePerson,attractiveTypeCode);
                return RsData.of("S-2", "입력하신 인스타유저(%s)의 호감 사유가 변경되었습니다.".formatted(username), likeablePerson);
            }
        }

        if (member.hasConnectedInstaMember() == false) {
            return RsData.of("F-2", "먼저 본인의 인스타그램 아이디를 입력해야 합니다.");
        }

        if (member.getInstaMember().getUsername().equals(username)) {
            return RsData.of("F-1", "본인을 호감상대로 등록할 수 없습니다.");
        }

        LikeablePerson likeablePerson = create(fromInstaMember, toInstaMember, attractiveTypeCode);
        return RsData.of("S-1", "입력하신 인스타유저(%s)를 호감상대로 등록되었습니다.".formatted(username), likeablePerson);
    }

    private LikeablePerson create(InstaMember fromInstaMember, InstaMember toInstaMember, int attractiveTypeCode) {
        LikeablePerson likeablePerson = LikeablePerson
                .builder()
                .fromInstaMember(fromInstaMember) // 호감을 표시하는 사람의 인스타 멤버
                .fromInstaMemberUsername(fromInstaMember.getUsername()) // 중요하지 않음
                .toInstaMember(toInstaMember) // 호감을 받는 사람의 인스타 멤버
                .toInstaMemberUsername(toInstaMember.getUsername()) // 중요하지 않음
                .attractiveTypeCode(attractiveTypeCode) // 1=외모, 2=능력, 3=성격
                .build();

        likeablePersonRepository.save(likeablePerson); // 저장

        // 너가 좋아하는 호감표시 생겼어.
        fromInstaMember.addFromLikeablePerson(likeablePerson);

        // 너를 좋아하는 호감표시 생겼어.
        toInstaMember.addToLikeablePerson(likeablePerson);

        return likeablePerson;
    }

    private boolean isSameAttractiveTypeCode(LikeablePerson likeablePerson, int attractiveTypeCode) {
        return likeablePerson.getAttractiveTypeCode() == attractiveTypeCode;
    }

    private boolean isSameToInstaMember(LikeablePerson likeablePerson, InstaMember toInstaMember) {
        return likeablePerson.getToInstaMember().equals(toInstaMember);
    }

    private void changeAttractiveTypeCode(InstaMember fromInstaMember, InstaMember toInstaMember, LikeablePerson likeablePerson, int attractiveTypeCode) {
        deleteLikeablePerson(fromInstaMember,toInstaMember,likeablePerson);
        create(fromInstaMember,toInstaMember,attractiveTypeCode);
    }

    private void deleteLikeablePerson(InstaMember fromInstaMember, InstaMember toInstaMember, LikeablePerson likeablePerson) {
        fromInstaMember.getFromLikeablePeople().remove(likeablePerson);
        toInstaMember.getToLikeablePeople().remove(likeablePerson);
        delete(likeablePerson);
    }

    public List<LikeablePerson> findByFromInstaMemberId(Long fromInstaMemberId) {
        return likeablePersonRepository.findByFromInstaMemberId(fromInstaMemberId);
    }

    public Optional<LikeablePerson> findById(Long id) {
        return likeablePersonRepository.findById(id);
    }

    @Transactional
    public RsData delete(LikeablePerson likeablePerson) {
        likeablePersonRepository.delete(likeablePerson);

        String likeCanceledUsername = likeablePerson.getToInstaMember().getUsername();
        return RsData.of("S-1", "%s님에 대한 호감을 취소하였습니다.".formatted(likeCanceledUsername));
    }

    public RsData canActorDelete(Member actor, LikeablePerson likeablePerson) {
        if (likeablePerson == null) return RsData.of("F-1", "이미 삭제되었습니다.");

        // 수행자의 인스타계정 번호
        long actorInstaMemberId = actor.getInstaMember().getId();
        // 삭제 대상의 작성자(호감표시한 사람)의 인스타계정 번호
        long fromInstaMemberId = likeablePerson.getFromInstaMember().getId();

        if (actorInstaMemberId != fromInstaMemberId)
            return RsData.of("F-2", "권한이 없습니다.");

        return RsData.of("S-1", "삭제가능합니다.");
    }
}