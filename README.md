### 스프링 데이터 JPA

- @JpaRepository (interface)

### H2 Database 처음 기본설정

1. h2.jar 실행
2. JDBC URL : jdbc:h2:~/**datajpa**
    
    → datajpa.mv.db 파일이 생성됨
    
3. 로그아웃
4. JDBC URL : **jdbc:h2:tcp://localhost/~/datajpa**

### 쿼리의 파라미터 로그 남기기

- 라이브러리 추가

```java
implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.5.7'
```

### 주의

- T findOne(ID)  →  Optional<T> findById(ID) 로 변경
- 제네릭 타입
    - T : 엔티티
    - ID : 엔티티의 식별자 타입
    - S : 엔티티와 그 자식 타입
- 주요 메서드
    - save(S)
    - delete(T)
    - findById(ID)
    - getOne(ID) : 엔티티를 프록시로 조회
    - findAll(...) : Sort나 Pageable 가능

### 쿼리메서드

- 메서드 이름으로 쿼리 생성
    - findByUsername
    - findBy**Username**And**Age**GreaterThan
        - Username(String) / Age(int)   → 파라미터 2개
        - Greater Than  : 크면
- 방법
    - 조회: find...By,  read...By, query...By...
        - findHelloBy
    - Count(long) : count...By
    - EXISTS(boolean) : exists...By
    - 삭제 : delete...By, remove...By
    - DISTINCT : findDistinct
        - findMemberDistinctBy
    - LIMIT : findFirst3, findFirst, findTop, findTop3

### 메서드에 쿼리 사용 ( @Query )

1. @Param 활용
    - 로딩 시점에 오류 발견!

```java
@Query("select m from Member m where m.username = :username and m.age = :age")
List<Member> findUser(@Param("username") String username, @Param("age") int age);
```

1. DTO
    - **new study.datajpa.dto.MemberDto(m.id, m.username, t.name)**

```elm
@Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
List<MemberDto> findMemberDto();
```

1. **in**

```java
@Query("select m from Member m where m.username in :name")
List<Member> findByNames(@ Param("names") List<String> names);
```

### 반환 타입

- 스프링 데이터 JPA는 유연한 반환 타입 지원
    1. 컬렉션
        - List<Member>  : **없으면 empty**
    2. 단건
        - Member  :  없으면 null
        - Optional<Member>  :  **없으면 empty**

### 페이징 / 정렬

1. 순수 JPA 페이징 

```elm
public List<Member> findByPage(int age, int offset, int limit){
    return em.createQuery("select m from Member m where m.age = :age order by m.username desc")
            .setParameter("age", age)
            .setFirstResult(offset)
            .setMaxResults(limit)
            .getResultList();
}
public long totalCount(int age){
    return em.createQuery("select count(m) from Member m where m.age = :age", Long.class)
            .setParameter("age", age)
            .getSingleResult();
}
```

1. 스프링 데이터 JPA 페이징, 정렬
    - Page : count 쿼리 O
    
    ```java
    // Repository
    Page<Member> findByAge(int age, Pageable pageable);
    ```
    
    ```java
    // 초기 설정
    // pageRequest 0~3 내림차순
    PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));
    Page<Member> page = memberRepository.findByAge(age, pageRequest);
    // count
    long totalElements = page.getTotalElements();
    // content
    List<Member> content = page.getContent();
    ```
    
    ```java
    assertThat(content.size()).isEqualTo(3);    // 한번에 3개씩
    assertThat(page.getTotalElements()).isEqualTo(5); // 총 5개
    assertThat(page.getNumber()).isEqualTo(0); // 페이지 번호
    assertThat(page.getTotalPages()).isEqualTo(2); // 3개씩 2개 페이지
    assertThat(page.isFirst()).isTrue();  // 현재 페이지 있는지
    assertThat(page.hasNext()).isTrue();  // 다음 페이지 있는지
    ```
    
    - Slice : count 쿼리 X, 다음페이지 확인가능
    - List : count쿼리X, 결과만 반환

### 페이지 유지 → DTO 변환

```java
Page<Member> page = memberRepository.findByAge(age, pageRequest);
Page<MemberDto> dtoPage = page.map(m -> new MemberDto(m.getId(), m.getUsername(), null));
```

### 벌크성 수정 쿼리

: 모든 직원의 연봉을 10%인상 등.. 한번에 처리

```java
@Modifying(clearAutomatically = true)   // 변경. 없으면 오류
@Query("update Member m set m.age=m.age+1 where m.age >= :age")
int bulkAgePlus(@Param("age") int age);
```

- 연산 전 수행 = @Modifying(clearAutomatically = true)
    - em.flush()
    - em.clear()
    

### @EntityGraph

: 연관관계들을 join fetch해서 가져올지 정해줌

```gherkin
@Override
@EntityGraph(attributePaths = {"team"})
List<Member> findAll();
```

```java
@EntityGraph(attributePaths = {"team"})
@Query("select m from Member m")
List<Member> findMemberEntityGraph();
```

### hint

: DB조회만 사용할 때 (최적화)

```gherkin
@QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
Member findReadOnlyByUsername(String username);
```

### Auditing

- 엔티티를 생성, 변경할 때, 변경한 **사람, 시간 추적**
    - 등록일
    - 수정일
    - 등록자
    - 수정자
1. 순수 JPA

```gherkin
@MappedSuperclass
public class JpaBaseEntity {
    @Column(updatable = false)
    private LocalDateTime createDate;
    private LocalDateTime updateDate;

    @PrePersist  // 최초등록
    public void prePersist(){
        LocalDateTime now = LocalDateTime.now();
        createDate = now;
        updateDate = now;
    }
    @PreUpdate
    public void preUpdate(){   // 업데이트
        updateDate = LocalDateTime.now();
    }
}
```

1. 스프링 데이터 JPA

```gherkin
@EnableJpaAuditing  // 메인에 추가
//
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {
    @CreatedDate         // 생성
    @Column(updatable = false)
    private LocalDateTime createDate;

    @LastModifiedDate   // 업데이트
    private LocalDateTime lastModifiedDate;
}
```

###
