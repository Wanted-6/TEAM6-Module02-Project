USE meatdb;

SET FOREIGN_KEY_CHECKS = 0;

-- =====================================
-- 기존 테이블 삭제 (의존성 역순)
-- =====================================
DROP TABLE IF EXISTS AuditLog;
DROP TABLE IF EXISTS Memo;
DROP TABLE IF EXISTS Comment;
DROP TABLE IF EXISTS Post;
DROP TABLE IF EXISTS Submission;
DROP TABLE IF EXISTS Assignment;
DROP TABLE IF EXISTS Grade;
DROP TABLE IF EXISTS Attendance;
DROP TABLE IF EXISTS Enrollment;
DROP TABLE IF EXISTS Section;
DROP TABLE IF EXISTS Course;
DROP TABLE IF EXISTS Profile;
DROP TABLE IF EXISTS Member;
DROP TABLE IF EXISTS Auditlog;
DROP TABLE IF EXISTS certificate;

-- 1. Member
-- =====================================
CREATE TABLE Member (
    member_id        INT PRIMARY KEY,
    login_id         VARCHAR(50) NOT NULL UNIQUE,
    password         VARCHAR(255) NOT NULL,
    name             VARCHAR(50) NOT NULL,
    phone            VARCHAR(20) UNIQUE,
    email            VARCHAR(100) NOT NULL UNIQUE,
    role             ENUM('STUDENT', 'INSTRUCTOR', 'ADMIN') NOT NULL,
    status           ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
    is_account_locked BIT(1) DEFAULT b'0' COMMENT '계정 잠금 여부',
    login_fail_count  INT DEFAULT 0 COMMENT '로그인 실패 횟수',
    is_temp_password TINYINT(1) DEFAULT 0 NOT NULL,
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================================
-- 2. Profile
-- =====================================
CREATE TABLE Profile (
    member_id        INT PRIMARY KEY,
    profile_image    VARCHAR(255),
    bio              TEXT,
    CONSTRAINT fk_profile_member
        FOREIGN KEY (member_id) REFERENCES Member(member_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- =====================================
-- 3. Course
-- =====================================
CREATE TABLE Course (
    course_id            INT PRIMARY KEY,
    instructor_id        INT NOT NULL,
    title                VARCHAR(100) NOT NULL,
    description          TEXT,
    category             VARCHAR(50),
    thumbnail_image      VARCHAR(255),
    capacity             INT NOT NULL,
    start_date           DATE NOT NULL,
    end_date             DATE NOT NULL,
    exam_due_date        DATE NULL,
    is_open              BOOLEAN NOT NULL DEFAULT FALSE,
    approval_status      ENUM('PENDING', 'APPROVED', 'REJECTED', 'DELETED') NOT NULL DEFAULT 'PENDING',
    reject_reason        VARCHAR(255) NULL,
    reviewed_by          INT NULL,
    reviewed_at          DATETIME NULL,
    deleted_at           DATETIME NULL,
    CONSTRAINT fk_course_instructor
        FOREIGN KEY (instructor_id) REFERENCES Member(member_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_course_reviewed_by
        FOREIGN KEY (reviewed_by) REFERENCES Member(member_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    CONSTRAINT chk_course_capacity
        CHECK (capacity > 0),
    CONSTRAINT chk_course_date
        CHECK (start_date <= end_date),
    CONSTRAINT chk_course_exam_due_date
        CHECK (exam_due_date >= end_date)
);

-- =====================================
-- 4. Section
-- =====================================
CREATE TABLE Section (
    section_id           INT PRIMARY KEY,
    course_id            INT NOT NULL,
    title                VARCHAR(100) NOT NULL,
    video_url            VARCHAR(500),
    material_file        VARCHAR(255),
    section_order        INT NOT NULL,
    open_date            DATE,
    CONSTRAINT fk_section_course
        FOREIGN KEY (course_id) REFERENCES Course(course_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT uq_section_course_order
        UNIQUE (course_id, section_order),
    CONSTRAINT chk_section_order_range
        CHECK (section_order BETWEEN 1 AND 8)
);

-- =====================================
-- 5. Enrollment
-- =====================================
DROP TABLE IF EXISTS Enrollment;

CREATE TABLE Enrollment (
    enrollment_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    member_id INT NOT NULL,
    course_id INT NOT NULL,
    status ENUM('ENROLLED', 'COMPLETED', 'CANCELLED', 'DROPPED')
        NOT NULL DEFAULT 'ENROLLED',
    enrolled_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at DATETIME NULL,

    CONSTRAINT fk_enrollment_member
        FOREIGN KEY (member_id) REFERENCES Member(member_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_enrollment_course
        FOREIGN KEY (course_id) REFERENCES Course(course_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT uq_enrollment_member_course
        UNIQUE (member_id, course_id),

    INDEX idx_enrollment_member_status (member_id, status),
    INDEX idx_enrollment_course_status (course_id, status)
);

-- =====================================
-- 6. Attendance (최종 버전)
-- =====================================
CREATE TABLE Attendance (
    attendance_id        INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id        INT NOT NULL,
    section_id           INT NOT NULL,
    status               ENUM('PRESENT', 'LATE', 'ABSENT') NOT NULL,
    checked_at           DATETIME NULL,
    recorded_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_manual            BOOLEAN NOT NULL DEFAULT FALSE,
    note                 VARCHAR(255) NULL,
    CONSTRAINT fk_attendance_enrollment
        FOREIGN KEY (enrollment_id) REFERENCES Enrollment(enrollment_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_attendance_section
        FOREIGN KEY (section_id) REFERENCES Section(section_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT uq_attendance_enrollment_section
        UNIQUE (enrollment_id, section_id)
);

-- =====================================
-- 7. Grade
-- =====================================
CREATE TABLE Grade (
    grade_id             INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id        INT NOT NULL UNIQUE,
    attendance_score     DECIMAL(5,2)  NULL,
    assignment_score     DECIMAL(5,2)  NULL,
    exam_score           DECIMAL(5,2)  NULL,
    attitude_score       DECIMAL(5,2)  NULL,
    total_score          DECIMAL(5,2)  NULL,
    is_passed            BOOLEAN  NULL,
    CONSTRAINT fk_grade_enrollment
        FOREIGN KEY (enrollment_id) REFERENCES Enrollment(enrollment_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT chk_attendance_score CHECK (attendance_score BETWEEN 0 AND 100),
    CONSTRAINT chk_assignment_score CHECK (assignment_score BETWEEN 0 AND 100),
    CONSTRAINT chk_exam_score CHECK (exam_score BETWEEN 0 AND 100),
    CONSTRAINT chk_attitude_score CHECK (attitude_score BETWEEN 0 AND 100),
    CONSTRAINT chk_total_score CHECK (total_score BETWEEN 0 AND 100)
);

-- =====================================
-- 8. Assignment
-- =====================================
CREATE TABLE Assignment (
    assignment_id        INT PRIMARY KEY,
    course_id           INT NOT NULL,
    title                VARCHAR(100) NOT NULL,
    description          TEXT,
    attachment_file      VARCHAR(255),
    due_date             DATETIME NOT NULL,
    CONSTRAINT fk_assignment_course
        FOREIGN KEY (course_id) REFERENCES Course(course_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);
-- =====================================
-- 9. Submission
-- =====================================
CREATE TABLE Submission (
    submission_id        INT PRIMARY KEY,
    assignment_id        INT NOT NULL,
    enrollment_id        INT NOT NULL,
    content              TEXT,
    attachment_file      VARCHAR(255),
    submitted_at         DATETIME NOT NULL,
    score                DECIMAL(5,2) NULL,
    feedback             TEXT,
    CONSTRAINT fk_submission_assignment
        FOREIGN KEY (assignment_id) REFERENCES Assignment(assignment_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_submission_enrollment
        FOREIGN KEY (enrollment_id) REFERENCES Enrollment(enrollment_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT uq_submission_assignment_enrollment
        UNIQUE (assignment_id, enrollment_id)
);

-- =====================================
-- 10. Post (최종 버전)
-- =====================================
CREATE TABLE Post (
    post_id              INT AUTO_INCREMENT PRIMARY KEY,
    member_id            INT NOT NULL,
    course_id            INT NULL,
    section_id           INT NULL,
    title                VARCHAR(200) NOT NULL,
    content              TEXT NOT NULL,
    post_type            ENUM(
                            'ADMIN_NOTICE',
                            'COURSE_NOTICE',
                            'FREE',
                            'SECTION_QNA'
                         ) NOT NULL,
    is_secret            BOOLEAN NOT NULL DEFAULT FALSE,
    answer_status        ENUM('PENDING', 'ANSWERED') NULL,
    view_count           INT NOT NULL DEFAULT 0,
    is_deleted           BOOLEAN NOT NULL DEFAULT FALSE,
    created_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                         ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_post_member
        FOREIGN KEY (member_id) REFERENCES Member(member_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_post_course
        FOREIGN KEY (course_id) REFERENCES Course(course_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_post_section
        FOREIGN KEY (section_id) REFERENCES Section(section_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- =====================================
-- 11. Comment (최종 버전)
-- =====================================
CREATE TABLE Comment (
    comment_id           INT AUTO_INCREMENT PRIMARY KEY,
    post_id              INT NOT NULL,
    member_id            INT NOT NULL,
    parent_comment_id    INT NULL,
    content              TEXT NOT NULL,
    is_deleted           BOOLEAN NOT NULL DEFAULT FALSE,
    created_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                         ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_comment_post
        FOREIGN KEY (post_id) REFERENCES Post(post_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_comment_member
        FOREIGN KEY (member_id) REFERENCES Member(member_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_comment_parent
        FOREIGN KEY (parent_comment_id) REFERENCES Comment(comment_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- =====================================
-- 12. Memo
-- =====================================
CREATE TABLE Memo (
    memo_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    member_id INT NOT NULL,
    content VARCHAR(255) NOT NULL,
    memo_date DATE NOT NULL,
    CONSTRAINT fk_memo_member
        FOREIGN KEY (member_id) REFERENCES Member(member_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- =====================================
-- 13. Auditlog
-- =====================================
CREATE TABLE Auditlog (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT COMMENT '로그인한 회원 고유번호 (비회원이면 NULL)',
    method_name VARCHAR(255) COMMENT '실행된 컨트롤러/서비스 메서드 이름',
    execution_time BIGINT COMMENT '실행 소요 시간(ms)',
    status VARCHAR(50) COMMENT '결과 (SUCCESS / FAIL)',
    error_message TEXT COMMENT '실패 시 에러 메시지 내용',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '로그 기록 시간'
);

-- Index
-- =====================================
CREATE INDEX idx_attendance_enrollment ON Attendance(enrollment_id);
CREATE INDEX idx_attendance_section ON Attendance(section_id);

CREATE INDEX idx_post_type ON Post(post_type);
CREATE INDEX idx_post_course ON Post(course_id);
CREATE INDEX idx_post_section ON Post(section_id);
CREATE INDEX idx_post_member ON Post(member_id);
CREATE INDEX idx_post_created_at ON Post(created_at);

CREATE INDEX idx_comment_post ON Comment(post_id);
CREATE INDEX idx_comment_member ON Comment(member_id);
CREATE INDEX idx_comment_parent ON Comment(parent_comment_id);
CREATE INDEX idx_comment_created_at ON Comment(created_at);

-- =====================================
-- Certificate
-- =====================================
CREATE TABLE Certificate (
    certificate_id INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id INT NOT NULL UNIQUE,
    status ENUM('REQUESTED', 'APPROVED', 'ISSUED', 'REVOKED') NOT NULL,
    total_score DECIMAL(5,2) NOT NULL,
    is_override BOOLEAN NOT NULL DEFAULT FALSE,
    requested_at DATETIME NULL,
    approved_at DATETIME NULL,
    issued_at DATETIME NULL,
    revoked_at DATETIME NULL,
    approved_by INT NULL,
    revoked_by INT NULL,
    certificate_file_path VARCHAR(255) NULL,
    CONSTRAINT fk_certificate_enrollment
        FOREIGN KEY (enrollment_id) REFERENCES Enrollment(enrollment_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_certificate_approved_by
        FOREIGN KEY (approved_by) REFERENCES Member(member_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    CONSTRAINT fk_certificate_revoked_by
        FOREIGN KEY (revoked_by) REFERENCES Member(member_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);


-- 2. member_id에 AUTO_INCREMENT 추가하기
ALTER TABLE member MODIFY COLUMN member_id INT AUTO_INCREMENT;

ALTER TABLE Course MODIFY course_id INT AUTO_INCREMENT;
ALTER TABLE Section MODIFY section_id INT AUTO_INCREMENT;
ALTER TABLE Assignment MODIFY assignment_id INT AUTO_INCREMENT;
ALTER TABLE Submission MODIFY submission_id INT AUTO_INCREMENT;


- 1. Member
INSERT INTO Member (member_id, login_id, password, name, phone, email, role, status, is_account_locked, login_fail_count, created_at) VALUES
(1,'student1','pass1234','김학생','010-1000-0001','student1@test.com','STUDENT','ACTIVE', b'0', 0, '2026-03-01 09:00:00'),
(2,'student2','pass1234','이학생','010-1000-0002','student2@test.com','STUDENT','ACTIVE', b'0', 0, '2026-03-01 09:05:00'),
(3,'student3','pass1234','박학생','010-1000-0003','student3@test.com','STUDENT','ACTIVE', b'0', 0, '2026-03-01 09:10:00'),
(4,'student4','pass1234','최학생','010-1000-0004','student4@test.com','STUDENT','ACTIVE', b'0', 0, '2026-03-01 09:15:00'),
(5,'student5','pass1234','정학생','010-1000-0005','student5@test.com','STUDENT','ACTIVE', b'0', 0, '2026-03-01 09:20:00'),
(6,'student6','pass1234','조학생','010-1000-0006','student6@test.com','STUDENT','ACTIVE', b'0', 0, '2026-03-01 09:25:00'),
(7,'student7','pass1234','윤학생','010-1000-0007','student7@test.com','STUDENT','ACTIVE', b'0', 0, '2026-03-01 09:30:00'),
(8,'student8','pass1234','장학생','010-1000-0008','student8@test.com','STUDENT','ACTIVE', b'0', 0, '2026-03-01 09:35:00'),
(9,'student9','pass1234','임학생','010-1000-0009','student9@test.com','STUDENT','ACTIVE', b'0', 0, '2026-03-01 09:40:00'),
(10,'student10','pass1234','한학생','010-1000-0010','student10@test.com','STUDENT','ACTIVE', b'0', 0, '2026-03-01 09:45:00'),
(11,'instructor1','pass1234','박강사','010-2000-0001','instructor1@test.com','INSTRUCTOR','ACTIVE', b'0', 0, '2026-03-01 10:00:00'),
(12,'instructor2','pass1234','이강사','010-2000-0002','instructor2@test.com','INSTRUCTOR','ACTIVE', b'0', 0, '2026-03-01 10:05:00'),
(13,'instructor3','pass1234','김강사','010-2000-0003','instructor3@test.com','INSTRUCTOR','ACTIVE', b'0', 0, '2026-03-01 10:10:00'),
(14,'admin1','pass1234','관리자1','010-3000-0001','admin1@test.com','ADMIN','ACTIVE', b'0', 0, '2026-03-01 10:15:00'),
(15,'admin2','pass1234','관리자2','010-3000-0002','admin2@test.com','ADMIN','ACTIVE', b'0', 0, '2026-03-01 10:20:00');

-- 2. Profile
INSERT INTO Profile VALUES
(1,'student1.jpg','열심히 공부하는 학생입니다.'),
(2,'student2.jpg','백엔드 개발자를 꿈꾸고 있습니다.'),
(3,'student3.jpg','데이터 분석에 관심이 있습니다.'),
(4,'student4.jpg','AI 개발자를 목표로 합니다.'),
(5,'student5.jpg','성실한 학생입니다.'),
(6,'student6.jpg','문제 해결을 좋아합니다.'),
(7,'student7.jpg','협업을 즐깁니다.'),
(8,'student8.jpg','프론트엔드 개발자를 꿈꿉니다.'),
(9,'student9.jpg','꾸준히 성장하는 개발자입니다.'),
(10,'student10.jpg','열정적인 학습자입니다.'),
(11,'instructor1.jpg','Spring Boot 전문가입니다.'),
(12,'instructor2.jpg','데이터베이스 전문가입니다.'),
(13,'instructor3.jpg','AI 전문가입니다.'),
(14,'admin1.jpg','시스템 관리자입니다.'),
(15,'admin2.jpg','플랫폼 운영 관리자입니다.');

-- 3. Course
INSERT INTO Course VALUES
(1, 11, 'Spring Boot 입문', 'Spring Boot 기초 과정', 'Backend', 'spring.jpg', 30,
 '2026-03-10', '2026-04-14', '2026-04-18',
 TRUE, 'APPROVED', NULL, 14, '2026-03-02 09:00:00', NULL),

(2, 12, 'Database 설계', '데이터베이스 설계와 SQL', 'Database', 'database.jpg', 25,
 '2026-03-15', '2026-05-03', '2026-05-06',
 TRUE, 'APPROVED', NULL, 14, '2026-03-02 09:10:00', NULL),

(3, 13, 'AI 기초', '인공지능과 머신러닝 입문', 'AI', 'ai.jpg', 20,
 '2026-03-20', '2026-05-08', '2026-05-11',
 TRUE, 'APPROVED', NULL, 15, '2026-03-02 09:20:00', NULL),

(4, 11, '클라우드 기초', '클라우드 컴퓨팅 입문', 'Infra', 'cloud.jpg', 40,
 '2026-04-01', '2026-07-15', '2026-07-18',
 FALSE, 'PENDING', NULL, NULL, NULL, NULL),

(5, 12, 'Docker 실습', '컨테이너 기초 및 실습', 'DevOps', 'docker.jpg', 35,
 '2026-04-05', '2026-07-20', '2026-07-23',
 FALSE, 'REJECTED', '강의 계획서와 평가 기준 보완이 필요합니다.', 14, '2026-03-03 14:30:00', NULL),

(6, 13, '데이터 시각화', '시각화 도구와 대시보드 제작', 'Data', 'viz.jpg', 25,
 '2026-04-10', '2026-07-25', '2026-07-28',
 FALSE, 'DELETED', NULL, 15, '2026-03-04 11:00:00', '2026-03-04 11:00:00');

-- 4. Section
INSERT INTO Section VALUES
-- course_id = 1
(1,1,'Spring Boot 소개','https://example.com/spring1','spring1.pdf',1,'2026-03-10'),
(2,1,'Spring Boot 프로젝트 설정','https://example.com/spring2','spring2.pdf',2,'2026-03-17'),
(3,1,'의존성 주입과 Bean 관리','https://example.com/spring3','spring3.pdf',3,'2026-03-24'),
(4,1,'Controller와 요청 처리','https://example.com/spring4','spring4.pdf',4,'2026-03-31'),
(5,1,'Thymeleaf 화면 구성','https://example.com/spring5','spring5.pdf',5,'2026-04-07'),
(6,1,'JPA 기초','https://example.com/spring6','spring6.pdf',6,'2026-04-14'),
(7,1,'Spring Security 입문','https://example.com/spring7','spring7.pdf',7,'2026-04-21'),
(8,1,'프로젝트 종합 실습','https://example.com/spring8','spring8.pdf',8,'2026-04-28'),

-- course_id = 2
(9,2,'데이터베이스 개요','https://example.com/db1','db1.pdf',1,'2026-03-15'),
(10,2,'ERD 설계','https://example.com/db2','db2.pdf',2,'2026-03-22'),
(11,2,'정규화 기초','https://example.com/db3','db3.pdf',3,'2026-03-29'),
(12,2,'키와 제약조건','https://example.com/db4','db4.pdf',4,'2026-04-05'),
(13,2,'SQL 기본 문법','https://example.com/db5','db5.pdf',5,'2026-04-12'),
(14,2,'조인과 서브쿼리','https://example.com/db6','db6.pdf',6,'2026-04-19'),
(15,2,'트랜잭션과 인덱스','https://example.com/db7','db7.pdf',7,'2026-04-26'),
(16,2,'데이터베이스 설계 실습','https://example.com/db8','db8.pdf',8,'2026-05-03'),

-- course_id = 3
(17,3,'AI 개요','https://example.com/ai1','ai1.pdf',1,'2026-03-20'),
(18,3,'머신러닝 기초','https://example.com/ai2','ai2.pdf',2,'2026-03-27'),
(19,3,'지도학습 이해','https://example.com/ai3','ai3.pdf',3,'2026-04-03'),
(20,3,'비지도학습 이해','https://example.com/ai4','ai4.pdf',4,'2026-04-10'),
(21,3,'모델 평가 지표','https://example.com/ai5','ai5.pdf',5,'2026-04-17'),
(22,3,'딥러닝 기초','https://example.com/ai6','ai6.pdf',6,'2026-04-24'),
(23,3,'신경망 실습','https://example.com/ai7','ai7.pdf',7,'2026-05-01'),
(24,3,'AI 프로젝트 종합','https://example.com/ai8','ai8.pdf',8,'2026-05-08');

-- 5. Enrollment
INSERT INTO Enrollment VALUES
(1,1,1,'ENROLLED','2026-03-05 10:00:00',NULL),
(2,2,1,'ENROLLED','2026-03-05 10:05:00',NULL),
(3,3,1,'ENROLLED','2026-03-05 10:10:00',NULL),
(4,4,2,'ENROLLED','2026-03-05 10:15:00',NULL),
(5,5,2,'ENROLLED','2026-03-05 10:20:00',NULL),
(6,6,2,'ENROLLED','2026-03-05 10:25:00',NULL),
(7,7,3,'ENROLLED','2026-03-05 10:30:00',NULL),
(8,8,3,'ENROLLED','2026-03-05 10:35:00',NULL),
(9,9,3,'ENROLLED','2026-03-05 10:40:00',NULL),
(10,10,1,'ENROLLED','2026-03-05 10:45:00',NULL);

-- 6. Attendance (Section 8개 구조 반영)
INSERT INTO Attendance
(enrollment_id, section_id, status, checked_at, recorded_at, is_manual, note)
VALUES
-- course 1 수강생 → course 1 section_id 1, 2
(1, 1, 'PRESENT', '2026-03-10 09:05:00', CURRENT_TIMESTAMP, FALSE, NULL),
(1, 2, 'PRESENT', '2026-03-17 09:03:00', CURRENT_TIMESTAMP, FALSE, NULL),
(2, 1, 'LATE',    '2026-03-10 09:15:00', CURRENT_TIMESTAMP, FALSE, NULL),
(2, 2, 'PRESENT', '2026-03-17 09:04:00', CURRENT_TIMESTAMP, FALSE, NULL),
(3, 1, 'ABSENT',  NULL,                   CURRENT_TIMESTAMP, FALSE, '미출석'),

-- course 2 수강생 → course 2 section_id 9, 10
(4, 9,  'PRESENT', '2026-03-15 09:02:00', CURRENT_TIMESTAMP, FALSE, NULL),
(5, 9,  'PRESENT', '2026-03-15 09:01:00', CURRENT_TIMESTAMP, FALSE, NULL),
(6, 10, 'LATE',    '2026-03-22 09:12:00', CURRENT_TIMESTAMP, FALSE, NULL),

-- course 3 수강생 → course 3 section_id 17, 18
(7, 17, 'PRESENT', '2026-03-20 09:03:00', CURRENT_TIMESTAMP, FALSE, NULL),
(8, 18, 'PRESENT', '2026-03-27 09:02:00', CURRENT_TIMESTAMP, FALSE, NULL);

-- 7. Grade
INSERT INTO Grade (
    enrollment_id,
    attendance_score,
    assignment_score,
    exam_score,
    attitude_score,
    total_score,
    is_passed
) VALUES
(1, 90, 95, 88, 92, 91.05, TRUE),
(2, 85, 80, 78, 85, 81.15, TRUE),
(3, 70, 75, 72, 80, 73.10, FALSE),
(4, 88, 90, 85, 87, 87.20, TRUE),
(5, 92, 93, 91, 90, 91.55, TRUE);

-- 8. Assignment
INSERT INTO Assignment VALUES
(1,1,'Spring Boot 환경 설정 과제','프로젝트 생성 및 실행','assignment1.pdf','2026-03-20 23:59:59'),
(2,2,'ERD 설계 과제','ERD 작성 및 제출','assignment2.pdf','2026-03-25 23:59:59'),
(3,4,'AI 개념 리포트','AI 개념 조사','assignment3.pdf','2026-03-30 23:59:59');

-- 9. Submission
INSERT INTO Submission VALUES
(1,1,1,'과제 제출합니다.','sub1.zip','2026-03-18 20:00:00',95,'훌륭합니다.'),
(2,1,2,'과제 제출합니다.','sub2.zip','2026-03-19 21:00:00',88,'잘했습니다.'),
(3,2,4,'ERD 제출합니다.','sub3.zip','2026-03-24 19:00:00',90,'좋은 설계입니다.'),
(4,3,7,'AI 리포트 제출','sub4.zip','2026-03-29 18:00:00',92,'우수합니다.');

-- 10. Post (최종 구조 기준 데이터)
INSERT INTO Post
(member_id, course_id, section_id, title, content, post_type, is_secret, answer_status, view_count)
VALUES
(14, NULL, NULL, '전체 공지', '시스템 점검 안내입니다.', 'ADMIN_NOTICE', FALSE, NULL, 120),
(11, 1, NULL, 'Spring Boot 공지', 'OT 안내입니다.', 'COURSE_NOTICE', FALSE, NULL, 95),
(2, NULL, NULL, '오늘 자유게시판 글', '다들 과제 했냐?', 'FREE', FALSE, NULL, 12),
(1, 1, 2, '2주차 섹션 질문', '이번 섹션 범위가 어디까지인가요?', 'SECTION_QNA', TRUE, 'PENDING', 8),
(14, NULL, NULL, '관리자14 공지', '관리자 14가 작성한 전체 공지입니다.', 'ADMIN_NOTICE', FALSE, NULL, 0),
(15, NULL, NULL, '관리자15 공지', '관리자 15가 작성한 전체 공지입니다.', 'ADMIN_NOTICE', FALSE, NULL, 0),
(11, 1, NULL, '강사11 코스 공지', '강사 11이 코스 1에 작성한 공지입니다.', 'COURSE_NOTICE', FALSE, NULL, 0),
(12, 2, NULL, '강사12 코스 공지', '강사 12가 코스 2에 작성한 공지입니다.', 'COURSE_NOTICE', FALSE, NULL, 0),
(1, NULL, NULL, '학생1 자유글', '학생 1이 작성한 자유게시판 글입니다.', 'FREE', FALSE, NULL, 0),
(2, NULL, NULL, '학생2 자유글', '학생 2가 작성한 자유게시판 글입니다.', 'FREE', FALSE, NULL, 0),
(11, NULL, NULL, '강사11 자유글', '강사 11이 작성한 자유게시판 글입니다.', 'FREE', FALSE, NULL, 0),
(1, 1, 2, '학생1 섹션 질문', '학생 1이 남긴 섹션 질문입니다.', 'SECTION_QNA', TRUE, 'PENDING', 0),
(2, 1, 2, '학생2 섹션 질문', '학생 2가 남긴 섹션 질문입니다.', 'SECTION_QNA', FALSE, 'PENDING', 0);

-- 11. Comment (최종 구조 기준 데이터)
INSERT INTO Comment
(post_id, member_id, parent_comment_id, content, is_deleted)
VALUES
((SELECT post_id FROM Post WHERE title = '2주차 섹션 질문' LIMIT 1), 11, NULL, '이번 섹션 범위는 3장까지입니다.', FALSE),
((SELECT post_id FROM Post WHERE title = '2주차 섹션 질문' LIMIT 1), 1, NULL, '감사합니다!', FALSE),
((SELECT post_id FROM Post WHERE title = '오늘 자유게시판 글' LIMIT 1), 2, NULL, '나도 아직 못 했다.', FALSE),
((SELECT post_id FROM Post WHERE title = '학생1 섹션 질문' LIMIT 1), 11, NULL, '강사 11의 답변입니다.', FALSE),
((SELECT post_id FROM Post WHERE title = '학생1 섹션 질문' LIMIT 1), 1, NULL, '학생 1의 추가 질문입니다.', FALSE),
((SELECT post_id FROM Post WHERE title = '학생2 섹션 질문' LIMIT 1), 12, NULL, '강사 12의 답변입니다.', FALSE),
((SELECT post_id FROM Post WHERE title = '강사11 자유글' LIMIT 1), 2, NULL, '학생 2가 남긴 자유게시판 댓글입니다.', FALSE),
((SELECT post_id FROM Post WHERE title = '학생1 자유글' LIMIT 1), 11, NULL, '강사 11도 자유게시판에 댓글을 남깁니다.', FALSE);

-- 대댓글
INSERT INTO Comment 
(post_id, member_id, parent_comment_id, content, is_deleted)
VALUES (
    (SELECT post_id 
     FROM Post 
     WHERE title = '학생1 섹션 질문' 
     LIMIT 1),
    2,
    (
        SELECT comment_id 
        FROM (
            SELECT comment_id
            FROM Comment
            WHERE post_id = (
                SELECT post_id 
                FROM Post 
                WHERE title = '학생1 섹션 질문' 
                LIMIT 1
            )
            AND member_id = 11
            AND content = '강사 11의 답변입니다.'
            LIMIT 1
        ) AS temp_comment
    ),
    '학생 2의 대댓글입니다.',
    FALSE
);

-- 12. Memo
INSERT INTO Memo VALUES
(1,1,'과제 제출 마감일 확인','2026-03-20'),
(2,2,'시험 일정 확인','2026-04-10'),
(3,3,'팀 프로젝트 준비','2026-04-15');


SET FOREIGN_KEY_CHECKS = 1;

SET FOREIGN_KEY_CHECKS = 0;

-- 2. member_id에 AUTO_INCREMENT 추가하기
ALTER TABLE member MODIFY COLUMN member_id INT AUTO_INCREMENT;

ALTER TABLE Course MODIFY course_id INT AUTO_INCREMENT;
ALTER TABLE Section MODIFY section_id INT AUTO_INCREMENT;
ALTER TABLE Assignment MODIFY assignment_id INT AUTO_INCREMENT;
ALTER TABLE Submission MODIFY submission_id INT AUTO_INCREMENT;

-- 3. 외래키(연결) 검사 다시 켜기 (이거 꼭 다시 켜야 해!)
SET FOREIGN_KEY_CHECKS = 1;

-- 1. 로그인 실패 횟수가 NULL인 데이터를 모두 0으로 초기화
UPDATE member 
SET login_fail_count = 0 
WHERE login_fail_count IS NULL;

-- 2. password 모두 1234로 수정
UPDATE member 
SET password = '$2a$10$9wLL/4itaUnVpSCaoETS9uqvBwqpZMRnWCVXqYbBFxeJuuNk6G1fe', -- 1234의 암호화 값
    login_fail_count = 0,    -- 실패 횟수 리셋
    is_account_locked = 0;    -- 계정 잠금 해제

ALTER TABLE member 
ADD COLUMN approval_status VARCHAR(20) DEFAULT 'APPROVED',
ADD COLUMN approval_code VARCHAR(10),
ADD COLUMN is_verified TINYINT(1) DEFAULT 1,
ADD COLUMN reject_reason TEXT,
ADD COLUMN grad_cert_path VARCHAR(255),
ADD COLUMN career_cert_path VARCHAR(255);

SHOW TABLES; 
