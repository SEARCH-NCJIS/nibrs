/*
 * Copyright 2017 Mark43, Inc.
 * https://www.mark43.com/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.search.nibrs.flatfile.exporter;

import org.junit.Test;
import org.search.nibrs.common.ParsedObject;
import org.search.nibrs.flatfile.NIBRSAgeBuilder;
import org.search.nibrs.model.*;
import org.search.nibrs.model.codes.*;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.search.nibrs.flatfile.exporter.FlatFileTranslator.*;
import static org.search.nibrs.model.ArresteeSegment.GROUP_A_ARRESTEE_SEGMENT_TYPE_IDENTIFIER;
import static org.search.nibrs.model.ArresteeSegment.GROUP_B_ARRESTEE_SEGMENT_TYPE_IDENTIFIER;

public class TestFlatFileTranslator {

    private static LocalDate defaultDate = LocalDate.of(2017, 8, 4);
    // region admin

    @Test
    public void testAdminAllFieldsWithCargoTheft()
    {
        GroupAIncidentReport ga = makeGroupAIncidentReport();
        ga.setReportDateIndicator("R");
        ga.setIncidentHour(new ParsedObject<>(21));
        ga.setExceptionalClearanceCode(ClearedExceptionallyCode.C.code);
        ga.setExceptionalClearanceDate(new ParsedObject<>(defaultDate));
        ga.setIncludesCargoTheft(true);
        ga.setCargoTheftIndicator(CargoTheftIndicatorCode.Y.code);

        assertEquals("00881I082017    NJ004080017-0002582  20170804R21C20170804                              Y",
                translateAdminSegment(ga));
    }

    @Test
    public void testAdminMinFieldsWithoutCargoTheft()
    {
        GroupAIncidentReport ga = makeGroupAIncidentReport();

        assertEquals("00871I082017    NJ004080017-0002582  20170804   N                                      ",
                translateAdminSegment(ga));
    }

    // endregion admin

    // region arrestee

    @Test
    public void testArresteeAEmptyFields()
    {
        ArresteeSegment as = makeArresteeSegment(GROUP_A_ARRESTEE_SEGMENT_TYPE_IDENTIFIER);

        assertEquals("01106I082017    NJ004080017-0002582                                                                           ",
                translateGroupAArresteeSegment(as));
    }

    @Test
    public void testArresteeAAllFields()
    {
        ArresteeSegment as = makeArresteeSegment(GROUP_A_ARRESTEE_SEGMENT_TYPE_IDENTIFIER);

        as.setArresteeSequenceNumber(new ParsedObject<>(1));
        as.setArrestTransactionNumber("12345");
        as.setArrestDate(new ParsedObject<>(defaultDate));
        as.setTypeOfArrest(TypeOfArrestCode.O.code);
        as.setMultipleArresteeSegmentsIndicator(MultipleArresteeSegmentsIndicator.M.code);
        as.setUcrArrestOffenseCode(OffenseCode._200.code);
        for (int i = 0; i < ArresteeSegment.ARRESTEE_ARMED_WITH_COUNT; i++)
        {
            as.setArresteeArmedWith(i, TypeOfWeaponForceCode._99.code);
            as.setAutomaticWeaponIndicator(i, AutomaticWeaponIndicatorCode.A.code);
        }
        as.setAge(NIBRSAgeBuilder.buildAgeFromRawString("40", as));
        as.setSex(SexCode.F.code);
        as.setRace(RaceCode.A.code);
        as.setEthnicity(EthnicityCode.N.code);
        as.setResidentStatus(ResidentStatusCode.R.code);

        assertEquals("01106I082017    NJ004080017-0002582  0112345       20170804OM20099A99A40  FANR                                ",
                translateGroupAArresteeSegment(as));
    }

    // endregion arrestee

    // region offense

    @Test
    public void testOffenseEmptyFields()
    {
        OffenseSegment os = makeOffenseSegment();

        assertEquals("00712I082017    NJ004080017-0002582                                    ",
                translateOffenseSegment(os));
    }

    @Test
    public void testOffenseAllFields()
    {
        OffenseSegment os = makeOffenseSegment();
        os.setUcrOffenseCode(OffenseCode._210.code);
        os.setOffenseAttemptedCompleted(OffenseAttemptedCompletedCode.C.code);
        for (int i = 0; i < OffenseSegment.OFFENDERS_SUSPECTED_OF_USING_COUNT; i++)
        {
            os.setOffendersSuspectedOfUsing(i, OffenderSuspectedOfUsingCode.A.code);
        }
        os.setLocationType(LocationTypeCode._04.code);
        os.setNumberOfPremisesEntered(new ParsedObject<>(2));
        os.setMethodOfEntry(MethodOfEntryCode.F.code);
        for (int i = 0; i < OffenseSegment.TYPE_OF_CRIMINAL_ACTIVITY_COUNT; i++)
        {
            os.setTypeOfCriminalActivity(i, TypeOfCriminalActivityCode.G.code);
        }
        for (int i = 0; i < OffenseSegment.AUTOMATIC_WEAPON_INDICATOR_COUNT; i++)
        {
            os.setTypeOfWeaponForceInvolved(i, TypeOfWeaponForceCode._11.code);
            os.setAutomaticWeaponIndicator(i, AutomaticWeaponIndicatorCode.A.code);
        }
        for (int i = 0; i < OffenseSegment.BIAS_MOTIVATION_COUNT; i++)
        {
            os.setBiasMotivation(i, BiasMotivationCode._99.code);
        }

        assertEquals("00712I082017    NJ004080017-0002582  210CAAA0402FGGG11A11A11A9999999999",
                translateOffenseSegment(os));
    }

    // endregion offense

    // region property

    @Test
    public void testPropertyEmptyFields()
    {
        PropertySegment ps = makePropertySegment();

        assertEquals("03073I082017    NJ004080017-0002582                                                                                                                                                                                                                                                                                ",
                translatePropertySegment(ps));
    }

    @Test
    public void testPropertyAllFields()
    {
        PropertySegment ps = makePropertySegment();
        ps.setTypeOfPropertyLoss(TypeOfPropertyLossCode._8.code);
        for (int i = 0; i < PropertySegment.PROPERTY_DESCRIPTION_COUNT; i++)
        {
            ps.setPropertyDescription(i, PropertyDescriptionCode._03.code);
            ps.setValueOfProperty(i, new ParsedObject<>(25000));
        }
        ps.setNumberOfStolenMotorVehicles(new ParsedObject<>(10));
        ps.setNumberOfRecoveredMotorVehicles(new ParsedObject<>(0));
        for (int i = 0; i < PropertySegment.SUSPECTED_DRUG_TYPE_COUNT; i++)
        {
            ps.setSuspectedDrugType(i, SuspectedDrugTypeCode._A.code);
            ps.setEstimatedDrugQuantity(i, new ParsedObject<>(1.2));
            ps.setTypeDrugMeasurement(i, TypeOfDrugMeasurementCode._KG.code);
        }

        assertEquals("03073I082017    NJ004080017-0002582  803000025000        03000025000        03000025000        03000025000        03000025000        03000025000        03000025000        03000025000        03000025000        03000025000        1000A000000001200KGA000000001200KGA000000001200KG                              ",
                translatePropertySegment(ps));
    }

    // endregion property

    // region victim

    @Test
    public void testVictimEmptyOrgWithoutLeoka()
    {
        VictimSegment vs = makeVictimSegment(1, TypeOfVictimCode.G);
        vs.setUcrOffenseCodeConnection(0, OffenseCode._23B.code);

        assertEquals("01294I082017    NJ004080017-0002582  00123B                           G                                                          ",
                translateVictimSegment(vs));
    }

    @Test
    public void testVictimAllFieldsWithLeoka()
    {
        VictimSegment vs = makeVictimSegment(1, TypeOfVictimCode.I);
        for (int i = 0; i < VictimSegment.UCR_OFFENSE_CODE_CONNECTION_COUNT; i++)
        {
            vs.setUcrOffenseCodeConnection(i, OffenseCode.values()[i].code);
        }
        vs.setAge(NIBRSAgeBuilder.buildAgeFromRawString(AgeOfVictimCode.BB.code, vs));
        vs.setSex(SexCode.M.code);
        vs.setRace(RaceOfOffenderCode.U.code);
        vs.setEthnicity(EthnicityCode.N.code);
        vs.setResidentStatus(ResidentStatusCode.N.code);
        for (int i = 0; i < VictimSegment.AGGRAVATED_ASSAULT_HOMICIDE_CIRCUMSTANCES_COUNT; i++)
        {
            vs.setAggravatedAssaultHomicideCircumstances(i, AggravatedAssaultHomicideCircumstancesCode.values()[i + 2].code);
        }
        vs.setAdditionalJustifiableHomicideCircumstances(AdditionalJustifiableHomicideCircumstancesCode.B.code);
        for (int i = 0; i < VictimSegment.TYPE_OF_INJURY_COUNT; i++)
        {
            vs.setTypeOfInjury(i, TypeInjuryCode.values()[i].code);
        }
        for (int i = 0; i < VictimSegment.OFFENDER_NUMBER_RELATED_COUNT; i++)
        {
            vs.setOffenderNumberRelated(i, new ParsedObject<>(i + 1));
            vs.setVictimOffenderRelationship(i, RelationshipOfVictimToOffenderCode.values()[i].code);
        }
        vs.setTypeOfOfficerActivityCircumstance(TypeOfOfficerActivityCircumstance._04.code);
        vs.setOfficerAssignmentType(OfficerAssignmentType.F.code);
        vs.setOfficerOtherJurisdictionORI("DCMPD0000");
        ((GroupAIncidentReport) vs.getParentReport()).setIncludesLeoka(true);

        assertEquals("01414I082017    NJ004080017-0002582  00172020013A13B13C51022058A58B61AIBB  MUNN0102BNBILM01SE02CS03PA04SB05CH06GP07GC08IL09SP10SC04FDCMPD0000",
                translateVictimSegment(vs));
    }

    // endregion victim

    // region offender

    @Test
    public void testUnknownOffender()
    {
        OffenderSegment os = makeOffenderSegment(0);

        assertEquals("00465I082017    NJ004080017-0002582  00       ",
                translateOffenderSegment(os));
    }

    @Test
    public void testOffenderAllFields()
    {
        OffenderSegment os = makeOffenderSegment(1);
        os.setAge(NIBRSAgeBuilder.buildAgeFromRawString("2830", os));
        os.setSex(SexCode.U.code);
        os.setRace(RaceCode.P.code);
        os.setEthnicity(EthnicityCode.H.code);

        assertEquals("00465I082017    NJ004080017-0002582  012830UPH",
                translateOffenderSegment(os));
    }

    // endregion offender

    // region group a report

    @Test
    public void testDeleteGroupAIncidentReportWithoutCargoTheft()
    {
        GroupAIncidentReport ga = makeGroupAIncidentReport();
        ga.setReportActionType('D');

        assertEquals("00871D082017    NJ004080017-0002582  20170804   N                                      \r\n",
                translateGroupAIncidentReport(ga));
    }

    // endregion group a report

    // region zero report

    @Test
    public void testZeroReport()
    {
        ZeroReport zr = new ZeroReport();
        zr.setReportActionType('D');
        zr.setMonthOfTape(1);
        zr.setYearOfTape(2017);
        zr.setCityIndicator("");
        zr.setOri("NJ0040800");

        assertEquals("00430D012017    NJ0040800000000000000012017",
                translateZeroReport(zr));
    }

    // endregion zero report

    // region group B Arrest Report

    @Test
    public void testGroupBArrestReportEmptyFields()
    {
        GroupBArrestReport gb = makeGroupBArrestReport();

        assertEquals("", translateGroupBArrestReport(gb));
    }

    @Test
    public void testGroupBArrestReportMultipleArrests()
    {
        GroupBArrestReport gb = makeGroupBArrestReport();
        ArresteeSegment firstAs = new ArresteeSegment(GROUP_B_ARRESTEE_SEGMENT_TYPE_IDENTIFIER);
        ArresteeSegment secondAs = new ArresteeSegment(GROUP_B_ARRESTEE_SEGMENT_TYPE_IDENTIFIER);

        gb.addArrestee(firstAs);
        gb.addArrestee(secondAs);

        firstAs.setArresteeSequenceNumber(new ParsedObject<>(1));
        firstAs.setArrestTransactionNumber("12345");
        firstAs.setArrestDate(new ParsedObject<>(defaultDate));
        firstAs.setTypeOfArrest(TypeOfArrestCode.O.code);
        firstAs.setUcrArrestOffenseCode(OffenseCode._200.code);
        for (int i = 0; i < ArresteeSegment.ARRESTEE_ARMED_WITH_COUNT; i++)
        {
            firstAs.setArresteeArmedWith(i, TypeOfWeaponForceCode._99.code);
            firstAs.setAutomaticWeaponIndicator(i, AutomaticWeaponIndicatorCode.A.code);
        }
        firstAs.setAge(NIBRSAgeBuilder.buildAgeFromRawString("40", firstAs));
        firstAs.setSex(SexCode.F.code);
        firstAs.setRace(RaceCode.A.code);
        firstAs.setEthnicity(EthnicityCode.N.code);
        firstAs.setResidentStatus(ResidentStatusCode.R.code);

        secondAs.setArrestTransactionNumber("ABCDEFG");
        secondAs.setArrestDate(new ParsedObject<>(defaultDate));

        assertEquals("00667A082017    NJ004089912345       0120170804O20099A99A40  FANR \r\n00667A082017    NJ0040899ABCDEFG       20170804                   \r\n",
                translateGroupBArrestReport(gb));
    }

    // endregion group B Arrest Report

    // region helpers

    public static GroupAIncidentReport makeGroupAIncidentReport()
    {
        GroupAIncidentReport ga = new GroupAIncidentReport();
        ga.setReportActionType('I');
        ga.setMonthOfTape(8);
        ga.setYearOfTape(2017);
        ga.setCityIndicator("");
        ga.setOri("NJ0040800");
        ga.setIncidentNumber("17-0002582");
        ga.setIncidentDate(new ParsedObject<>(defaultDate));
        ga.setExceptionalClearanceCode(ClearedExceptionallyCode.N.code);
        return ga;
    }

    public static GroupBArrestReport makeGroupBArrestReport()
    {
        GroupBArrestReport gb = new GroupBArrestReport();

        gb.setReportActionType('A');
        gb.setMonthOfTape(8);
        gb.setYearOfTape(2017);
        gb.setCityIndicator("");
        gb.setOri("NJ0040899");

        return gb;
    }

    private static OffenderSegment makeOffenderSegment(int sequenceNumber)
    {
        OffenderSegment os = new OffenderSegment();
        os.setOffenderSequenceNumber(new ParsedObject<>(sequenceNumber));

        GroupAIncidentReport ga = makeGroupAIncidentReport();
        ga.addOffender(os);
        return os;
    }

    private static VictimSegment makeVictimSegment(int sequenceNumber, TypeOfVictimCode type)
    {
        VictimSegment vs = new VictimSegment();
        vs.setVictimSequenceNumber(new ParsedObject<>(sequenceNumber));
        vs.setTypeOfVictim(type.code);

        GroupAIncidentReport ga = makeGroupAIncidentReport();
        ga.addVictim(vs);
        return vs;
    }

    private static ArresteeSegment makeArresteeSegment(char segmentType)
    {
        ArresteeSegment as = new ArresteeSegment(segmentType);

        if (segmentType == GROUP_A_ARRESTEE_SEGMENT_TYPE_IDENTIFIER)
        {
            GroupAIncidentReport ga = makeGroupAIncidentReport();
            ga.addArrestee(as);
        }
        else
        {
            GroupBArrestReport gb = makeGroupBArrestReport();
            gb.addArrestee(as);
        }
        return as;
    }

    public static OffenseSegment makeOffenseSegment()
    {
        OffenseSegment os = new OffenseSegment();
        GroupAIncidentReport ga = makeGroupAIncidentReport();
        ga.addOffense(os);

        return os;
    }

    private static PropertySegment makePropertySegment()
    {
        PropertySegment ps = new PropertySegment();
        GroupAIncidentReport ga = makeGroupAIncidentReport();
        ga.addProperty(ps);

        return ps;
    }

    // endregion helpers
}
