package com.example.skillswap;

import com.example.skillswap.entity.Member;
import com.example.skillswap.entity.SkillOffer;
import com.example.skillswap.repository.MemberRepository;
import com.example.skillswap.repository.SkillOfferRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Inserts demo data ONLY if the database is empty.
 * Safe to restart — will not duplicate data.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final SkillOfferRepository skillOfferRepository;

    public DataInitializer(MemberRepository memberRepository, SkillOfferRepository skillOfferRepository) {
        this.memberRepository = memberRepository;
        this.skillOfferRepository = skillOfferRepository;
    }

    @Override
    public void run(String... args) {
        if (memberRepository.count() > 0) {
            System.out.println("[DataInitializer] Data already exists — skipping seed.");
            return;
        }

        // Member 1: Arun
        Member arun = new Member();
        arun.setName("Arun");
        arun.setEmail("arun@example.com");
        arun.setPassword("password123");
        arun.setCreditBalance(new BigDecimal("10.00")); // Give starter credits
        memberRepository.save(arun);

        SkillOffer javaOffer = new SkillOffer();
        javaOffer.setMember(arun);
        javaOffer.setSkillName("Java Programming");
        javaOffer.setDescription("I can teach Java basics, OOP, Spring Boot, and REST APIs.");
        javaOffer.setAvailableHours(new BigDecimal("20.00"));
        javaOffer.setActive(true);
        skillOfferRepository.save(javaOffer);

        // Member 2: Rahul
        Member rahul = new Member();
        rahul.setName("Rahul");
        rahul.setEmail("rahul@example.com");
        rahul.setPassword("password123");
        rahul.setCreditBalance(new BigDecimal("10.00"));
        memberRepository.save(rahul);

        SkillOffer cookingOffer = new SkillOffer();
        cookingOffer.setMember(rahul);
        cookingOffer.setSkillName("Cooking");
        cookingOffer.setDescription("Learn to cook Indian and international cuisines.");
        cookingOffer.setAvailableHours(new BigDecimal("15.00"));
        cookingOffer.setActive(true);
        skillOfferRepository.save(cookingOffer);

        System.out.println("[DataInitializer] Demo data seeded successfully!");
        System.out.println("  Members: Arun (arun@example.com), Rahul (rahul@example.com)");
        System.out.println("  Skills: Java Programming, Cooking");
    }
}
