package SpringBootBE.BackEnd.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "lesson_progress")
@NoArgsConstructor
@AllArgsConstructor
public class LessonProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProgressID")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EnrollmentID")
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LessonID")
    private Lesson lesson;

    @Column(name = "WatchedPercentage")
    private BigDecimal watchedPercentage;

    @Column(name = "IsCompleted")
    private Boolean isCompleted;

    @Column(name = "LastWatchedAt")
    private LocalDateTime lastWatchedAt;
}

