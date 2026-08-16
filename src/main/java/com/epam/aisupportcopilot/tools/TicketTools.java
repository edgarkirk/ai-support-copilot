package com.epam.aisupportcopilot.tools;

import com.epam.aisupportcopilot.dto.CategoryCount;
import com.epam.aisupportcopilot.dto.MonthlyTicketStat;
import com.epam.aisupportcopilot.dto.TicketSummary;
import com.epam.aisupportcopilot.enums.TicketCategory;
import com.epam.aisupportcopilot.service.TicketAnalysisService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TicketTools {

    private static final int DESCRIPTION_PREVIEW_LENGTH = 120;

    private final TicketAnalysisService ticketAnalysisService;

    @Tool(description = "Get the total number of support tickets for a given category. " +
            "Categories: NOTIFICATIONS, ACCOUNT, UI, PERFORMANCE, SUBSCRIPTION, DELIVERY, SEARCH, PROFILE, AUTHENTICATION, PAYMENT, REFUND")
    public String getTicketCount(
            @ToolParam(description = "Ticket category, e.g. AUTHENTICATION") TicketCategory category) {
        long count = ticketAnalysisService.countByCategory(category);
        return category + " tickets total: " + count;
    }

    @Tool(description = "Get the number of support tickets for a category within a date range. " +
            "Use ISO date-time format: yyyy-MM-ddTHH:mm:ss")
    public String getTicketCountByDateRange(
            @ToolParam(description = "Ticket category") TicketCategory category,
            @ToolParam(description = "Start date-time, e.g. 2026-07-01T00:00:00") LocalDateTime from,
            @ToolParam(description = "End date-time, e.g. 2026-07-31T23:59:59") LocalDateTime to) {
        long count = ticketAnalysisService.countByCategoryAndDateRange(category, from, to);
        return category + " tickets from " + from + " to " + to + ": " + count;
    }

    @Tool(description = "Get monthly ticket trend for a category. " +
            "Returns ticket count per month, useful for identifying spikes or trends.")
    public String getMonthlyTrend(
            @ToolParam(description = "Ticket category") TicketCategory category) {
        List<MonthlyTicketStat> trend = ticketAnalysisService.getMonthlyTrend(category);
        return formatList(trend,
                s -> s.year() + "-" + String.format("%02d", s.month()) + ": " + s.count() + " tickets",
                category + " monthly trend:\n");
    }

    @Tool(description = "Get ticket count breakdown by all categories within a date range. " +
            "Useful for comparing which categories have the most tickets in a period.")
    public String getCategoryBreakdown(
            @ToolParam(description = "Start date-time") LocalDateTime from,
            @ToolParam(description = "End date-time") LocalDateTime to) {
        List<CategoryCount> breakdown = ticketAnalysisService.getCategoryBreakdown(from, to);
        return formatList(breakdown,
                cc -> cc.category() + ": " + cc.count(),
                "Category breakdown (" + from + " to " + to + "):\n");
    }

    @Tool(description = "Get the 10 most recent tickets for a category. " +
            "Returns priority, date, and description preview.")
    public String getRecentTickets(
            @ToolParam(description = "Ticket category") TicketCategory category) {
        List<TicketSummary> tickets = ticketAnalysisService.getRecentTickets(category);
        return formatList(tickets,
                t -> "[" + t.priority() + "] " + t.createdAt() + " - " + truncate(t.description()),
                "Recent " + category + " tickets:\n");
    }

    private <T> String formatList(List<T> items, Function<T, String> mapper, String header) {
        return items.stream()
                .map(mapper)
                .collect(Collectors.joining("\n", header, ""));
    }

    private String truncate(String text) {
        if (text == null || text.length() <= DESCRIPTION_PREVIEW_LENGTH) {
            return text;
        }
        return text.substring(0, DESCRIPTION_PREVIEW_LENGTH) + "...";
    }
}