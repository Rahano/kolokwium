package edu.iis.mto.oven;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OvenTest {

    @Mock
    Fan fan;
    @Mock
    HeatingModule heatingModule;

    private Oven oven;
    private BakingProgram bakingProgram;
    private ProgramStage programStage1;
    private ProgramStage programStage2;
    private ProgramStage programStage3;
    private List<ProgramStage> stageList;

    @BeforeEach
    private void setup(){
        oven = new Oven(heatingModule, fan);
    }

    @Test
    void ovenWithBakingProgramContainingOneProgramStageShouldCallHeatingModuleOnceToActivateHeaterFanShouldNotBeTunedOn() {
        programStage1 = ProgramStage.builder().withHeat(HeatType.HEATER).withStageTime(5).withTargetTemp(180).build();
        stageList = List.of(programStage1);
        bakingProgram = BakingProgram.builder().withInitialTemp(180).withStages(stageList).build();
        oven.start(bakingProgram);

        verify(fan, times(0)).on();
        verify(heatingModule, times(1)).heater(settings(programStage1));
    }

    @Test
    void ovenWithBakingProgramContainingOneProgramStageShouldCallFanToTurnOnOnceAndTurnOffOnceANdHeatingModuleOnceToActivateThermalCircuit() throws HeatingException {
        programStage1 = ProgramStage.builder().withHeat(HeatType.THERMO_CIRCULATION).withStageTime(10).withTargetTemp(200).build();
        stageList = List.of(programStage1);
        bakingProgram = BakingProgram.builder().withInitialTemp(200).withStages(stageList).build();
        oven.start(bakingProgram);

        verify(fan, times(1)).on();
        verify(fan, times(1)).off();
        verify(heatingModule, times(1)).termalCircuit(settings(programStage1));
    }

    @Test
    void ovenWithBakingProgramContainingOneProgramStageShouldCallFanToTurnOnOnceAndTurnOffOnceANdHeatingModuleOnceToActivateThermalCircuitShouldThrowOvenException() throws HeatingException {
        programStage1 = ProgramStage.builder().withHeat(HeatType.THERMO_CIRCULATION).withStageTime(10).withTargetTemp(100).build();
        stageList = List.of(programStage1);
        bakingProgram = BakingProgram.builder().withInitialTemp(100).withStages(stageList).build();
        doThrow(HeatingException.class).when(heatingModule).termalCircuit(settings(programStage1));

        var thrown = assertThrows(OvenException.class, () -> {
            oven.start(bakingProgram);
        });
        assertEquals(HeatingException.class, thrown.getCause().getClass());
        verify(fan, times(1)).on();
        verify(fan, times(1)).off();
        verify(heatingModule, times(1)).termalCircuit(settings(programStage1));
    }

    @Test
    void ovenWithBakingProgramContainingThreeProgramStageShouldCallHeatingModuleThreeTimesToActivateHeaterTwoTimesAndGrillOnceFanShouldNotBeTunedOn() {
        programStage1 = ProgramStage.builder().withHeat(HeatType.HEATER).withStageTime(5).withTargetTemp(180).build();
        programStage2 = ProgramStage.builder().withHeat(HeatType.GRILL).withStageTime(5).withTargetTemp(200).build();
        programStage3 = ProgramStage.builder().withHeat(HeatType.HEATER).withStageTime(10).withTargetTemp(180).build();
        stageList = List.of(programStage1, programStage2, programStage3);
        bakingProgram = BakingProgram.builder().withInitialTemp(180).withStages(stageList).build();
        oven.start(bakingProgram);

        verify(fan, times(0)).on();
        verify(heatingModule, times(1)).heater(settings(programStage1));
        verify(heatingModule, times(1)).grill(settings(programStage2));
        verify(heatingModule, times(1)).heater(settings(programStage3));
    }

    @Test
    void ovenWithBakingProgramContainingOneProgramStageShouldCallHeatingModuleOnceToActivateGrillFanShouldNotBeTunedOn() {
        programStage1 = ProgramStage.builder().withHeat(HeatType.GRILL).withStageTime(5).withTargetTemp(180).build();
        stageList = List.of(programStage1);
        bakingProgram = BakingProgram.builder().withInitialTemp(180).withStages(stageList).build();
        oven.start(bakingProgram);

        verify(fan, times(0)).on();
        verify(heatingModule, times(1)).grill(settings(programStage1));
    }

    private HeatingSettings settings(ProgramStage stage) {
        return HeatingSettings.builder()
                              .withTargetTemp(stage.getTargetTemp())
                              .withTimeInMinutes(stage.getStageTime())
                              .build();
    }
}
