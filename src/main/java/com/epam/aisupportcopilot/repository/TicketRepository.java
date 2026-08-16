package com.epam.aisupportcopilot.repository;

import com.epam.aisupportcopilot.dto.CategoryCount;
import com.epam.aisupportcopilot.dto.MonthlyTicketStat;
import com.epam.aisupportcopilot.entity.Ticket;
import com.epam.aisupportcopilot.enums.TicketCategory;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    long countByCategory(TicketCategory category);

    long countByCategoryAndCreatedAtBetween(TicketCategory category,
                                            LocalDateTime from,
                                            LocalDateTime to);

    @Query("""
        SELECT new com.epam.aisupportcopilot.dto.MonthlyTicketStat(
            CAST(EXTRACT(YEAR FROM t.createdAt) AS int),
            CAST(EXTRACT(MONTH FROM t.createdAt) AS int),
            COUNT(t)
        )
        FROM Ticket t
        WHERE t.category = :category
        GROUP BY EXTRACT(YEAR FROM t.createdAt), EXTRACT(MONTH FROM t.createdAt)
        ORDER BY EXTRACT(YEAR FROM t.createdAt), EXTRACT(MONTH FROM t.createdAt)
        """)
    List<MonthlyTicketStat> countMonthlyByCategory(TicketCategory category);

    @Query("""
        SELECT new com.epam.aisupportcopilot.dto.CategoryCount(
            t.category,
            COUNT(t)
        )
        FROM Ticket t
        WHERE t.createdAt BETWEEN :from AND :to
        GROUP BY t.category
        ORDER BY COUNT(t) DESC
        """)
    List<CategoryCount> countByCategoryBetween(LocalDateTime from, LocalDateTime to);

    List<Ticket> findTop10ByCategoryOrderByCreatedAtDesc(TicketCategory category);
}
