package edu.iis.mto.oven;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;

import org.hamcrest.MatcherAssert;
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
    private HeatingSettings heatingSettings;
    private BakingProgram bakingProgram;
    private ProgramStage programStage1;
    private List<ProgramStage> stageList;

    @BeforeEach
    private void setup(){
        oven = new Oven(heatingModule, fan);
    }

    @Test
    void ovenWithBakingProgramContainingOneProgramStageShouldCallHeatingModuleOnceToActivateHeater() {
        programStage1 = ProgramStage.builder().withHeat(HeatType.HEATER).withStageTime(5).withTargetTemp(180).build();
        heatingSettings = HeatingSettings.builder().withTargetTemp(180).withTimeInMinutes(5).build();
        stageList = List.of(programStage1);
        bakingProgram = BakingProgram.builder().withInitialTemp(180).withStages(stageList).build();
        oven.start(bakingProgram);
        Mockito.verify(heatingModule, Mockito.times(1)).heater(heatingSettings);
    }

}
