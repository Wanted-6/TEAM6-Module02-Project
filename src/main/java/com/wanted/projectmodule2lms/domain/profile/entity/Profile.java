package com.wanted.projectmodule2lms.domain.profile.entity;

import com.wanted.projectmodule2lms.domain.member.model.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "profile")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
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

    public Profile(Member member, String profileImage, String bio) {
        this.member = member;
        this.profileImage = profileImage;
        this.bio = bio;
    }

    public void updateBio(String bio) { this.bio = bio; }
    public void updateProfileImage(String profileImage) { this.profileImage = profileImage; }

    public void update(String bio, String profileImage) {
        this.bio = bio;
        this.profileImage = profileImage;
    }

}
