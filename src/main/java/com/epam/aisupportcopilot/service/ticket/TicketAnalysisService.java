package com.epam.aisupportcopilot.service.ticket;

import com.epam.aisupportcopilot.dto.CategoryCount;
import com.epam.aisupportcopilot.dto.MonthlyTicketStat;
import com.epam.aisupportcopilot.dto.TicketSummary;
import com.epam.aisupportcopilot.enums.TicketCategory;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Provides analytical queries over the support ticket database.
 * All methods are backed by Caffeine cache to avoid redundant database hits
 * during multi-tool LLM interactions.
 */
public interface TicketAnalysisService {

    /**
     * Total ticket count for a category (all time).
     */
    long countByCategory(TicketCategory category);

    /** Ticket count for a category within a specific date range. */
    long countByCategoryAndDateRange(TicketCategory category, LocalDateTime from, LocalDateTime to);

    /** Monthly ticket counts for a category across all available months. */
    List<MonthlyTicketStat> getMonthlyTrend(TicketCategory category);

    /** Ticket count per category within a date range. */
    List<CategoryCount> getCategoryBreakdown(LocalDateTime from, LocalDateTime to);

    /** The 10 most recent tickets for a category. */
    List<TicketSummary> getRecentTickets(TicketCategory category);
}