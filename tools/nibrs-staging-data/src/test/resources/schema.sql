/*
 * Copyright 2016 SEARCH-The National Consortium for Justice Information and Statistics
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

/*
 * When update the DDL from the SQL Power ddl generator,  need to add " AUTO_INCREMENT" to all the pkIds. 
 */
Drop schema if exists search_nibrs_staging;

CREATE schema search_nibrs_staging;

Use search_nibrs_staging; 



CREATE TABLE Owner (
                OwnerId IDENTITY NOT NULL,
                FederationId VARCHAR(100) NOT NULL,
                Email VARCHAR(100),
                FirstName VARCHAR(100) NOT NULL,
                LastName VARCHAR(100) NOT NULL,
                CONSTRAINT OwnerId PRIMARY KEY (OwnerId)
);
create unique index owner_federationId_idx on Owner(FederationId); 


CREATE TABLE FileUploadLogs (
                FileUploadLogsId IDENTITY NOT NULL,
                OwnerId INTEGER NOT NULL,
                UploadFileNames VARCHAR(2000) NOT NULL,
                TotalReportsCount INTEGER DEFAULT 0 NOT NULL,
                ValidReportsCount INTEGER DEFAULT 0 NOT NULL,
                PersistedCount INTEGER DEFAULT 0 NOT NULL,
                FailedToPersistCount INTEGER DEFAULT 0 NOT NULL,
                ValidationErrorsCount INTEGER DEFAULT 0 NOT NULL,
                FileUploadCompleteTimestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT FileUploadLogsId PRIMARY KEY (FileUploadLogsId)
);


CREATE TABLE NibrsErrorCodeType (
                NibrsErrorCodeTypeId IDENTITY NOT NULL,
                Code VARCHAR(3) NOT NULL,
                Type VARCHAR(30) NOT NULL,
                Message VARCHAR(300) NOT NULL,
                Description VARCHAR(4000) NOT NULL,
                CONSTRAINT NibrsErrorCodeTypeId PRIMARY KEY (NibrsErrorCodeTypeId)
);


CREATE TABLE Submission (
                SubmissionID IDENTITY NOT NULL,
                IncidentIdentifier VARCHAR(50) NOT NULL,
                MessageIdentifier INTEGER NOT NULL,
                RequestFilePath VARCHAR(300) NOT NULL,
                ResponseFilePath VARCHAR(300),
                AcceptedIndicator BOOLEAN DEFAULT false NOT NULL,
                ResponseTimestamp TIMESTAMP,
                FaultCode VARCHAR(100),
                FaultDescription VARCHAR(500),
                NIBRSReportCategoryCode VARCHAR(30) NOT NULL,
                SubmissionTimestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT SubmissionID PRIMARY KEY (SubmissionID)
);
COMMENT ON COLUMN Submission.NIBRSReportCategoryCode IS 'Group A or Group B.';


CREATE TABLE Violation (
                ViolationID IDENTITY NOT NULL,
                SubmissionID INTEGER NOT NULL,
                ViolationCode VARCHAR(10) NOT NULL,
                ViolationDescription VARCHAR(200) NOT NULL,
                ViolationLevel VARCHAR(1) NOT NULL,
                NibrsErrorCodeTypeId INTEGER,
                ViolationTimestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT ViolationID PRIMARY KEY (ViolationID)
);


CREATE TABLE CargoTheftIndicatorType (
                CargoTheftIndicatorTypeID IDENTITY NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(50) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(50) NOT NULL,
                CONSTRAINT CargoTheftIndicatorTypeID PRIMARY KEY (CargoTheftIndicatorTypeID)
);


CREATE TABLE AgencyType (
                AgencyTypeID IDENTITY NOT NULL,
                StateCode VARCHAR(2) NOT NULL,
                StateDescription VARCHAR(80) NOT NULL,
                NIBRSCode VARCHAR(2) NOT NULL,
                NIBRSDescription VARCHAR(80) NOT NULL,
                CONSTRAINT AgencyTypeID PRIMARY KEY (AgencyTypeID)
);


CREATE TABLE DateType (
                DateTypeID IDENTITY NOT NULL,
                CalendarDate DATE NOT NULL,
                YearNum INTEGER NOT NULL,
                YearLabel CHAR(4) NOT NULL,
                CalendarQuarter INTEGER NOT NULL,
                MonthNum INTEGER NOT NULL,
                MonthName VARCHAR(12) NOT NULL,
                FullMonth CHAR(7) NOT NULL,
                DayNum INTEGER NOT NULL,
                DayOfWeek VARCHAR(9) NOT NULL,
                DayOfWeekSort INTEGER NOT NULL,
                DateMMDDYYYY CHAR(10) NOT NULL,
                CONSTRAINT DateTypeID PRIMARY KEY (DateTypeID)
);


CREATE TABLE Agency (
                AgencyID IDENTITY NOT NULL,
                AgencyORI CHAR(9) NOT NULL,
                AgencyName VARCHAR(60) NOT NULL,
                AgencyTypeID INTEGER NOT NULL,
                StateCode CHAR(2) NOT NULL,
                StateName VARCHAR(20) NOT NULL,
                Population INTEGER,
                CONSTRAINT Agency_pk PRIMARY KEY (AgencyID)
);


CREATE TABLE OfficerAssignmentTypeType (
                OfficerAssignmentTypeTypeID IDENTITY NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(100) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(100) NOT NULL,
                CONSTRAINT OfficerAssignmentTypeType_pk PRIMARY KEY (OfficerAssignmentTypeTypeID)
);


CREATE TABLE OfficerActivityCircumstanceType (
                OfficerActivityCircumstanceTypeID IDENTITY NOT NULL,
                StateCode VARCHAR(2) NOT NULL,
                StateDescription VARCHAR(75) NOT NULL,
                NIBRSCode VARCHAR(2) NOT NULL,
                NIBRSDescription VARCHAR(75) NOT NULL,
                CONSTRAINT OfficerActivityCircumstanceType_pk PRIMARY KEY (OfficerActivityCircumstanceTypeID)
);


CREATE TABLE TypeOfWeaponForceInvolvedType (
                TypeOfWeaponForceInvolvedTypeID IDENTITY NOT NULL,
                StateCode VARCHAR(2) NOT NULL,
                StateDescription VARCHAR(30) NOT NULL,
                NIBRSCode VARCHAR(2) NOT NULL,
                NIBRSDescription VARCHAR(30) NOT NULL,
                TypeOfWeaponForceInvolvedCategory VARCHAR(40) NOT NULL,
                CONSTRAINT TypeOfWeaponForceInvolvedType_pk PRIMARY KEY (TypeOfWeaponForceInvolvedTypeID)
);


CREATE TABLE SuspectedDrugTypeType (
                SuspectedDrugTypeTypeID IDENTITY NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(32) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(32) NOT NULL,
                CONSTRAINT SuspectedDrugTypeType_pk PRIMARY KEY (SuspectedDrugTypeTypeID)
);


CREATE TABLE BiasMotivationType (
                BiasMotivationTypeID IDENTITY NOT NULL,
                StateCode VARCHAR(2) NOT NULL,
                StateDescription VARCHAR(60) NOT NULL,
                NIBRSCode VARCHAR(2) NOT NULL,
                NIBRSDescription VARCHAR(60) NOT NULL,
                BiasMotivationCategory VARCHAR(30) NOT NULL,
                CONSTRAINT BiasMotivationType_pk PRIMARY KEY (BiasMotivationTypeID)
);


CREATE TABLE MethodOfEntryType (
                MethodOfEntryTypeID IDENTITY NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(10) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(10) NOT NULL,
                CONSTRAINT MethodOfEntryType_pk PRIMARY KEY (MethodOfEntryTypeID)
);


CREATE TABLE LocationTypeType (
                LocationTypeTypeID IDENTITY NOT NULL,
                StateCode VARCHAR(2) NOT NULL,
                StateDescription VARCHAR(45) NOT NULL,
                NIBRSCode VARCHAR(2) NOT NULL,
                NIBRSDescription VARCHAR(45) NOT NULL,
                CONSTRAINT LocationTypeType_pk PRIMARY KEY (LocationTypeTypeID)
);


CREATE TABLE ClearedExceptionallyType (
                ClearedExceptionallyTypeID IDENTITY NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(220) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(220) NOT NULL,
                CONSTRAINT ClearedExceptionallyType_pk PRIMARY KEY (ClearedExceptionallyTypeID)
);


CREATE TABLE DispositionOfArresteeUnder18Type (
                DispositionOfArresteeUnder18TypeID IDENTITY NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(30) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(30) NOT NULL,
                CONSTRAINT DispositionOfArresteeUnder18Type_pk PRIMARY KEY (DispositionOfArresteeUnder18TypeID)
);


CREATE TABLE ArresteeWasArmedWithType (
                ArresteeWasArmedWithTypeID IDENTITY NOT NULL,
                StateCode VARCHAR(2) NOT NULL,
                StateDescription VARCHAR(30) NOT NULL,
                NIBRSCode VARCHAR(2) NOT NULL,
                NIBRSDescription VARCHAR(30) NOT NULL,
                CONSTRAINT ArresteeWasArmedWithType_pk PRIMARY KEY (ArresteeWasArmedWithTypeID)
);


CREATE TABLE MultipleArresteeSegmentsIndicatorType (
                MultipleArresteeSegmentsIndicatorTypeID IDENTITY NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(15) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(15) NOT NULL,
                CONSTRAINT MultipleArresteeSegmentsIndicatorType_pk PRIMARY KEY (MultipleArresteeSegmentsIndicatorTypeID)
);


CREATE TABLE TypeOfArrestType (
                TypeOfArrestTypeID IDENTITY NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(20) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(20) NOT NULL,
                CONSTRAINT TypeOfArrestType_pk PRIMARY KEY (TypeOfArrestTypeID)
);


CREATE TABLE AdditionalJustifiableHomicideCircumstancesType (
                AdditionalJustifiableHomicideCircumstancesTypeID IDENTITY NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(80) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(80) NOT NULL,
                CONSTRAINT AdditionalJustifiableHomicideCircumstancesType_pk PRIMARY KEY (AdditionalJustifiableHomicideCircumstancesTypeID)
);


CREATE TABLE TypeInjuryType (
                TypeInjuryTypeID IDENTITY NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(25) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(25) NOT NULL,
                CONSTRAINT TypeInjuryType_pk PRIMARY KEY (TypeInjuryTypeID)
);


CREATE TABLE ResidentStatusOfPersonType (
                ResidentStatusOfPersonTypeID IDENTITY NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(25) NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(25) NOT NULL,
                CONSTRAINT ResidentStatusOfPersonType_pk PRIMARY KEY (ResidentStatusOfPersonTypeID)
);


CREATE TABLE EthnicityOfPersonType (
                EthnicityOfPersonTypeID IDENTITY NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(25) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(25) NOT NULL,
                CONSTRAINT EthnicityOfPersonType_pk PRIMARY KEY (EthnicityOfPersonTypeID)
);


CREATE TABLE TypeOfVictimType (
                TypeOfVictimTypeID IDENTITY NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(75) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(75) NOT NULL,
                CONSTRAINT TypeOfVictimType_pk PRIMARY KEY (TypeOfVictimTypeID)
);


CREATE TABLE TypeDrugMeasurementType (
                TypeDrugMeasurementTypeID IDENTITY NOT NULL,
                StateCode VARCHAR(2) NOT NULL,
                StateDescription VARCHAR(20) NOT NULL,
                NIBRSCode VARCHAR(2) NOT NULL,
                NIBRSDescription VARCHAR(20) NOT NULL,
                CONSTRAINT TypeDrugMeasurementType_pk PRIMARY KEY (TypeDrugMeasurementTypeID)
);


CREATE TABLE PropertyDescriptionType (
                PropertyDescriptionTypeID IDENTITY NOT NULL,
                StateCode VARCHAR(2) NOT NULL,
                StateDescription VARCHAR(70) NOT NULL,
                NIBRSCode VARCHAR(2) NOT NULL,
                NIBRSDescription VARCHAR(70) NOT NULL,
                CONSTRAINT PropertyDescriptionType_pk PRIMARY KEY (PropertyDescriptionTypeID)
);


CREATE TABLE RaceOfPersonType (
                RaceOfPersonTypeID IDENTITY NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(42) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(42) NOT NULL,
                CONSTRAINT RaceOfPersonType_pk PRIMARY KEY (RaceOfPersonTypeID)
);


CREATE TABLE SexOfPersonType (
                SexOfPersonTypeID IDENTITY NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(35) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(35) NOT NULL,
                CONSTRAINT SexOfPersonType_pk PRIMARY KEY (SexOfPersonTypeID)
);


CREATE TABLE TypeOfCriminalActivityType (
                TypeOfCriminalActivityTypeID IDENTITY NOT NULL,
                StateCode VARCHAR(2) NOT NULL,
                StateDescription VARCHAR(80) NOT NULL,
                NIBRSCode VARCHAR(2) NOT NULL,
                NIBRSDescription VARCHAR(80) NOT NULL,
                CONSTRAINT TypeOfCriminalActivityType_pk PRIMARY KEY (TypeOfCriminalActivityTypeID)
);


CREATE TABLE OffenderSuspectedOfUsingType (
                OffenderSuspectedOfUsingTypeID IDENTITY NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(50) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(50) NOT NULL,
                CONSTRAINT OffenderSuspectedOfUsingType_pk PRIMARY KEY (OffenderSuspectedOfUsingTypeID)
);


CREATE TABLE SegmentActionTypeType (
                SegmentActionTypeTypeID IDENTITY NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(25) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(25) NOT NULL,
                CONSTRAINT SegmentActionTypeType_pk PRIMARY KEY (SegmentActionTypeTypeID)
);


CREATE TABLE PreCertificationError (
                PreCertificationErrorId IDENTITY NOT NULL,
                MonthOfTape VARCHAR(2),
                YearOfTape VARCHAR(4),
                ORI VARCHAR(9),
                AgencyID INTEGER NOT NULL,
                NibrsErrorCodeTypeId INTEGER NOT NULL,
                SegmentActionTypeTypeID INTEGER NOT NULL,
                SourceLocation VARCHAR(15),
                IncidentIdentifier VARCHAR(50),
                DataElement VARCHAR(4),
                RejectedValue VARCHAR(50),
                PreCertificationErrorTimestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                OwnerId INTEGER,
                CONSTRAINT PreCertificationErrorId PRIMARY KEY (PreCertificationErrorId)
);


CREATE TABLE VictimOffenderRelationshipType (
                VictimOffenderRelationshipTypeID IDENTITY NOT NULL,
                StateCode VARCHAR(2) NOT NULL,
                StateDescription VARCHAR(100) NOT NULL,
                NIBRSCode VARCHAR(2) NOT NULL,
                NIBRSDescription VARCHAR(100) NOT NULL,
                CONSTRAINT VictimOffenderRelationshipType_pk PRIMARY KEY (VictimOffenderRelationshipTypeID)
);


CREATE TABLE AggravatedAssaultHomicideCircumstancesType (
                AggravatedAssaultHomicideCircumstancesTypeID IDENTITY NOT NULL,
                StateCode VARCHAR(3) NOT NULL,
                StateDescription VARCHAR(55) NOT NULL,
                NIBRSCode VARCHAR(3) NOT NULL,
                NIBRSDescription VARCHAR(55) NOT NULL,
                CONSTRAINT AggravatedAssaultHomicideCircumstancesType_pk PRIMARY KEY (AggravatedAssaultHomicideCircumstancesTypeID)
);


CREATE TABLE TypePropertyLossEtcType (
                TypePropertyLossEtcTypeID IDENTITY NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(85) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(85) NOT NULL,
                CONSTRAINT TypePropertyLossEtcType_pk PRIMARY KEY (TypePropertyLossEtcTypeID)
);


CREATE TABLE UCROffenseCodeType (
                UCROffenseCodeTypeID IDENTITY NOT NULL,
                StateCode VARCHAR(3) NOT NULL,
                StateDescription VARCHAR(70) NOT NULL,
                NIBRSCode VARCHAR(3) NOT NULL,
                NIBRSDescription VARCHAR(70) NOT NULL,
                OffenseCategory1 VARCHAR(70) NOT NULL,
                OffenseCategory2 VARCHAR(70) NOT NULL,
                OffenseCategory3 VARCHAR(70) NOT NULL,
                OffenseCategory4 VARCHAR(70) NOT NULL,
                CONSTRAINT UCROffenseCode_pk PRIMARY KEY (UCROffenseCodeTypeID)
);


CREATE TABLE ArrestReportSegment (
                ArrestReportSegmentID IDENTITY NOT NULL,
                SegmentActionTypeTypeID INTEGER NOT NULL,
                StateCode CHAR(2) NOT NULL,
                MonthOfTape VARCHAR(2),
                YearOfTape VARCHAR(4),
                CityIndicator VARCHAR(4),
                AgencyID INTEGER NOT NULL,
                ORI VARCHAR(9),
                ArrestTransactionNumber VARCHAR(12),
                ArresteeSequenceNumber INTEGER NOT NULL,
                ArrestDate DATE,
                ArrestDateID INTEGER NOT NULL,
                TypeOfArrestTypeID INTEGER NOT NULL,
                AgeOfArresteeMin INTEGER,
                AgeOfArresteeMax INTEGER,
                NonNumericAge VARCHAR(2),
                AgeNumArrestee INTEGER,
                SexOfPersonTypeID INTEGER NOT NULL,
                RaceOfPersonTypeID INTEGER NOT NULL,
                EthnicityOfPersonTypeID INTEGER NOT NULL,
                ResidentStatusOfPersonTypeID INTEGER NOT NULL,
                DispositionOfArresteeUnder18TypeID INTEGER NOT NULL,
                UCROffenseCodeTypeID INTEGER NOT NULL,
                SubmissionID INTEGER,
                OwnerId INTEGER,
                ReportTimestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT ArresteReport_pk PRIMARY KEY (ArrestReportSegmentID)
);


CREATE TABLE ArrestReportSegmentWasArmedWith (
                ArrestReportSegmentWasArmedWithID IDENTITY NOT NULL,
                ArrestReportSegmentID INTEGER NOT NULL,
                ArresteeWasArmedWithTypeID INTEGER NOT NULL,
                AutomaticWeaponIndicator VARCHAR(1),
                CONSTRAINT ArrestReportSegmentWasArmedWithID PRIMARY KEY (ArrestReportSegmentWasArmedWithID)
);


CREATE TABLE AdministrativeSegment (
                AdministrativeSegmentID IDENTITY NOT NULL,
                SegmentActionTypeTypeID INTEGER NOT NULL,
                StateCode CHAR(2) NOT NULL,
                MonthOfTape VARCHAR(2),
                YearOfTape VARCHAR(4),
                CityIndicator VARCHAR(4),
                ORI VARCHAR(9),
                AgencyID INTEGER NOT NULL,
                IncidentNumber VARCHAR(12),
                IncidentDate DATE,
                IncidentDateID INTEGER NOT NULL,
                ReportDateIndicator VARCHAR(1),
                IncidentHour VARCHAR(2),
                ClearedExceptionallyTypeID INTEGER NOT NULL,
                ExceptionalClearanceDate DATE,
                ExceptionalClearanceDateID INTEGER NOT NULL,
                CargoTheftIndicatorTypeID INTEGER NOT NULL,
                SubmissionID INTEGER,
                OwnerId INTEGER,
                ReportTimestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT AdministrativeSegment_pk PRIMARY KEY (AdministrativeSegmentID)
);


CREATE TABLE ArresteeSegment (
                ArresteeSegmentID IDENTITY NOT NULL,
                SegmentActionTypeTypeID INTEGER NOT NULL,
                AdministrativeSegmentID INTEGER NOT NULL,
                ArresteeSequenceNumber INTEGER NOT NULL,
                ArrestTransactionNumber VARCHAR(12),
                ArrestDate DATE,
                ArrestDateID INTEGER NOT NULL,
                TypeOfArrestTypeID INTEGER NOT NULL,
                MultipleArresteeSegmentsIndicatorTypeID INTEGER NOT NULL,
                AgeOfArresteeMin INTEGER,
                AgeOfArresteeMax INTEGER,
                AgeNumArrestee INTEGER,
                NonNumericAge VARCHAR(2),
                SexOfPersonTypeID INTEGER NOT NULL,
                RaceOfPersonTypeID INTEGER NOT NULL,
                EthnicityOfPersonTypeID INTEGER NOT NULL,
                ResidentStatusOfPersonTypeID INTEGER NOT NULL,
                DispositionOfArresteeUnder18TypeID INTEGER NOT NULL,
                UCROffenseCodeTypeID INTEGER NOT NULL,
                CONSTRAINT Arrestee_pk PRIMARY KEY (ArresteeSegmentID)
);


CREATE TABLE ArresteeSegmentWasArmedWith (
                ArresteeSegmentWasArmedWithID IDENTITY NOT NULL,
                ArresteeSegmentID INTEGER NOT NULL,
                ArresteeWasArmedWithTypeID INTEGER NOT NULL,
                AutomaticWeaponIndicator VARCHAR(1),
                CONSTRAINT ArresteeSegmentWasArmedWithID PRIMARY KEY (ArresteeSegmentWasArmedWithID)
);


CREATE TABLE OffenderSegment (
                OffenderSegmentID IDENTITY NOT NULL,
                SegmentActionTypeTypeID INTEGER NOT NULL,
                AdministrativeSegmentID INTEGER NOT NULL,
                OffenderSequenceNumber INTEGER,
                AgeOfOffenderMin INTEGER,
                AgeOfOffenderMax INTEGER,
                AgeNumOffender INTEGER,
                NonNumericAge VARCHAR(2),
                SexOfPersonTypeID INTEGER NOT NULL,
                RaceOfPersonTypeID INTEGER NOT NULL,
                EthnicityOfPersonTypeID INTEGER NOT NULL,
                CONSTRAINT OffenderSegment_pk PRIMARY KEY (OffenderSegmentID)
);


CREATE TABLE VictimSegment (
                VictimSegmentID IDENTITY NOT NULL,
                SegmentActionTypeTypeID INTEGER NOT NULL,
                AdministrativeSegmentID INTEGER NOT NULL,
                VictimSequenceNumber INTEGER,
                TypeOfVictimTypeID INTEGER NOT NULL,
                OfficerActivityCircumstanceTypeID INTEGER NOT NULL,
                OfficerAssignmentTypeTypeID INTEGER NOT NULL,
                OfficerOtherJurisdictionORI VARCHAR(9),
                AgeOfVictimMin INTEGER,
                AgeOfVictimMax INTEGER,
                AgeNumVictim INTEGER,
                AgeNeonateIndicator INTEGER NOT NULL,
                AgeFirstWeekIndicator INTEGER NOT NULL,
                AgeFirstYearIndicator INTEGER NOT NULL,
                NonNumericAge VARCHAR(2),
                SexOfPersonTypeID INTEGER NOT NULL,
                RaceOfPersonTypeID INTEGER NOT NULL,
                EthnicityOfPersonTypeID INTEGER NOT NULL,
                ResidentStatusOfPersonTypeID INTEGER NOT NULL,
                AdditionalJustifiableHomicideCircumstancesTypeID INTEGER NOT NULL,
                CONSTRAINT VictimSegment_pk PRIMARY KEY (VictimSegmentID)
);


CREATE TABLE TypeInjury (
                TypeInjuryID IDENTITY NOT NULL,
                VictimSegmentID INTEGER NOT NULL,
                TypeInjuryTypeID INTEGER NOT NULL,
                CONSTRAINT TypeInjury_pk PRIMARY KEY (TypeInjuryID)
);


CREATE TABLE AggravatedAssaultHomicideCircumstances (
                AggravatedAssaultHomicideCircumstancesID IDENTITY NOT NULL,
                VictimSegmentID INTEGER NOT NULL,
                AggravatedAssaultHomicideCircumstancesTypeID INTEGER NOT NULL,
                CONSTRAINT AggravatedAssaultHomicideCircumstances_pk PRIMARY KEY (AggravatedAssaultHomicideCircumstancesID)
);


CREATE TABLE VictimOffenderAssociation (
                VictimOffenderAssociationID IDENTITY NOT NULL,
                VictimSegmentID INTEGER NOT NULL,
                OffenderSegmentID INTEGER NOT NULL,
                VictimOffenderRelationshipTypeID INTEGER NOT NULL,
                CONSTRAINT VictimOffenderAssociation_pk PRIMARY KEY (VictimOffenderAssociationID)
);


CREATE TABLE PropertySegment (
                PropertySegmentID IDENTITY NOT NULL,
                SegmentActionTypeTypeID INTEGER NOT NULL,
                AdministrativeSegmentID INTEGER NOT NULL,
                TypePropertyLossEtcTypeID INTEGER NOT NULL,
                NumberOfStolenMotorVehicles INTEGER,
                NumberOfRecoveredMotorVehicles INTEGER,
                CONSTRAINT PropertySegmentPK PRIMARY KEY (PropertySegmentID)
);


CREATE TABLE PropertyType (
                PropertyTypeID IDENTITY NOT NULL,
                PropertySegmentID INTEGER NOT NULL,
                PropertyDescriptionTypeID INTEGER NOT NULL,
                ValueOfProperty NUMERIC,
                RecoveredDate DATE,
                RecoveredDateID INTEGER NOT NULL,
                CONSTRAINT PropertyTypeID PRIMARY KEY (PropertyTypeID)
);


CREATE TABLE SuspectedDrugType (
                SuspectedDrugTypeID IDENTITY NOT NULL,
                PropertySegmentID INTEGER NOT NULL,
                SuspectedDrugTypeTypeID INTEGER NOT NULL,
                TypeDrugMeasurementTypeID INTEGER NOT NULL,
                EstimatedDrugQuantity DECIMAL(10,3),
                CONSTRAINT SuspectedDrugType_pk PRIMARY KEY (SuspectedDrugTypeID)
);


CREATE TABLE OffenseSegment (
                OffenseSegmentID IDENTITY NOT NULL,
                SegmentActionTypeTypeID INTEGER NOT NULL,
                AdministrativeSegmentID INTEGER NOT NULL,
                UCROffenseCodeTypeID INTEGER NOT NULL,
                OffenseAttemptedCompleted VARCHAR(1),
                LocationTypeTypeID INTEGER NOT NULL,
                NumberOfPremisesEntered INTEGER,
                MethodOfEntryTypeID INTEGER NOT NULL,
                CONSTRAINT Offense_pk PRIMARY KEY (OffenseSegmentID)
);


CREATE TABLE BiasMotivation (
                BiasMotivationID IDENTITY NOT NULL,
                OffenseSegmentID INTEGER NOT NULL,
                BiasMotivationTypeID INTEGER NOT NULL,
                CONSTRAINT BiasMotivationID PRIMARY KEY (BiasMotivationID)
);


CREATE TABLE VictimOffenseAssociation (
                VictimOffenseAssociationID IDENTITY NOT NULL,
                VictimSegmentID INTEGER NOT NULL,
                OffenseSegmentID INTEGER NOT NULL,
                CONSTRAINT VictimOffenseAssociation_pk PRIMARY KEY (VictimOffenseAssociationID)
);


CREATE TABLE TypeOfWeaponForceInvolved (
                TypeOfWeaponForceInvolvedID IDENTITY NOT NULL,
                AutomaticWeaponIndicator VARCHAR(1) NOT NULL,
                OffenseSegmentID INTEGER NOT NULL,
                TypeOfWeaponForceInvolvedTypeID INTEGER NOT NULL,
                CONSTRAINT TypeOfWeaponForceInvolved_pk PRIMARY KEY (TypeOfWeaponForceInvolvedID)
);


CREATE TABLE TypeCriminalActivity (
                TypeCriminalActivityID IDENTITY NOT NULL,
                OffenseSegmentID INTEGER NOT NULL,
                TypeOfCriminalActivityTypeID INTEGER NOT NULL,
                CONSTRAINT TypeCriminalActivity_pk PRIMARY KEY (TypeCriminalActivityID)
);


CREATE TABLE OffenderSuspectedOfUsing (
                OffenderSuspectedOfUsingID IDENTITY NOT NULL,
                OffenseSegmentID INTEGER NOT NULL,
                OffenderSuspectedOfUsingTypeID INTEGER NOT NULL,
                CONSTRAINT OffenderSuspectedOfUsing_pk PRIMARY KEY (OffenderSuspectedOfUsingID)
);


CREATE TABLE ZeroReportingSegment (
                ZeroReportingSegmentID IDENTITY NOT NULL,
                AdministrativeSegmentID INTEGER NOT NULL,
                RecordDescriptionWord VARCHAR(4),
                SegmentLevel VARCHAR(1),
                StateCode CHAR(2) NOT NULL,
                MonthOfTape VARCHAR(2),
                YearOfTape VARCHAR(4),
                CityIndicator VARCHAR(4),
                IncidentDate DATE,
                IncidentDateID INTEGER NOT NULL,
                ReportDateIndicator VARCHAR(1),
                IncidentHour VARCHAR(4),
                CleardExceptionally VARCHAR(1),
                ExceptionalClearanceDate DATE,
                OwnerInd INTEGER,
                ReportTimestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT ZeroReportingSegment_Pk PRIMARY KEY (ZeroReportingSegmentID)
);


CREATE TABLE LEOKASegment (
                LEOKASegmentID IDENTITY NOT NULL,
                AdministrativeSegmentID INTEGER NOT NULL,
                RecordDescriptionWord VARCHAR(4),
                SegmentLevel VARCHAR(1),
                StateCode CHAR(2) NOT NULL,
                MonthOfTape VARCHAR(2),
                YearOfTape VARCHAR(4),
                CityIndicator VARCHAR(4),
                Filler VARCHAR(12),
                LEOKAData VARCHAR(600),
                OwnerInd INTEGER,
                ReportTimestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT idLEOKASegment PRIMARY KEY (LEOKASegmentID)
);


ALTER TABLE ArrestReportSegment ADD CONSTRAINT Owner_ArrestReportSegment_fk
FOREIGN KEY (OwnerId)
REFERENCES Owner (OwnerId)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE AdministrativeSegment ADD CONSTRAINT Owner_AdministrativeSegment_fk
FOREIGN KEY (OwnerId)
REFERENCES Owner (OwnerId)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE PreCertificationError ADD CONSTRAINT Owner_PreCertificationError_fk
FOREIGN KEY (OwnerId)
REFERENCES Owner (OwnerId)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ZeroReportingSegment ADD CONSTRAINT OwnerId_ZeroReportingSegment_fk
FOREIGN KEY (OwnerInd)
REFERENCES Owner (OwnerId)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE LEOKASegment ADD CONSTRAINT OwnerId_LEOKASegment_fk
FOREIGN KEY (OwnerInd)
REFERENCES Owner (OwnerId)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE FileUploadLogs ADD CONSTRAINT Owner_FileUploadLogs_fk
FOREIGN KEY (OwnerId)
REFERENCES Owner (OwnerId)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE PreCertificationError ADD CONSTRAINT NibrsErrorCodeType_NibrsErrorId_fk
FOREIGN KEY (NibrsErrorCodeTypeId)
REFERENCES NibrsErrorCodeType (NibrsErrorCodeTypeId)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE Violation ADD CONSTRAINT NibrsErrorCodeType_Violation_fk
FOREIGN KEY (NibrsErrorCodeTypeId)
REFERENCES NibrsErrorCodeType (NibrsErrorCodeTypeId)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE Violation ADD CONSTRAINT Submission_Violation_fk
FOREIGN KEY (SubmissionID)
REFERENCES Submission (SubmissionID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE AdministrativeSegment ADD CONSTRAINT Submission_AdministrativeSegment_fk
FOREIGN KEY (SubmissionID)
REFERENCES Submission (SubmissionID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArrestReportSegment ADD CONSTRAINT Submission_ArrestReportSegment_fk
FOREIGN KEY (SubmissionID)
REFERENCES Submission (SubmissionID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE AdministrativeSegment ADD CONSTRAINT CargoTheftIndicatorType_AdministrativeSegment_fk
FOREIGN KEY (CargoTheftIndicatorTypeID)
REFERENCES CargoTheftIndicatorType (CargoTheftIndicatorTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE Agency ADD CONSTRAINT AgencyType_Agency_fk
FOREIGN KEY (AgencyTypeID)
REFERENCES AgencyType (AgencyTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE AdministrativeSegment ADD CONSTRAINT Date_AdministrativeSegment_fk
FOREIGN KEY (IncidentDateID)
REFERENCES DateType (DateTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE PropertyType ADD CONSTRAINT Date_PropertyType_fk
FOREIGN KEY (RecoveredDateID)
REFERENCES DateType (DateTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArresteeSegment ADD CONSTRAINT Date_ArresteeSegment_fk
FOREIGN KEY (ArrestDateID)
REFERENCES DateType (DateTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArrestReportSegment ADD CONSTRAINT Date_ArrestReportSegment_fk
FOREIGN KEY (ArrestDateID)
REFERENCES DateType (DateTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ZeroReportingSegment ADD CONSTRAINT Date_ZeroReportingSegment_fk
FOREIGN KEY (IncidentDateID)
REFERENCES DateType (DateTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE AdministrativeSegment ADD CONSTRAINT DateType_AdministrativeSegment_fk
FOREIGN KEY (ExceptionalClearanceDateID)
REFERENCES DateType (DateTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE AdministrativeSegment ADD CONSTRAINT Agency_AdministrativeSegment_fk
FOREIGN KEY (AgencyID)
REFERENCES Agency (AgencyID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArrestReportSegment ADD CONSTRAINT Agency_ArrestReportSegment_fk
FOREIGN KEY (AgencyID)
REFERENCES Agency (AgencyID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE PreCertificationError ADD CONSTRAINT Agency_NibrsErrorId_fk
FOREIGN KEY (AgencyID)
REFERENCES Agency (AgencyID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE VictimSegment ADD CONSTRAINT OfficerAssignmentTypeType_VictimSegment_fk
FOREIGN KEY (OfficerAssignmentTypeTypeID)
REFERENCES OfficerAssignmentTypeType (OfficerAssignmentTypeTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE VictimSegment ADD CONSTRAINT TypeOfOfficerActivityCircumstanceType_VictimSegment_fk
FOREIGN KEY (OfficerActivityCircumstanceTypeID)
REFERENCES OfficerActivityCircumstanceType (OfficerActivityCircumstanceTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE TypeOfWeaponForceInvolved ADD CONSTRAINT TypeOfWeaponForceInvolvedType_TypeOfWeaponForceInvolved_fk
FOREIGN KEY (TypeOfWeaponForceInvolvedTypeID)
REFERENCES TypeOfWeaponForceInvolvedType (TypeOfWeaponForceInvolvedTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE SuspectedDrugType ADD CONSTRAINT SuspectedDrugTypeType_SuspectedDrugType_fk
FOREIGN KEY (SuspectedDrugTypeTypeID)
REFERENCES SuspectedDrugTypeType (SuspectedDrugTypeTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE BiasMotivation ADD CONSTRAINT BiasMotivationType_BiasMotivation_fk
FOREIGN KEY (BiasMotivationTypeID)
REFERENCES BiasMotivationType (BiasMotivationTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE OffenseSegment ADD CONSTRAINT MethodOfEntryType_OffenseSegment_fk
FOREIGN KEY (MethodOfEntryTypeID)
REFERENCES MethodOfEntryType (MethodOfEntryTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE OffenseSegment ADD CONSTRAINT LocationTypeType_OffenseSegment_fk
FOREIGN KEY (LocationTypeTypeID)
REFERENCES LocationTypeType (LocationTypeTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE AdministrativeSegment ADD CONSTRAINT ClearedExceptionallyType_AdminSeg_fk
FOREIGN KEY (ClearedExceptionallyTypeID)
REFERENCES ClearedExceptionallyType (ClearedExceptionallyTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArresteeSegment ADD CONSTRAINT DispositionOfArresteeUnder18Lookup_ArresteeSegment_fk
FOREIGN KEY (DispositionOfArresteeUnder18TypeID)
REFERENCES DispositionOfArresteeUnder18Type (DispositionOfArresteeUnder18TypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArrestReportSegment ADD CONSTRAINT DispositionOfArresteeUnder18Lookup_ArrestReportSegment_fk
FOREIGN KEY (DispositionOfArresteeUnder18TypeID)
REFERENCES DispositionOfArresteeUnder18Type (DispositionOfArresteeUnder18TypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArresteeSegmentWasArmedWith ADD CONSTRAINT ArresteeArmedWithTypeLookup_ArresteeArmedWith_fk
FOREIGN KEY (ArresteeWasArmedWithTypeID)
REFERENCES ArresteeWasArmedWithType (ArresteeWasArmedWithTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArrestReportSegmentWasArmedWith ADD CONSTRAINT ArresteeWasArmedWithType_ArrestReportSegmentWasArmedWith_1_fk
FOREIGN KEY (ArresteeWasArmedWithTypeID)
REFERENCES ArresteeWasArmedWithType (ArresteeWasArmedWithTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArresteeSegment ADD CONSTRAINT MultipleArresteeSegmentsIndicatorLookup_ArresteeSegment_fk
FOREIGN KEY (MultipleArresteeSegmentsIndicatorTypeID)
REFERENCES MultipleArresteeSegmentsIndicatorType (MultipleArresteeSegmentsIndicatorTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArresteeSegment ADD CONSTRAINT TypeOfArrestLookup_ArresteeSegment_fk
FOREIGN KEY (TypeOfArrestTypeID)
REFERENCES TypeOfArrestType (TypeOfArrestTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArrestReportSegment ADD CONSTRAINT TypeOfArrestLookup_ArrestReportSegment_fk
FOREIGN KEY (TypeOfArrestTypeID)
REFERENCES TypeOfArrestType (TypeOfArrestTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE VictimSegment ADD CONSTRAINT AdditionalJustifiableHomicideCircumstancesType_VictimSegment_fk
FOREIGN KEY (AdditionalJustifiableHomicideCircumstancesTypeID)
REFERENCES AdditionalJustifiableHomicideCircumstancesType (AdditionalJustifiableHomicideCircumstancesTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE TypeInjury ADD CONSTRAINT TypeOfInjuryLookup_InjuryTable_fk
FOREIGN KEY (TypeInjuryTypeID)
REFERENCES TypeInjuryType (TypeInjuryTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE VictimSegment ADD CONSTRAINT ResidentStatusOfPersonType_VictimSegment_fk
FOREIGN KEY (ResidentStatusOfPersonTypeID)
REFERENCES ResidentStatusOfPersonType (ResidentStatusOfPersonTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArresteeSegment ADD CONSTRAINT ResidentStatusOfPersonType_ArresteeSegment_fk
FOREIGN KEY (ResidentStatusOfPersonTypeID)
REFERENCES ResidentStatusOfPersonType (ResidentStatusOfPersonTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArrestReportSegment ADD CONSTRAINT ResidentStatusOfPersonType_ArrestReportSegment_fk
FOREIGN KEY (ResidentStatusOfPersonTypeID)
REFERENCES ResidentStatusOfPersonType (ResidentStatusOfPersonTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE VictimSegment ADD CONSTRAINT EthnicityOfPersonType_VictimSegment_fk
FOREIGN KEY (EthnicityOfPersonTypeID)
REFERENCES EthnicityOfPersonType (EthnicityOfPersonTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArresteeSegment ADD CONSTRAINT EthnicityOfPersonType_ArresteeSegment_fk
FOREIGN KEY (EthnicityOfPersonTypeID)
REFERENCES EthnicityOfPersonType (EthnicityOfPersonTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArrestReportSegment ADD CONSTRAINT EthnicityOfPersonType_ArrestReportSegment_fk
FOREIGN KEY (EthnicityOfPersonTypeID)
REFERENCES EthnicityOfPersonType (EthnicityOfPersonTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE OffenderSegment ADD CONSTRAINT EthnicityOfPersonType_OffenderSegment_fk
FOREIGN KEY (EthnicityOfPersonTypeID)
REFERENCES EthnicityOfPersonType (EthnicityOfPersonTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE VictimSegment ADD CONSTRAINT TypeOfVictimType_VictimSegment_fk
FOREIGN KEY (TypeOfVictimTypeID)
REFERENCES TypeOfVictimType (TypeOfVictimTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE SuspectedDrugType ADD CONSTRAINT TypeDrugMeasurementType_SuspectedDrugType_fk
FOREIGN KEY (TypeDrugMeasurementTypeID)
REFERENCES TypeDrugMeasurementType (TypeDrugMeasurementTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE PropertyType ADD CONSTRAINT PropertyDescriptionType_PropertyType_fk
FOREIGN KEY (PropertyDescriptionTypeID)
REFERENCES PropertyDescriptionType (PropertyDescriptionTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE VictimSegment ADD CONSTRAINT RaceOfPersonType_VictimSegment_fk
FOREIGN KEY (RaceOfPersonTypeID)
REFERENCES RaceOfPersonType (RaceOfPersonTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE OffenderSegment ADD CONSTRAINT RaceOfPersonType_Offender_fk
FOREIGN KEY (RaceOfPersonTypeID)
REFERENCES RaceOfPersonType (RaceOfPersonTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArresteeSegment ADD CONSTRAINT RaceOfPersonType_ArresteeSegment_fk
FOREIGN KEY (RaceOfPersonTypeID)
REFERENCES RaceOfPersonType (RaceOfPersonTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArrestReportSegment ADD CONSTRAINT RaceOfPersonType_ArrestReportSegment_fk
FOREIGN KEY (RaceOfPersonTypeID)
REFERENCES RaceOfPersonType (RaceOfPersonTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE OffenderSegment ADD CONSTRAINT SexCodeLookup_Offender_fk
FOREIGN KEY (SexOfPersonTypeID)
REFERENCES SexOfPersonType (SexOfPersonTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArrestReportSegment ADD CONSTRAINT SexCodeLookup_ArrestReportSegment_fk
FOREIGN KEY (SexOfPersonTypeID)
REFERENCES SexOfPersonType (SexOfPersonTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE VictimSegment ADD CONSTRAINT SexOfPersonType_VictimSegment_fk
FOREIGN KEY (SexOfPersonTypeID)
REFERENCES SexOfPersonType (SexOfPersonTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArresteeSegment ADD CONSTRAINT SexOfPersonType_ArresteeSegment_fk
FOREIGN KEY (SexOfPersonTypeID)
REFERENCES SexOfPersonType (SexOfPersonTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE TypeCriminalActivity ADD CONSTRAINT TypeOfCriminalActivityType_TypeCriminalActivity_fk
FOREIGN KEY (TypeOfCriminalActivityTypeID)
REFERENCES TypeOfCriminalActivityType (TypeOfCriminalActivityTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE OffenderSuspectedOfUsing ADD CONSTRAINT OffenderSuspectedOfUsingType_OffenderSuspectedOfUsing_fk
FOREIGN KEY (OffenderSuspectedOfUsingTypeID)
REFERENCES OffenderSuspectedOfUsingType (OffenderSuspectedOfUsingTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE AdministrativeSegment ADD CONSTRAINT SegmentActionType_AdminSeg_fk
FOREIGN KEY (SegmentActionTypeTypeID)
REFERENCES SegmentActionTypeType (SegmentActionTypeTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE OffenseSegment ADD CONSTRAINT SegmentActionType_OffenseSegment_fk
FOREIGN KEY (SegmentActionTypeTypeID)
REFERENCES SegmentActionTypeType (SegmentActionTypeTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE PropertySegment ADD CONSTRAINT SegmentActionType_PropertySegment_fk
FOREIGN KEY (SegmentActionTypeTypeID)
REFERENCES SegmentActionTypeType (SegmentActionTypeTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE VictimSegment ADD CONSTRAINT SegmentActionType_VictimSegment_fk
FOREIGN KEY (SegmentActionTypeTypeID)
REFERENCES SegmentActionTypeType (SegmentActionTypeTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE OffenderSegment ADD CONSTRAINT SegmentActionType_Offender_fk
FOREIGN KEY (SegmentActionTypeTypeID)
REFERENCES SegmentActionTypeType (SegmentActionTypeTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArresteeSegment ADD CONSTRAINT SegmentActionType_ArresteeSegment_fk
FOREIGN KEY (SegmentActionTypeTypeID)
REFERENCES SegmentActionTypeType (SegmentActionTypeTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArrestReportSegment ADD CONSTRAINT SegmentActionType_ArrestReportSegment_fk
FOREIGN KEY (SegmentActionTypeTypeID)
REFERENCES SegmentActionTypeType (SegmentActionTypeTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE PreCertificationError ADD CONSTRAINT SegmentActionTypeType_NibrsErrorId_fk
FOREIGN KEY (SegmentActionTypeTypeID)
REFERENCES SegmentActionTypeType (SegmentActionTypeTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE VictimOffenderAssociation ADD CONSTRAINT VictimOffenderRelationshipType_VictimAssociation_fk
FOREIGN KEY (VictimOffenderRelationshipTypeID)
REFERENCES VictimOffenderRelationshipType (VictimOffenderRelationshipTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE AggravatedAssaultHomicideCircumstances ADD CONSTRAINT AAHomicideCircumstancesType_AAHomicideCircumstances_fk
FOREIGN KEY (AggravatedAssaultHomicideCircumstancesTypeID)
REFERENCES AggravatedAssaultHomicideCircumstancesType (AggravatedAssaultHomicideCircumstancesTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE PropertySegment ADD CONSTRAINT TypePropertyLossEtcType_PropertySegment_fk
FOREIGN KEY (TypePropertyLossEtcTypeID)
REFERENCES TypePropertyLossEtcType (TypePropertyLossEtcTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE OffenseSegment ADD CONSTRAINT UCROffenseCodeType_OffenseSegment_fk
FOREIGN KEY (UCROffenseCodeTypeID)
REFERENCES UCROffenseCodeType (UCROffenseCodeTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArresteeSegment ADD CONSTRAINT UCROffenseCodeType_ArresteeSegment_fk
FOREIGN KEY (UCROffenseCodeTypeID)
REFERENCES UCROffenseCodeType (UCROffenseCodeTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArrestReportSegment ADD CONSTRAINT UCROffenseCodeType_ArrestReportSegment_fk
FOREIGN KEY (UCROffenseCodeTypeID)
REFERENCES UCROffenseCodeType (UCROffenseCodeTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArrestReportSegmentWasArmedWith ADD CONSTRAINT ArrestReportSegment_ArrestReportSegmentWasArmedWith_1_fk
FOREIGN KEY (ArrestReportSegmentID)
REFERENCES ArrestReportSegment (ArrestReportSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE LEOKASegment ADD CONSTRAINT AdminSeg_LEOKASegment_fk
FOREIGN KEY (AdministrativeSegmentID)
REFERENCES AdministrativeSegment (AdministrativeSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ZeroReportingSegment ADD CONSTRAINT AdminSeg_ZeroReportingSegment_fk
FOREIGN KEY (AdministrativeSegmentID)
REFERENCES AdministrativeSegment (AdministrativeSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE OffenseSegment ADD CONSTRAINT AdministrativeSegment_OffenseSegment_fk
FOREIGN KEY (AdministrativeSegmentID)
REFERENCES AdministrativeSegment (AdministrativeSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE PropertySegment ADD CONSTRAINT AdministrativeSegment_PropertySegment_fk
FOREIGN KEY (AdministrativeSegmentID)
REFERENCES AdministrativeSegment (AdministrativeSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE VictimSegment ADD CONSTRAINT AdministrativeSegment_VictimSegment_fk
FOREIGN KEY (AdministrativeSegmentID)
REFERENCES AdministrativeSegment (AdministrativeSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE OffenderSegment ADD CONSTRAINT AdministrativeSegment_Offender_fk
FOREIGN KEY (AdministrativeSegmentID)
REFERENCES AdministrativeSegment (AdministrativeSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArresteeSegment ADD CONSTRAINT AdministrativeSegment_ArresteeSegment_fk
FOREIGN KEY (AdministrativeSegmentID)
REFERENCES AdministrativeSegment (AdministrativeSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArresteeSegmentWasArmedWith ADD CONSTRAINT ArresteeSegment_ArresteeWasArmedWith_fk
FOREIGN KEY (ArresteeSegmentID)
REFERENCES ArresteeSegment (ArresteeSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE VictimOffenderAssociation ADD CONSTRAINT OffenderSegment_SegmentAssociation_fk
FOREIGN KEY (OffenderSegmentID)
REFERENCES OffenderSegment (OffenderSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE VictimOffenderAssociation ADD CONSTRAINT VictimSegment_UCROffenseCodeAssociation_fk
FOREIGN KEY (VictimSegmentID)
REFERENCES VictimSegment (VictimSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE AggravatedAssaultHomicideCircumstances ADD CONSTRAINT VictimSegment_AggravatedAssaultHomicideCircumstances_fk
FOREIGN KEY (VictimSegmentID)
REFERENCES VictimSegment (VictimSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE TypeInjury ADD CONSTRAINT VictimSegment_InjuryTable_fk
FOREIGN KEY (VictimSegmentID)
REFERENCES VictimSegment (VictimSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE VictimOffenseAssociation ADD CONSTRAINT VictimSegment_VictimOffenseAssociation_fk
FOREIGN KEY (VictimSegmentID)
REFERENCES VictimSegment (VictimSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE SuspectedDrugType ADD CONSTRAINT PropertySegment_SuspectedDrugType_fk
FOREIGN KEY (PropertySegmentID)
REFERENCES PropertySegment (PropertySegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE PropertyType ADD CONSTRAINT PropertySegment_PropertyType_fk
FOREIGN KEY (PropertySegmentID)
REFERENCES PropertySegment (PropertySegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE OffenderSuspectedOfUsing ADD CONSTRAINT OffenseSegment_OffenderSuspectedOfUsing_fk
FOREIGN KEY (OffenseSegmentID)
REFERENCES OffenseSegment (OffenseSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE TypeCriminalActivity ADD CONSTRAINT OffenseSegment_TypeCriminalActivity_fk
FOREIGN KEY (OffenseSegmentID)
REFERENCES OffenseSegment (OffenseSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE TypeOfWeaponForceInvolved ADD CONSTRAINT OffenseSegment_TypeOfWeaponForceInvolved_fk
FOREIGN KEY (OffenseSegmentID)
REFERENCES OffenseSegment (OffenseSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE VictimOffenseAssociation ADD CONSTRAINT OffenseSegment_VictimOffenseAssociation_fk
FOREIGN KEY (OffenseSegmentID)
REFERENCES OffenseSegment (OffenseSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE BiasMotivation ADD CONSTRAINT OffenseSegment_BiasMotivation_fk
FOREIGN KEY (OffenseSegmentID)
REFERENCES OffenseSegment (OffenseSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;