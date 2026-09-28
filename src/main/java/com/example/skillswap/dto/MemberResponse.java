package com.example.skillswap.dto;

import com.example.skillswap.entity.Member;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class MemberResponse {

    private Long id;
    private String name;
    private String email;
    private BigDecimal creditBalance;
    private LocalDateTime createdAt;

    public static MemberResponse from(Member member) {

        MemberResponse response = new MemberResponse();

        response.setId(member.getId());
        response.setName(member.getName());
        response.setEmail(member.getEmail());

        response.setCreditBalance(
                member.getCreditBalance()
        );

        response.setCreatedAt(
                member.getCreatedAt()
        );

        return response;
    }
}