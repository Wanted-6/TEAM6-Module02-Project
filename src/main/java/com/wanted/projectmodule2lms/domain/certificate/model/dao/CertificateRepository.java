package com.wanted.projectmodule2lms.domain.certificate.model.dao;

import com.wanted.projectmodule2lms.domain.certificate.model.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Integer> {
    Optional<Certificate> findByEnrollmentId(Integer enrollmentId);

    List<Certificate> findAllByOrderByRequestedAtDesc();
}
