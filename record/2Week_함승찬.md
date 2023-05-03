## Title: [2Week] 함승찬

### 미션 요구사항 분석 & 체크리스트

### 호감 표시 예외처리 케이스 3가지 처리

1. 한명의 인스타회원이 다른 인스타회원에게 중복으로 호감표시 불가능
    - [x]  호감을 표시한 다른 인스타회원이 기존 호감 리스트에 존재하는지 확인
    - [x]  이미 존재한다면 RsData에 F메세지를 담아 return
2. 한명의 인스타회원이 11명 이상의 호감상대를 등록 불가능
    - [x]  로그인한 사용자의 호감리스트를 찾아 크기를 확인
    - [x]  크기가 11 이상이면 RsData에 F메세지를 담아 return
3. 케이스 4 가 발생했을 때 기존의 사유와 다른 사유로 호감을 표시하는 경우에는 성공으로 처리
    - [x]  위에 케이스에서 기존 호감 리스트에 존재한다면 `LikeablePerson` 의 속성 `attractiveTypeCode` 의 값을 구한다. 새로운 호감 등록에 선택
      된 `attractiveTypeCode` 의 값을 비교한다.
    - [x]  두 값이 다르다면 기존의 `attractiveTypeCode` 를 새로운 호감 등록에 선택된`attractiveTypeCode`로 변경해 준다.

---

### N주차 미션 요약

---

**[접근 방법]**

1. 한명의 인스타회원이 다른 인스타회원에게 중복으로 호감표시 불가능
    - 호감상대를 등록할 때 접속한 member의 InstaMember 속성 중  `fromLikeablePeople` 리스트에 호감을 표현한 상대 InstaMember를 저장합니다.
    - 현재 member의 `fromLikeablePeople` 리스트를 찾은 다음 등록할 상대의 InstaMember를 포함하고 있는지 확인하였습니다.

2. 한명의 인스타회원이 11명 이상의 호감상대를 등록 불가능
    - 위에서 구한 `fromLikeablePeople` 리스트를 이용한다.
    - 크기가 10인 상태에서 (`if (likeablePeople.size() == 10)` )새로운 호감등록이 들어오는 것을 제한한다.

3. 케이스 4 가 발생했을 때 기존의 사유와 다른 사유로 호감을 표시하는 경우에는 성공으로 처리
    - 기존의 `attractiveTypeCode` 값과 새로 등록된  `attractiveTypeCode` 값을 비교한다.
    - 두 값이 다르면 기존에 저장되었던 `likeablePeson`  객체 제거 후 바뀐 `attractiveTypeCode` 값으로 새로운`likeablePeson` 객체를 생성

**[특이사항]**

1.

세 번째 케이스를 처리하기 위해서 기존에 등록된 데이터를 삭제하고, attractiveTypeCode를 바꾼 데이터를 재생성하였다. setter를 사용하지 않고 데이터를 변경하고 싶었다. 하지만 삭제 후 다시
생성하는 과정이 효율적인 것 같진 않다.

기존의 데이터를 제거하기 위해서 지난 주 구현되었던 delete 메소드를 사용하였지만, 데이터가 삭제되지 않았다. LikeablePerson을 delete해도 InstaMmeber 에 likeablePerson을
저장하는 List에 데이터가 저장되어 있어 삭제되지 않았다고 생각하였다.

`deleteLikeablePerson` 메소드에서

```java
fromInstaMember.getFromLikeablePeople().remove(likeablePerson);
toInstaMember.getToLikeablePeople().remove(likeablePerson);
```

두 과정 후에 delete를 실행해야 완전히 제거가 되었다. 영속성 컨텍스트와 관련된 부분 같지만 확실하지 않아서 조금 더 공부하고 질문의 필요성을 느꼈다.

2.

@Value를 통해 상수값을 받으니 변수를 선언할 때 static final 을 사용하였다.

```java
fromInstaMember.getFromLikeablePeople().remove(likeablePerson);
toInstaMember.getToLikeablePeople().remove(likeablePerson);
```

그 결과 이런 오류가 발생하였다.

Description:

Parameter 2 of constructor in ~ required a bean of type 'java.lang.String' that could not be found.

Action:

Consider defining a bean of type 'java.lang.String' in your configuration.

오류의 원인은 Stirng타입의 likeableMax가 `@RequiredArgsConstructor` 어노테이션으로 인해

생성자를 만들어서 의존성을 주입하는 타입에 String 타입의 빈을 요구해서다. final을 제거하

니 실행이되었다.

어느 타이밍에 @Value로 값이 주입되는지 궁금하다.

**리펙토링**

1. 코드의 가독성이 떨어진다

```java
for (LikeablePerson likeablePerson : likeablePeople) {
            if (isSameToInstaMember(likeablePerson, toInstaMember)) {
                if (isSameAttractiveTypeCode(likeablePerson, attractiveTypeCode)) {
                    return RsData.of("F-3", "이미 등록된 호감상대입니다. 중복해서 호감상대로 등록할 수 없습니다");
                }
                changeAttractiveTypeCode(fromInstaMember, toInstaMember, likeablePerson,attractiveTypeCode);
                return RsData.of("S-2", "입력하신 인스타유저(%s)의 호감 사유가 변경되었습니다.".formatted(username), likeablePerson);
            }
        }
```

이 부분에서 등록되어 있는 likeablePerson을 하나씩 비교하기 위해 for문을 사용하였고

그 안에서 중복된 사용자인지 검증하고 바로 매력타입코드도 비교를 한다. gramgram 과정

을 모르는 사람이 보아도 이해되는 코드를 목표로 리펙토링을 해야겠다…

2. 삭제 기능

기존에 구현되어있는 delete만 사용하여도 데이터가 삭제 되게 할 수 있을 것 같다. 더 찾아볼

필요성을 느꼈다