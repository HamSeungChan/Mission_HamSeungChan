package com.ll.gramgram.boundedContext.likeablePerson.service;

import com.ll.gramgram.base.appConfig.AppConfig;
import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.instaMember.service.InstaMemberService;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.likeablePerson.repository.LikeablePersonRepository;
import com.ll.gramgram.boundedContext.member.entity.Member;
import lombok.RequiredArgsConstructor;
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

    private RsData canLike(Member member, String username, int attractiveTypeCode) {

        if (!member.hasConnectedInstaMember()) {
            return RsData.of("F-2", "먼저 본인의 인스타그램 아이디를 입력해야 합니다.");
        }

        List<LikeablePerson> likeablePeople = member.getInstaMember().getFromLikeablePeople();


        if (member.getInstaMember().getUsername().equals(username)) {
            return RsData.of("F-1", "본인을 호감상대로 등록할 수 없습니다.");
        }

        Optional<LikeablePerson> duplicateLikeablePerson = findDuplicate(likeablePeople, username);
        if (duplicateLikeablePerson.isPresent()) {
            if (duplicateLikeablePerson.get().getAttractiveTypeCode() != attractiveTypeCode) {
                return RsData.of("S-2", "%s님에 대해서 호감변경이 가능합니다.".formatted(username));
            }
            return RsData.of("F-3", ("이미 %s님에 대해서 호감표시를 했습니다").formatted(username));
        }

        long likeablePersonFromMax = AppConfig.getLikeablePersonFromMax();
        if (likeablePeople.size() >= likeablePersonFromMax) {
            return RsData.of("F-4", ("호감등록은 %d 명까지 가능합니다.").formatted(likeablePersonFromMax));
        }
        return RsData.of("S-1", "%s님에 대해서 호감표시가 가능합니다.".formatted(username));
    }

    private Optional<LikeablePerson> findDuplicate(List<LikeablePerson> likeablePeople, String username) {
        return likeablePeople
                .stream()
                .filter(likeablePerson -> likeablePerson.getToInstaMember().getUsername().equals(username))
                .findFirst();
    }

    @Transactional
    public RsData<LikeablePerson> like(Member member, String username, int attractiveTypeCode) {

        RsData canLikeRsData = canLike(member, username, attractiveTypeCode);

        if (canLikeRsData.isFail()) return canLikeRsData;

        if (canLikeRsData.getResultCode().equals("S-2")) {
            return modifyAttractiveTypeCode(member, username, attractiveTypeCode);
        }

        InstaMember fromInstaMember = member.getInstaMember();
        InstaMember toInstaMember = instaMemberService.findByUsernameOrCreate(username).getData();

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

        return RsData.of("S-1", "입력하신 인스타유저(%s)를 호감상대로 등록되었습니다.".formatted(username), likeablePerson);
    }

    private RsData checkAttractiveTypeCode(LikeablePerson likeablePerson, int attractiveTypeCode) {
        if (isSameAttractiveTypeCode(likeablePerson.getAttractiveTypeCode(), attractiveTypeCode)) {
            return RsData.of("F-3", "이미 등록된 호감상대입니다. 중복해서 호감상대로 등록할 수 없습니다");
        }
        return RsData.of("F-3", "이미 등록된 호감상대입니다. 중복해서 호감상대로 등록할 수 없습니다");
    }

    private boolean isSameAttractiveTypeCode(int oldAttractiveTypeCode, int newAttractiveTypeCode) {
        return oldAttractiveTypeCode == newAttractiveTypeCode;
    }

    public Optional<LikeablePerson> findById(Long id) {
        return likeablePersonRepository.findById(id);
    }

    @Transactional
    public RsData delete(LikeablePerson likeablePerson) {
        likeablePerson.getFromInstaMember().removeFromLikeablePerson(likeablePerson);
        likeablePerson.getToInstaMember().removeToLikeablePerson(likeablePerson);
        likeablePersonRepository.delete(likeablePerson);

        String likeCanceledUsername = likeablePerson.getToInstaMember().getUsername();
        return RsData.of("S-1", "%s님에 대한 호감을 취소하였습니다.".formatted(likeCanceledUsername));
    }

    public RsData canDelete(Member actor, LikeablePerson likeablePerson) {
        if (likeablePerson == null) return RsData.of("F-1", "이미 삭제되었습니다.");

        // 수행자의 인스타계정 번호
        long actorInstaMemberId = actor.getInstaMember().getId();
        // 삭제 대상의 작성자(호감표시한 사람)의 인스타계정 번호
        long fromInstaMemberId = likeablePerson.getFromInstaMember().getId();

        if (actorInstaMemberId != fromInstaMemberId)
            return RsData.of("F-2", "권한이 없습니다.");

        return RsData.of("S-1", "삭제가능합니다.");
    }


    private RsData<LikeablePerson> modifyAttractiveTypeCode(Member member, String username, int attractiveTypeCode) {
        Optional<LikeablePerson> duplicateLikeablePerson = findDuplicate(member.getInstaMember().getFromLikeablePeople(), username);
        if (duplicateLikeablePerson.isEmpty()) {
            return RsData.of("F-7", "호감표시를 하지 않았습니다.");
        }
        LikeablePerson fromLikeablePerson = duplicateLikeablePerson.get();
        String oldAttractiveTypeDisplayName = fromLikeablePerson.getAttractiveTypeDisplayName();
        fromLikeablePerson.setAttractiveTypeCode(attractiveTypeCode);

        likeablePersonRepository.save(fromLikeablePerson);

        String newAttractiveTypeDisplayName = fromLikeablePerson.getAttractiveTypeDisplayName();

        return RsData.of("S-3", "%s님에 대한 호감사유를 %s에서 %s(으)로 변경합니다.".formatted(username, oldAttractiveTypeDisplayName, newAttractiveTypeDisplayName));
    }

    public Optional<LikeablePerson> findByFromInstaMember_usernameAndToInstaMember_username(String fromInstaMemberUsername, String toInstaMemberUsername) {
        return likeablePersonRepository.findByFromInstaMember_usernameAndToInstaMember_username(fromInstaMemberUsername, toInstaMemberUsername);
    }
}