package com.wanted.projectmodule2lms.domain.profile.dao;

import com.wanted.projectmodule2lms.domain.profile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Integer> {
}
