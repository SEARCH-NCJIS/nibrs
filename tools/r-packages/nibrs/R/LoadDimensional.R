# Copyright 2016 SEARCH-The National Consortium for Justice Information and Statistics
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

DIMENSION_TABLES <- c(
  'AdditionalJustifiableHomicideCircumstancesType',
  'AggravatedAssaultHomicideCircumstancesType',
  'ArresteeWasArmedWithType',
  'BiasMotivationType',
  'ClearedExceptionallyType',
  'DispositionOfArresteeUnder18Type',
  'EthnicityOfPersonType',
  'LocationTypeType',
  'MethodOfEntryType',
  'OffenderSuspectedOfUsingType',
  'OfficerActivityCircumstanceType',
  'OfficerAssignmentTypeType',
  'PropertyDescriptionType',
  'RaceOfPersonType',
  'VictimOffenderRelationshipType',
  'ResidentStatusOfPersonType',
  'SexOfPersonType',
  'SuspectedDrugTypeType',
  'TypeDrugMeasurementType',
  'TypeInjuryType',
  'TypeOfArrestType',
  'TypeOfCriminalActivityType',
  'TypeOfVictimType',
  'TypeOfWeaponForceInvolvedType',
  'TypePropertyLossEtcType',
  'UCROffenseCodeType',
  'DateType',
  'Agency',
  'AgencyType'
)

FACT_TABLES <- c(
  'AdministrativeSegment',
  'OffenseSegment',
  'VictimSegment',
  'VictimOffenseAssociation',
  'OffenderSuspectedOfUsing',
  'TypeCriminalActivity',
  'BiasMotivation',
  'TypeOfWeaponForceInvolved',
  'TypeInjury',
  'AggravatedAssaultHomicideCircumstances',
  'OffenderSegment',
  'VictimOffenderAssociation',
  'PropertySegment',
  'PropertyType',
  'SuspectedDrugType',
  'ArresteeSegment',
  'ArresteeSegmentWasArmedWith',
  'ArrestReportSegment',
  'ArrestReportSegmentWasArmedWith'
)

DEFAULT_CATEGORY_ENHANCEMENT_FUNCTION_FACTORY <- list(
  addLocationTypeCategories=function(tdf) {
    tdf %>%
      mutate(
        LocationTypeDetailCategoryDescription=case_when(
          LocationTypeTypeID %in% c(2,3,5,6,7,8,12,14,17,19,21,23,24,37,38,40,41,44,45,47,48,54,55) ~ "Commercial",
          LocationTypeTypeID %in% c(1,11,22,39,57) ~ "Public",
          LocationTypeTypeID %in% c(4) ~ "Religious",
          LocationTypeTypeID %in% c(9) ~ "Health Facility",
          LocationTypeTypeID %in% c(10,16,42,50,51) ~ "Outdoors",
          LocationTypeTypeID %in% c(13,18) ~ "Roadway",
          LocationTypeTypeID %in% c(15) ~ "Public Safety",
          LocationTypeTypeID %in% c(20) ~ "Residence",
          LocationTypeTypeID %in% c(25,99) ~ "Other/Unknown",
          LocationTypeTypeID %in% c(46) ~ "Farm",
          LocationTypeTypeID %in% c(49) ~ "Military",
          LocationTypeTypeID %in% c(52,53) ~ "Educational Institution",
          LocationTypeTypeID %in% c(56) ~ "Tribal",
          LocationTypeTypeID %in% c(58) ~ "Cyberspace",
          TRUE ~ "Unknown"
        ),
        LocationTypeCategoryDescription=case_when(
          LocationTypeTypeID %in% c(20) ~ "Residence",
          LocationTypeTypeID %in% c(25,99) ~ "Other/Unknown",
          TRUE ~ "Non-Residence"
        )
      )
  },
  addVictimOffenderRelationshipCategories=function(tdf) {
    tdf %>% mutate(
      VictimOffenderRelationshipCategoryDescription=case_when(
        VictimOffenderRelationshipTypeID %in% c(1,2,3,4,5,6,7,8,9,10,11,12) ~ "Victim was Family",
        VictimOffenderRelationshipTypeID %in% c(18,20) ~ "Victim was Intimate Partner",
        VictimOffenderRelationshipTypeID %in% c(14,15,16,17,19,21,22,23,24) ~ "Victim was Otherwise Known",
        VictimOffenderRelationshipTypeID %in% c(25) ~ "Victim was Stranger",
        VictimOffenderRelationshipTypeID %in% c(99) ~ "Relationship Unknown",
        VictimOffenderRelationshipTypeID %in% c(98) ~ "None",
        VictimOffenderRelationshipTypeID %in% c(13) ~ "Victim Was Offender",
        TRUE ~ "Unknown"
      )
    )
  },
  addPropertyDescriptionCategories=function(tdf) {
    tdf %>% mutate(PropertyDescriptionCategoryDescription=StateDescription)
  }
)

DEFAULT_AGE_GROUP_FUNCTION_FACTORY <- list(
  createAgeGroup=function(age, AgeNeonateIndicator=NULL, AgeFirstWeekIndicator=NULL, AgeFirstYearIndicator=NULL, NonNumericAge=NULL) {
    if (is.null(AgeNeonateIndicator)) {
      AgeNeonateIndicator=rep(0, length(age))
    }
    if (is.null(AgeFirstWeekIndicator)) {
      AgeFirstWeekIndicator=rep(0, length(age))
    }
    if (is.null(AgeFirstYearIndicator)) {
      AgeFirstYearIndicator=rep(0, length(age))
    }
    if (is.null(NonNumericAge)) {
      NonNumericAge=rep('0', length(age))
    }
    case_when(
      AgeNeonateIndicator==1 ~ '1 or less',
      AgeFirstWeekIndicator==1 ~ '1 or less',
      AgeFirstYearIndicator==1 ~ '1 or less',
      age <= 1 ~ '1 or less',
      age <= 5 ~ '2-5',
      age <= 10 ~ '6-10',
      age <= 13 ~ '11-13',
      age <= 17 ~ '14-17',
      age <= 20 ~ '18-20',
      age <= 25 ~ '21-25',
      age <= 30 ~ '26-30',
      age <= 40 ~ '31-40',
      age <= 50 ~ '41-50',
      age <= 60 ~ '51-60',
      age <= 70 ~ '61-70',
      age <= 80 ~ '71-80',
      age >= 81 ~ '81+',
      NonNumericAge == '00' ~ 'Unknown',
      is.na(age) ~ 'Blank',
      TRUE ~ 'N/A'
    )
  },
  createAgeGroupSort=function(age, AgeNeonateIndicator=NULL, AgeFirstWeekIndicator=NULL, AgeFirstYearIndicator=NULL, NonNumericAge=NULL) {
    if (is.null(AgeNeonateIndicator)) {
      AgeNeonateIndicator=rep(0, length(age))
    }
    if (is.null(AgeFirstWeekIndicator)) {
      AgeFirstWeekIndicator=rep(0, length(age))
    }
    if (is.null(AgeFirstYearIndicator)) {
      AgeFirstYearIndicator=rep(0, length(age))
    }
    if (is.null(NonNumericAge)) {
      NonNumericAge=rep('0', length(age))
    }
    case_when(
      AgeNeonateIndicator==1 ~ 1,
      AgeFirstWeekIndicator==1 ~ 1,
      AgeFirstYearIndicator==1 ~ 1,
      age <= 1 ~ 1,
      age <= 5 ~ 2,
      age <= 10 ~ 3,
      age <= 13 ~ 4,
      age <= 17 ~ 5,
      age <= 20 ~ 6,
      age <= 25 ~ 7,
      age <= 30 ~ 8,
      age <= 40 ~ 9,
      age <= 50 ~ 10,
      age <= 60 ~ 11,
      age <= 70 ~ 12,
      age <= 80 ~ 13,
      age >= 81 ~ 14,
      NonNumericAge == '00' ~ 99,
      is.na(age) ~ 98,
      TRUE ~ 100
    )
  }
)

#' Load the NIBRS dimensional database from a staging database
#'
#' Load the NIBRS dimensional database from a staging database, optionally taking a random sample of Administrative Segment
#' and Group B Arrest Segment records.
#' @importFrom DBI dbReadTable
#' @importFrom lubridate is.Date
#' @import dplyr
#' @import purrr
#' @import tibble
#' @param stagingConn connection to the staging database
#' @param dimensionalConn connection to the dimensional database
#' @param writeToDatabase whether to write to the dimensional database or not
#' @param sampleFraction numeric value between 0 and 1 indicating percentage of records in the sample.  NULL to take all records.
#' @param seed random seed.  Set this to the same value in subsequent calls to generate the same random sample.
#' @return a list with all the dimensional database tables, as tibbles
#' @export
loadDimensionalFromStagingDatabase <- function(
  stagingConn=DBI::dbConnect(RMariaDB::MariaDB(), host="localhost", dbname="search_nibrs_staging", username="root"),
  dimensionalConn=DBI::dbConnect(RMariaDB::MariaDB(), host="localhost", dbname="search_nibrs_dimensional", username="root"),
  writeToDatabase=TRUE, ageGroupFunctionFactory=NULL, categoryEnhancementFunctionFactory=NULL,
  sampleFraction=NULL, seed=12341234) {

  writeLines('Reading dimension tables from staging')
  dimensionTables <- map(DIMENSION_TABLES, function(tableName) {
    dbReadTable(stagingConn, tableName) %>% as_tibble()
  }) %>% set_names(DIMENSION_TABLES)

  writeLines('Reading fact tables from staging')
  factTables <- map(FACT_TABLES, function(tableName) {
    dbReadTable(stagingConn, tableName) %>% as_tibble()
  }) %>% set_names(FACT_TABLES)

  dimensionTables$State <- loadStatesForDimensional()

  if (is.null(ageGroupFunctionFactory)) {
    ageGroupFunctionFactory <- DEFAULT_AGE_GROUP_FUNCTION_FACTORY
  }

  if (is.null(categoryEnhancementFunctionFactory)) {
    categoryEnhancementFunctionFactory <- DEFAULT_CATEGORY_ENHANCEMENT_FUNCTION_FACTORY
  } else {
    categoryEnhancementFunctionFactory <- imap(DEFAULT_CATEGORY_ENHANCEMENT_FUNCTION_FACTORY, function(f, fname) {
      if (fname %in% names(categoryEnhancementFunctionFactory)) {
        writeLines(paste0('Found custom category enhancement function ', fname))
        categoryEnhancementFunctionFactory[[fname]]
      } else {
        f
      }
    })
  }

  if (writeToDatabase) {
    ret <- loadDimensionalFromObjectLists(dimensionTables, factTables, dimensionalConn, ageGroupFunctionFactory, categoryEnhancementFunctionFactory, sampleFraction, seed)
  } else {
    ret <- convertStagingTablesToDimensional(dimensionTables, factTables, ageGroupFunctionFactory, categoryEnhancementFunctionFactory, sampleFraction, seed)
  }

  ret

}

#' Convert lists of dimension and fact tables from staging to dimensional format
#'
#' Convert lists of dimension and fact tables from staging to dimensional format, optionally taking a random sample of Administrative Segment
#' and Group B Arrest Segment records.
#' @importFrom DBI dbReadTable
#' @importFrom lubridate is.Date
#' @import dplyr
#' @import purrr
#' @import tibble
#' @param dimensionTables list of dimension tables in the staging format
#' @param factTables list of fact tables in the staging format
#' @param sampleFraction numeric value between 0 and 1 indicating percentage of records in the sample.  NULL to take all records.
#' @param seed random seed.  Set this to the same value in subsequent calls to generate the same random sample.
#' @return a list with all the dimensional database tables, as tibbles
#' @export
convertStagingTablesToDimensional <- function(dimensionTables, factTables, ageGroupFunctionFactory, categoryEnhancementFunctionFactory, sampleFraction=NULL, seed=12341234, writeProgressDetail=TRUE) {

  factTables <- map(factTables, function(factTableDf) {
    if ('SegmentActionTypeTypeID' %in% colnames(factTableDf)) {
      factTableDf <- factTableDf %>% select(-SegmentActionTypeTypeID)
    }
    # if ('NonNumericAge' %in% colnames(factTableDf)) {
    #   factTableDf <- factTableDf %>% select(-NonNumericAge)
    # }
    factTableDf
  })

  dimensionTables <- enhanceDimensionTables(dimensionTables, categoryEnhancementFunctionFactory)

  createOffenderAgeDim <- function(AgeOfOffenderMin, NonNumericAge) {
    if (is.null(NonNumericAge)) {
      NonNumericAge=rep('0', length(age))
    }
    case_when(
      AgeOfOffenderMin == 99 ~ '> 98 years',
      NonNumericAge == '00' ~ 'Unknown',
      is.na(AgeOfOffenderMin) ~ 'Blank',
      TRUE ~ as.character(AgeOfOffenderMin)
    )
  }

  createVictimAgeDim <- function(AgeNeonateIndicator, AgeFirstWeekIndicator, AgeFirstYearIndicator, AgeOfVictimMin) {
    case_when(
      AgeNeonateIndicator == 1 ~ '< 24 hours',
      AgeFirstWeekIndicator == 1 ~ '< 1 week',
      AgeFirstYearIndicator == 1 ~ '< 1 year',
      AgeOfVictimMin == 99 ~ '> 98 years',
      is.na(AgeOfVictimMin) ~ 'Blank',
      TRUE ~ as.character(AgeOfVictimMin)
    )
  }

  createVictimAgeDimSort <- function(AgeNeonateIndicator, AgeFirstWeekIndicator, AgeFirstYearIndicator, AgeOfVictimMin) {
    case_when(
      AgeNeonateIndicator == 1 ~ 1L,
      AgeFirstWeekIndicator == 1 ~ 2L,
      AgeFirstYearIndicator == 1 ~ 3L,
      AgeOfVictimMin == 99 ~ 110L,
      is.na(AgeOfVictimMin) ~ 111L,
      TRUE ~ as.integer(AgeOfVictimMin+4)
    )
  }

  createClearedIndicator <- function(ArresteeSegmentID, ClearedExceptionallyTypeID) {
    case_when(
      !is.na(ArresteeSegmentID) ~ 1L,
      ClearedExceptionallyTypeID < 6 ~ 1L,
      TRUE ~ 0L
    )
  }

  createClearanceType <- function(ClearedExceptionallyTypeID, ArresteeSegmentID) {
    case_when(
      ClearedExceptionallyTypeID < 6 ~ ClearedExceptionallyTypeID,
      is.na(ArresteeSegmentID) ~ 12L,
      TRUE ~ 11L
    )
  }

  createUcrAgeGroup <- function(age, AgeNeonateIndicator=NULL, AgeFirstWeekIndicator=NULL, AgeFirstYearIndicator=NULL, NonNumericAge=NULL) {
    if (is.null(AgeNeonateIndicator)) {
      AgeNeonateIndicator=rep(0, length(age))
    }
    if (is.null(AgeFirstWeekIndicator)) {
      AgeFirstWeekIndicator=rep(0, length(age))
    }
    if (is.null(AgeFirstYearIndicator)) {
      AgeFirstYearIndicator=rep(0, length(age))
    }
    if (is.null(NonNumericAge)) {
      NonNumericAge=rep('0', length(age))
    }
    case_when(
      AgeNeonateIndicator==1 ~ '< 10',
      AgeFirstWeekIndicator==1 ~ '< 10',
      AgeFirstYearIndicator==1 ~ '< 10',
      age <=  9 ~ '< 10',
      age <= 12 ~ '10-12',
      age <= 14 ~ '13-14',
      age %in% 15:24 ~ as.character(age),
      age <= 29 ~ '25-29',
      age <= 34 ~ '30-34',
      age <= 39 ~ '35-39',
      age <= 44 ~ '40-44',
      age <= 49 ~ '45-49',
      age <= 54 ~ '50-54',
      age <= 59 ~ '55-59',
      age <= 64 ~ '60-64',
      age >= 65 ~ '65+',
      NonNumericAge == '00' ~ 'Unknown',
      is.na(age) ~ 'Blank',
      TRUE ~ 'N/A'
    )
  }

  createAgeGroup <- ageGroupFunctionFactory$createAgeGroup

  createUcrAgeGroupSort <- function(age, AgeNeonateIndicator=NULL, AgeFirstWeekIndicator=NULL, AgeFirstYearIndicator=NULL, NonNumericAge=NULL) {
    if (is.null(AgeNeonateIndicator)) {
      AgeNeonateIndicator=rep(0, length(age))
    }
    if (is.null(AgeFirstWeekIndicator)) {
      AgeFirstWeekIndicator=rep(0, length(age))
    }
    if (is.null(AgeFirstYearIndicator)) {
      AgeFirstYearIndicator=rep(0, length(age))
    }
    if (is.null(NonNumericAge)) {
      NonNumericAge=rep('0', length(age))
    }
    case_when(
      AgeNeonateIndicator==1 ~ 1,
      AgeFirstWeekIndicator==1 ~ 1,
      AgeFirstYearIndicator==1 ~ 1,
      age <=  9 ~ 1,
      age <= 12 ~ 2,
      age <= 14 ~ 3,
      age %in% 15:24 ~ age-11,
      age <= 29 ~ 14,
      age <= 34 ~ 15,
      age <= 39 ~ 16,
      age <= 44 ~ 17,
      age <= 49 ~ 18,
      age <= 54 ~ 19,
      age <= 59 ~ 20,
      age <= 64 ~ 21,
      age >= 65 ~ 22,
      NonNumericAge == '00' ~ 99,
      is.na(age) ~ 98,
      TRUE ~ 100
    )
  }

  createAgeGroupSort <- ageGroupFunctionFactory$createAgeGroupSort

  createMotorVehiclesDim <- function(mv) {
    case_when(
      mv==0 ~ '0',
      mv==1 ~ '1',
      mv==2 ~ '2',
      TRUE ~ '3+'
    )
  }

  ret <- list()

  samp <- function(adf) {
    ret <- adf
    if (!is.null(sampleFraction)) {
      set.seed(seed)
      ret <- adf %>% sample_frac(sampleFraction)
    }
    ret
  }

  if (writeProgressDetail) writeLines('Creating Incident View')

  iv <- factTables$AdministrativeSegment %>%
    samp() %>%
    left_join(factTables$VictimSegment %>% select(-NonNumericAge), by='AdministrativeSegmentID') %>%
    left_join(factTables$VictimOffenseAssociation, by='VictimSegmentID')

  iv <- iv %>% filter(!is.na(OffenseSegmentID)) %>%
    left_join(factTables$OffenseSegment %>% select(-AdministrativeSegmentID), by='OffenseSegmentID') %>%
    bind_rows(
      iv %>% filter(is.na(OffenseSegmentID)) %>% select(-OffenseSegmentID) %>%
        left_join(factTables$OffenseSegment, by='AdministrativeSegmentID')
    )

  iv <- iv %>%
    left_join(factTables$OffenderSuspectedOfUsing, by='OffenseSegmentID') %>%
    left_join(factTables$TypeCriminalActivity, by='OffenseSegmentID') %>%
    left_join(factTables$BiasMotivation, by='OffenseSegmentID') %>%
    left_join(factTables$TypeOfWeaponForceInvolved, by='OffenseSegmentID') %>%
    left_join(factTables$TypeInjury, by='VictimSegmentID') %>%
    left_join(factTables$AggravatedAssaultHomicideCircumstances, by='VictimSegmentID') %>%
    left_join(factTables$VictimOffenderAssociation, by='VictimSegmentID')

  os <- factTables$OffenderSegment %>%
    rename(
      OffenderSexOfPersonTypeID=SexOfPersonTypeID,
      OffenderRaceOfPersonTypeID=RaceOfPersonTypeID,
      OffenderEthnicityOfPersonTypeID=EthnicityOfPersonTypeID
    )

  iv <- iv %>% filter(!is.na(OffenderSegmentID)) %>%
    left_join(os %>% select(-AdministrativeSegmentID), by='OffenderSegmentID') %>%
    bind_rows(
      iv %>% filter(is.na(OffenderSegmentID)) %>% select(-OffenderSegmentID) %>%
        left_join(os, by='AdministrativeSegmentID')
    )

  ret$FullIncidentView <- iv %>%
    left_join(factTables$PropertySegment, by='AdministrativeSegmentID') %>%
    left_join(factTables$PropertyType, by='PropertySegmentID') %>%
    left_join(factTables$SuspectedDrugType, by='PropertySegmentID') %>%
    left_join(factTables$ArresteeSegment %>% select(AdministrativeSegmentID, ArresteeSegmentID), by='AdministrativeSegmentID') %>%
    mutate_at(vars(ends_with('ID')), as.integer) %>%
    mutate(
      NumberOfPremisesEnteredDim=case_when(is.na(NumberOfPremisesEntered) ~ 'N/A', TRUE ~ as.character(NumberOfPremisesEntered)),
      StolenMotorVehiclesDim=createMotorVehiclesDim(NumberOfStolenMotorVehicles),
      RecoveredMotorVehiclesDim=createMotorVehiclesDim(NumberOfRecoveredMotorVehicles),
      VictimAgeDim=createVictimAgeDim(AgeNeonateIndicator, AgeFirstWeekIndicator, AgeFirstYearIndicator, AgeOfVictimMin),
      VictimAgeDimSort=createVictimAgeDimSort(AgeNeonateIndicator, AgeFirstWeekIndicator, AgeFirstYearIndicator, AgeOfVictimMin),
      VictimAgeGroup=createAgeGroup(AgeOfVictimMin, AgeNeonateIndicator, AgeFirstWeekIndicator, AgeFirstYearIndicator, NonNumericAge=NULL),
      VictimAgeGroupSort=createAgeGroupSort(AgeOfVictimMin, AgeNeonateIndicator, AgeFirstWeekIndicator, AgeFirstYearIndicator, NonNumericAge=NULL),
      VictimUcrAgeGroup=createUcrAgeGroup(AgeOfVictimMin, AgeNeonateIndicator, AgeFirstWeekIndicator, AgeFirstYearIndicator, NonNumericAge=NULL),
      VictimUcrAgeGroupSort=createUcrAgeGroupSort(AgeOfVictimMin, AgeNeonateIndicator, AgeFirstWeekIndicator, AgeFirstYearIndicator, NonNumericAge=NULL),
      OffenderAgeDim=createOffenderAgeDim(AgeOfOffenderMin, NonNumericAge=NonNumericAge),
      OffenderAgeGroup=createAgeGroup(AgeOfOffenderMin, NonNumericAge=NonNumericAge),
      OffenderAgeGroupSort=createAgeGroupSort(AgeOfOffenderMin, NonNumericAge=NonNumericAge),
      OffenderUcrAgeGroup=createUcrAgeGroup(AgeOfOffenderMin, NonNumericAge=NonNumericAge),
      OffenderUcrAgeGroupSort=createUcrAgeGroupSort(AgeOfOffenderMin, NonNumericAge=NonNumericAge),
      ClearedIndicator=createClearedIndicator(ArresteeSegmentID, ClearedExceptionallyTypeID),
      ClearanceType=createClearanceType(ClearedExceptionallyTypeID, ArresteeSegmentID),
      AggravatedAssaultHomicideCircumstancesTypeID=case_when(is.na(AggravatedAssaultHomicideCircumstancesTypeID) ~ 99998L, TRUE ~ AggravatedAssaultHomicideCircumstancesTypeID),
      OffenderSuspectedOfUsingTypeID=case_when(is.na(OffenderSuspectedOfUsingTypeID) ~ 9L, TRUE ~ OffenderSuspectedOfUsingTypeID),
      TypeOfCriminalActivityTypeID=case_when(is.na(TypeOfCriminalActivityTypeID) ~ 7L, TRUE ~ TypeOfCriminalActivityTypeID),
      BiasMotivationTypeID=case_when(is.na(BiasMotivationTypeID) ~ 99998L, TRUE ~ BiasMotivationTypeID),
      TypeOfWeaponForceInvolvedTypeID=case_when(is.na(TypeOfWeaponForceInvolvedTypeID) ~ 99999L, TRUE ~ TypeOfWeaponForceInvolvedTypeID),
      AutomaticWeaponIndicator=case_when(is.na(AutomaticWeaponIndicator) ~ 'N', TRUE ~ AutomaticWeaponIndicator),
      TypeInjuryTypeID=case_when(is.na(TypeInjuryTypeID) ~ 1L, TRUE ~ TypeInjuryTypeID),
      VictimOffenseAssociationID=case_when(is.na(VictimOffenseAssociationID) ~ -1L, TRUE ~ VictimOffenseAssociationID),
      VictimOffenderAssociationID=case_when(is.na(VictimOffenderAssociationID) ~ -1L, TRUE ~ VictimOffenderAssociationID),
      TypePropertyLossEtcTypeID=case_when(is.na(TypePropertyLossEtcTypeID) ~ 1L, TRUE ~ TypePropertyLossEtcTypeID),
      NumberOfStolenMotorVehicles=case_when(is.na(NumberOfStolenMotorVehicles) ~ 0L, TRUE ~ NumberOfStolenMotorVehicles),
      NumberOfRecoveredMotorVehicles=case_when(is.na(NumberOfRecoveredMotorVehicles) ~ 0L, TRUE ~ NumberOfRecoveredMotorVehicles),
      PropertyDescriptionTypeID=case_when(is.na(PropertyDescriptionTypeID) ~ 99998L, TRUE ~ PropertyDescriptionTypeID),
      ValueOfProperty=case_when(is.na(ValueOfProperty) ~ 0, TRUE ~ ValueOfProperty),
      RecoveredDateID=case_when(is.na(RecoveredDateID) ~ 99998L, TRUE ~ RecoveredDateID),
      SuspectedDrugTypeTypeID=case_when(is.na(SuspectedDrugTypeTypeID) ~ 99998L, TRUE ~ SuspectedDrugTypeTypeID),
      TypeDrugMeasurementTypeID=case_when(is.na(TypeDrugMeasurementTypeID) ~ 99998L, TRUE ~ TypeDrugMeasurementTypeID),
      EstimatedDrugQuantity=case_when(is.na(EstimatedDrugQuantity) ~ 0, TRUE ~ EstimatedDrugQuantity),
      IncidentHour=as.integer(IncidentHour),
      CompletionStatusIndicator=case_when(
        is.na(OffenseAttemptedCompleted) | OffenseAttemptedCompleted=='' ~ 99998L,
        OffenseAttemptedCompleted=='C' ~ 1L,
        TRUE ~ 0L)
    ) %>%
    left_join(factTables$OffenseSegment %>% select(AdministrativeSegmentID, OffenseSegmentID) %>% distinct() %>%
                group_by(AdministrativeSegmentID) %>% summarize(OffensesPerIncident=n()), by='AdministrativeSegmentID') %>%
    mutate(OffensesPerIncident=case_when(is.na(OffensesPerIncident) ~ 0L, TRUE ~ OffensesPerIncident),
           OffensesPerIncidentDim=case_when(
             OffensesPerIncident <= 4 ~ as.character(OffensesPerIncident),
             TRUE ~ '5+'
           )) %>%
    left_join(factTables$VictimSegment %>% select(AdministrativeSegmentID, VictimSegmentID) %>% distinct() %>%
                group_by(AdministrativeSegmentID) %>% summarize(VictimsPerIncident=n()), by='AdministrativeSegmentID') %>%
    mutate(VictimsPerIncident=case_when(is.na(VictimsPerIncident) ~ 0L, TRUE ~ VictimsPerIncident),
           VictimsPerIncidentDim=case_when(
             VictimsPerIncident <= 4 ~ as.character(VictimsPerIncident),
             TRUE ~ '5+'
           )) %>%
    left_join(factTables$OffenderSegment %>% select(AdministrativeSegmentID, OffenderSegmentID) %>% distinct() %>%
                group_by(AdministrativeSegmentID) %>% summarize(OffendersPerIncident=n()), by='AdministrativeSegmentID') %>%
    mutate(OffendersPerIncident=case_when(is.na(OffendersPerIncident) ~ 0L, TRUE ~ OffendersPerIncident),
           OffendersPerIncidentDim=case_when(
             OffendersPerIncident <= 4 ~ as.character(OffendersPerIncident),
             TRUE ~ '5+'
           )) %>%
    left_join(factTables$ArresteeSegment %>% select(AdministrativeSegmentID, ArresteeSegmentID) %>% distinct() %>%
                group_by(AdministrativeSegmentID) %>% summarize(ArresteesPerIncident=n()), by='AdministrativeSegmentID') %>%
    mutate(ArresteesPerIncident=case_when(is.na(ArresteesPerIncident) ~ 0L, TRUE ~ ArresteesPerIncident),
           ArresteesPerIncidentDim=case_when(
             ArresteesPerIncident <= 4 ~ as.character(ArresteesPerIncident),
             TRUE ~ '5+'
           )) %>%
    left_join(dimensionTables$State %>% select(StateCode, StateID), by='StateCode') %>%
    select(
      AdministrativeSegmentID,
      StateID,
      IncidentHour,
      ClearedExceptionallyTypeID,
      IncidentDate,
      IncidentDateID,
      AgencyID,
      ORI,
      CargoTheftIndicatorTypeID,
      ReportDateIndicator,
      OffenseAttemptedCompleted,
      LocationTypeTypeID,
      NumberOfPremisesEntered,
      MethodOfEntryTypeID,
      UCROffenseCodeTypeID,
      TypeOfVictimTypeID,
      OfficerActivityCircumstanceTypeID,
      OfficerAssignmentTypeTypeID,
      SexOfPersonTypeID,
      RaceOfPersonTypeID,
      EthnicityOfPersonTypeID,
      ResidentStatusOfPersonTypeID,
      AdditionalJustifiableHomicideCircumstancesTypeID,
      AgeNumVictim,
      AgeOfVictimMin,
      AgeOfVictimMax,
      AgeNeonateIndicator,
      AgeFirstWeekIndicator,
      AgeFirstYearIndicator,
      AgeNumOffender,
      AgeOfOffenderMin,
      AgeOfOffenderMax,
      OffenderSexOfPersonTypeID,
      OffenderRaceOfPersonTypeID,
      OffenderEthnicityOfPersonTypeID,
      VictimOffenderRelationshipTypeID,
      RecoveredDate,
      VictimSegmentID,
      OffenseSegmentID,
      OffenderSegmentID,
      PropertySegmentID,
      PropertyTypeID,
      AggravatedAssaultHomicideCircumstancesTypeID,
      OffenderSuspectedOfUsingTypeID,
      TypeOfCriminalActivityTypeID,
      BiasMotivationTypeID,
      TypeOfWeaponForceInvolvedTypeID,
      AutomaticWeaponIndicator,
      TypeInjuryTypeID,
      VictimOffenseAssociationID,
      VictimOffenderAssociationID,
      TypePropertyLossEtcTypeID,
      NumberOfStolenMotorVehicles,
      NumberOfRecoveredMotorVehicles,
      PropertyDescriptionTypeID,
      ValueOfProperty,
      RecoveredDateID,
      SuspectedDrugTypeTypeID,
      TypeDrugMeasurementTypeID,
      EstimatedDrugQuantity,
      ArresteeSegmentID,
      OffensesPerIncident,
      OffensesPerIncidentDim,
      OffendersPerIncident,
      OffendersPerIncidentDim,
      VictimsPerIncident,
      VictimsPerIncidentDim,
      ArresteesPerIncident,
      ArresteesPerIncidentDim,
      NumberOfPremisesEnteredDim,
      VictimAgeDim,
      VictimAgeDimSort,
      VictimAgeGroup,
      VictimAgeGroupSort,
      VictimUcrAgeGroup,
      VictimUcrAgeGroupSort,
      OffenderAgeDim,
      OffenderAgeGroup,
      OffenderAgeGroupSort,
      OffenderUcrAgeGroup,
      OffenderUcrAgeGroupSort,
      ClearedIndicator,
      ClearanceType,
      RecoveredMotorVehiclesDim,
      StolenMotorVehiclesDim,
      CompletionStatusIndicator
    )

  if (writeProgressDetail) writeLines('Creating Victim-Offense View')
  ret$FullVictimOffenseView <- factTables$AdministrativeSegment %>%
    samp() %>%
    left_join(factTables$VictimSegment, by='AdministrativeSegmentID') %>%
    left_join(factTables$VictimOffenseAssociation, by=c('VictimSegmentID')) %>%
    left_join(factTables$OffenseSegment %>% select(-AdministrativeSegmentID), by='OffenseSegmentID') %>%
    left_join(factTables$OffenderSuspectedOfUsing, by='OffenseSegmentID') %>%
    left_join(factTables$TypeCriminalActivity, by='OffenseSegmentID') %>%
    left_join(factTables$BiasMotivation, by='OffenseSegmentID') %>%
    left_join(factTables$TypeOfWeaponForceInvolved, by='OffenseSegmentID') %>%
    left_join(factTables$TypeInjury, by='VictimSegmentID') %>%
    left_join(factTables$AggravatedAssaultHomicideCircumstances, by='VictimSegmentID') %>%
    left_join(factTables$ArresteeSegment %>% select(AdministrativeSegmentID, ArresteeSegmentID), by='AdministrativeSegmentID') %>%
    mutate_at(vars(ends_with('ID')), as.integer) %>%
    mutate(
      NumberOfPremisesEnteredDim=case_when(is.na(NumberOfPremisesEntered) ~ 'N/A', TRUE ~ as.character(NumberOfPremisesEntered)),
      VictimAgeDim=createVictimAgeDim(AgeNeonateIndicator, AgeFirstWeekIndicator, AgeFirstYearIndicator, AgeOfVictimMin),
      VictimAgeDimSort=createVictimAgeDimSort(AgeNeonateIndicator, AgeFirstWeekIndicator, AgeFirstYearIndicator, AgeOfVictimMin),
      VictimAgeGroup=createAgeGroup(AgeOfVictimMin, AgeNeonateIndicator, AgeFirstWeekIndicator, AgeFirstYearIndicator, NonNumericAge=NULL),
      VictimAgeGroupSort=createAgeGroupSort(AgeOfVictimMin, AgeNeonateIndicator, AgeFirstWeekIndicator, AgeFirstYearIndicator, NonNumericAge=NULL),
      VictimUcrAgeGroup=createUcrAgeGroup(AgeOfVictimMin, AgeNeonateIndicator, AgeFirstWeekIndicator, AgeFirstYearIndicator, NonNumericAge=NULL),
      VictimUcrAgeGroupSort=createUcrAgeGroupSort(AgeOfVictimMin, AgeNeonateIndicator, AgeFirstWeekIndicator, AgeFirstYearIndicator, NonNumericAge=NULL),
      ClearedIndicator=createClearedIndicator(ArresteeSegmentID, ClearedExceptionallyTypeID),
      ClearanceType=createClearanceType(ClearedExceptionallyTypeID, ArresteeSegmentID),
      AggravatedAssaultHomicideCircumstancesTypeID=case_when(is.na(AggravatedAssaultHomicideCircumstancesTypeID) ~ 99998L, TRUE ~ AggravatedAssaultHomicideCircumstancesTypeID),
      OffenderSuspectedOfUsingTypeID=case_when(is.na(OffenderSuspectedOfUsingTypeID) ~ 9L, TRUE ~ OffenderSuspectedOfUsingTypeID),
      TypeOfCriminalActivityTypeID=case_when(is.na(TypeOfCriminalActivityTypeID) ~ 7L, TRUE ~ TypeOfCriminalActivityTypeID),
      BiasMotivationTypeID=case_when(is.na(BiasMotivationTypeID) ~ 99998L, TRUE ~ BiasMotivationTypeID),
      TypeOfWeaponForceInvolvedTypeID=case_when(is.na(TypeOfWeaponForceInvolvedTypeID) ~ 99999L, TRUE ~ TypeOfWeaponForceInvolvedTypeID),
      AutomaticWeaponIndicator=case_when(is.na(AutomaticWeaponIndicator) ~ 'N', TRUE ~ AutomaticWeaponIndicator),
      TypeInjuryTypeID=case_when(is.na(TypeInjuryTypeID) ~ 1L, TRUE ~ TypeInjuryTypeID),
      VictimOffenseAssociationID=case_when(is.na(VictimOffenseAssociationID) ~ -1L, TRUE ~ VictimOffenseAssociationID),
      IncidentHour=as.integer(IncidentHour),
      CompletionStatusIndicator=case_when(
        is.na(OffenseAttemptedCompleted) | OffenseAttemptedCompleted=='' ~ 99998L,
        OffenseAttemptedCompleted=='C' ~ 1L,
        TRUE ~ 0L)
    ) %>%
    left_join(dimensionTables$State %>% select(StateCode, StateID), by='StateCode') %>%
    select(
      AdministrativeSegmentID,
      StateID,
      IncidentHour,
      ClearedExceptionallyTypeID,
      IncidentDate,
      IncidentDateID,
      AgencyID,
      ORI,
      OffenseAttemptedCompleted,
      LocationTypeTypeID,
      NumberOfPremisesEntered,
      NumberOfPremisesEnteredDim,
      MethodOfEntryTypeID,
      UCROffenseCodeTypeID,
      OffenseSegmentID,
      TypeOfVictimTypeID,
      OfficerActivityCircumstanceTypeID,
      OfficerAssignmentTypeTypeID,
      SexOfPersonTypeID,
      RaceOfPersonTypeID,
      EthnicityOfPersonTypeID,
      ResidentStatusOfPersonTypeID,
      AdditionalJustifiableHomicideCircumstancesTypeID,
      AgeNumVictim,
      AgeOfVictimMin,
      AgeOfVictimMax,
      AgeNeonateIndicator,
      AgeFirstWeekIndicator,
      AgeFirstYearIndicator,
      VictimAgeDim,
      VictimAgeDimSort,
      VictimAgeGroup,
      VictimAgeGroupSort,
      VictimUcrAgeGroup,
      VictimUcrAgeGroupSort,
      VictimSegmentID,
      AggravatedAssaultHomicideCircumstancesTypeID,
      OffenderSuspectedOfUsingTypeID,
      TypeOfCriminalActivityTypeID,
      BiasMotivationTypeID,
      TypeOfWeaponForceInvolvedTypeID,
      AutomaticWeaponIndicator,
      TypeInjuryTypeID,
      VictimOffenseAssociationID,
      ClearedIndicator,
      ClearanceType,
      CompletionStatusIndicator
    )

  if (writeProgressDetail) writeLines('Creating Victim-Offender View')
  ret$FullVictimOffenderView <- factTables$AdministrativeSegment %>%
    samp() %>%
    left_join(factTables$VictimSegment %>%
                select(-NonNumericAge) %>%
                rename(
                  VictimSexOfPersonTypeID=SexOfPersonTypeID,
                  VictimRaceOfPersonTypeID=RaceOfPersonTypeID,
                  VictimEthnicityOfPersonTypeID=EthnicityOfPersonTypeID,
                  VictimResidentStatusOfPersonTypeID=ResidentStatusOfPersonTypeID
                ), by='AdministrativeSegmentID') %>%
    left_join(factTables$VictimOffenderAssociation, by=c('VictimSegmentID')) %>%
    left_join(factTables$OffenderSegment %>% select(-AdministrativeSegmentID) %>%
                rename(
                  OffenderSexOfPersonTypeID=SexOfPersonTypeID,
                  OffenderRaceOfPersonTypeID=RaceOfPersonTypeID,
                  OffenderEthnicityOfPersonTypeID=EthnicityOfPersonTypeID,
                ), by='OffenderSegmentID') %>%
    left_join(factTables$TypeInjury, by='VictimSegmentID') %>%
    left_join(factTables$AggravatedAssaultHomicideCircumstances, by='VictimSegmentID') %>%
    left_join(factTables$ArresteeSegment %>% select(AdministrativeSegmentID, ArresteeSegmentID), by='AdministrativeSegmentID') %>%
    mutate_at(vars(ends_with('ID')), as.integer) %>%
    mutate(
      VictimAgeDim=createVictimAgeDim(AgeNeonateIndicator, AgeFirstWeekIndicator, AgeFirstYearIndicator, AgeOfVictimMin),
      VictimAgeDimSort=createVictimAgeDimSort(AgeNeonateIndicator, AgeFirstWeekIndicator, AgeFirstYearIndicator, AgeOfVictimMin),
      VictimAgeGroup=createAgeGroup(AgeOfVictimMin, AgeNeonateIndicator, AgeFirstWeekIndicator, AgeFirstYearIndicator, NonNumericAge=NULL),
      VictimAgeGroupSort=createAgeGroupSort(AgeOfVictimMin, AgeNeonateIndicator, AgeFirstWeekIndicator, AgeFirstYearIndicator, NonNumericAge=NULL),
      VictimUcrAgeGroup=createUcrAgeGroup(AgeOfVictimMin, AgeNeonateIndicator, AgeFirstWeekIndicator, AgeFirstYearIndicator, NonNumericAge=NULL),
      VictimUcrAgeGroupSort=createUcrAgeGroupSort(AgeOfVictimMin, AgeNeonateIndicator, AgeFirstWeekIndicator, AgeFirstYearIndicator, NonNumericAge=NULL),
      OffenderAgeDim=createOffenderAgeDim(AgeOfOffenderMin, NonNumericAge=NonNumericAge),
      OffenderAgeGroup=createAgeGroup(AgeOfOffenderMin, NonNumericAge=NonNumericAge),
      OffenderAgeGroupSort=createAgeGroupSort(AgeOfOffenderMin, NonNumericAge=NonNumericAge),
      OffenderUcrAgeGroup=createUcrAgeGroup(AgeOfOffenderMin, NonNumericAge=NonNumericAge),
      OffenderUcrAgeGroupSort=createUcrAgeGroupSort(AgeOfOffenderMin, NonNumericAge=NonNumericAge),
      ClearedIndicator=createClearedIndicator(ArresteeSegmentID, ClearedExceptionallyTypeID),
      ClearanceType=createClearanceType(ClearedExceptionallyTypeID, ArresteeSegmentID),
      AggravatedAssaultHomicideCircumstancesTypeID=case_when(is.na(AggravatedAssaultHomicideCircumstancesTypeID) ~ 99998L, TRUE ~ AggravatedAssaultHomicideCircumstancesTypeID),
      TypeInjuryTypeID=case_when(is.na(TypeInjuryTypeID) ~ 1L, TRUE ~ TypeInjuryTypeID),
      VictimOffenderAssociationID=case_when(is.na(VictimOffenderAssociationID) ~ -1L, TRUE ~ VictimOffenderAssociationID),
      IncidentHour=as.integer(IncidentHour)
    ) %>%
    left_join(dimensionTables$State %>% select(StateCode, StateID), by='StateCode') %>%
    select(
      AdministrativeSegmentID,
      StateID,
      IncidentHour,
      ClearedExceptionallyTypeID,
      IncidentDate,
      IncidentDateID,
      AgencyID,
      ORI,
      OffenderSequenceNumber,
      AgeNumOffender,
      AgeOfOffenderMin,
      AgeOfOffenderMax,
      OffenderAgeDim,
      OffenderAgeGroup,
      OffenderAgeGroupSort,
      OffenderUcrAgeGroup,
      OffenderUcrAgeGroupSort,
      OffenderSexOfPersonTypeID,
      OffenderRaceOfPersonTypeID,
      OffenderEthnicityOfPersonTypeID,
      OffenderSegmentID,
      VictimSequenceNumber,
      TypeOfVictimTypeID,
      OfficerActivityCircumstanceTypeID,
      OfficerAssignmentTypeTypeID,
      VictimSexOfPersonTypeID,
      VictimRaceOfPersonTypeID,
      VictimEthnicityOfPersonTypeID,
      VictimResidentStatusOfPersonTypeID,
      AdditionalJustifiableHomicideCircumstancesTypeID,
      AgeNumVictim,
      AgeOfVictimMin,
      AgeOfVictimMax,
      AgeNeonateIndicator,
      AgeFirstWeekIndicator,
      AgeFirstYearIndicator,
      VictimAgeDim,
      VictimAgeDimSort,
      VictimAgeGroup,
      VictimAgeGroupSort,
      VictimUcrAgeGroup,
      VictimUcrAgeGroupSort,
      VictimSegmentID,
      VictimOffenderRelationshipTypeID,
      AggravatedAssaultHomicideCircumstancesTypeID,
      TypeInjuryTypeID,
      VictimOffenderAssociationID,
      ClearedIndicator,
      ClearanceType
    )

  if (writeProgressDetail) writeLines('Creating Group A Arrest View')
  ret$FullGroupAArrestView <- factTables$AdministrativeSegment %>%
    samp() %>%
    inner_join(factTables$ArresteeSegment, by='AdministrativeSegmentID') %>%
    left_join(factTables$ArresteeSegmentWasArmedWith, by='ArresteeSegmentID') %>%
    mutate_at(vars(ends_with('ID')), as.integer) %>%
    mutate(
      ArresteeAgeDim=createOffenderAgeDim(AgeOfArresteeMin, NonNumericAge),
      ArresteeAgeGroup=createAgeGroup(AgeOfArresteeMin, NonNumericAge),
      ArresteeAgeGroupSort=createAgeGroupSort(AgeOfArresteeMin, NonNumericAge),
      ArresteeUcrAgeGroup=createUcrAgeGroup(AgeOfArresteeMin, NonNumericAge),
      ArresteeUcrAgeGroupSort=createUcrAgeGroupSort(AgeOfArresteeMin, NonNumericAge),
      ArresteeWasArmedWithTypeID=case_when(is.na(ArresteeWasArmedWithTypeID) ~ -1L, TRUE ~ ArresteeWasArmedWithTypeID),
      AutomaticWeaponIndicator=case_when(is.na(AutomaticWeaponIndicator) ~ 'N', TRUE ~ AutomaticWeaponIndicator),
      ArrestDateID=case_when(is.na(ArrestDateID) ~ 99998L, TRUE ~ ArrestDateID),
      IncidentHour=as.integer(IncidentHour)
    ) %>%
    left_join(dimensionTables$State %>% select(StateCode, StateID), by='StateCode') %>%
    select(
      AdministrativeSegmentID,
      StateID,
      IncidentHour,
      ClearedExceptionallyTypeID,
      IncidentDate,
      IncidentDateID,
      AgencyID,
      ORI,
      ArresteeSequenceNumber,
      ArrestTransactionNumber,
      ArrestDate,
      TypeOfArrestTypeID,
      MultipleArresteeSegmentsIndicatorTypeID,
      AgeNumArrestee,
      AgeOfArresteeMin,
      AgeOfArresteeMax,
      ArresteeAgeDim,
      ArresteeAgeGroup,
      ArresteeAgeGroupSort,
      ArresteeUcrAgeGroup,
      ArresteeUcrAgeGroupSort,
      SexOfPersonTypeID,
      RaceOfPersonTypeID,
      EthnicityOfPersonTypeID,
      ResidentStatusOfPersonTypeID,
      DispositionOfArresteeUnder18TypeID,
      UCROffenseCodeTypeID,
      ArrestDateID,
      ArresteeSegmentID,
      ArresteeSegmentWasArmedWithID,
      ArresteeWasArmedWithTypeID,
      AutomaticWeaponIndicator
    )

  if (writeProgressDetail) writeLines('Creating Property View')
  ret$FullPropertyView <- factTables$AdministrativeSegment %>%
    samp() %>%
    inner_join(factTables$PropertySegment, by='AdministrativeSegmentID') %>%
    inner_join(factTables$PropertyType, by='PropertySegmentID') %>%
    left_join(factTables$SuspectedDrugType, by='PropertySegmentID') %>%
    left_join(factTables$ArresteeSegment, by='AdministrativeSegmentID') %>%
    mutate_at(vars(ends_with('ID')), as.integer) %>%
    mutate(
      StolenMotorVehiclesDim=createMotorVehiclesDim(NumberOfStolenMotorVehicles),
      RecoveredMotorVehiclesDim=createMotorVehiclesDim(NumberOfRecoveredMotorVehicles),
      ClearedIndicator=createClearedIndicator(ArresteeSegmentID, ClearedExceptionallyTypeID),
      ClearanceType=createClearanceType(ClearedExceptionallyTypeID, ArresteeSegmentID),
      TypePropertyLossEtcTypeID=case_when(is.na(TypePropertyLossEtcTypeID) ~ 1L, TRUE ~ TypePropertyLossEtcTypeID),
      NumberOfStolenMotorVehicles=case_when(is.na(NumberOfStolenMotorVehicles) ~ 0L, TRUE ~ NumberOfStolenMotorVehicles),
      NumberOfRecoveredMotorVehicles=case_when(is.na(NumberOfRecoveredMotorVehicles) ~ 0L, TRUE ~ NumberOfRecoveredMotorVehicles),
      PropertyDescriptionTypeID=case_when(is.na(PropertyDescriptionTypeID) ~ 99998L, TRUE ~ PropertyDescriptionTypeID),
      ValueOfProperty=case_when(is.na(ValueOfProperty) ~ 0, TRUE ~ ValueOfProperty),
      RecoveredDateID=case_when(is.na(RecoveredDateID) ~ 99998L, TRUE ~ RecoveredDateID),
      SuspectedDrugTypeTypeID=case_when(is.na(SuspectedDrugTypeTypeID) ~ 99998L, TRUE ~ SuspectedDrugTypeTypeID),
      TypeDrugMeasurementTypeID=case_when(is.na(TypeDrugMeasurementTypeID) ~ 99998L, TRUE ~ TypeDrugMeasurementTypeID),
      EstimatedDrugQuantity=case_when(is.na(EstimatedDrugQuantity) ~ 0, TRUE ~ EstimatedDrugQuantity),
      IncidentHour=as.integer(IncidentHour)
    ) %>%
    left_join(dimensionTables$State %>% select(StateCode, StateID), by='StateCode') %>%
    select(
      AdministrativeSegmentID,
      StateID,
      IncidentHour,
      ClearedExceptionallyTypeID,
      IncidentDate,
      IncidentDateID,
      AgencyID,
      ORI,
      TypePropertyLossEtcTypeID,
      NumberOfStolenMotorVehicles,
      NumberOfRecoveredMotorVehicles,
      PropertySegmentID,
      PropertyDescriptionTypeID,
      ValueOfProperty,
      RecoveredDate,
      RecoveredDateID,
      SuspectedDrugTypeID,
      PropertyTypeID,
      SuspectedDrugTypeTypeID,
      TypeDrugMeasurementTypeID,
      EstimatedDrugQuantity,
      ClearedIndicator,
      ClearanceType,
      StolenMotorVehiclesDim,
      RecoveredMotorVehiclesDim
    )

  if (writeProgressDetail) writeLines('Creating Group B Arrest View')
  ret$FullGroupBArrestView <- factTables$ArrestReportSegment %>%
    samp() %>%
    left_join(factTables$ArrestReportSegmentWasArmedWith, by='ArrestReportSegmentID') %>%
    mutate_at(vars(ends_with('ID')), as.integer) %>%
    mutate(
      ArresteeAgeDim=createOffenderAgeDim(AgeOfArresteeMin, NonNumericAge),
      ArresteeAgeGroup=createAgeGroup(AgeOfArresteeMin, NonNumericAge),
      ArresteeAgeGroupSort=createAgeGroupSort(AgeOfArresteeMin, NonNumericAge),
      ArresteeUcrAgeGroup=createUcrAgeGroup(AgeOfArresteeMin, NonNumericAge),
      ArresteeUcrAgeGroupSort=createUcrAgeGroupSort(AgeOfArresteeMin, NonNumericAge),
      ArresteeWasArmedWithTypeID=case_when(is.na(ArresteeWasArmedWithTypeID) ~ -1L, TRUE ~ ArresteeWasArmedWithTypeID),
      AutomaticWeaponIndicator=case_when(is.na(AutomaticWeaponIndicator) ~ 'N', TRUE ~ AutomaticWeaponIndicator),
      ArrestDateID=case_when(is.na(ArrestDateID) ~ 99998L, TRUE ~ ArrestDateID)
    ) %>%
    left_join(dimensionTables$State %>% select(StateCode, StateID), by='StateCode') %>%
    select(
      ArrestDate,
      TypeOfArrestTypeID,
      UCROffenseCodeTypeID,
      AgeNumArrestee,
      AgeOfArresteeMin,
      AgeOfArresteeMax,
      ArresteeAgeDim,
      ArresteeAgeGroup,
      ArresteeAgeGroupSort,
      ArresteeUcrAgeGroup,
      ArresteeUcrAgeGroupSort,
      SexOfPersonTypeID,
      RaceOfPersonTypeID,
      EthnicityOfPersonTypeID,
      ResidentStatusOfPersonTypeID,
      DispositionOfArresteeUnder18TypeID,
      ArrestReportSegmentID,
      ArrestDateID,
      AgencyID,
      ORI,
      ArrestReportSegmentWasArmedWithID,
      ArresteeWasArmedWithTypeID,
      AutomaticWeaponIndicator,
      StateID
    )

  ret <- map(ret, function(fdf) {
    attr(fdf, 'type') <- 'FT'
    fdf
  }) %>%
    c(dimensionTables)

  ret

}

#' Load the NIBRS dimensional database from a single list of staging tables
#'
#' Load the NIBRS dimensional database from a single list of staging tables, optionally taking a random sample of Administrative Segment
#' and Group B Arrest Segment records.
#' @importFrom DBI dbReadTable
#' @importFrom lubridate is.Date
#' @import dplyr
#' @import purrr
#' @import tibble
#' @param stagingTableList list of tables in the staging format
#' @param dimensionalConn connection to the dimensional database
#' @param sampleFraction numeric value between 0 and 1 indicating percentage of records in the sample.  NULL to take all records.
#' @param seed random seed.  Set this to the same value in subsequent calls to generate the same random sample.
#' @return a list with all the dimensional database tables, as tibbles
#' @export
loadDimensionalFromObjectList <- function(
  stagingTableList,
  dimensionalConn=DBI::dbConnect(RMariaDB::MariaDB(), host="localhost", dbname="search_nibrs_dimensional", username="root"),
  ageGroupFunctionFactory, categoryEnhancementFunctionFactory, sampleFraction=NULL, seed=12341234) {

  loadDimensionalFromObjectLists(stagingTableList[DIMENSION_TABLES], stagingTableList[FACT_TABLES], dimensionalConn, ageGroupFunctionFactory, categoryEnhancementFunctionFactory, sampleFraction, seed)

}


#' Load the NIBRS dimensional database from lists of staging dimension and fact tables
#'
#' Load the NIBRS dimensional database from lists of staging dimension and fact tables, optionally taking a random sample of Administrative Segment
#' and Group B Arrest Segment records.
#' @importFrom DBI dbReadTable
#' @importFrom lubridate is.Date
#' @import dplyr
#' @import purrr
#' @import tibble
#' @param dimensionTables list of dimension tables in the staging format
#' @param factTables list of fact tables in the staging format
#' @param dimensionalConn connection to the dimensional database
#' @param sampleFraction numeric value between 0 and 1 indicating percentage of records in the sample.  NULL to take all records.
#' @param seed random seed.  Set this to the same value in subsequent calls to generate the same random sample.
#' @return a list with all the dimensional database tables, as tibbles
#' @export
loadDimensionalFromObjectLists <- function(
  dimensionTables, factTables,
  dimensionalConn=DBI::dbConnect(RMariaDB::MariaDB(), host="localhost", dbname="search_nibrs_dimensional", username="root"),
  ageGroupFunctionFactory, categoryEnhancementFunctionFactory, sampleFraction=NULL, seed=12341234) {

  ret <- convertStagingTablesToDimensional(dimensionTables, factTables, ageGroupFunctionFactory, categoryEnhancementFunctionFactory, sampleFraction, seed)

  iwalk(ret, function(ddf, tableName) {
    writeLines(paste0('Writing dimensional db table ', tableName))
    writeDataFrameToDatabase(dimensionalConn, ddf, tableName, viaBulk=TRUE, localBulk=FALSE, append=FALSE)
    writeLines(paste0('Creating indexes for table ', tableName))
    if (attr(ddf, 'type') == 'CT') {
      # note: "create index if not exists" only works on MariaDB...
      writeLines(paste0('Creating PK for code table ', tableName))
	  createIndexIfNotExists(dimensionalConn, tableName, paste0(tableName,'_pk'), paste0(tableName, 'ID'))
    } else {
      ddf %>% head(0) %>% select_if(~!(is.double(.x) | is.Date(.x))) %>% colnames() %>% head(63) %>% walk(function(cnm) {
        # head(63) because MariaDB only supports creating indexes for 64 columns
        writeLines(paste0('Creating index for FK ', cnm))
		createIndexIfNotExists(dimensionalConn, tableName, paste0('idx_', cnm), cnm)
      })
    }
  })

  createIndexIfNotExists(dimensionalConn, 'Agency', 'idx_AgencyTypeID', 'AgencyTypeID')

  ret

}

createIndexIfNotExists<- function(dimensionalConn, tableName, indexName, columnName){
	isIndexExists = dbGetQuery(dimensionalConn, paste0("select count(*) > 0 from information_schema.statistics where table_name = '",  tableName, "' and index_name='", indexName, "' and table_schema = database()"))
	writeLines(paste0('isIndexExists: ', isIndexExists == 1))
	if (isIndexExists != 1){
		createIndexStatement = paste0('create index ', indexName, ' on ', tableName, '(', columnName, ');')
		writeLines(createIndexStatement)
		dbExecute(dimensionalConn, createIndexStatement)
	}
}
#' @import dplyr
#' @import tibble
#' @import stringr
enhanceDimensionTables <- function(dimensionTables, categoryEnhancementFunctionFactory) {

  ret <- dimensionTables

  ret$LocationTypeType <- ret$LocationTypeType %>% categoryEnhancementFunctionFactory$addLocationTypeCategories()
  ret$VictimOffenderRelationshipType <- ret$VictimOffenderRelationshipType %>% categoryEnhancementFunctionFactory$addVictimOffenderRelationshipCategories()
  ret$PropertyDescriptionType <- ret$PropertyDescriptionType %>% categoryEnhancementFunctionFactory$addPropertyDescriptionCategories()

  ret$CompletionStatusType <- tribble(
    ~CompletionStatusTypeID, ~CompletionStatusDescription,
    1, 'Completed',
    0, 'Attempted'
  ) %>% mutate(CompletionStatusTypeID=as.integer(CompletionStatusTypeID))

  ret$ClearanceType <- tribble(
    ~ClearanceTypeID, ~ClearanceTypeDescription, ~ClearanceTypeCategory, ~ClearanceTypeCategorySort, ~ClearanceTypeYN,
    1, 'Death of Offender', 'Cleared Exceptionally', 1, 'Cleared',
    2, 'Prosecution Declined', 'Cleared Exceptionally', 1, 'Cleared',
    3, 'In Custody of Other Jurisdiction', 'Cleared Exceptionally', 1, 'Cleared',
    4, 'Victim Refused to Cooperate', 'Cleared Exceptionally', 1, 'Cleared',
    5, 'Juvenile/No Custody', 'Cleared Exceptionally', 1, 'Cleared',
    11, 'Cleared by Arrest', 'Cleared by Arrest', 2, 'Cleared',
    12, 'Not Cleared', 'Not Cleared', 3, 'Not Cleared'
  ) %>% mutate(ClearanceTypeID=as.integer(ClearanceTypeID))

  ret$HourType <- tibble(
    HourTypeID=0:23
  ) %>% mutate(
    HourTypeDescription=paste0(str_pad(HourTypeID, 2, 'left', '0'), ':00-', str_pad(HourTypeID+1, 2, 'left', '0'), ':00'),
    HourTypeDetailCategory=case_when(
      HourTypeID %in% 0:5 ~ 'Early Morning',
      HourTypeID %in% 6:11 ~ 'Morning',
      HourTypeID %in% 12:17 ~ 'Afternoon',
      TRUE ~ 'Evening'
    ),
    HourTypeDetailCategorySort=case_when(
      HourTypeID %in% 0:5 ~ 1L,
      HourTypeID %in% 6:11 ~ 2L,
      HourTypeID %in% 12:17 ~ 3L,
      TRUE ~ 4L
    ),
    HourTypeCategory=case_when(
      HourTypeID %in% 0:11 ~ 'AM',
      TRUE ~ 'PM'
    ),
    HourTypeCategorySort=case_when(
      HourTypeID %in% 0:11 ~ 1L,
      TRUE ~ 2L
    ),
  ) %>% bind_rows(
    tribble(
      ~HourTypeID, ~HourTypeDescription, ~HourTypeDetailCategory, ~HourTypeDetailCategorySort, ~HourTypeCategory, ~HourTypeCategorySort,
      99, 'Unknown', 'Unknown', 100, 'Unknown', 100
    )
  ) %>% mutate(HourTypeID=as.integer(HourTypeID))

  map2(ret, names(ret), function(ddf, tableName) {
    ret <- ddf
    if ('FBICode' %in% colnames(ddf)) {
      writeLines(paste0('Renaming FBI Code/Description columns to NIBRS Code/Description in table ', tableName))
      ret <- rename(ddf, NIBRSCode=FBICode, NIBRSDescription=FBIDescription)
    }
    attr(ret, 'type') <- 'CT'
    ret
  })

}
