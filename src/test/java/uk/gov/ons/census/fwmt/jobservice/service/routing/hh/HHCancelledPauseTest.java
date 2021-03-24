package uk.gov.ons.census.fwmt.jobservice.service.routing.hh;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class HHCancelledPauseTest {

  @InjectMocks
  private HHCancelledPause hhCancelledPause;

  @Test
  void shouldCheckisValid() {
    final FwmtActionInstruction request = FwmtActionInstruction.builder()
        .actionInstruction(ActionInstructionType.PAUSE)
        .surveyName("CENSUS")
        .addressType("HH").build();

    final GatewayCache cache = GatewayCache.builder()
        .existsInFwmt(true)
        .lastActionInstruction("Cancel")
        .build();

    assertTrue(hhCancelledPause.isValid(request, cache));
    assertFalse(hhCancelledPause.isValid(request, null));
    request.setActionInstruction(ActionInstructionType.CREATE);
    assertFalse(hhCancelledPause.isValid(request, cache));

  }
}