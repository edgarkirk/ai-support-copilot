package com.epam.aisupportcopilot.controller;

import com.epam.aisupportcopilot.dto.CategoryCount;
import com.epam.aisupportcopilot.dto.MonthlyTicketStat;
import com.epam.aisupportcopilot.dto.TicketSummary;
import com.epam.aisupportcopilot.enums.TicketCategory;
import com.epam.aisupportcopilot.service.TicketAnalysisService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketAnalysisService ticketAnalysisService;

    @GetMapping("/count/{category}")
    public long countByCategory(@PathVariable TicketCategory category) {
        return ticketAnalysisService.countByCategory(category);
    }

    @GetMapping("/count/{category}/range")
    public long countByCategoryAndRange(
        @PathVariable TicketCategory category,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ticketAnalysisService.countByCategoryAndDateRange(category, from, to);
    }

    @GetMapping("/trend/{category}")
    public List<MonthlyTicketStat> monthlyTrend(@PathVariable TicketCategory category) {
        return ticketAnalysisService.getMonthlyTrend(category);
    }

    @GetMapping("/breakdown")
    public List<CategoryCount> categoryBreakdown(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ticketAnalysisService.getCategoryBreakdown(from, to);
    }

    @GetMapping("/recent/{category}")
    public List<TicketSummary> recentTickets(@PathVariable TicketCategory category) {
        return ticketAnalysisService.getRecentTickets(category);
    }
}