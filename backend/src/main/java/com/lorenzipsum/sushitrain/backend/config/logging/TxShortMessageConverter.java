package com.lorenzipsum.sushitrain.backend.config.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class TxShortMessageConverter extends ClassicConverter {

    private static final String TX_LOGGER = "org.springframework.orm.jpa.JpaTransactionManager";

    @Override
    public String convert(ILoggingEvent event) {
        if (event == null) return "";

        // Safety: if somehow used for other loggers, do not rewrite them.
        if (!TX_LOGGER.equals(event.getLoggerName())) return event.getFormattedMessage();

        String msg = event.getFormattedMessage();
        if (msg == null) return "TX: (no message)";

        if (msg.startsWith("Creating new transaction with name")) return "TX: transaction opened";
        if (msg.startsWith("Rolling back JPA transaction")) return "TX: rolling back";
        if (msg.startsWith("Committing JPA transaction on EntityManager")) return "TX: committing";
        if (msg.startsWith("Closing JPA EntityManager after transaction")) return "TX: transaction closed";

        return "TX: (ignored)";
    }
}