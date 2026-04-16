package com.wanted.projectmodule2lms.domain.calendar.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "Memo")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CalendarMemo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "memo_id")
    private Integer memoId;

    @Column(name = "member_id", nullable = false)
    private Integer memberId;

    @Column(name = "content", nullable = false, length = 255)
    private String content;

    @Column(name = "memo_date", nullable = false)
    private LocalDate memoDate;

    public CalendarMemo(Integer memberId, String content, LocalDate memoDate) {
        this.memberId = memberId;
        this.content = content;
        this.memoDate = memoDate;
    }

    public void changeContent(String content) {
        this.content = content;
    }
}