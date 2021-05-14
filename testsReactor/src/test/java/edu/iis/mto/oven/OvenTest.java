package edu.iis.mto.oven;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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
    void ovenWithBakingProgramContainingOneProgramStageShouldCallHeatingModuleOnceToActivateHeater() {
        programStage1 = ProgramStage.builder().withHeat(HeatType.HEATER).withStageTime(5).withTargetTemp(180).build();
        stageList = List.of(programStage1);
        bakingProgram = BakingProgram.builder().withInitialTemp(180).withStages(stageList).build();
        oven.start(bakingProgram);

        Mockito.verify(heatingModule, Mockito.times(1)).heater(settings(programStage1));
    }

    @Test
    void ovenWithBakingProgramContainingOneProgramStageShouldCallFanToTurnOnOnceAndTurnOffOnceANdHeatingModuleOnceToActivateThermalCircuit() throws HeatingException {
        programStage1 = ProgramStage.builder().withHeat(HeatType.THERMO_CIRCULATION).withStageTime(10).withTargetTemp(200).build();
        stageList = List.of(programStage1);
        bakingProgram = BakingProgram.builder().withInitialTemp(200).withStages(stageList).build();
        oven.start(bakingProgram);

        Mockito.verify(fan, Mockito.times(1)).on();
        Mockito.verify(fan, Mockito.times(1)).off();
        Mockito.verify(heatingModule, Mockito.times(1)).termalCircuit(settings(programStage1));
    }

    @Test
    void ovenWithBakingProgramContainingOneProgramStageShouldCallFanToTurnOnOnceAndTurnOffOnceANdHeatingModuleOnceToActivateThermalCircuitShouldThrowOvenException() throws HeatingException {
        programStage1 = ProgramStage.builder().withHeat(HeatType.THERMO_CIRCULATION).withStageTime(10).withTargetTemp(100).build();
        stageList = List.of(programStage1);
        bakingProgram = BakingProgram.builder().withInitialTemp(200).withStages(stageList).build();
        Mockito.doThrow(HeatingException.class).when(heatingModule).termalCircuit(settings(programStage1));

        Assertions.assertThrows(OvenException.class, () -> {
            oven.start(bakingProgram);
        });
        Mockito.verify(fan, Mockito.times(1)).on();
        Mockito.verify(fan, Mockito.times(1)).off();
        Mockito.verify(heatingModule, Mockito.times(1)).termalCircuit(settings(programStage1));
    }

    @Test
    void ovenWithBakingProgramContainingThreeProgramStageShouldCallHeatingModuleThreeTimesToActivateHeaterTwoTimesAndGrillOnce() {
        programStage1 = ProgramStage.builder().withHeat(HeatType.HEATER).withStageTime(5).withTargetTemp(180).build();
        programStage2 = ProgramStage.builder().withHeat(HeatType.GRILL).withStageTime(5).withTargetTemp(200).build();
        programStage3 = ProgramStage.builder().withHeat(HeatType.HEATER).withStageTime(10).withTargetTemp(180).build();
        stageList = List.of(programStage1, programStage2, programStage3);
        bakingProgram = BakingProgram.builder().withInitialTemp(180).withStages(stageList).build();
        oven.start(bakingProgram);

        Mockito.verify(heatingModule, Mockito.times(1)).heater(settings(programStage1));
        Mockito.verify(heatingModule, Mockito.times(1)).grill(settings(programStage2));
        Mockito.verify(heatingModule, Mockito.times(1)).heater(settings(programStage3));
    }

    private HeatingSettings settings(ProgramStage stage) {
        return HeatingSettings.builder()
                              .withTargetTemp(stage.getTargetTemp())
                              .withTimeInMinutes(stage.getStageTime())
                              .build();
    }
}
