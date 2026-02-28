package com.lorenzipsum.sushitrain.backend.infrastructure.scheduling;

import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.BeltSlotJpaDao;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.OrderJpaDao;
import com.lorenzipsum.sushitrain.backend.infrastructure.persistence.jpa.repo.PlateJpaDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataIntegrityRepairServiceTest {

    @Mock
    PlateJpaDao plateJpaDao;

    @Mock
    BeltSlotJpaDao beltSlotJpaDao;

    @Mock
    OrderJpaDao orderJpaDao;

    @InjectMocks
    DataIntegrityRepairService service;

    @Test
    void repairKnownAnomalies_no_anomalies() {
        given(orderJpaDao.closeDuplicateOpenOrdersPerSeat()).willReturn(0);
        given(plateJpaDao.findOnBeltPlateIdsAlreadyAssignedToOrderLine()).willReturn(List.of());

        var result = service.repairKnownAnomalies();

        assertEquals(0, result.detectedPlates());
        assertEquals(0, result.clearedSlots());
        assertEquals(0, result.markedPicked());
        assertEquals(0, result.duplicateOpenOrdersClosed());
        verify(orderJpaDao).closeDuplicateOpenOrdersPerSeat();
        verify(plateJpaDao).findOnBeltPlateIdsAlreadyAssignedToOrderLine();
        verifyNoInteractions(beltSlotJpaDao);
    }

    @Test
    void repairKnownAnomalies_repairs_detected_rows() {
        UUID plate1 = UUID.randomUUID();
        UUID plate2 = UUID.randomUUID();
        var ids = List.of(plate1, plate2);

        given(orderJpaDao.closeDuplicateOpenOrdersPerSeat()).willReturn(1);
        given(plateJpaDao.findOnBeltPlateIdsAlreadyAssignedToOrderLine()).willReturn(ids);
        given(beltSlotJpaDao.clearPlateAssignments(ids)).willReturn(2);
        given(plateJpaDao.markPicked(ids)).willReturn(2);

        var result = service.repairKnownAnomalies();

        assertEquals(2, result.detectedPlates());
        assertEquals(2, result.clearedSlots());
        assertEquals(2, result.markedPicked());
        assertEquals(1, result.duplicateOpenOrdersClosed());
        verify(orderJpaDao).closeDuplicateOpenOrdersPerSeat();
        verify(plateJpaDao).findOnBeltPlateIdsAlreadyAssignedToOrderLine();
        verify(beltSlotJpaDao).clearPlateAssignments(ids);
        verify(plateJpaDao).markPicked(ids);
        verifyNoMoreInteractions(orderJpaDao, plateJpaDao, beltSlotJpaDao);
    }
}
