package com.epam.aisupportcopilot.dto;

import com.epam.aisupportcopilot.enums.TicketCategory;
import com.epam.aisupportcopilot.enums.TicketPriority;
import com.epam.aisupportcopilot.enums.TicketStatus;
import java.time.LocalDateTime;

public record TicketSummary(
    Long id,
    LocalDateTime createdAt,
    TicketCategory category,
    TicketPriority priority,
    TicketStatus status,
    String description
) {
}