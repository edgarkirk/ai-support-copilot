package com.epam.aisupportcopilot.dto;

import com.epam.aisupportcopilot.enums.TicketCategory;

public record CategoryCount(
    TicketCategory category,
    long count
) {
}