package com.wanted.projectmodule2lms.domain.profile.dto;

import com.wanted.projectmodule2lms.domain.member.model.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile {

    @Id
    @Column(name = "member_id")
    private Integer memberId;
    @OneToOne
    @MapsId
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(columnDefinition = "TEXT")
    private String bio;

    // 프로필 초기 설정
    public Profile(Member member, String profileImage, String bio) {
        this.member = member;
        this.profileImage = profileImage;
        this.bio = bio;
    }

    // 수정
    public void updateBio(String bio) {
        this.bio = bio;
    }

    public void updateProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}