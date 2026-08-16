package com.epam.aisupportcopilot.service;

import com.epam.aisupportcopilot.dto.CategoryCount;
import com.epam.aisupportcopilot.dto.MonthlyTicketStat;
import com.epam.aisupportcopilot.dto.TicketSummary;
import com.epam.aisupportcopilot.entity.Ticket;
import com.epam.aisupportcopilot.enums.TicketCategory;
import com.epam.aisupportcopilot.repository.TicketRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketAnalysisServiceImpl implements TicketAnalysisService {

    private final TicketRepository ticketRepository;

    @Override
    public long countByCategory(TicketCategory category) {
        return ticketRepository.countByCategory(category);
    }

    @Override
    public long countByCategoryAndDateRange(TicketCategory category,
                                            LocalDateTime from,
                                            LocalDateTime to) {
        return ticketRepository.countByCategoryAndCreatedAtBetween(category, from, to);
    }

    @Override
    public List<MonthlyTicketStat> getMonthlyTrend(TicketCategory category) {
        return ticketRepository.countMonthlyByCategory(category);
    }

    @Override
    public List<CategoryCount> getCategoryBreakdown(LocalDateTime from, LocalDateTime to) {
        return ticketRepository.countByCategoryBetween(from, to);
    }

    @Override
    public List<TicketSummary> getRecentTickets(TicketCategory category) {
        return ticketRepository.findTop10ByCategoryOrderByCreatedAtDesc(category).stream()
                .map(this::toSummary)
                .toList();
    }

    private TicketSummary toSummary(Ticket ticket) {
        return new TicketSummary(
                ticket.getId(),
                ticket.getCreatedAt(),
                ticket.getCategory(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getDescription()
        );
    }
}