package com.epam.aisupportcopilot.service;

import com.epam.aisupportcopilot.dto.CategoryCount;
import com.epam.aisupportcopilot.dto.MonthlyTicketStat;
import com.epam.aisupportcopilot.dto.TicketSummary;
import com.epam.aisupportcopilot.enums.TicketCategory;
import java.time.LocalDateTime;
import java.util.List;

public interface TicketAnalysisService {

    long countByCategory(TicketCategory category);

    long countByCategoryAndDateRange(TicketCategory category, LocalDateTime from, LocalDateTime to);

    List<MonthlyTicketStat> getMonthlyTrend(TicketCategory category);

    List<CategoryCount> getCategoryBreakdown(LocalDateTime from, LocalDateTime to);

    List<TicketSummary> getRecentTickets(TicketCategory category);
}