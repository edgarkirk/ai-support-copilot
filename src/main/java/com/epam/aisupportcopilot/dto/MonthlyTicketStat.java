package com.epam.aisupportcopilot.dto;

public record MonthlyTicketStat(
    int year,
    int month,
    long count
) {
}