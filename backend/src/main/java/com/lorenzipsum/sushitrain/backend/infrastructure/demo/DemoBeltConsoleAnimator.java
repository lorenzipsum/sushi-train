package com.lorenzipsum.sushitrain.backend.infrastructure.demo;

import com.lorenzipsum.sushitrain.backend.application.belt.BeltService;
import com.lorenzipsum.sushitrain.backend.domain.belt.Belt;
import com.lorenzipsum.sushitrain.backend.interfaces.rest.seat.dto.SeatStateDto;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component
@Profile("demo-mode")
@EnableConfigurationProperties(DemoBeltAnimationProperties.class)
class DemoBeltConsoleAnimator {
    private static final String ANSI_CLEAR = "\u001b[H\u001b[2J";
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final BeltService beltService;
    private final DemoBeltAnimationProperties props;
    private final AtomicBoolean running = new AtomicBoolean(false);

    DemoBeltConsoleAnimator(BeltService beltService, DemoBeltAnimationProperties props) {
        this.beltService = beltService;
        this.props = props;
    }

    @Scheduled(
            fixedDelayString = "${app.demo-belt-animation.fixed-delay:1500ms}",
            initialDelayString = "${app.demo-belt-animation.initial-delay:3s}"
    )
    void render() {
        if (!props.isEnabled()) return;
        if (!running.compareAndSet(false, true)) return;

        try {
            UUID beltId = resolveBeltId();
            if (beltId == null) {
                printFrame("[demo-mode] No belts found.\n");
                return;
            }

            Belt belt = beltService.getBelt(beltId);
            List<SeatStateDto> seatStates = beltService.getSeatStates(beltId);
            printFrame(renderFrame(belt, seatStates));
        } catch (RuntimeException ex) {
            System.err.println("[demo-mode] Belt animation skipped: " + ex.getMessage());
        } finally {
            running.set(false);
        }
    }

    private UUID resolveBeltId() {
        if (props.getBeltId() != null) return props.getBeltId();
        var belts = beltService.getAllBelts();
        if (belts.isEmpty()) return null;
        return belts.getFirst().getId();
    }

    private String renderFrame(Belt belt, List<SeatStateDto> seats) {
        int slotCount = belt.getSlotCount();
        int offset = belt.currentOffsetAt(Instant.now());

        char[] seatTrack = new char[slotCount];
        char[] beltTrack = new char[slotCount];
        java.util.Arrays.fill(seatTrack, ' ');
        java.util.Arrays.fill(beltTrack, '.');

        for (var seat : seats) {
            int pos = seat.positionIndex();
            if (pos >= 0 && pos < slotCount) {
                seatTrack[pos] = seat.isOccupied() ? 'X' : 'O';
            }
        }

        for (var slot : belt.getSlots()) {
            if (slot.getPlateId() == null) continue;
            int visualPos = Math.floorMod(slot.getPositionIndex() + offset, slotCount);
            beltTrack[visualPos] = 'o';
        }

        long occupiedSeats = seats.stream().filter(SeatStateDto::isOccupied).count();
        long platesOnBelt = belt.getSlots().stream().filter(s -> s.getPlateId() != null).count();

        String seatSummary = seats.stream()
                .sorted(Comparator.comparingInt(SeatStateDto::positionIndex))
                .map(s -> s.label() + ":" + (s.isOccupied() ? "occupied" : "free"))
                .collect(Collectors.joining(" | "));

        return "[demo-mode] Sushi Belt Live\n"
                + "Belt: " + belt.getName() + " (" + belt.getId() + ")\n"
                + "Time: " + TIME_FMT.format(Instant.now()) + "  Offset: " + offset
                + "  Plates: " + platesOnBelt + "  Seats occupied: " + occupiedSeats + "/" + seats.size() + "\n"
                + "Legend: X occupied seat, O free seat, o plate, . empty slot\n"
                + "Seats: |" + new String(seatTrack) + "|\n"
                + "Belt : |" + new String(beltTrack) + "|\n"
                + "Seat summary: " + seatSummary + "\n";
    }

    private void printFrame(String frame) {
        if (props.isClearScreen()) {
            System.out.print(ANSI_CLEAR);
        }
        System.out.print(frame);
    }
}
