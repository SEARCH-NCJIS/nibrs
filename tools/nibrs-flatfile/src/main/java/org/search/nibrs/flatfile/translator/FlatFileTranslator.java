package org.search.nibrs.flatfile.translator;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.search.nibrs.common.ParsedObject;
import org.search.nibrs.model.AbstractPersonSegment;
import org.search.nibrs.model.AbstractReport;
import org.search.nibrs.model.AbstractSegment;
import org.search.nibrs.model.ArresteeSegment;
import org.search.nibrs.model.GroupAIncidentReport;
import org.search.nibrs.model.GroupBArrestReport;
import org.search.nibrs.model.OffenderSegment;
import org.search.nibrs.model.OffenseSegment;
import org.search.nibrs.model.PropertySegment;
import org.search.nibrs.model.VictimSegment;
import org.search.nibrs.model.ZeroReport;

import java.text.SimpleDateFormat;

public class FlatFileTranslator {

    private FlatFileTranslator() {}

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    // region group a incident report

    public static String translateGroupAIncidentReport(GroupAIncidentReport gair)
    {
        StringBuilder flatFileOutput = new StringBuilder();

        flatFileOutput.append(translateAdminSegment(gair)).append("\r\n"); // only one admin segment per REN
        gair.getOffenses().forEach(os -> flatFileOutput.append(translateOffenseSegment(os)).append("\r\n"));
        gair.getProperties().forEach(ps -> flatFileOutput.append(translatePropertySegment(ps)).append("\r\n"));
        gair.getVictims().forEach(vs -> flatFileOutput.append(translateVictimSegment(vs)).append("\r\n"));
        gair.getOffenders().forEach(os -> flatFileOutput.append(translateOffenderSegment(os)).append("\r\n"));
        gair.getArrestees().forEach(as -> flatFileOutput.append(translateGroupAArresteeSegment(as)).append("\r\n"));

        return flatFileOutput.toString();
    }

    public static String translateAdminSegment(GroupAIncidentReport gair)
    {
        return segmentHeader(gair.getAdminSegmentLength(), gair.getAdminSegmentLevel(), gair, true)
                + (gair.getIncidentDate().isMissing() ? spaces(8) : DATE_FORMAT.format(gair.getIncidentDate().getValue()))
                + spacesIfNull(gair.getReportDateIndicator(), 1)
                + parsedIntToString(gair.getIncidentHour(), 2)
                + spacesIfNull(gair.getExceptionalClearanceCode(), 1)
                + (gair.getExceptionalClearanceDate().isMissing() ? spaces(8) : DATE_FORMAT.format(gair.getExceptionalClearanceDate().getValue()))
                + spaces(30)
                + (gair.includesCargoTheft() ? spacesIfNull(gair.getCargoTheftIndicator(), 1) : "");
    }

    public static String translateGroupAArresteeSegment(ArresteeSegment as)
    {
        return segmentHeader(as)
                + parsedIntToString(as.getArresteeSequenceNumber(), 2)
                + rightPad(as.getArrestTransactionNumber(), 12)
                + (as.getArrestDate().isMissing() ? spaces(8) : DATE_FORMAT.format(as.getArrestDate().getValue()))
                + spacesIfNull(as.getTypeOfArrest(), 1)
                + spacesIfNull(as.getMultipleArresteeSegmentsIndicator(), 1)
                + rightPad(as.getUcrArrestOffenseCode(), 3)
                + weaponTrackerHelper(as.getArresteeArmedWith(), as.getAutomaticWeaponIndicator())
                + translatePerson(as)
                + spacesIfNull(as.getResidentStatus(), 1)
                + spacesIfNull(as.getDispositionOfArresteeUnder18(), 1)
                + spaces(31); // meaningless deprecated blanks
    }

    public static String translateOffenseSegment(OffenseSegment os)
    {
        return segmentHeader(os)
                + rightPad(os.getUcrOffenseCode(), 3)
                + spacesIfNull(os.getOffenseAttemptedCompleted(), 1)
                + joinStringArray(os.getOffendersSuspectedOfUsing(), 1)
                + leftPad(os.getLocationType(), 2, '0')
                + parsedIntToString(os.getNumberOfPremisesEntered(), 2)
                + spacesIfNull(os.getMethodOfEntry(), 1)
                + joinStringArray(os.getTypeOfCriminalActivity(), 1)
                + weaponTrackerHelper(os.getTypeOfWeaponForceInvolved(), os.getAutomaticWeaponIndicator())
                + joinStringArray(os.getBiasMotivation(), 2);
    }

    public static String translatePropertySegment(PropertySegment ps)
    {
        StringBuilder flatFileOutput = new StringBuilder();

        flatFileOutput.append(segmentHeader(ps));
        flatFileOutput.append(spacesIfNull(ps.getTypeOfPropertyLoss(), 1));
        for (int i = 0; i < PropertySegment.PROPERTY_DESCRIPTION_COUNT; i++)
        {
            flatFileOutput.append(rightPad(ps.getPropertyDescription(i), 2));
            flatFileOutput.append((ps.getValueOfProperty(i).isMissing() ?
                    spaces(9) : leftPad(ps.getValueOfProperty(i).toString(), 9, '0')));
            flatFileOutput.append((ps.getDateRecovered(i).isMissing() ?
                    spaces(8) : DATE_FORMAT.format(ps.getDateRecovered(i).getValue())));
        }

        flatFileOutput.append(parsedIntToString(ps.getNumberOfStolenMotorVehicles(), 2));
        flatFileOutput.append(parsedIntToString(ps.getNumberOfRecoveredMotorVehicles(), 2));

        for (int i = 0; i < PropertySegment.SUSPECTED_DRUG_TYPE_COUNT; i++)
        {
            flatFileOutput.append(spacesIfNull(ps.getSuspectedDrugType(i), 1));
            flatFileOutput.append((ps.getEstimatedDrugQuantity(i) == null ?
                    spaces(12) : leftPad(String.valueOf((int)(ps.getEstimatedDrugQuantity(i) * 1000)), 12, '0')));
            flatFileOutput.append(rightPad(ps.getTypeDrugMeasurement(i), 2));
        }

        flatFileOutput.append(spaces(30)); //required filler

        return flatFileOutput.toString();
    }

    public static String translateVictimSegment(VictimSegment vs)
    {
        return segmentHeader(vs)
                + parsedIntToString(vs.getVictimSequenceNumber(), 3)
                + joinStringArray(vs.getUcrOffenseCodeConnection(), 3)
                + spacesIfNull(vs.getTypeOfVictim(), 1)
                + translatePerson(vs)
                + spacesIfNull(vs.getResidentStatus(), 1)
                + joinStringArray(vs.getAggravatedAssaultHomicideCircumstances(), 2)
                + spacesIfNull(vs.getAdditionalJustifiableHomicideCircumstances(), 1)
                + joinStringArray(vs.getTypeOfInjury(), 1)
                + victimOffenderRelation(vs)
                + victimLeokaElements(vs);
    }

    private static String victimLeokaElements(VictimSegment vs)
    {
        if (!((GroupAIncidentReport) vs.getParentReport()).includesLeoka())
        {
            return "";
        }

        return spacesIfNull(vs.getTypeOfOfficerActivityCircumstance(), 2)
                + spacesIfNull(vs.getOfficerAssignmentType(), 1)
                + spacesIfNull(vs.getOfficerOtherJurisdictionORI(), 9);
    }

    public static String translateOffenderSegment(OffenderSegment os)
    {
        return segmentHeader(os)
                + parsedIntToString(os.getOffenderSequenceNumber(), 2)
                + translatePerson(os);
    }

    // endregion group a incident report

    // region group b arrest report

    public static String translateGroupBArrestReport(GroupBArrestReport gbar)
    {
        StringBuilder flatFileOutput = new StringBuilder();

        gbar.getArrestees().forEach(as ->
                flatFileOutput.append(translateGroupBArresteeSegment(as)).append("\r\n")
        );

        return flatFileOutput.toString();
    }

    public static String translateGroupBArresteeSegment(ArresteeSegment as)
    {
        return segmentHeader(as.getSegmentLength(), as.getParentReport().getAdminSegmentLevel(), as.getParentReport(), false)
                + rightPad(as.getArrestTransactionNumber(), 12)
                + parsedIntToString(as.getArresteeSequenceNumber(), 2)
                + (as.getArrestDate().isMissing() ? spaces(8) : DATE_FORMAT.format(as.getArrestDate().getValue()))
                + spacesIfNull(as.getTypeOfArrest(), 1)
                + rightPad(as.getUcrArrestOffenseCode(), 3)
                + weaponTrackerHelper(as.getArresteeArmedWith(), as.getAutomaticWeaponIndicator())
                + translatePerson(as)
                + spacesIfNull(as.getResidentStatus(), 1)
                + spacesIfNull(as.getDispositionOfArresteeUnder18(), 1);
    }

    // endregion group b arrest report

    // region zero report

    public static String translateZeroReport(ZeroReport zr)
    {
        return zr.getAdminSegmentLength()
                + zr.getAdminSegmentLevel()
                + spacesIfNull(zr.getReportActionType(), 1)
                + leftPad(zr.getMonthOfTape().toString(), 2, '0')
                + spacesIfNull(zr.getYearOfTape(), 4)
                + rightPad(zr.getCityIndicator(), 4)
                + rightPad(zr.getOri(), 8)
                + zr.getIncidentNumber()
                + leftPad(zr.getMonthOfTape().toString(), 2, '0')
                + spacesIfNull(zr.getYearOfTape(), 4);
    }

    // endregion zero report

    // region helpers

    public static String segmentHeader(AbstractSegment as)
    {
        return segmentHeader(as.getSegmentLength(), as.getSegmentType(), as.getParentReport(), true);
    }

    public static String segmentHeader(String segmentLength, char segmentLevel, AbstractReport ar,
                                       boolean includeIdentifier)
    {
        String identifier = includeIdentifier ? rightPad(ar.getIdentifier(), 12) : "";
        return segmentLength
                + segmentLevel
                + spacesIfNull(ar.getReportActionType(), 1)
                + leftPad(ar.getMonthOfTape().toString(), 2, '0')
                + spacesIfNull(ar.getYearOfTape(), 4)
                + rightPad(ar.getCityIndicator(), 4)
                + rightPad(ar.getOri(), 8)
                + identifier;
    }

    private static String translatePerson(AbstractPersonSegment ps)
    {
        return (ps.getAge() == null ? spaces(4) : rightPad(ps.getAge().toString(), 4))
                + spacesIfNull(ps.getSex(), 1)
                + spacesIfNull(ps.getRace(), 1)
                + spacesIfNull(ps.getEthnicity(), 1);
    }

    private static String victimOffenderRelation(VictimSegment vs)
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < VictimSegment.OFFENDER_NUMBER_RELATED_COUNT; i++)
        {
            sb.append(parsedIntToString(vs.getOffenderNumberRelated(i), 2));
            sb.append(spacesIfNull(vs.getVictimOffenderRelationship(i), 2));
        }
        return sb.toString();
    }

    /**
     * ArresteeSegment.ARRESTEE_ARMED_WITH_COUNT and ArresteeSegment.AUTOMATIC_WEAPON_INDICATOR_COUNT are 2
     * OffenseSegment.TYPE_OF_WEAPON_FORCE_INVOLVED_COUNT and OffenseSegment.AUTOMATIC_WEAPON_INDICATOR_COUNT are 3
     * This method is used for both ArresteeSegment and OffenseSegment,
     * but will always be weaponArray.size() == automaticArray.size()
     */
    public static String weaponTrackerHelper(String[] weaponArray, String[] automaticArray)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < weaponArray.length; i++)
        {
            sb.append(rightPad(weaponArray[i], 2));
            sb.append(rightPad(automaticArray[i], 1));
        }
        return sb.toString();
    }

    public static String parsedIntToString(ParsedObject<Integer> parsed, int size)
    {
        return parsed == null || parsed.getValue() == null
                ? spaces(size)
                : leftPad(parsed.getValue().toString(), size, '0');
    }

    public static String spacesIfNull(Object obj, int size)
    {
        return obj == null ? spaces(size) : obj.toString();
    }

    public static String spaces(int size)
    {
        return StringUtils.repeat(' ', size);
    }

    public static String leftPad(String s, int size, char padChar)
    {
        return s == null ? spaces(size) : StringUtils.leftPad(s, size, padChar);
    }

    public static String rightPad(String s, int size)
    {
        return s == null ? spaces(size) : StringUtils.rightPad(s, size);
    }

    private static String joinStringArray(String[] array, int size)
    {
        return String.join("", Arrays.stream(array).map(x -> spacesIfNull(x, size)).collect(Collectors.toList()));
    }

    // endregion helpers
}
