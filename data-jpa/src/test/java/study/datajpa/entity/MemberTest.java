package study.datajpa.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.repository.MemberRepository;
import study.datajpa.repository.MemberRepositoryTest;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(value = false)
@WebAppConfiguration
class MemberTest {
    @PersistenceContext
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Test
    public void testEntity(){
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        // 초기화
        em.flush(); // 영속성 컨텍스트 -> DB
        em.clear(); // 영속성 컨텍스트 클리어

        // 확인
        List<Member> members = em.createQuery("select m from Member m", Member.class)
                .getResultList();
        // 출력
        for (Member member : members) {
            System.out.println("member = " + member);
            System.out.println("-> member.team = " + member.getTeam());
        }
    }
    @Test
    public void JpaEvenBaseEntity() throws InterruptedException {
        //given
        Member member = new Member("member1");
        memberRepository.save(member);  // @PrePersist
        Thread.sleep(100);
        member.setUsername("member2");
        em.flush();
        em.clear();
        //when
        Member findMember = memberRepository.findById(member.getId()).get();
        //then
        System.out.println("findMember.getCreateDate() = " + findMember.getCreateDate());
        System.out.println("findMember.getUpdateDate() = " + findMember.getLastModifiedDate());
        System.out.println("findMember.getCreateBy() = " + findMember.getCreateBy());
        System.out.println("findMember.getLastModifiedBy() = " + findMember.getLastModifiedBy());
    }

}












