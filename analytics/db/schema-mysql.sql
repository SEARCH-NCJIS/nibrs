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
 * When recreate the ddl from SQL Power,  don't forget to add ` AUTO_INCREMENT` to all the primary IDs.
 */
Drop schema if exists search_nibrs_staging;

CREATE schema search_nibrs_staging;
use search_nibrs_staging;

CREATE TABLE Owner (
                OwnerId INT AUTO_INCREMENT NOT NULL,
                FederationId VARCHAR(100) NOT NULL,
                Email VARCHAR(100),
                FirstName VARCHAR(100) NOT NULL,
                LastName VARCHAR(100) NOT NULL,
                PRIMARY KEY (OwnerId)
);
create unique index owner_federationId_idx on Owner(FederationId); 


CREATE TABLE NibrsErrorCodeType (
                NibrsErrorCodeTypeId INT AUTO_INCREMENT NOT NULL,
                Code VARCHAR(3) NOT NULL,
                Type VARCHAR(30) NOT NULL,
                Message VARCHAR(300) NOT NULL,
                Description VARCHAR(4000) NOT NULL,
                PRIMARY KEY (NibrsErrorCodeTypeId)
);


CREATE TABLE Submission (
                SubmissionID INT AUTO_INCREMENT NOT NULL,
                IncidentIdentifier VARCHAR(50) NOT NULL,
                MessageIdentifier INT NOT NULL,
                RequestFilePath VARCHAR(300) NOT NULL,
                ResponseFilePath VARCHAR(300),
                AcceptedIndicator BOOLEAN DEFAULT false NOT NULL,
                ResponseTimestamp timestamp,
                FaultCode VARCHAR(100),
                FaultDescription VARCHAR(500),
                NIBRSReportCategoryCode VARCHAR(30) NOT NULL,
                SubmissionTimestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                PRIMARY KEY (SubmissionID)
);

ALTER TABLE Submission MODIFY COLUMN NIBRSReportCategoryCode VARCHAR(30) COMMENT 'Group A or Group B.';


CREATE TABLE Violation (
                ViolationID INT AUTO_INCREMENT NOT NULL,
                SubmissionID INT NOT NULL,
                ViolationCode VARCHAR(10) NOT NULL,
                ViolationDescription VARCHAR(200) NOT NULL,
                ViolationLevel VARCHAR(1) NOT NULL,
                NibrsErrorCodeTypeId INT,
                ViolationTimestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                PRIMARY KEY (ViolationID)
);


CREATE TABLE CargoTheftIndicatorType (
                CargoTheftIndicatorTypeID INT AUTO_INCREMENT NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(50) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(50) NOT NULL,
                PRIMARY KEY (CargoTheftIndicatorTypeID)
);


CREATE TABLE AgencyType (
                AgencyTypeID INT AUTO_INCREMENT NOT NULL,
                StateCode VARCHAR(2) NOT NULL,
                StateDescription VARCHAR(80) NOT NULL,
                NIBRSCode VARCHAR(2) NOT NULL,
                NIBRSDescription VARCHAR(80) NOT NULL,
                PRIMARY KEY (AgencyTypeID)
);


CREATE TABLE DateType (
                DateTypeID INT AUTO_INCREMENT NOT NULL,
                CalendarDate DATE NOT NULL,
                YearNum INT NOT NULL,
                YearLabel CHAR(4) NOT NULL,
                CalendarQuarter INT NOT NULL,
                MonthNum INT NOT NULL,
                MonthName VARCHAR(12) NOT NULL,
                FullMonth CHAR(7) NOT NULL,
                DayNum INT NOT NULL,
                DayOfWeek VARCHAR(9) NOT NULL,
                DayOfWeekSort INT NOT NULL,
                DateMMDDYYYY CHAR(10) NOT NULL,
                PRIMARY KEY (DateTypeID)
);


CREATE TABLE Agency (
                AgencyID INT AUTO_INCREMENT NOT NULL,
                AgencyORI CHAR(9) NOT NULL,
                AgencyName VARCHAR(60) NOT NULL,
                AgencyTypeID INT NOT NULL,
                StateCode CHAR(2) NOT NULL,
                StateName VARCHAR(20) NOT NULL,
                Population INT,
                PRIMARY KEY (AgencyID)
);


CREATE TABLE OfficerAssignmentTypeType (
                OfficerAssignmentTypeTypeID INT AUTO_INCREMENT NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(100) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(100) NOT NULL,
                PRIMARY KEY (OfficerAssignmentTypeTypeID)
);


CREATE TABLE OfficerActivityCircumstanceType (
                OfficerActivityCircumstanceTypeID INT AUTO_INCREMENT NOT NULL,
                StateCode VARCHAR(2) NOT NULL,
                StateDescription VARCHAR(75) NOT NULL,
                NIBRSCode VARCHAR(2) NOT NULL,
                NIBRSDescription VARCHAR(75) NOT NULL,
                PRIMARY KEY (OfficerActivityCircumstanceTypeID)
);


CREATE TABLE TypeOfWeaponForceInvolvedType (
                TypeOfWeaponForceInvolvedTypeID INT AUTO_INCREMENT NOT NULL,
                StateCode VARCHAR(2) NOT NULL,
                StateDescription VARCHAR(30) NOT NULL,
                NIBRSCode VARCHAR(2) NOT NULL,
                NIBRSDescription VARCHAR(30) NOT NULL,
                PRIMARY KEY (TypeOfWeaponForceInvolvedTypeID)
);


CREATE TABLE SuspectedDrugTypeType (
                SuspectedDrugTypeTypeID INT AUTO_INCREMENT NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(32) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(32) NOT NULL,
                PRIMARY KEY (SuspectedDrugTypeTypeID)
);


CREATE TABLE BiasMotivationType (
                BiasMotivationTypeID INT AUTO_INCREMENT NOT NULL,
                StateCode VARCHAR(2) NOT NULL,
                StateDescription VARCHAR(60) NOT NULL,
                NIBRSCode VARCHAR(2) NOT NULL,
                NIBRSDescription VARCHAR(60) NOT NULL,
                BiasMotivationCategory VARCHAR(30) NOT NULL,
                PRIMARY KEY (BiasMotivationTypeID)
);


CREATE TABLE MethodOfEntryType (
                MethodOfEntryTypeID INT AUTO_INCREMENT NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(10) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(10) NOT NULL,
                PRIMARY KEY (MethodOfEntryTypeID)
);


CREATE TABLE LocationTypeType (
                LocationTypeTypeID INT AUTO_INCREMENT NOT NULL,
                StateCode VARCHAR(2) NOT NULL,
                StateDescription VARCHAR(45) NOT NULL,
                NIBRSCode VARCHAR(2) NOT NULL,
                NIBRSDescription VARCHAR(45) NOT NULL,
                PRIMARY KEY (LocationTypeTypeID)
);


CREATE TABLE ClearedExceptionallyType (
                ClearedExceptionallyTypeID INT AUTO_INCREMENT NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(220) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(220) NOT NULL,
                PRIMARY KEY (ClearedExceptionallyTypeID)
);


CREATE TABLE DispositionOfArresteeUnder18Type (
                DispositionOfArresteeUnder18TypeID INT AUTO_INCREMENT NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(30) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(30) NOT NULL,
                PRIMARY KEY (DispositionOfArresteeUnder18TypeID)
);


CREATE TABLE ArresteeWasArmedWithType (
                ArresteeWasArmedWithTypeID INT AUTO_INCREMENT NOT NULL,
                StateCode VARCHAR(2) NOT NULL,
                StateDescription VARCHAR(30) NOT NULL,
                NIBRSCode VARCHAR(2) NOT NULL,
                NIBRSDescription VARCHAR(30) NOT NULL,
                PRIMARY KEY (ArresteeWasArmedWithTypeID)
);


CREATE TABLE MultipleArresteeSegmentsIndicatorType (
                MultipleArresteeSegmentsIndicatorTypeID INT AUTO_INCREMENT NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(15) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(15) NOT NULL,
                PRIMARY KEY (MultipleArresteeSegmentsIndicatorTypeID)
);


CREATE TABLE TypeOfArrestType (
                TypeOfArrestTypeID INT AUTO_INCREMENT NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(20) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(20) NOT NULL,
                PRIMARY KEY (TypeOfArrestTypeID)
);


CREATE TABLE AdditionalJustifiableHomicideCircumstancesType (
                AdditionalJustifiableHomicideCircumstancesTypeID INT AUTO_INCREMENT NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(80) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(80) NOT NULL,
                PRIMARY KEY (AdditionalJustifiableHomicideCircumstancesTypeID)
);


CREATE TABLE TypeInjuryType (
                TypeInjuryTypeID INT AUTO_INCREMENT NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(25) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(25) NOT NULL,
                PRIMARY KEY (TypeInjuryTypeID)
);


CREATE TABLE ResidentStatusOfPersonType (
                ResidentStatusOfPersonTypeID INT AUTO_INCREMENT NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(25) NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(25) NOT NULL,
                PRIMARY KEY (ResidentStatusOfPersonTypeID)
);


CREATE TABLE EthnicityOfPersonType (
                EthnicityOfPersonTypeID INT AUTO_INCREMENT NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(25) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(25) NOT NULL,
                PRIMARY KEY (EthnicityOfPersonTypeID)
);


CREATE TABLE TypeOfVictimType (
                TypeOfVictimTypeID INT AUTO_INCREMENT NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(75) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(75) NOT NULL,
                PRIMARY KEY (TypeOfVictimTypeID)
);


CREATE TABLE TypeDrugMeasurementType (
                TypeDrugMeasurementTypeID INT AUTO_INCREMENT NOT NULL,
                StateCode VARCHAR(2) NOT NULL,
                StateDescription VARCHAR(20) NOT NULL,
                NIBRSCode VARCHAR(2) NOT NULL,
                NIBRSDescription VARCHAR(20) NOT NULL,
                PRIMARY KEY (TypeDrugMeasurementTypeID)
);


CREATE TABLE PropertyDescriptionType (
                PropertyDescriptionTypeID INT AUTO_INCREMENT NOT NULL,
                StateCode VARCHAR(2) NOT NULL,
                StateDescription VARCHAR(70) NOT NULL,
                NIBRSCode VARCHAR(2) NOT NULL,
                NIBRSDescription VARCHAR(70) NOT NULL,
                PRIMARY KEY (PropertyDescriptionTypeID)
);


CREATE TABLE RaceOfPersonType (
                RaceOfPersonTypeID INT AUTO_INCREMENT NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(42) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(42) NOT NULL,
                PRIMARY KEY (RaceOfPersonTypeID)
);


CREATE TABLE SexOfPersonType (
                SexOfPersonTypeID INT AUTO_INCREMENT NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(35) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(35) NOT NULL,
                PRIMARY KEY (SexOfPersonTypeID)
);


CREATE TABLE TypeOfCriminalActivityType (
                TypeOfCriminalActivityTypeID INT AUTO_INCREMENT NOT NULL,
                StateCode VARCHAR(2) NOT NULL,
                StateDescription VARCHAR(80) NOT NULL,
                NIBRSCode VARCHAR(2) NOT NULL,
                NIBRSDescription VARCHAR(80) NOT NULL,
                PRIMARY KEY (TypeOfCriminalActivityTypeID)
);


CREATE TABLE OffenderSuspectedOfUsingType (
                OffenderSuspectedOfUsingTypeID INT AUTO_INCREMENT NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(50) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(50) NOT NULL,
                PRIMARY KEY (OffenderSuspectedOfUsingTypeID)
);


CREATE TABLE SegmentActionTypeType (
                SegmentActionTypeTypeID INT AUTO_INCREMENT NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(25) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(25) NOT NULL,
                PRIMARY KEY (SegmentActionTypeTypeID)
);


CREATE TABLE PreCertificationError (
                PreCertificationErrorId INT AUTO_INCREMENT NOT NULL,
                MonthOfTape VARCHAR(2),
                YearOfTape VARCHAR(4),
                ORI VARCHAR(9),
                AgencyID INT NOT NULL,
                NibrsErrorCodeTypeId INT NOT NULL,
                SegmentActionTypeTypeID INT NOT NULL,
                SourceLocation VARCHAR(15),
                IncidentIdentifier VARCHAR(50),
                DataElement VARCHAR(4),
                RejectedValue VARCHAR(50),
                PreCertificationErrorTimestamp DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
                OwnerId INT,
                PRIMARY KEY (PreCertificationErrorId)
);


CREATE TABLE VictimOffenderRelationshipType (
                VictimOffenderRelationshipTypeID INT AUTO_INCREMENT NOT NULL,
                StateCode VARCHAR(2) NOT NULL,
                StateDescription VARCHAR(100) NOT NULL,
                NIBRSCode VARCHAR(2) NOT NULL,
                NIBRSDescription VARCHAR(100) NOT NULL,
                PRIMARY KEY (VictimOffenderRelationshipTypeID)
);


CREATE TABLE AggravatedAssaultHomicideCircumstancesType (
                AggravatedAssaultHomicideCircumstancesTypeID INT AUTO_INCREMENT NOT NULL,
                StateCode VARCHAR(3) NOT NULL,
                StateDescription VARCHAR(55) NOT NULL,
                NIBRSCode VARCHAR(3) NOT NULL,
                NIBRSDescription VARCHAR(55) NOT NULL,
                PRIMARY KEY (AggravatedAssaultHomicideCircumstancesTypeID)
);


CREATE TABLE TypePropertyLossEtcType (
                TypePropertyLossEtcTypeID INT AUTO_INCREMENT NOT NULL,
                StateCode VARCHAR(1) NOT NULL,
                StateDescription VARCHAR(85) NOT NULL,
                NIBRSCode VARCHAR(1) NOT NULL,
                NIBRSDescription VARCHAR(85) NOT NULL,
                PRIMARY KEY (TypePropertyLossEtcTypeID)
);


CREATE TABLE UCROffenseCodeType (
                UCROffenseCodeTypeID INT AUTO_INCREMENT NOT NULL,
                StateCode VARCHAR(3) NOT NULL,
                StateDescription VARCHAR(70) NOT NULL,
                NIBRSCode VARCHAR(3) NOT NULL,
                NIBRSDescription VARCHAR(70) NOT NULL,
                OffenseCategory1 VARCHAR(70) NOT NULL,
                OffenseCategory2 VARCHAR(70) NOT NULL,
                OffenseCategory3 VARCHAR(70) NOT NULL,
                OffenseCategory4 VARCHAR(70) NOT NULL,
                PRIMARY KEY (UCROffenseCodeTypeID)
);


CREATE TABLE ArrestReportSegment (
                ArrestReportSegmentID INT AUTO_INCREMENT NOT NULL,
                SegmentActionTypeTypeID INT NOT NULL,
                StateCode CHAR(2) NOT NULL,
                MonthOfTape VARCHAR(2),
                YearOfTape VARCHAR(4),
                CityIndicator VARCHAR(4),
                AgencyID INT NOT NULL,
                ORI VARCHAR(9),
                ArrestTransactionNumber VARCHAR(12),
                ArresteeSequenceNumber INT NOT NULL,
                ArrestDate DATE,
                ArrestDateID INT NOT NULL,
                TypeOfArrestTypeID INT NOT NULL,
                AgeOfArresteeMin INT,
                AgeOfArresteeMax INT,
                NonNumericAge VARCHAR(2),
                AgeNumArrestee INT,
                SexOfPersonTypeID INT NOT NULL,
                RaceOfPersonTypeID INT NOT NULL,
                EthnicityOfPersonTypeID INT NOT NULL,
                ResidentStatusOfPersonTypeID INT NOT NULL,
                DispositionOfArresteeUnder18TypeID INT NOT NULL,
                UCROffenseCodeTypeID INT NOT NULL,
                SubmissionID INT,
                OwnerId INT,
                ReportTimestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                PRIMARY KEY (ArrestReportSegmentID)
);


CREATE TABLE ArrestReportSegmentWasArmedWith (
                ArrestReportSegmentWasArmedWithID INT AUTO_INCREMENT NOT NULL,
                ArrestReportSegmentID INT NOT NULL,
                ArresteeWasArmedWithTypeID INT NOT NULL,
                AutomaticWeaponIndicator VARCHAR(1),
                PRIMARY KEY (ArrestReportSegmentWasArmedWithID)
);
CREATE INDEX arrestreportsegment_idx
 ON ArrestReportSegment
 ( ORI, ArrestTransactionNumber, MonthOfTape, YearOfTape);


CREATE TABLE AdministrativeSegment (
                AdministrativeSegmentID INT AUTO_INCREMENT NOT NULL,
                SegmentActionTypeTypeID INT NOT NULL,
                StateCode CHAR(2) NOT NULL,
                MonthOfTape VARCHAR(2),
                YearOfTape VARCHAR(4),
                CityIndicator VARCHAR(4),
                ORI VARCHAR(9),
                AgencyID INT NOT NULL,
                IncidentNumber VARCHAR(12),
                IncidentDate DATE,
                IncidentDateID INT NOT NULL,
                ReportDateIndicator VARCHAR(1),
                IncidentHour VARCHAR(2),
                ClearedExceptionallyTypeID INT NOT NULL,
                ExceptionalClearanceDate DATE,
                ExceptionalClearanceDateID INT NOT NULL,
                CargoTheftIndicatorTypeID INT NOT NULL,
                SubmissionID INT,
                OwnerId INT,
                ReportTimestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                PRIMARY KEY (AdministrativeSegmentID)
);


CREATE INDEX administrativesegment_idx
 ON AdministrativeSegment
 ( ORI, IncidentNumber, MonthOfTape, YearOfTape);

CREATE TABLE ArresteeSegment (
                ArresteeSegmentID INT AUTO_INCREMENT NOT NULL,
                SegmentActionTypeTypeID INT NOT NULL,
                AdministrativeSegmentID INT NOT NULL,
                ArresteeSequenceNumber INT NOT NULL,
                ArrestTransactionNumber VARCHAR(12),
                ArrestDate DATE,
                ArrestDateID INT NOT NULL,
                TypeOfArrestTypeID INT NOT NULL,
                MultipleArresteeSegmentsIndicatorTypeID INT NOT NULL,
                AgeOfArresteeMin INT,
                AgeOfArresteeMax INT,
                AgeNumArrestee INT,
                NonNumericAge VARCHAR(2),
                SexOfPersonTypeID INT NOT NULL,
                RaceOfPersonTypeID INT NOT NULL,
                EthnicityOfPersonTypeID INT NOT NULL,
                ResidentStatusOfPersonTypeID INT NOT NULL,
                DispositionOfArresteeUnder18TypeID INT NOT NULL,
                UCROffenseCodeTypeID INT NOT NULL,
                PRIMARY KEY (ArresteeSegmentID)
);


CREATE TABLE ArresteeSegmentWasArmedWith (
                ArresteeSegmentWasArmedWithID INT AUTO_INCREMENT NOT NULL,
                ArresteeSegmentID INT NOT NULL,
                ArresteeWasArmedWithTypeID INT NOT NULL,
                AutomaticWeaponIndicator VARCHAR(1),
                PRIMARY KEY (ArresteeSegmentWasArmedWithID)
);


CREATE TABLE OffenderSegment (
                OffenderSegmentID INT AUTO_INCREMENT NOT NULL,
                SegmentActionTypeTypeID INT NOT NULL,
                AdministrativeSegmentID INT NOT NULL,
                OffenderSequenceNumber INT,
                AgeOfOffenderMin INT,
                AgeOfOffenderMax INT,
                AgeNumOffender INT,
                NonNumericAge VARCHAR(2),
                SexOfPersonTypeID INT NOT NULL,
                RaceOfPersonTypeID INT NOT NULL,
                EthnicityOfPersonTypeID INT NOT NULL,
                PRIMARY KEY (OffenderSegmentID)
);


CREATE TABLE VictimSegment (
                VictimSegmentID INT AUTO_INCREMENT NOT NULL,
                SegmentActionTypeTypeID INT NOT NULL,
                AdministrativeSegmentID INT NOT NULL,
                VictimSequenceNumber INT,
                TypeOfVictimTypeID INT NOT NULL,
                OfficerActivityCircumstanceTypeID INT NOT NULL,
                OfficerAssignmentTypeTypeID INT NOT NULL,
                OfficerOtherJurisdictionORI VARCHAR(9),
                AgeOfVictimMin INT,
                AgeOfVictimMax INT,
                AgeNumVictim INT,
                AgeNeonateIndicator INT NOT NULL,
                AgeFirstWeekIndicator INT NOT NULL,
                AgeFirstYearIndicator INT NOT NULL,
                NonNumericAge VARCHAR(2),
                SexOfPersonTypeID INT NOT NULL,
                RaceOfPersonTypeID INT NOT NULL,
                EthnicityOfPersonTypeID INT NOT NULL,
                ResidentStatusOfPersonTypeID INT NOT NULL,
                AdditionalJustifiableHomicideCircumstancesTypeID INT NOT NULL,
                PRIMARY KEY (VictimSegmentID)
);


CREATE TABLE TypeInjury (
                TypeInjuryID INT AUTO_INCREMENT NOT NULL,
                VictimSegmentID INT NOT NULL,
                TypeInjuryTypeID INT NOT NULL,
                PRIMARY KEY (TypeInjuryID)
);


CREATE TABLE AggravatedAssaultHomicideCircumstances (
                AggravatedAssaultHomicideCircumstancesID INT AUTO_INCREMENT NOT NULL,
                VictimSegmentID INT NOT NULL,
                AggravatedAssaultHomicideCircumstancesTypeID INT NOT NULL,
                PRIMARY KEY (AggravatedAssaultHomicideCircumstancesID)
);


CREATE TABLE VictimOffenderAssociation (
                VictimOffenderAssociationID INT AUTO_INCREMENT NOT NULL,
                VictimSegmentID INT NOT NULL,
                OffenderSegmentID INT NOT NULL,
                VictimOffenderRelationshipTypeID INT NOT NULL,
                PRIMARY KEY (VictimOffenderAssociationID)
);


CREATE TABLE PropertySegment (
                PropertySegmentID INT AUTO_INCREMENT NOT NULL,
                SegmentActionTypeTypeID INT NOT NULL,
                AdministrativeSegmentID INT NOT NULL,
                TypePropertyLossEtcTypeID INT NOT NULL,
                NumberOfStolenMotorVehicles INT,
                NumberOfRecoveredMotorVehicles INT,
                PRIMARY KEY (PropertySegmentID)
);


CREATE TABLE PropertyType (
                PropertyTypeID INT AUTO_INCREMENT NOT NULL,
                PropertySegmentID INT NOT NULL,
                PropertyDescriptionTypeID INT NOT NULL,
                ValueOfProperty NUMERIC,
                RecoveredDate DATE,
                RecoveredDateID INT NOT NULL,
                PRIMARY KEY (PropertyTypeID)
);


CREATE TABLE SuspectedDrugType (
                SuspectedDrugTypeID INT AUTO_INCREMENT NOT NULL,
                PropertySegmentID INT NOT NULL,
                SuspectedDrugTypeTypeID INT NOT NULL,
                TypeDrugMeasurementTypeID INT NOT NULL,
                EstimatedDrugQuantity DECIMAL(10,3),
                PRIMARY KEY (SuspectedDrugTypeID)
);


CREATE TABLE OffenseSegment (
                OffenseSegmentID INT AUTO_INCREMENT NOT NULL,
                SegmentActionTypeTypeID INT NOT NULL,
                AdministrativeSegmentID INT NOT NULL,
                UCROffenseCodeTypeID INT NOT NULL,
                OffenseAttemptedCompleted VARCHAR(1),
                LocationTypeTypeID INT NOT NULL,
                NumberOfPremisesEntered INT,
                MethodOfEntryTypeID INT NOT NULL,
                PRIMARY KEY (OffenseSegmentID)
);


CREATE TABLE BiasMotivation (
                BiasMotivationID INT AUTO_INCREMENT NOT NULL,
                OffenseSegmentID INT NOT NULL,
                BiasMotivationTypeID INT NOT NULL,
                PRIMARY KEY (BiasMotivationID)
);


CREATE TABLE VictimOffenseAssociation (
                VictimOffenseAssociationID INT AUTO_INCREMENT NOT NULL,
                VictimSegmentID INT NOT NULL,
                OffenseSegmentID INT NOT NULL,
                PRIMARY KEY (VictimOffenseAssociationID)
);


CREATE TABLE TypeOfWeaponForceInvolved (
                TypeOfWeaponForceInvolvedID INT AUTO_INCREMENT NOT NULL,
                AutomaticWeaponIndicator VARCHAR(1) NOT NULL,
                OffenseSegmentID INT NOT NULL,
                TypeOfWeaponForceInvolvedTypeID INT NOT NULL,
                PRIMARY KEY (TypeOfWeaponForceInvolvedID)
);


CREATE TABLE TypeCriminalActivity (
                TypeCriminalActivityID INT AUTO_INCREMENT NOT NULL,
                OffenseSegmentID INT NOT NULL,
                TypeOfCriminalActivityTypeID INT NOT NULL,
                PRIMARY KEY (TypeCriminalActivityID)
);


CREATE TABLE OffenderSuspectedOfUsing (
                OffenderSuspectedOfUsingID INT AUTO_INCREMENT NOT NULL,
                OffenseSegmentID INT NOT NULL,
                OffenderSuspectedOfUsingTypeID INT NOT NULL,
                PRIMARY KEY (OffenderSuspectedOfUsingID)
);


CREATE TABLE ZeroReportingSegment (
                ZeroReportingSegmentID INT AUTO_INCREMENT NOT NULL,
                AdministrativeSegmentID INT NOT NULL,
                RecordDescriptionWord VARCHAR(4),
                SegmentLevel VARCHAR(1),
                StateCode CHAR(2) NOT NULL,
                MonthOfTape VARCHAR(2),
                YearOfTape VARCHAR(4),
                CityIndicator VARCHAR(4),
                IncidentDate DATE,
                IncidentDateID INT NOT NULL,
                ReportDateIndicator VARCHAR(1),
                IncidentHour VARCHAR(4),
                CleardExceptionally VARCHAR(1),
                ExceptionalClearanceDate DATE,
                OwnerId INT,
                ReportTimestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                PRIMARY KEY (ZeroReportingSegmentID)
);


CREATE TABLE LEOKASegment (
                LEOKASegmentID INT AUTO_INCREMENT NOT NULL,
                AdministrativeSegmentID INT NOT NULL,
                RecordDescriptionWord VARCHAR(4),
                SegmentLevel VARCHAR(1),
                StateCode CHAR(2) NOT NULL,
                MonthOfTape VARCHAR(2),
                YearOfTape VARCHAR(4),
                CityIndicator VARCHAR(4),
                Filler VARCHAR(12),
                LEOKAData VARCHAR(600),
                OwnerId INT,
                ReportTimestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                PRIMARY KEY (LEOKASegmentID)
);


ALTER TABLE ArrestReportSegment ADD CONSTRAINT owner_arrestreportsegment_fk
FOREIGN KEY (OwnerId)
REFERENCES Owner (OwnerId)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE AdministrativeSegment ADD CONSTRAINT owner_administrativesegment_fk
FOREIGN KEY (OwnerId)
REFERENCES Owner (OwnerId)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE PreCertificationError ADD CONSTRAINT owner_precertificationerror_fk
FOREIGN KEY (OwnerId)
REFERENCES Owner (OwnerId)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ZeroReportingSegment ADD CONSTRAINT ownerid_zeroreportingsegment_fk
FOREIGN KEY (OwnerId)
REFERENCES Owner (OwnerId)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE LEOKASegment ADD CONSTRAINT ownerid_leokasegment_fk
FOREIGN KEY (OwnerId)
REFERENCES Owner (OwnerId)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE PreCertificationError ADD CONSTRAINT nibrserrorcodetype_nibrserrorid_fk
FOREIGN KEY (NibrsErrorCodeTypeId)
REFERENCES NibrsErrorCodeType (NibrsErrorCodeTypeId)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE Violation ADD CONSTRAINT nibrserrorcodetype_violation_fk
FOREIGN KEY (NibrsErrorCodeTypeId)
REFERENCES NibrsErrorCodeType (NibrsErrorCodeTypeId)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE Violation ADD CONSTRAINT submission_violation_fk
FOREIGN KEY (SubmissionID)
REFERENCES Submission (SubmissionID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE AdministrativeSegment ADD CONSTRAINT submission_administrativesegment_fk
FOREIGN KEY (SubmissionID)
REFERENCES Submission (SubmissionID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArrestReportSegment ADD CONSTRAINT submission_arrestreportsegment_fk
FOREIGN KEY (SubmissionID)
REFERENCES Submission (SubmissionID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE AdministrativeSegment ADD CONSTRAINT cargotheftindicatortype_administrativesegment_fk
FOREIGN KEY (CargoTheftIndicatorTypeID)
REFERENCES CargoTheftIndicatorType (CargoTheftIndicatorTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE Agency ADD CONSTRAINT agencytype_agency_fk
FOREIGN KEY (AgencyTypeID)
REFERENCES AgencyType (AgencyTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE AdministrativeSegment ADD CONSTRAINT date_administrativesegment_fk
FOREIGN KEY (IncidentDateID)
REFERENCES DateType (DateTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE PropertyType ADD CONSTRAINT date_propertytype_fk
FOREIGN KEY (RecoveredDateID)
REFERENCES DateType (DateTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArresteeSegment ADD CONSTRAINT date_arresteesegment_fk
FOREIGN KEY (ArrestDateID)
REFERENCES DateType (DateTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArrestReportSegment ADD CONSTRAINT date_arrestreportsegment_fk
FOREIGN KEY (ArrestDateID)
REFERENCES DateType (DateTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ZeroReportingSegment ADD CONSTRAINT date_zeroreportingsegment_fk
FOREIGN KEY (IncidentDateID)
REFERENCES DateType (DateTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE AdministrativeSegment ADD CONSTRAINT datetype_administrativesegment_fk
FOREIGN KEY (ExceptionalClearanceDateID)
REFERENCES DateType (DateTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE AdministrativeSegment ADD CONSTRAINT agency_administrativesegment_fk
FOREIGN KEY (AgencyID)
REFERENCES Agency (AgencyID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArrestReportSegment ADD CONSTRAINT agency_arrestreportsegment_fk
FOREIGN KEY (AgencyID)
REFERENCES Agency (AgencyID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE PreCertificationError ADD CONSTRAINT agency_nibrserrorid_fk
FOREIGN KEY (AgencyID)
REFERENCES Agency (AgencyID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE VictimSegment ADD CONSTRAINT officerassignmenttypetype_victimsegment_fk
FOREIGN KEY (OfficerAssignmentTypeTypeID)
REFERENCES OfficerAssignmentTypeType (OfficerAssignmentTypeTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE VictimSegment ADD CONSTRAINT typeofofficeractivitycircumstancetype_victimsegment_fk
FOREIGN KEY (OfficerActivityCircumstanceTypeID)
REFERENCES OfficerActivityCircumstanceType (OfficerActivityCircumstanceTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE TypeOfWeaponForceInvolved ADD CONSTRAINT typeofweaponforceinvolvedtype_typeofweaponforceinvolved_fk
FOREIGN KEY (TypeOfWeaponForceInvolvedTypeID)
REFERENCES TypeOfWeaponForceInvolvedType (TypeOfWeaponForceInvolvedTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE SuspectedDrugType ADD CONSTRAINT suspecteddrugtypetype_suspecteddrugtype_fk
FOREIGN KEY (SuspectedDrugTypeTypeID)
REFERENCES SuspectedDrugTypeType (SuspectedDrugTypeTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE BiasMotivation ADD CONSTRAINT biasmotivationtype_biasmotivation_fk
FOREIGN KEY (BiasMotivationTypeID)
REFERENCES BiasMotivationType (BiasMotivationTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE OffenseSegment ADD CONSTRAINT methodofentrytype_offensesegment_fk
FOREIGN KEY (MethodOfEntryTypeID)
REFERENCES MethodOfEntryType (MethodOfEntryTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE OffenseSegment ADD CONSTRAINT locationtypetype_offensesegment_fk
FOREIGN KEY (LocationTypeTypeID)
REFERENCES LocationTypeType (LocationTypeTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE AdministrativeSegment ADD CONSTRAINT clearedexceptionallytype_adminseg_fk
FOREIGN KEY (ClearedExceptionallyTypeID)
REFERENCES ClearedExceptionallyType (ClearedExceptionallyTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArresteeSegment ADD CONSTRAINT dispositionofarresteeunder18lookup_arresteesegment_fk
FOREIGN KEY (DispositionOfArresteeUnder18TypeID)
REFERENCES DispositionOfArresteeUnder18Type (DispositionOfArresteeUnder18TypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArrestReportSegment ADD CONSTRAINT dispositionofarresteeunder18lookup_arrestreportsegment_fk
FOREIGN KEY (DispositionOfArresteeUnder18TypeID)
REFERENCES DispositionOfArresteeUnder18Type (DispositionOfArresteeUnder18TypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArresteeSegmentWasArmedWith ADD CONSTRAINT arresteearmedwithtypelookup_arresteearmedwith_fk
FOREIGN KEY (ArresteeWasArmedWithTypeID)
REFERENCES ArresteeWasArmedWithType (ArresteeWasArmedWithTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArrestReportSegmentWasArmedWith ADD CONSTRAINT arresteewasarmedwithtype_arrestreportsegmentwasarmedwith_1_fk
FOREIGN KEY (ArresteeWasArmedWithTypeID)
REFERENCES ArresteeWasArmedWithType (ArresteeWasArmedWithTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArresteeSegment ADD CONSTRAINT multiplearresteesegmentsindicatorlookup_arresteesegment_fk
FOREIGN KEY (MultipleArresteeSegmentsIndicatorTypeID)
REFERENCES MultipleArresteeSegmentsIndicatorType (MultipleArresteeSegmentsIndicatorTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArresteeSegment ADD CONSTRAINT typeofarrestlookup_arresteesegment_fk
FOREIGN KEY (TypeOfArrestTypeID)
REFERENCES TypeOfArrestType (TypeOfArrestTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArrestReportSegment ADD CONSTRAINT typeofarrestlookup_arrestreportsegment_fk
FOREIGN KEY (TypeOfArrestTypeID)
REFERENCES TypeOfArrestType (TypeOfArrestTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE VictimSegment ADD CONSTRAINT additionaljustifiablehomicidecircumstancestype_victimsegment_fk
FOREIGN KEY (AdditionalJustifiableHomicideCircumstancesTypeID)
REFERENCES AdditionalJustifiableHomicideCircumstancesType (AdditionalJustifiableHomicideCircumstancesTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE TypeInjury ADD CONSTRAINT typeofinjurylookup_injurytable_fk
FOREIGN KEY (TypeInjuryTypeID)
REFERENCES TypeInjuryType (TypeInjuryTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE VictimSegment ADD CONSTRAINT residentstatusofpersontype_victimsegment_fk
FOREIGN KEY (ResidentStatusOfPersonTypeID)
REFERENCES ResidentStatusOfPersonType (ResidentStatusOfPersonTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArresteeSegment ADD CONSTRAINT residentstatusofpersontype_arresteesegment_fk
FOREIGN KEY (ResidentStatusOfPersonTypeID)
REFERENCES ResidentStatusOfPersonType (ResidentStatusOfPersonTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArrestReportSegment ADD CONSTRAINT residentstatusofpersontype_arrestreportsegment_fk
FOREIGN KEY (ResidentStatusOfPersonTypeID)
REFERENCES ResidentStatusOfPersonType (ResidentStatusOfPersonTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE VictimSegment ADD CONSTRAINT ethnicityofpersontype_victimsegment_fk
FOREIGN KEY (EthnicityOfPersonTypeID)
REFERENCES EthnicityOfPersonType (EthnicityOfPersonTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArresteeSegment ADD CONSTRAINT ethnicityofpersontype_arresteesegment_fk
FOREIGN KEY (EthnicityOfPersonTypeID)
REFERENCES EthnicityOfPersonType (EthnicityOfPersonTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArrestReportSegment ADD CONSTRAINT ethnicityofpersontype_arrestreportsegment_fk
FOREIGN KEY (EthnicityOfPersonTypeID)
REFERENCES EthnicityOfPersonType (EthnicityOfPersonTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE OffenderSegment ADD CONSTRAINT ethnicityofpersontype_offendersegment_fk
FOREIGN KEY (EthnicityOfPersonTypeID)
REFERENCES EthnicityOfPersonType (EthnicityOfPersonTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE VictimSegment ADD CONSTRAINT typeofvictimtype_victimsegment_fk
FOREIGN KEY (TypeOfVictimTypeID)
REFERENCES TypeOfVictimType (TypeOfVictimTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE SuspectedDrugType ADD CONSTRAINT typedrugmeasurementtype_suspecteddrugtype_fk
FOREIGN KEY (TypeDrugMeasurementTypeID)
REFERENCES TypeDrugMeasurementType (TypeDrugMeasurementTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE PropertyType ADD CONSTRAINT propertydescriptiontype_propertytype_fk
FOREIGN KEY (PropertyDescriptionTypeID)
REFERENCES PropertyDescriptionType (PropertyDescriptionTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE VictimSegment ADD CONSTRAINT raceofpersontype_victimsegment_fk
FOREIGN KEY (RaceOfPersonTypeID)
REFERENCES RaceOfPersonType (RaceOfPersonTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE OffenderSegment ADD CONSTRAINT raceofpersontype_offender_fk
FOREIGN KEY (RaceOfPersonTypeID)
REFERENCES RaceOfPersonType (RaceOfPersonTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArresteeSegment ADD CONSTRAINT raceofpersontype_arresteesegment_fk
FOREIGN KEY (RaceOfPersonTypeID)
REFERENCES RaceOfPersonType (RaceOfPersonTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArrestReportSegment ADD CONSTRAINT raceofpersontype_arrestreportsegment_fk
FOREIGN KEY (RaceOfPersonTypeID)
REFERENCES RaceOfPersonType (RaceOfPersonTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE OffenderSegment ADD CONSTRAINT sexcodelookup_offender_fk
FOREIGN KEY (SexOfPersonTypeID)
REFERENCES SexOfPersonType (SexOfPersonTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArrestReportSegment ADD CONSTRAINT sexcodelookup_arrestreportsegment_fk
FOREIGN KEY (SexOfPersonTypeID)
REFERENCES SexOfPersonType (SexOfPersonTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE VictimSegment ADD CONSTRAINT sexofpersontype_victimsegment_fk
FOREIGN KEY (SexOfPersonTypeID)
REFERENCES SexOfPersonType (SexOfPersonTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArresteeSegment ADD CONSTRAINT sexofpersontype_arresteesegment_fk
FOREIGN KEY (SexOfPersonTypeID)
REFERENCES SexOfPersonType (SexOfPersonTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE TypeCriminalActivity ADD CONSTRAINT typeofcriminalactivitytype_typecriminalactivity_fk
FOREIGN KEY (TypeOfCriminalActivityTypeID)
REFERENCES TypeOfCriminalActivityType (TypeOfCriminalActivityTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE OffenderSuspectedOfUsing ADD CONSTRAINT offendersuspectedofusingtype_offendersuspectedofusing_fk
FOREIGN KEY (OffenderSuspectedOfUsingTypeID)
REFERENCES OffenderSuspectedOfUsingType (OffenderSuspectedOfUsingTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE AdministrativeSegment ADD CONSTRAINT segmentactiontype_adminseg_fk
FOREIGN KEY (SegmentActionTypeTypeID)
REFERENCES SegmentActionTypeType (SegmentActionTypeTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE OffenseSegment ADD CONSTRAINT segmentactiontype_offensesegment_fk
FOREIGN KEY (SegmentActionTypeTypeID)
REFERENCES SegmentActionTypeType (SegmentActionTypeTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE PropertySegment ADD CONSTRAINT segmentactiontype_propertysegment_fk
FOREIGN KEY (SegmentActionTypeTypeID)
REFERENCES SegmentActionTypeType (SegmentActionTypeTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE VictimSegment ADD CONSTRAINT segmentactiontype_victimsegment_fk
FOREIGN KEY (SegmentActionTypeTypeID)
REFERENCES SegmentActionTypeType (SegmentActionTypeTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE OffenderSegment ADD CONSTRAINT segmentactiontype_offender_fk
FOREIGN KEY (SegmentActionTypeTypeID)
REFERENCES SegmentActionTypeType (SegmentActionTypeTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArresteeSegment ADD CONSTRAINT segmentactiontype_arresteesegment_fk
FOREIGN KEY (SegmentActionTypeTypeID)
REFERENCES SegmentActionTypeType (SegmentActionTypeTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArrestReportSegment ADD CONSTRAINT segmentactiontype_arrestreportsegment_fk
FOREIGN KEY (SegmentActionTypeTypeID)
REFERENCES SegmentActionTypeType (SegmentActionTypeTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE PreCertificationError ADD CONSTRAINT segmentactiontypetype_nibrserrorid_fk
FOREIGN KEY (SegmentActionTypeTypeID)
REFERENCES SegmentActionTypeType (SegmentActionTypeTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE VictimOffenderAssociation ADD CONSTRAINT victimoffenderrelationshiptype_victimassociation_fk
FOREIGN KEY (VictimOffenderRelationshipTypeID)
REFERENCES VictimOffenderRelationshipType (VictimOffenderRelationshipTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE AggravatedAssaultHomicideCircumstances ADD CONSTRAINT aahomicidecircumstancestype_aahomicidecircumstances_fk
FOREIGN KEY (AggravatedAssaultHomicideCircumstancesTypeID)
REFERENCES AggravatedAssaultHomicideCircumstancesType (AggravatedAssaultHomicideCircumstancesTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE PropertySegment ADD CONSTRAINT typepropertylossetctype_propertysegment_fk
FOREIGN KEY (TypePropertyLossEtcTypeID)
REFERENCES TypePropertyLossEtcType (TypePropertyLossEtcTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE OffenseSegment ADD CONSTRAINT ucroffensecodetype_offensesegment_fk
FOREIGN KEY (UCROffenseCodeTypeID)
REFERENCES UCROffenseCodeType (UCROffenseCodeTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArresteeSegment ADD CONSTRAINT ucroffensecodetype_arresteesegment_fk
FOREIGN KEY (UCROffenseCodeTypeID)
REFERENCES UCROffenseCodeType (UCROffenseCodeTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArrestReportSegment ADD CONSTRAINT ucroffensecodetype_arrestreportsegment_fk
FOREIGN KEY (UCROffenseCodeTypeID)
REFERENCES UCROffenseCodeType (UCROffenseCodeTypeID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArrestReportSegmentWasArmedWith ADD CONSTRAINT arrestreportsegment_arrestreportsegmentwasarmedwith_1_fk
FOREIGN KEY (ArrestReportSegmentID)
REFERENCES ArrestReportSegment (ArrestReportSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE LEOKASegment ADD CONSTRAINT adminseg_leokasegment_fk
FOREIGN KEY (AdministrativeSegmentID)
REFERENCES AdministrativeSegment (AdministrativeSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ZeroReportingSegment ADD CONSTRAINT adminseg_zeroreportingsegment_fk
FOREIGN KEY (AdministrativeSegmentID)
REFERENCES AdministrativeSegment (AdministrativeSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE OffenseSegment ADD CONSTRAINT administrativesegment_offensesegment_fk
FOREIGN KEY (AdministrativeSegmentID)
REFERENCES AdministrativeSegment (AdministrativeSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE PropertySegment ADD CONSTRAINT administrativesegment_propertysegment_fk
FOREIGN KEY (AdministrativeSegmentID)
REFERENCES AdministrativeSegment (AdministrativeSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE VictimSegment ADD CONSTRAINT administrativesegment_victimsegment_fk
FOREIGN KEY (AdministrativeSegmentID)
REFERENCES AdministrativeSegment (AdministrativeSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE OffenderSegment ADD CONSTRAINT administrativesegment_offender_fk
FOREIGN KEY (AdministrativeSegmentID)
REFERENCES AdministrativeSegment (AdministrativeSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArresteeSegment ADD CONSTRAINT administrativesegment_arresteesegment_fk
FOREIGN KEY (AdministrativeSegmentID)
REFERENCES AdministrativeSegment (AdministrativeSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE ArresteeSegmentWasArmedWith ADD CONSTRAINT arresteesegment_arresteewasarmedwith_fk
FOREIGN KEY (ArresteeSegmentID)
REFERENCES ArresteeSegment (ArresteeSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE VictimOffenderAssociation ADD CONSTRAINT offendersegment_segmentassociation_fk
FOREIGN KEY (OffenderSegmentID)
REFERENCES OffenderSegment (OffenderSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE VictimOffenderAssociation ADD CONSTRAINT victimsegment_ucroffensecodeassociation_fk
FOREIGN KEY (VictimSegmentID)
REFERENCES VictimSegment (VictimSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE AggravatedAssaultHomicideCircumstances ADD CONSTRAINT victimsegment_aggravatedassaulthomicidecircumstances_fk
FOREIGN KEY (VictimSegmentID)
REFERENCES VictimSegment (VictimSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE TypeInjury ADD CONSTRAINT victimsegment_injurytable_fk
FOREIGN KEY (VictimSegmentID)
REFERENCES VictimSegment (VictimSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE VictimOffenseAssociation ADD CONSTRAINT victimsegment_victimoffenseassociation_fk
FOREIGN KEY (VictimSegmentID)
REFERENCES VictimSegment (VictimSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE SuspectedDrugType ADD CONSTRAINT propertysegment_suspecteddrugtype_fk
FOREIGN KEY (PropertySegmentID)
REFERENCES PropertySegment (PropertySegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE PropertyType ADD CONSTRAINT propertysegment_propertytype_fk
FOREIGN KEY (PropertySegmentID)
REFERENCES PropertySegment (PropertySegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE OffenderSuspectedOfUsing ADD CONSTRAINT offensesegment_offendersuspectedofusing_fk
FOREIGN KEY (OffenseSegmentID)
REFERENCES OffenseSegment (OffenseSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE TypeCriminalActivity ADD CONSTRAINT offensesegment_typecriminalactivity_fk
FOREIGN KEY (OffenseSegmentID)
REFERENCES OffenseSegment (OffenseSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE TypeOfWeaponForceInvolved ADD CONSTRAINT offensesegment_typeofweaponforceinvolved_fk
FOREIGN KEY (OffenseSegmentID)
REFERENCES OffenseSegment (OffenseSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE VictimOffenseAssociation ADD CONSTRAINT offensesegment_victimoffenseassociation_fk
FOREIGN KEY (OffenseSegmentID)
REFERENCES OffenseSegment (OffenseSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE BiasMotivation ADD CONSTRAINT offensesegment_biasmotivation_fk
FOREIGN KEY (OffenseSegmentID)
REFERENCES OffenseSegment (OffenseSegmentID)
ON DELETE NO ACTION
ON UPDATE NO ACTION;

ALTER TABLE administrativesegment ADD INDEX `idx_incidentnum_agencyid` (`AgencyID` ASC,`IncidentNumber` ASC);
ALTER TABLE arrestreportsegment ADD INDEX `idx_atn_agencyid` (`AgencyID` ASC, `ArrestTransactionNumber` ASC);
ALTER TABLE administrativesegment ADD INDEX `idx_incidentnum_agencyid_ownerId` (`AgencyID` ASC,`IncidentNumber` ASC, `ownerId` asc);
ALTER TABLE arrestreportsegment ADD INDEX `idx_atn_agencyid_ownerId` (`AgencyID` ASC, `ArrestTransactionNumber` ASC, `ownerId` asc);