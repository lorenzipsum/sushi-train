package com.lorenzipsum.sushitrain.backend.config.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class TxSqlNoiseFilter extends Filter<ILoggingEvent> {

    private static final String TX_LOGGER = "org.springframework.orm.jpa.JpaTransactionManager";

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (event == null) return FilterReply.NEUTRAL;

        // Never hide warnings/errors from anyone
        if (event.getLevel().isGreaterOrEqual(Level.WARN)) {
            return FilterReply.NEUTRAL;
        }

        String logger = event.getLoggerName();
        if (!TX_LOGGER.equals(logger)) {
            return FilterReply.NEUTRAL;
        }

        String msg = event.getFormattedMessage();
        if (msg == null) return FilterReply.DENY;

        // only show specific transaction log entries to avoid noise
        if (msg.startsWith("Creating new transaction")) return FilterReply.NEUTRAL;
        if (msg.startsWith("Rolling back JPA transaction")) return FilterReply.NEUTRAL;
        if (msg.startsWith("Closing JPA EntityManager after transaction")) return FilterReply.NEUTRAL;

        return FilterReply.DENY;
    }
}
