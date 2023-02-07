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
package org.search.nibrs.model.codes;

public enum NIBRSErrorCode {


/**
 * 028, 029 cannot be implemented	
 */
	_017("017","Structure Check","CANNOT HAVE CHARACTERS OTHER THAN A-Z, 0-9, HYPHENS, AND/OR BLANKS","Zero-Reporting Segment (Level 0). Although Data Element 2 (Incident Number) should be entered with 12 zeros, a pre-edit discovered characters other than A through Z, 0 through 9, hyphens, and/or blanks had been entered."),
	_050("050","Structure Check","SEGMENT LEVELS ARE OUT OF NUMERICAL ORDER FOR THIS INCIDENT","Segment Levels in a Group A Incident Report must be organized in numerical order. For example, an incident having segments 1, 2, 2, 3, 4, 4, 4, 5 must be written in that order, not as 1, 2, 2, 5, 3, 4, 4, 4."),
	_051("051","Structure Check","INVALID RECORD LEVEL ON SUBMISSION","Segment Level must contain data values 0–7."),
/**
 * 052 not in 3.1	
 */
	_052("052","Structure Check","NOT A VALID ORI–NOT IN UCR ORI FILE","Data Element 1 (ORI) and Data Element 25C (Officer–ORI Other Jurisdiction) must be a valid nine-character NCIC ORI."),
/**
 * 055 not implemented	
 */
	_055("055","Structure Check","CANNOT HAVE A GROUP A INCIDENT REPORT WITHOUT LEVEL 1 SEGMENT","Segment Level 1 (Administrative Segment) with Segment Action Type I=Incident Report must be the first segment submitted for each Group A Incident Report."),
/**
 * 056 not implemented	
 */
	_056("056","Structure Check","DUPLICATE INCIDENT– PREVIOUSLY ADDED","Data Element 2 (Incident Number) must be a unique number for each incident submitted. No two incidents can have the same incident number."),
/**
 * 058 not implemented	
 */
	_058("058","Structure Check","ALL SEGMENTS IN A SUBMISSION MUST HAVE SAME MONTH AND YEAR OF SUBMISSION","Month of Submission and Year of Submission must contain the same data values for each segment in a NIBRS submission. The first segment processed will be compared with all other segments to check for this condition."),
/**
 * 059 not implemented	
 */
	_059("059","Structure Check","ALL SEGMENTS IN SUBMISSION MUST BE FROM SAME STATE","Data Element 1 (ORI) must contain the same state abbreviation code (e.g., SC, MD, etc.) in the first two positions (record positions 17 & 18). For nonfederal LEAs, every segment in a submission must have the same state code in the first two positions of the ORI."),
/**
 * 060 not implemented	
 */
	_060("060","Structure Check","PROCESSING DATE PRECEDES MONTH AND YEAR OF SUBMISSION","Month of Submission and Year of Submission must precede the date the FBI receives and processes a NIBRS submission. This edit checks for data submitted for a future month/year."),
	_065("065","Structure Check","EACH LEVEL 2 OFFENSE MUST HAVE AT LEAST ONE VICTIM","Segment Level 2 (Offense Segment) must have at least one Segment Level 4 (Victim Segment) connected to it by entering the offense code identified in Data Element 6 (UCR Offense Code) in Data Element 24 (Victim Connected to UCR Offense Code)."),
	_070("070","Structure Check","THE CORRESPONDING OFFENDER RECORD MUST BE PRESENT","Data Element 34 (Offender Numbers To Be Related) has a value that does not have a corresponding Offender Segment. For example, if the field value shown in Data Element 34 is 15, an Offender Segment does not exist with a value of 15 in Data Element 36 (Offender Sequence Number)."),
	_071("071","Structure Check","CANNOT HAVE ARRESTS IF CLEARED EXCEPTIONALLY","Segment Level 6 (Arrestee Segment) with Segment Action Type I=Incident Report cannot be submitted with Data Element 42 (Arrest Date) containing an arrest date on or earlier than the date entered in Data Element 5 (Exceptional Clearance Date) when Data Element 4 (Cleared Exceptionally) contains a data value other than N=Not Applicable (indicating the incident is cleared exceptionally)."),
	_072("072","Structure Check","RECOVERED PROPERTY MUST ALSO BE REPORTED AS STOLEN","Segment Level 3 (Property Segment) must ALSO be submitted with Data Element 14 (Type Property Loss/Etc.) as 7=Stolen/Etc. before it can be submitted as 5=Recovered for the same property in Data Element 15 (Property Description). Any property being reported as recovered must first be reported as stolen. There are three exceptions to this rule: 1) When recovered property information is submitted as Segment Action Type A=Add and Data Element 2 (Incident Number) is not on file in the national UCR database. This condition may indicate recovered property is being reported for a pre-NIBRS incident; therefore, the stolen property information will not be on file in the national database. 2) When Data Element 6 (UCR Offense Code) contains an offense that allows property to be recovered without first being stolen in that same incident (i.e., 250=Counterfeiting/Forgery and 280=Stolen Property Offenses) 3) When a vehicle was stolen and the recovered property in Data Element 15 (Property Description) is 38=Vehicle Parts/Accessories"),
	_073("073","Structure Check","NUMBER OF RECOVERED VEHICLES CANNOT BE GREATER THAN THE NUMBER STOLEN","Segment Level 3 (Property Segment) with Segment Action Type I=Incident Report must contain a data value in Data Element 18 (Number of Stolen Motor Vehicles) greater than or equal to the data value entered in Data Element 19 (Number of Recovered Motor Vehicles) within the same incident."),
	_074("074","Structure Check","PROPERTY SEGMENT MUST EXIST WITH THIS OFFENSE","Segment Level 3 (Property Segment) with Segment Action Type I=Incident Report must be submitted when Data Element 6 (UCR Offense Code) contains an offense of Kidnapping/Abduction , Crimes Against Property, Drug/Narcotic Offenses, or Gambling Offenses."),
	_075("075","Structure Check","MISSING A MANDATORY SEGMENT LEVEL FOR A COMPLETE INCIDENT","Segment Levels 1, 2, 4, and 5 (Administrative Segment, Offense Segment, Victim Segment, and Offender Segment) with Segment Action Type I=Incident Report must be submitted for each Group A Incident Report; they are mandatory."),
	_076("076","Structure Check","PROPERTY RECORD (LEVEL 3) CAN NOT EXIST WITH OFFENSES SUBMITTED","Segment Level 3 (Property Segment) with Segment Action Type I = Incident Report cannot be submitted unless Data Element 6 (UCR Offense Code) contains an offense of Crimes Against Property, 100 = Kidnapping/Abduction, 35A = Drug/Narcotic Offenses, 521 = Violation of National Firearm Act of 1934*, 522 = Weapons of Mass Destruction*, 526 = Explosives*, 26H = Money Laundering*, Commerce Violations*, or Gambling Offenses.\n "
			+ "*Denotes offenses for federal and tribal LEA reporting only"),
	_077("077","Structure Check","NEED A PROPERTY LOSS CODE OF '1' OR '8' WHEN THIS OFFENSE IS ATTEMPTED","Data Element 7 (Offense Attempted/Completed) is A=Attempted and Data Element 6 (UCR Offense Code) is a Crime Against Property, Gambling, Kidnapping, or Drug/Narcotic Offense. However, there is no Data Element 14 (Type Property Loss/Etc.) of 1=None or 8=Unknown."),
	_078("078","Structure Check","A VALID PROPERTY LOSS CODE DOES NOT EXIST FOR THIS COMPLETED OFFENSE","If Data Element 6 (UCR Offense Code) is a Crime Against Property, Kidnaping, Gambling, or Drug/Narcotic Offense, and Data Element 7 (Offense Attempted/Completed) is C=Completed, a Property Segment (Level 3) must be sent with a valid code in Data Element 14 (Type Property Loss/Etc.)."),
	_080("080","Structure Check","CRIMES AGAINST SOCIETY CAN HAVE ONLY ONE SOCIETY/PUBLIC VICTIM","Segment Level 4 (Victim Segment) can be submitted only once and Data Element 25 (Type of Victim) must be S=Society/Public when Data Element 6 (UCR Offense Code) contains only a Crime Against Society."),
	_081("081","Structure Check","THIS PROPERTY LOSS CODE CAN NOT EXIST WITH THE OFFENSES SUBMITTED","Data Element 14 (Type Property Loss/Etc.) must be 1=None or 8=Unknown when Data Element 6 (UCR Offense Code) contains an offense of Kidnapping/Abduction, Crimes against Property, Drug/Narcotic Offenses, or Gambling Offenses and Data Element 7 (Offense Attempted/Completed) is A=Attempted. Data Element 14 (Type Property Loss/Etc.) must be 1=None or 5=Recovered when Data Element 6 (UCR Offense Code) is 280=Stolen Property Offenses and Data Element 7 (Offense Attempted/Completed) is C=Completed. Data Element 14 (Type Property Loss/Etc.) must be 1=None, 5=Recovered, 7=Stolen/Etc., or 8=Unknown when Data Element 6 (UCR Offense Code) is 100=Kidnapping/Abduction, 220=Burglary/ Breaking & Entering, or 510=Bribery and Data Element 7 (Offense Attempted/Completed) is C=Completed. Data Element 14 (Type Property Loss/Etc.) must be 1=None or 6=Seized when Data Element 6 (UCR Offense Code) is 35A=Drug/ Narcotic Violations or 35B=Drug Equipment Violations and Data Element 7 (Offense Attempted/Completed) is C=Completed. Data Element 14 (Type Property Loss/Etc.) must be 2=Burned when Data Element 6 (UCR Offense Code) is 200=Arson and Data Element 7 (Offense Attempted/Completed) is C=Completed. Data Element 14 (Type Property Loss/Etc.) must be 3=Counterfeited/Forged, 5=Recovered, or 6=Seized when Data Element 6 (UCR Offense Code) is 250=Counterfeiting/Forgery and Data Element 7 (Offense Attempted/Completed) is C=Completed. Data Element 14 (Type Property Loss/Etc.) must be 4=Destroyed/Damaged/Vandalized when Data Element 6 (UCR Offense Code) is 290=Destruction/Damage/Vandalism of Property and Data Element 7 (Offense Attempted/Completed) is C=Completed. Data Element 14 (Type Property Loss/Etc.) must be 5=Recovered or 7=Stolen/Etc. when Data Element 6 (UCR Offense Code) is any of the following and Data Element 7 (Offense Attempted/Completed) is C=Completed: 120=Robbery 210=Extortion/Blackmail 23A=Pocket-picking 23B=Purse Snatching 23C=Shoplifting 23D=Theft from Building 23E=Theft from Coin-Operated Machine or Device 23F=Theft from Motor Vehicle 23G=Theft of Motor Vehicle Parts or Accessories 23H=All other Larceny 240=Motor Vehicle Theft 26A=False Pretenses/Swindle/Confidence Game 26B=Credit Card/Automated Teller Machine Fraud 26C=Impersonation 26D=Welfare Fraud 26E=Wire Fraud270=Embezzlement Data Element 14 (Type Property Loss/Etc.) must be 6=Seized when Data Element 6 (UCR Offense Code) is any of the following and Data Element 7 (Offense Attempted/Completed) is C=Completed: 39A=Betting/W agering 39B=Operating/Promoting/Assisting Gambling 39C=Gambling Equipment Violation 39D=Sports Tampering"),
	_084("084","Structure Check","RECOVERED PROPERTY VALUE CAN NOT BE GREATER THAN THE VALUE WHEN STOLEN","Data Element 16 (Value of Property) for property classified as 7=Stolen/Etc. in Data Element 14 (Type Property Loss/Etc.) must be greater than or equal to the value entered in Data Element 16 (Value of Property) for property classified as 5=Recovered for the same property specified in Data Element 15 (Property Description) in an incident. Note: This edit also applies when a vehicle was stolen and the recovered property in Data Element 15 (Property Description) is 38=Vehicle Parts/Accessories. The value of recovered parts cannot exceed the value of stolen vehicles."),
	_085("085","Structure Check","EACH VICTIM MUST BE CONNECTED TO AT LEAST TWO OFFENDERS. (The current FBI validation routine does not require victims to be associated with offenders for property offenses, but this edit will be enforced in the future. At this time, this error will not be generated in files submitted to the FBI unless the associated offense is a crime against person or robbery.)","Segment Level 4 (Victim Segment) with a data value in Data Element 24 (Victim Connected to UCR Offense Code) of a Crime Against Person or Robbery must contain at least two offender sequence numbers in Data Element 34 (Offender Number to be Related) when there are three or more Segment Level 5 (Offender Segment) records submitted for the incident."),
/**
 * 088 not implemented	
 */
	_088("088","Structure Check","GROUP A AND GROUP B ARREST REPORTS CANNOT HAVE SAME IDENTIFIER","Segment Level 6 (Arrestee Segment) and Segment Level 7 (Group B Arrest Report Segment) cannot have the same data values entered in Data Element 2 (Incident Number) and Data Element 41 (Arrest Transaction Number), respectively, for the same ORI."),
/**
 * 090-94 not implemented	
 */
/**
 * 096 cannot be validated
 */
	_091("091","Structure Check","ZERO-REPORTING YEAR IS INVALID","A Segment Level 0 was submitted that did not have four numeric digits in positions 40 through 43."),
	_099("099","Structure Check","GROUP B ARREST REPORT CAN HAVE ONLY ONE ARRESTEE","Only one arrestee can be reported in a group B arrest report."),
	_101("101","Admin Segment","MUST BE PRESENT— MANDATORY FIELD","The referenced data element in a Group A Incident Report must contain data when the referenced data element is mandatory or when the conditions are met for data that must be entered into a conditionally mandatory field."),
	_104("104","Admin Segment","INVALID DATA VALUE—NOT ON FBI VALIDATION TABLE","The referenced data element must contain a valid data value when it is entered."),
	_105("105","Admin Segment","INVALID DATA VALUE FOR DATE","The data element in error contains a date that is not entered correctly. Each component of the date must be valid; that is, months must be 01 through 12, days must be 01 through 31, and year must include the century (i.e., 19xx, 20xx). In addition, days cannot exceed maximum for the month (e.g., June cannot have 31days). Also, the date cannot exceed the current date."),
	_106("106","Admin Segment","INVALID DATA VALUE FOR HOUR","For Offenses of 09A, 13A, 13B and 13C ONLY–When data element 25 (Type of Victim) = L (Law Enforcement Officer) then Data Element 3 (Incident Date/Hour) must be populated with a valid hour (00-23). Incident Hour Unknown (Blank) is not a valid entry."),
	_115("115","Admin Segment","CANNOT HAVE EMBEDDED BLANKS BETWEEN FIRST AND LAST NON-BLANK CHARACTERS","(Incident Number) Must be blank right-fill if under 12 characters in length. Cannot have embedded blanks between the first and last characters entered."),
/**
 * 116 not implemented
 */
	_116("116","Admin Segment","MUST BE LEFT-JUSTIFIED– BLANK DETECTED IN FIRST POSITION","(Incident Number) must be left-justified with blank right-fill. Since the number is less than 12 characters, it must begin in position 1"),
	_117("117","Admin Segment","CANNOT HAVE CHARACTERS OTHER THAN A–Z, 0–9, AND/OR HYPHEN, AND/OR BLANKS","(Incident Number) can only have character combinations of A through Z, 0 through 9, hyphens, and/or blanks. For example, 89-123-SC is valid, but 89+123*SC is invalid."),
/**
 * 118 cannot be validated	
 */
	_118("118","Admin Segment","DATE CANNOT BE ON OR AFTER THE INACTIVE DATE [yyyymmdd] OF THE ORI","The UCR Program has determined that an ORI will no longer be submitting data to the FBI as of an inactive date. No data from this ORI will be accepted after this date."),
	_119("119","Admin Segment","CARGO THEFT DATA CAN ONLY BE SUBMITTED FOR SPECIFIC OFFENSES","Data Element 2A (Cargo Theft) must be blank, unless Data Element 6 (UCR Offense Code) includes at least one of the following:120=Robbery 210=Extortion/Blackmail 220=Burglary/Breaking & Entering 23D=Theft From Building 23F=Theft From Motor Vehicle 24H=All Other Larceny 240=Motor Vehicle Theft 26A=False Pretenses/Swindle/Confidence Game 26B=Credit Card/Automated Teller Machine Fraud 26C=Impersonation 26E=Wire Fraud 26F = Identity Theft 26G = Hacking/Computer Invasion 270=Embezzlement 510=Bribery"),
	_122("122","Admin Segment","CARGO THEFT VALUE REQUIRED WITH SPECIFIC OFFENSES","Data Element 2A (Cargo Theft) must be populated with a Y = Yes or N = No when Data Element 6 (UCR Offense Code) includes at least one of the following: 120=Robbery 210=Extortion/Blackmail 220=Burglary/Breaking & Entering 23D=Theft From Building 23F=Theft From Motor Vehicle 24H=All Other Larceny 240=Motor Vehicle Theft 26A=False Pretenses/Swindle/Confidence Game 26B=Credit Card/Automated Teller Machine Fraud 26C=Impersonation 26E=Wire Fraud 26F = Identity Theft 26G = Hacking/Computer Invasion 270=Embezzlement 510=Bribery"),
/**
 * 151 not implemented	
 */
	_151("151","Admin Segment","REPORT INDICATOR MUST BE BLANK OR R","This field must be blank if the incident date is known. If the incident date is unknown, then the report date would be entered instead and must be indicated with an R in the Report Indicator field within the Administrative Segment."),
	_152("152","Admin Segment","INVALID HOUR ENTRY","If Hour is entered within Data Element 3 (Incident Date/Hour), it must be 00 through 23. If 00=Midnight is entered, be careful that the Incident Date is entered as if the time was 1 minute past midnight. Note: When an incident occurs exactly at midnight, Data Element 3 (Incident Date) would be entered as if the time is 1 minute past midnight. For example, when a crime occurred exactly at midnight on Thursday, Friday's date would be entered."),
	_153("153","Admin Segment","VALUE ENTERED CONFLICTS WITH PRESENCE OF AN ENTRY IN EXCEPTIONAL CLEARANCE DATE","Data Element 4 (Cleared Exceptionally) cannot be N=Not Applicable if Data Element 5 (Exceptional Clearance Date) is entered."),
	_155("155","Admin Segment","CLEARANCE DATE [yyyymmdd] PREDATES INCIDENT DATE [yyyymmdd]","Data Element 5 (Exceptional Clearance Date) is earlier than Data Element 3 (Incident Date/Hour)."),
	_156("156","Admin Segment","AN ENTRY MUST BE MADE WHEN CLEARED EXCEPTIONALLY HAS ENTRIES OF A-E","Data Element 5 (Exceptional Clearance Date) must be present if the case was cleared exceptionally. Data Element 4 (Cleared Exceptionally) has an entry of A through E; therefore, the date must also be entered."),
	_170("170","Admin Segment","INCIDENT DATE CANNOT BE AFTER YEAR [yyyy] AND MONTH [mm] OF ELECTRONIC SUBMISSION","Data Element 3 The date cannot be later than the year and month the electronic submission represents. For example, the May 1999 electronic submission cannot contain incidents happening after this date."),
/**
 * 171 not in 3.1	
 */
	_171("171","Admin Segment","INCIDENT DATE IS OUTSIDE THE BASE DATE CALCULATION","A Group 'A'Incident Report was submitted with a date entered into Data Element 3 (Incident Date/Hour) that is earlier than January 1 of the previous year, using the Month of Tape and Year of Tape as a reference point. For example, if the Month of Tape and Year of Tape contain a value of 01/1999, but the incident date is 12/25/1997, the incident will be rejected. Volume 2, section I, provides specifications concerning the FBI's 2- year database. Note: The exception is when an exceptional clearance is being submitted with a Segment Action Type of W=Time-Window Submission. The incident date may be any past date, but cannot be any earlier than January 1, 1950."),
	_172("172","Admin Segment","INCIDENT DATE/HOUR FOR 'I'RECORDS CANNOT PREDATE 01/01/1991","Data Element 3 (Incident Date) cannot be earlier than 01/01/1991. This edit will preclude dates that are obviously incorrect since the FBI began accepting NIBRS data on this date."),
/**
 * 173 cannot be implemented	
 */
	_173("173","Admin Segment","INCIDENT DATE CANNOT BE BEFORE DATE ORI WENT IBR","A Group 'A'Incident Report was submitted with Data Element 3 (Incident Date/Hour) containing a date that occurred before the agency converted over to NIBRS. Because of this, the record was rejected. At some point, the participant will convert its local agencies from Summary reporting to Incident-Based Reporting. Once the participant starts to send NIBRS data for a converted agency to the FBI, any data received from this agency that could possibly be interpreted as duplicate reporting within both the Summary System and NIBRS for the same month will be rejected by the FBI. In other words, if the participant sends IBR data for an agency for the first time on September 1999, monthly submittal, dates for incidents, recovered property, and arrests must be within September. The exception is when exceptional clearances occur for a pre-IBR incident. In this case, Data Element 3 (Incident Date/Hour) may be earlier than September 1999, but Data Element 5 (Exceptional Clearance Date) must be within September 1999. The FBI will reject data submitted for prior months. Thereafter, all data coming from this agency must have dates subsequent to the initial start date of September 1999, except as mentioned previously. The Summary System already contains aggregate data for the months prior to NIBRS conversion. If the FBI were to accept IBR data for months previously reported with Summary data, the result would be duplicate reporting for those months."),
/**
 * 175 not implemented	
 */
	_175("175","Admin Segment","CANNOT CALCULATE BASE DATE FROM INCIDENT DATE [yyymmdd]","The electronic submission control date (positions 7 through 12, month and year) and Data Element 3 (Incident Date/Hour) must both be valid dates for calculating timeframes."),
	_178("178","Admin Segment","THIS ADMINISTRATIVE SEGMENT HAS A CONFLICTING LENGTH","Segment Length for the Administrative Segment (Level 1) must be 87 characters (not reporting Cargo Theft) or 88 characters (reporting Cargo Theft). All Administrative Segments in a submission must be formatted in only one of these two lengths."),
/**
 * 197 not implemented	
 */
	_197("197","Admin Segment","MISSING INCIDENT DATE FOR DELETE","Data Element 3 (Incident Date) is missing for a Group A Incident Report with a Segment Action Type of D = Delete; must be populated with a valid data value and cannot be blank."),
/**
 * 198 cannot be validated
 */
/**
 * 199 cannot be validated
 */
	_201("201","Offense Segment","MUST BE PRESENT— MANDATORY FIELD","The referenced data element in an Incident must contain data when the referenced data element is mandatory or when the conditions are met for data that must be entered into a conditionally mandatory field."),
/**
 * 202 not implemented	
 */
	_202("202","Offense Segment","CONTAINS NONNUMERIC ENTRY","Data Element 10 (Number of Premises Entered) is not a numeric entry of 01 through 99."),
	_204("204","Offense Segment","INVALID DATA VALUE","The referenced data element must contain a valid data value when it is entered; blank is permissible on non-mandatory fields."),
	_205("205","Offense Segment","ERROR–INVALID OFFENSE CODE FOR LOCATION TYPE CYBERSPACE","Data Element 9 (Location Type)=Cyberspace Can only be entered when Data Element 6 Offense Code is one of the violations listed below: 210=Extortion/Blackmail 250=Counterfeiting/Forgery 270=Embezzlement 280=Stolen Property Offenses 290=Destruction/Damage/Vandalism of Property 370=Pornography/Obscene Material 510=Bribery 26A =False Pretenses/Swindle/Confidence Game 26B =Credit Card/Automated Teller Machine Fraud 26C =Impersonation 26D =Welfare Fraud 26E =Wire Fraud 26F =Identity Theft 26G =Hacking/Computer Invasion 9A =Betting/Wagering 39B =Operating/Promoting/Assisting Gambling 39D =Gambling Equipment Violations 13C =Intimidation 35A =Drug/Narcotic Violations 35B =Drug Equipment Violations 520=Weapon Law Violations 64A =Human Trafficking, Commercial Sex Acts 64B =Human Trafficking, Involuntary Servitude 40A =Prostitution 40B =Assisting or Promoting Prostitution 40C =Purchasing Prostitution"),
	_206("206","Offense Segment","ERROR - DUPLICATE VALUE=[value]","The referenced data element in error is one that contains multiple data values. When more than one code is entered, none can be duplicate codes."),
	_207("207","Offense Segment","ERROR - MUTUALLY EXCLUSIVE VALUE=[value]","The data element in error can have multiple data values and was entered with multiple values. However, mutually exclusive values cannot be entered with any other data value. Refer to individual data elements for mutually exclusive data values."),
/**
 * 215 not implemented	
 */
	_215("215","Offense Segment","CANNOT CONTAIN EMBEDDED BLANKS BETWEEN FIRST AND LAST NON-BLANK CHARACTERS","Must be blank right-fill if under 12 characters in length. Cannot have embedded blanks between the first and last characters entered."),
/**
 * 216 not implemented	
 */
	_216("216","Offense Segment","MUST BE LEFT-JUSTIFIED– BLANK DETECTED IN FIRST POSITION","Must be left-justified with blank right-fill if under 12 characters in length."),
/**
 * 217 not implemented	
 */
	_217("217","Offense Segment","CANNOT HAVE CHARACTERS OTHER THAN A–Z, 0–9, AND/OR HYPHEN, AND/OR BLANKS","Must contain a valid character combination of the following: A–Z (capital letters only) 0–9 Hyphen Example: 11-123-SC is valid, but 11+123*SC is not valid."),
	_219("219","Offense Segment","DATA CAN ONLY BE ENTERED FOR SPECIFIC OFFENSES","Data Element 12 (Type Criminal Activity/Gang Information) Type criminal activity codes of 'B', 'C', 'D', 'E', 'O', 'P', 'T', or 'U'can only be entered when the UCR Offense Code is: 250=Counterfeiting/Forgery 280=Stolen Property Offenses 35A=Drug/Narcotic Violations 35B=Drug Equipment Violations 39C=Gambling Equipment Violations 370=Pornography/Obscene Material 520=Weapon Law Violations (Type Criminal Activity/Gang Information) Gang information codes of 'J', 'G', and 'N'can only be entered when the UCR Offense Code is:09A=Murder and Non-negligent Manslaughter 09B=Negligent Manslaughter 100=Kidnapping/Abduction 11A=Rape 11B=Sodomy 11C=Sexual Assault With An Object 11D=Fondling 120=Robbery 13A=Aggravated Assault 13B=Simple Assault 13C=Intimidation (Type Criminal Activity/Gang Information) Criminal Activity codes of 'A', 'F', 'I', and 'S'can only be entered when the UCR Offense Code is: 720=Animal Cruelty"),
	_220("220","Offense Segment","DATA MUST BE ENTERED FOR SPECIFIC OFFENSES","Data Element 12 (Type Criminal Activity/Gang Information) Must be populated with a valid data value and cannot be blank when Data Element 6 (UCR Offense Code) is: 250=Counterfeiting/Forgery 280=Stolen Property Offenses 35A=Drug/Narcotic Violations 35B=Drug Equipment Violations 39C=Gambling Equipment Violations 370=Pornography/Obscene Material 520=Weapon Law Violations 720=Animal Cruelty"),
	_221("221","Offense Segment","DATA MUST BE ENTERED FOR SPECIFIC OFFENSES","Data Element 13 (Type Weapon/Force Involved) must be populated with a valid data value and cannot be blank when Data Element 6 (UCR Offense Code) is: 09A=Murder and Non-negligent Manslaughter 09B=Negligent Manslaughter 09C=Justifiable Homicide 100=Kidnapping/Abduction 11A=Rape 11B=Sodomy 11C=Sexual Assault With An Object 11D=Fondling 120=Robbery 13A=Aggravated Assault 13B=Simple Assault 210=Extortion/Blackmail 520=Weapon Law Violations 64A=Human Trafficking, Commercial Sex Acts 64B=Human Trafficking, Involuntary Servitude"),
	_222("222","Offense Segment","DATA CAN ONLY BE BE ENTERED FOR SPECIFIC OFFENSES","Data Element 13 (Type Weapon/Force Involved) must be populated with a valid data value and cannot be blank when Data Element 6 (UCR Offense Code) is: 09A=Murder and Non-negligent Manslaughter 09B=Negligent Manslaughter 09C=Justifiable Homicide 100=Kidnapping/Abduction 11A=Rape 11B=Sodomy 11C=Sexual Assault With An Object 11D=Fondling 120=Robbery 13A=Aggravated Assault 13B=Simple Assault 210=Extortion/Blackmail 520=Weapon Law Violations 64A=Human Trafficking, Commercial Sex Acts 64B=Human Trafficking, Involuntary Servitude"),
	_251("251","Offense Segment","INVALID CODE","(Offense Attempted/Completed) Must be a valid code of A=Attempted or C=Completed."),
	_252("252","Offense Segment","OFFENSE CODE MUST BE 220 WITH A LOCATION TYPE OF 14 OR 19 FOR DATA TO BE ENTERED","When Data Element 10 (Number of Premises Entered) is entered, Data Element 9 (Location Type) must be 14=Hotel/Motel/Etc. or 19=Rental Storage Facility, and Data Element 6 (UCR Offense Code) must be 220 (Burglary)."),
	_253("253","Offense Segment","MUST BE PRESENT WHEN OFFENSE CODE IS 220","Data Element was not entered; it must be entered when UCR Offense Code of 220=Burglary has been entered."),
	_254("254","Offense Segment","MUST BE BLANK WHEN OFFENSE IS OTHER THAN 220","Data Element only applies to UCR Offense Code of 220=Burglary. Since a burglary offense was not entered, the Method of Entry should not have been entered."),
	_255("255","Offense Segment","AUTOMATIC INDICATOR MUST BE BLANK OR 'A'","Must be A=Automatic or blank=Not Automatic"),
	_256("256","Offense Segment","OFFENSE CODES OF 09A, 09B, 09C, 13A, 13B, AND 13C MUST HAVE ENTRY OF 'C'","Code must be C=Completed if Data Element 6 (UCR Offense Code) is an Assault or Homicide."),
	_257("257","Offense Segment","MUST BE PRESENT WITH AN OFFENSE CODE OF 220 AND A LOCATION TYPE OF 14 OR 19","Must be entered if offense code is 220 (Burglary) and if Data Element 9 (Location Type) contains 14=Hotel/Motel/Etc. or 19=Rental Storage Facility."),
	_258("258","Offense Segment","WEAPON TYPE MUST=11, 12, 13, 14, OR 15 FOR AN 'A' IN THE AUTO INDICATOR","In Data Element 13 (Type of Weapon/Force Involved), A=Automatic is the third character of code. It is valid only with the following codes: 11=Firearm (Type Not Stated) 12=Handgun 13=Rifle 14=Shotgun 15=Other Firearm A weapon code other than those mentioned was entered with the automatic indicator. An automatic weapon is, by definition, a firearm."),
	_262("262","Offense Segment","DUPLICATE OFFENSE SEGMENT","When a Group 'A' Incident Report is submitted, the individual segments comprising the incident cannot contain duplicates. In this case, two Offense Segments were submitted having the same offense in Data Element 6 (UCR Offense Code)."),
	_263("263","Offense Segment","CANNOT HAVE MORE THAN 10 OFFENSES","Can be submitted only 10 times for each Group A Incident Report; 10 offense codes are allowed for each incident."),
	_264("264","Offense Segment","GROUP 'A' OFFENSE CANNOT CONTAIN A GROUP 'B' OFFENSE","Data Element 6 (UCR Offense Code) must be a Group 'A' UCR Offense Code, not a Group 'B' Offense Code."),
	_265("265","Offense Segment","INVALID WEAPON [weapon-code] WITH AN OFFENSE OF 13B","If an Offense Segment (Level 2) was submitted for 13B=Simple Assault, Data Element 13 (Type Weapon/Force Involved) can only have codes of 40=Personal Weapons, 90=Other, 95=Unknown, and 99=None. All other codes are not valid because they do not relate to a simple assault."),
	_266("266","Offense Segment","NO OTHER OFFENSE CAN BE SUBMITTED WITH AN 09C OFFENSE","When a Justifiable Homicide is reported, no other offense may be reported in the Group 'A' Incident Report. These should be submitted on another Group 'A' Incident Report."),
	_267("267","Offense Segment","INVALID WEAPON [weapon-code] WITH AN OFFENSE OF [offense]","If a homicide offense is submitted, Data Element 13 (Type Weapon/Force Involved) cannot have 99=None. Some type of weapon/force must be used in a homicide offense."),
	_268("268","Offense Segment","A LARCENY OFFENSE CANNOT HAVE A MOTOR VEHICLE PROPERTY DESCRIPTION ENTERED","Cannot be submitted with a data value for a motor vehicle in Data Element 15 (Property Description) when Data Element 6 (UCR Offense Code) contains an offense of (23A–23H)=Larceny/Theft Offenses; stolen vehicles cannot be reported for a larceny"),
	_269("269","Offense Segment","POSSIBLE CLASSIFICATION ERROR OF AGGRAVATED ASSAULT 13A CODED AS SIMPLE 13B","If Data Element 6 (UCR Offense Code) is 13B=Simple Assault and the weapon involved is 11=Firearm, 12=Handgun, 13=Rifle, 14=Shotgun, or 15=Other Firearm, then the offense should instead be classified as 13A=Aggravated Assault."),
	_270("270","Offense Segment","JUSTIFIABLE HOMICIDE MUST BE CODED AS NON-BIAS MOTIVATED","Must be 88=None when Data Element 6 (UCR Offense Code) is 09C=Justifiable Homicide."),
	_284("284","Offense Segment","THIS OFFENSE SEGMENT HAS A CONFLICTING LENGTH","Segment Length for the Offense Segment (Level 2) must be 63 characters (reporting only Bias Motivation #1) or 71 characters (reporting Bias Motivations #2–#5). All Offense Segments in a submission must be formatted in only one of these two lengths."),
	_301("301","Property Segment","MUST BE PRESENT— MANDATORY FIELD","The referenced data element in a Group A Incident Report must be populated with a valid data value and cannot be blank."),
/**
 * 302 not implemented	
 */
	_302("302","Property Segment","CONTAINS NONNUMERIC ENTRY","Must be numeric entry with zero left-fill. If Data Element 21 (Estimated Quantity) has the error, note that any decimal fractional quantity must be expressed in thousandths as three numeric digits. If no fractional quantity was involved, then all zeros should be entered."),
	_304("304","Property Segment","INVALID DATA VALUE","The referenced data element must contain a valid data value when it is entered; Blank is permissible on non-mandatory fields."),
	_305("305","Property Segment","DATE RECOVERED IS INVALID","Each component of the date must be valid; that is, months must be 01 through 12, days must be 01 through 31, and year must include the century (i.e., 19xx, 20xx). In addition, days cannot exceed maximum for the month (e.g., June cannot have 31 days). The date cannot be later than that entered within the Month of Electronic Submission and Year of Electronic submission fields on the data record. For example, if Month of Electronic Submission and Year of Electronic Submission are 06/1999, the recovered date cannot contain any date 07/01/1999 or later. Cannot be earlier than Data Element 3 (Incident Date/Hour)."),
	_306("306","Property Segment","NCA07: DUPLICATE VALUE=[value]","The referenced data element in error is one that contains multiple data values. When more than one code is entered, none can be duplicate codes. There are two exceptions to this rule: 1) When a data value is entered in both Drug Type 1 and Drug Type 2, but different measurement categories are entered in Data Element 22 (Type Drug Measurement); this is allowed. For example, when A=Crack Cocaine is entered in Drug Type 1 and it is also entered in Drug Type 2, Data Element 22 (Type Drug Measurement) must be two different measurement categories (i.e., grams and liters) and not grams and pounds (same weight category). 2) When the data value is U=Unknown; it can be entered only once."),
/**
 * 315, 316, 317 not implemented
 */
	_315("315","Property Segment","CANNOT HAVE EMBEDDED BLANKS BETWEEN FIRST AND LAST NON-BLANK CHARACTERS","Data Element 2 (Incident Number) Must be blank right-fill if under 12 characters in length. Cannot have embedded blanks between the first and last characters entered."),
	_316("316","Property Segment","MUST BE LEFT-JUSTIFIED– BLANK DETECTED IN FIRST POSITION","Data Element 2 (Incident Number) Must be left- justified with blank right-fill if under 12 characters in length."),
	_317("317","Property Segment","CANNOT HAVE CHARACTERS OTHER THAN A–Z, 0–9, AND/OR HYPHEN, AND/OR BLANKS","Must contain a valid character combination of the following: A–Z (capital letters only) 0–9 Hyphen Example: 11-123-SC is valid, but 11+123*SC is not valid."),
/**
* 320 not in 3.1
*/
	_320("320","Property Segment","RECOVERED DATE PREDATES STOLEN DATE","The date property is recovered cannot be before the date it is stolen"),
/**
 * 342 not in 3.1	
 */
	_342("342","Property Segment","WARNING - PROPERTY DESC(15) HAD VALUE (16) THAT EXCEEDED THRESHOLD OF [$1,000,000]","When referenced data element contains a value that exceeds an FBI-assigned threshold amount, a warning message will be created. The participant is asked to check to see if the value entered was a data entry error, or if it was intended to be entered. A warning message is always produced when the value is $1,000,000 or greater. For example, if the value of a property is $12,000.99 but is inadvertently entered as $1,200,099 in the computer record sent to the FBI, a warning message will be generated. In this case, the cents were entered as whole dollars."),
/**
 * 343 not in 3.1	
 */
	_343("343","Property Segment","WARNING - 280 OFFENSE HAS RECOVERED VEHICLE BUT 240 DOESN'T SHOW STOLEN","This is a warning message only. This warning is generated when a 280 Stolen Property Offense and a 240 Motor Vehicle Theft are submitted that contain questionable property reporting. When the incident contains a recovered vehicle but does not also have a stolen vehicle, this warning message is created. The incident should be reviewed and if there was indeed a stolen vehicle, the incident should be resubmitted reflecting both stolen and recovered vehicles."),
	_351("351","Property Segment","PROPERTY VALUE OF ZERO IS NOT ALLOWED","Data Element 16 (Value of Property) Cannot be zero unless Data Element 15 (Property Description) is: Mandatory zero 09=Credit/Debit Cards 22=Nonnegotiable Instruments 48=Documents–Personal or Business 65=Identity Documents 66=Identity–Intangible Optional zero 77=Other 99=(blank)–this data value is not currently used by the FBI.by the FBI"),
	_352("352","Property Segment","DATA ELEMENTS 15 - 22 MUST BE BLANK WHEN PROPERTY LOSS CODE=1 OR 8","When this error occurs, data were found in one or more of the referenced data elements. These data elements must be blank based on other data element values that prohibit data being entered in these data elements. For example, if Data Element 14 (Type property Loss/Etc.) is 8=Unknown, Data Elements 15 through 22 must be blank. If it is 1=None and offense is 35A, then Data Elements 15 through 19 and 21 through 22 must be blank. If it is 1=None and offense is not 35A, then Data Elements 15 through 22 must be blank. The exception to this rule is when Data Element 6 (UCR Offense Code) is 35A=Drug/ Narcotic Violations and Data Element 14 (Type Property Loss/Etc.) is 1=None; Data Element 20 (Suspected Drug Type) must be entered."),
	_353("353","Property Segment","PENDING INVENTORY MUST HAVE PROPERTY VALUE OF 1","Data Element 15 (Property Description) is 88=Pending Inventory, but Data Element 16 (Value of Property) is not $1. Determine which of the data elements was entered incorrectly."),
	_354("354","Property Segment","DATA ELEMENT 15 WAS NOT ENTERED, BUT VALUE ($) WAS","Data Element 16 (Value of Property) contains a value, but Data Element 15 (Property Description) was not entered."),
	_355("355","Property Segment","PROPERTY LOSS CODE (14) MUST=5 (RECOVERED) FOR DATA TO BE ENTERED","Data Element 14 (Type Property Loss/Etc.) must be 5=Recovered for Data Element 17 (Date Recovered) to be entered."),
	_356("356","Property Segment","PROPERTY DESCRIPTION (15) AND VALUE (16) MUST BOTH EXIST IF DATA ARE PRESENT","Data Element 17 (Date Recovered) was entered, but Data Elements 15 (Property Description) and/or 16 (Property Value) were not entered."),
	_357("357","Property Segment","PROPERTY LOSS (14) MUST BE 7 WITH AN OFFENSE CODE OF 240 FOR DATA TO BE ENTERED","Data Element 18 (Number of Stolen Motor Vehicles) was entered. However, Data Element 14 (Type Property Loss/Etc.) 7=Stolen/Etc. was not entered, and/or Data Element 6 (UCR Offense Code) of 240=Motor Vehicle Theft was not entered, and/or Data Element 7 (Offense Attempted/Completed) was A=Attempted."),
	_358("358","Property Segment","DATA MUST EXIST WITH AN OFFENSE CODE OF 240 AND A PROPERTY","LOSS OF 7 Entry must be made for Data Element 18 (Number of Stolen Motor Vehicles) when Data Element 6 (UCR Offense Code) is 240=Motor Vehicle Theft, Data Element 7 (Offense Attempted/Completed) is C=Completed, and Data Element 14 (Type Property Loss/Etc.) is 7=Stolen/Etc."),
	_359("359","Property Segment","ALL NONVEHICULAR PROPERTY DESCRIPTIONS WERE ENTERED","Must be one of the following when Data Element 18 (Number of Stolen Motor Vehicles) or Data Element 19 (Number of Recovered Motor Vehicles) contain a data value other than 00=Unknown: 03=Automobiles 05=Buses 24=Other Motor Vehicles 28=Recreational Vehicles 37=Trucks"),
	_360("360","Property Segment","PROPERTY LOSS (14) MUST BE 5 WITH AN OFFENSE CODE OF 240 FOR DATA TO BE ENTERED","Data Element 19 (Number of Recovered Motor Vehicles) was entered. However, Data Element 14 (Type Property Loss/Etc.) 5=Recovered was not entered, and/or Data Element 6 (UCR Offense Code) of 240=Motor Vehicle Theft was not entered, and/or Data Element 7 (Offense Attempted/Completed) was A=Attempted. The exception to this rule is when recovered property is reported for a pre-NIBRS incident. In this case, Segment Level 3 (Property Segment) will contain A=Add, but the data value in Data Element 2 (Incident Number) will not match an incident already on file in the national UCR database. The segment will be processed, but used only for SRS purposes and will not be included in the agency's NIBRS figures."),
	_361("361","Property Segment","DATA MUST EXIST WITH AN OFFENSE CODE OF 240 AND A PROPERTY LOSS OF 5","Entry must be made when Data Element 6 (UCR Offense Code) is 240=Motor Vehicle Theft, Data Element 14 (Type Property Loss/Etc.) is 5=Recovered, and Data Element 15 (Property Description) contains a vehicle code."),
	_362("362","Property Segment","TWO OTHER CODES MUST BE ENTERED WHEN AN 'X' IS PRESENT","Since X=Over 3 Drug Types was entered in Data Element 20 (Suspected Drug Type), two other codes must also be entered. There are less than three codes present."),
	_363("363","Property Segment","WITH A CODE OF 'X' BOTH QUANTITY (21) AND MEASUREMENT (22) MUST BE BLANK","Since Data Element 20 (Suspected Drug Type) contains X=Over 3 Drug Types, Data Element 21 (Estimated Quantity) and 22 (Type Measurement) must be blank"),
	_364("364","Property Segment","WITH DATA ENTERED BOTH QUANTITY (21) AND MEASUREMENT (22) MUST BE PRESENT","When Data Element 6 (UCR Offense Code) is 35A=Drug/Narcotic Violations, 14 (Type Property Loss/Etc.) is 6=Seized, 15 (Type Property Loss/Etc.) is 10=Drugs, and Data Element 20 (Suspected Drug Type) is entered, both Data Element 21 (Estimated Quantity) and 22 (Type Measurement) must also be entered."),
	_365("365","Property Segment","OFFENSE=35A AND PROPERTY LOSS=6 AND DESCRIPTION=10 MUST EXIST","Data Element 20 (Suspected Drug Type) was entered, but one or more required data elements were not entered. Data Element 6 (UCR Offense Code) must be 35A=Drug/Narcotic Violations, Data Element 14 (Type Property Loss/Etc.) must be 6=Seized, and Data Element 15 (Property Description) must be 10=Drugs/Narcotics. There could be multiple underlying reasons causing this error to be detected. One of them might be that Data Element 20 (Suspected Drug Type) was entered by mistake. Perhaps the code entered in Data Element 15 (Property Description) should have been 01=Aircraft, but by entering the code as 10=Drugs/Narcotics, someone thought that Data Element 20 must be entered, etc."),
	_366("366","Property Segment","WITH DATA ENTERED BOTH TYPE (20) AND MEASUREMENT (22) MUST BE PRESENT","Data Element 21 (Estimated Quantity) was entered, but 20 (Suspected Drug Type) and/or 22 (Type Measurement) were not entered; both must be entered."),
	_367("367","Property Segment","DRUG TYPE MUST BE 'E', 'G', OR 'K' FOR A VALUE OF 'NP'","Data Element 22 (Type Measurement) was entered with NP in combination with an illogical drug type. Based upon the various ways a drug can be measured, very few edits can be done to check for illogical combinations of drug type and measurement. The only restriction will be to limit NP=Number of Plants to the following drugs: DRUG MEASUREMENT E=Marijuana NP G=Opium NP K=Other Hallucinogens NP All other Data Element 22 (Type Measurement) codes are applicable to any Data Element 20 (Suspected Drug Type) code."),
	_368("368","Property Segment","WITH DATA ENTERED BOTH TYPE (20) AND QUANTITY (21) MUST BE PRESENT","Data Element 22 (Type Measurement) was entered, but 20 (Suspected Drug Type) and/or 21 (Estimated Quantity) were not entered; both must be entered."),
	_372("372","Property Segment","DATA ELEMENTS 15-22 WERE ALL BLANK WITH THIS PROPERTY LOSS CODE","If Data Element 14 (Type Property/Loss/Etc.) is 2=Burned, 3=Counterfeited/ Forged, 4=Destroyed/Damaged/Vandalized, 5=Recovered, 6=Seized, or 7=Stolen/Etc., Data Elements 15 through 22 must have applicable entries in the segment."),
	_375("375","Property Segment","MANDATORY FIELD WITH THE PROPERTY LOSS CODE ENTERED","At least one Data Element 15 (Property Description) code must be entered when Data Element 14 (Type Property Loss/Etc.) contains Property Segment(s) for: 2=Burned 3=Counterfeited/Forged 4=Destroyed/Damaged/Vandalized 5=Recovered 6=Seized 7=Stolen/Etc."),
	_376("376","Property Segment","DUPLICATE PROPERTY SEGMENT ON ELECTRONIC SUBMISSION (TYPE LOSS=[loss- code])","When a Group 'A' Incident Report is submitted, the individual segments comprising the incident cannot contain duplicates. Example, two property segments cannot be submitted having the same entry in Data Element 14 (Type Property Loss/Etc.)."),
	_382("382","Property Segment","DRUG/NARCOTIC VIOLATIONS OFFENSE MUST BE SUBMITTED FOR SEIZED DRUGS","Segment Level 3 (Property Segment) cannot be submitted with 10=Drugs/Narcotics in Data Element 15 (Property Description) and blanks in Data Element 16 (Value of Property) unless Data Element 6 (UCR Offense Code) is 35A=Drug/Narcotic Violations."),
	_383("383","Property Segment","PROPERTY VALUE MUST BE BLANK FOR 35A (SINGLE OFFENSE)","Data Element 16 (Value of Property) has a value other than zero entered. Since Data Element 15 (Property Description) code is 10=Drugs/Narcotics and the only Crime Against Property offense submitted is a 35A=Drug/Narcotic Violations, Data Element 16 (Value of Property) must be blank."),
	_384("384","Property Segment","DRUG QUANTITY MUST BE NONE WHEN DRUG MEASUREMENT IS NOT REPORTED","Data Element 21 (Estimated Drug Quantity) must be 000000001000=None (i.e., 1) when Data Element 22 (Type Drug Measurement) is XX=Not Reported indicating the drugs were sent to a laboratory for analysis. When the drug analysis is received by the LEA, Data Element 21 and Data Element 22 should be updated with the correct data values."),
	_387("387","Property Segment","WITH A PROPERTY LOSS=6 AND ONLY OFFENSE 35A CANNOT HAVE DESCRIPTION 11 or WITH A PROPERTY LOSS=6 AND ONLY OFFENSE 35B CANNOT HAVE DESCRIPTION 10","To ensure that 35A-35B Drug/Narcotic Offenses- Drug Equipment Violations are properly reported, Data Element 15 (Property Description) of 11=Drug/Narcotic Equipment is not allowed with only a 35A Drug/Narcotic Violation. Similarly, 10=Drugs/Narcotics is not allowed with only a 35B Drug Equipment Violation. And Data Element 14 (Type Property Loss/Etc.) is 6=Seized."),
	_388("388","Property Segment","NUMBER STOLEN IS LESS THAN NUMBER OF VEHICLE CODES","More than one vehicle code was entered in Data Element 15 (Property Description), but the number stolen in Data Element 18 (Number of Stolen Motor Vehicles) is less than this number. For example, if vehicle codes of 03=Automobiles and 05=Buses were entered as being stolen, then the number stolen must be at least 2, unless the number stolen was unknown (00). The exception to this rule is when 00=Unknown is entered in Data Element 18."),
	_389("389","Property Segment","NUMBER RECOVERED IS LESS THAN NUMBER OF VEHICLE CODES","More than one vehicle code was entered in Data Element 15 (Property Description), but the number recovered in Data Element 19 (Number of Recovered Motor Vehicles) was less than this number. For example, if vehicle codes of 03=Automobiles and 05=Buses were entered as being recovered, then the number recovered must be at least 2, unless the number recovered was unknown (00). The exception to this rule is when 00=Unknown is entered in Data Element 18."),
	_390("390","Property Segment","ILLOGICAL PROPERTY DESCRIPTION FOR THE OFFENSE SUBMITTED","Data Element 15 (Property Description) must contain a data value that is logical for one or more of the offenses entered in Data Element 6 (UCR Offense Code). Illogical combinations include: 1) Property descriptions for structures are illogical with 220=Burglary/Breaking & Entering or 240=Motor Vehicle Theft 2) Property descriptions for items that would not fit in a purse or pocket (aircraft, vehicles, structures, a person's identity, watercraft, etc.) are illogical with 23A=Pocket-picking or 23B=Purse- snatching 3) Property descriptions that cannot be shoplifted due to other UCR definitions (aircraft, vehicles, structures, a person's identity, watercraft, etc.) are illogical with 23C=Shoplifting 4) Property descriptions for vehicles and structures are illogical with 23D=Theft from Building, 23E=Theft from Coin-Operated Machine or Device, 23F=Theft from Motor Vehicle, and 23G=Theft of Motor Vehicle Parts or Accessories Property descriptions for vehicles are illogical with 23H=All Other Larceny"),
	_391("391","Property Segment","PROPERTY VALUE MUST BE ZERO FOR DESCRIPTION SUBMITTED","Data Element 15 (Property Description) has a code that requires a zero value in Data Element 16 (Value of Property). Either the wrong property description code was entered or the property value was not entered. (This error was formerly error number 340, a warning message.) Data Element 16 (Value of Property) must be zero when Data Element 15 (Property Description) is: 09=Credit/Debit Cards 22=Nonnegotiable Instruments 48=Documents–Personal or Business 65=Identity Documents 66=Identity–Intangible"),
	_392("392","Property Segment","35A OFFENSE ENTERED AND 1=NONE ENTERED; MISSING SUSPECTED DRUG TYPE (20)","An offense of 35A Drug/Narcotic Violations and Data Element 14 (Type Property Loss/Etc.) with1=None were entered but Data Element 20 (Suspected Drug Type) was not submitted. Since a drug"),
	_401("401","Victim Segment","MUST BE PRESENT -- MANDATORY FIELD","The referenced data element in a Group A Incident Report must be populated with a valid data value and cannot be blank."),
	_402("402","Victim Segment","CONTAINS NONNUMERIC DIGITS","Must contain numeric entry with zero left-fill."),
	_404("404","Victim Segment","INVALID DATA VALUE","The referenced data element must contain a valid data value when it is entered; blank is permissible on non-mandatory fields."),
	_406("406","Victim Segment","NCA07: DUPLICATE VALUE=[value]","The referenced data element in error is one that contains multiple data values. When more than one code is entered, none can be duplicate codes."),
	_407("407","Victim Segment","ERROR - MUTUALLY EXCLUSIVE VALUE=[value]","Data Element 33 (Type Injury) Can have multiple data values and was entered with multiple values. However, the entry shown between the brackets in [value] above cannot be entered with any other data value."),
/**
 * 408 not implemented
 */
	_408("408","Victim Segment","EXACT AGE MUST BE IN FIRST TWO POSITIONS","Data Element 26 (Age of Victim) contains data, but is not left-justified. A single two-character age must be in positions 1 and 2 of the field."),
	_409("409","Victim Segment","CONTAINS NONNUMERIC ENTRY","Data Element 26 (Age of Victim) contains more than two characters indicating a possible age-range was being attempted. If so, the field must contain numeric entry of four digits."),
	_410("410","Victim Segment","FIRST AGE MUST BE LESS THAN SECOND FOR AGE RANGE","Data Element 26 (Age of Victim) was entered as an age-range. Accordingly, the first age component must be less than the second age."),
/**
 * 415 not implemented
 */
	_415("415","Victim Segment","CANNOT HAVE EMBEDDED BLANKS BETWEEN FIRST AND LAST NONBLANK CHARACTERS","Data Element 2 (Incident Number) Must be blank right-fill if under 12 characters in length. Cannot have embedded blanks between the first and last characters entered."),
/**
 * 416 not implemented	
 */
	_416("416","Victim Segment","MUST BE LEFT- JUSTIFIED–BLANK DETECTED IN FIRST POSITION","Data Element 2 (Incident Number) Must be left-justified with blank right-fill if under 12 characters in length."),
/**
 * 417 not implemented	
 */
	_417("417","Victim Segment","CANNOT HAVE CHARACTERS OTHER THAN A–Z, 0–9, AND/OR HYPHEN, AND/OR BLANKS","Data Element 2 (Incident Number)Must contain a valid character combination of the following: A–Z (capital letters only) 0–9 Hyphen Example: 11-123-SC is valid, but 11+123*SC is not valid."),
	_419("419","Victim Segment","DATA CAN ONLY BE ENTERED FOR SPECIFIC OFFENSES","Data Element 31 (Aggravated Assault/Homicide Circumstances) can only be entered when one or more of the offenses in Data Element 24 (Victim Connected to UCR Offense Code) are: 09A=Murder and Non-negligent Manslaughter 09B=Negligent Manslaughter 09C=Justifiable Homicide 13A=Aggravated Assault Data Element 33 (Type Injury) can only be entered when one or more of the offenses in Data Element 24 (Victim Connected to UCR Offense Code) are: 100=Kidnapping/Abduction 11A=Rape 11B=Sodomy 11C=Sexual Assault With An Object 11D=Fondling 120=Robbery 13A=Aggravated Assault 13B=Simple Assault 210=Extortion/Blackmail 64A=Human Trafficking, Commercial Sex Acts 64B=Human Trafficking, Involuntary Servitude"),
	_422("422","Victim Segment","AGE RANGE CANNOT HAVE '00' IN FIRST TWO POSITIONS","Data Element 26 (Age of Victim) was entered as an age-range. Therefore, the first age component cannot be 00 (unknown)."),
/**
 * 449 not in 3.1	
 */
    _449("449","Victim Segment","WARNING–VICTIM IS SPOUSE, BUT AGE IS LESS THAN 18","Data Element 26 (Age of Victim) cannot be less than 18 years old when Data Element 35 (Relationship of Victim to Offender) contains a relationship of SE = Spouse."),
	_450__2_1("450","Victim Segment","VICTIM IS SPOUSE, BUT AGE IS LESS THAN 10","Data Element 35 (Relationship of Victim to Offender) contains a relationship of SE=Spouse. When this is so, the age of the victim cannot be less than 10 years."),
	_450__3_1("450","Victim Segment","VICTIM IS SPOUSE, BUT AGE IS LESS THAN 13","Data Element 35 (Relationship of Victim to Offender) contains a relationship of SE=Spouse. When this is so, the age of the victim cannot be less than 13 years."),
	_451("451","Victim Segment","VICTIM NUMBER ALREADY EXISTS","When a Group 'A' Incident Report is submitted, the individual segments comprising the incident cannot contain duplicates. In this case, two victim segments were submitted having the same entry in Data Element 23 (Victim Sequence Number)."),
	_453("453","Victim Segment","MUST BE PRESENT WHEN VICTIM TYPE (25)=I","The Data Element associated with this error must be present when Data Element 25 (Type of Victim) is I=Individual."),
	_454("454","Victim Segment","MUST BE ENTERED WHEN VICTIM TYPE IS LAW ENFORCEMENT OFFICER","Data Element 25A (Type of Officer Activity/Circumstance), Data Element 25B (Officer Assignment Type), Data Element 26 (Age of Victim), Data Element 27 (Sex of Victim), and Data Element 28 (Race of Victim) must be entered when Data Element 25 (Type of Victim) is L=Law Enforcement Officer."),
	_455("455","Victim Segment","ADDITIONAL JUSTIFIABLE HOMICIDE IS MANDATORY WITH A 20 OR 21 ENTERED","Data Element 31 (Aggravated Assault/Homicide Circumstances) contains: 20=Criminal Killed by Private Citizen Or 21=Criminal Killed by Police Officer, but Data Element 32 (Additional Justifiable Homicide Circumstances) was not entered."),
	_456("456","Victim Segment","ONLY ONE VALUE GREATER THAN OR EQUAL TO 10 CAN BE ENTERED","Data Element 31 (Aggravated Assault/Homicide Circumstances) was entered with two entries, but was rejected for one of the following reasons: 1) Value 10=Unknown Circumstances is mutually exclusive with any other value. 2) More than one category (i.e., Aggravated Assault, Negligent Manslaughter, etc.) was entered."),
	_457("457","Victim Segment","WHEN DATA ELEMENT 32 IS ENTERED, DATA ELEMENT 31 MUST EQUAL 20 OR 21","Data Element 32 (Additional Justifiable Homicide Circumstances) was entered, but Data Element 31 (Aggravated Assault/Homicide Circumstances) does not reflect a justifiable homicide circumstance."),
	_458("458","Victim Segment","VICTIM TYPE (25) MUST BE 'I' OR 'L' FOR DATA TO BE ENTERED","The Data Element associated with this error cannot be entered when Data Element 25 (Type of Victim) is not I=Individual or L=Law Enforcement Officer when Data Element 24 (Victim Connected to UCR Offense Code) contains a Crime Against Person."),
	_459("459","Victim Segment","NEED A CRIME AGAINST PERSON OR ROBBERY FOR DATA TO BE ENTERED","Data Element 34 (Offender Numbers To Be Related) was entered but should only be entered if one or more of the offenses entered into Data Element 24 [Victim Connected to UCR Offense Code(s)] is a Crime Against Person or is a Robbery Offense (120). None of these types of offenses were entered."),
	_460("460","Victim Segment","RELATIONSHIP MUST BE ENTERED WHEN AN OFFENDER NUMBER (34) EXISTS","Corresponding Data Element 35 (Relationship of Victim to Offenders) data must be entered when Data Element 34 (Offender Numbers To Be Related) is entered with a value greater than 00."),
/**
 * 461 not in 3.1
 */
	_461("461","Victim Segment","VICTIM TYPE CANNOT EQUAL 'S' WITH AN OFFENSE OF 220","Data Element 25 (Type of Victim) cannot have a value of S=Society/Public when the offense is 220=Burglary/Breaking and Entering."),
	_462("462","Victim Segment","INVALID AGGRAVATED ASSAULT/HOMICIDE FOR 13A OFFENSE","An Offense Segment (Level 2) was submitted for 13A=Aggravated Assault. Accordingly, Data Element 31 (Aggravated Assault/Homicide Circumstances) can only have codes of 01 through 06 and 08 through 10. All other codes, including 07=Mercy Killing, are not valid because they do not relate to an aggravated assault"),
	_463("463","Victim Segment","INVALID AGGRAVATED ASSAULT/HOMICIDE FOR 09C OFFENSE","When a Justifiable Homicide is reported, Data Element 31 (Aggravated Assault/Homicide Circumstances) can only have codes of 20=Criminal Killed by Private Citizen or 21=Criminal Killed by Police Officer. In this case, a code other than the two mentioned was entered."),
	_464("464","Victim Segment","ENTRY FOR TYPE OF VICTIM MUST BE 'I' OR 'L' WHEN THIS OFFENSE CODE IS ENTERED","Data Element 24 (Victim Connected to UCR Offense Codes) contains a Crime Against Person, but Data Element 25 (Type of Victim) is not I=Individual or L=Law Enforcement Officer when Data Element 24 (Victim Connected to UCR Offense Code) contains a Crime Against Person."),
	_465("465","Victim Segment","ENTRY FOR TYPE OF VICTIM MUST BE 'S' WHEN THIS OFFENSE CODE IS ENTERED","Data Element 24 (Victim Connected to UCR Offense Codes) contains a Crime Against Society, but Data Element 25 (Type of Victim) is not S=Society."),
	_466("466","Victim Segment","OFFENSE MUST BE SUBMITTED AS LEVEL 2 RECORD IF VICTIM IS CONNECTED","Each UCR Offense Code entered into Data Element 24 (Victim Connected to UCR Offense Codes) must have the Offense Segment for the value. In this case, the victim was connected to offenses that were not submitted as Offense Segments. A victim cannot be connected to an offense when the offense itself is not present."),
	_467("467","Victim Segment","ENTRY FOR TYPE OF VICTIM CANNOT BE 'S' WHEN THIS OFFENSE CODE IS ENTERED","Data Element 24 (Victim Connected to UCR Offense Codes) contains a Crime Against Property, but Data Element 25 (Type of Victim) is S=Society. This is not an allowable code for Crime Against Property offenses."),
	_468("468","Victim Segment","RELATIONSHIP CANNOT BE ENTERED WHEN RELATED TO OFFENDER NUMBER '00'","Data Element 35 (Relationship of Victim to Offenders) cannot be entered when Data Element 34 (Offender Number to be Related) is zero. Zero means that the number of offenders is unknown; therefore, the relationship cannot be entered."),
	_469("469","Victim Segment","VICTIM SEX MUST BE 'M' OR 'F' FOR AN 11A OR 36B OFFENSE","Data Element 27 (Sex of Victim) must be M=Male or F=Female to be connected to offense codes of 11A=Forcible Rape and 36B=Statutory Rape."),
	_470("470","Victim Segment","WHEN 'VO' RELATIONSHIP IS PRESENT, MUST HAVE TWO OR MORE VICTIMS AND OFFENDERS","Data Element 35 (Relationship of Victim to Offenders) has a relationship of VO=Victim Was Offender. When this code is entered, a minimum of two victim and two offender segments must be submitted. In this case, only one victim and/or one offender segment was submitted. The entry of VO on one or more of the victims indicates situations such as brawls and domestic disputes. In the vast majority of cases, each victim is also the offender; therefore, every victim record would contain a VO code. However, there may be some situations where only one of the victims is also the offender, but where the other victim(s) is not also the offender(s)."),
	_471("471","Victim Segment","ONLY ONE VO RELATIONSHIP PER VICTIM","Data Element 35 (Relationship of Victim to Offenders) has relationships of VO=Victim Was Offender that point to multiple offenders, which is an impossible situation. A single victim cannot be two offenders."),
	_472("472","Victim Segment","WHEN OFFENDER AGE/SEX/RACE ARE UNKNOWN, RELATIONSHIP MUST BE 'RU'=UNKNOWN","Data Element 35 (Relationship of Victim to Offenders) has a relationship to the offender that is not logical. In this case, the offender was entered with unknown values for age, sex, and race. Under these circumstances, the relationship must be entered as RU=Relationship Unknown."),
	_474("474","Victim Segment","ONLY ONE VO RELATIONSHIP CAN BE ASSIGNED TO A SPECIFIC OFFENDER","Segment Level 4 (Victim Segment) cannot be submitted multiple times with VO=Victim Was Offender in Data Element 35 (Relationship of Victim to Offender) when Data Element 34 (Offender Number to be Related) contains the same data value (indicating the same offender)."),
	_475("475","Victim Segment","ONLY ONE 'SE'L RELATIONSHIP PER VICTIM","A victim can only have one spousal relationship. In this instance, the victim has a relationship of SE=Spouse to two or more offenders."),
	_476("476","Victim Segment","ONLY ONE 'SE' RELATIONSHIP CAN BE ASSIGNED TO A SPECIFIC OFFENDER","An offender can only have one spousal relationship. In this instance, two or more victims have a relationship of SE=Spouse to the same offender."),
	_477("477","Victim Segment","INVALID AGGRAVATED ASSAULT/HOMICIDE CIRCUMSTANCES FOR CONNECTED OFFENSE","A victim segment was submitted with Data Element 24 (Victim Connected to UCR Offense Code) having an offense that does not have a permitted code for Data Element 31 (Aggravated Assault/Homicide Circumstances). Only those circumstances listed in Volume 1, Section VI, are valid for the particular offense."),
	_478("478","Victim Segment","VICTIM CONNECTED TO AN INVALID COMBINATION OF OFFENSES","Mutually Exclusive offenses are ones that cannot occur to the same victim by UCR definitions. A Lesser Included offense is one that is an element of another offense and should not be reported as having happened to the victim along with the other offense. Lesser Included and Mutually Exclusive offenses are defined as follows: 1) Murder-Aggravated assault, simple assault, and intimidation are all lesser included offenses of murder. Negligent manslaughter is mutually exclusive. 2) Aggravated Assault-Simple assault and intimidation are lesser included Note: Aggravated assault is a lesser included offense of murder, forcible rape, forcible sodomy, sexual assault with an object, and robbery. 3) Simple Assault-Intimidation is a lesser included offense of simple assault. Note: Simple assault is a lesser included offense of murder, aggravated assault, forcible rape, forcible sodomy, sexual assault with an object, forcible fondling, and robbery. 4) Intimidation-Intimidation is a lesser included offense of murder, aggravated assault, forcible rape, forcible sodomy, sexual assault with an object, forcible fondling, and robbery. 5) Negligent Manslaughter-Murder, aggravated assault, simple assault, and intimidation are mutually exclusive offenses. Uniform Crime Reporting Handbook, NIBRS Edition, page 17, defines negligent manslaughter as 'The killing of another person through negligence.' Page 12 of the same publication shows that assault offenses are characterized by 'unlawful attack[s].' offenses of aggravated assault. 6) Forcible Rape-Aggravated assault, simple assault, intimidation, and forcible fondling are lesser included offenses of forcible rape. Incest and statutory rape are mutually exclusive offenses and cannot occur with forcible rape. The prior two offenses involve consent, while the latter involves forced action against the victim's will. 7) Forcible Sodomy-Aggravated assault, simple assault, intimidation, and forcible fondling are lesser included offenses of forcible sodomy. Incest and statutory rape are mutually exclusive offenses and cannot occur with forcible sodomy. The prior two offenses involve consent, while the latter involves forced action against the victim's will. 8) Sexual Assault with an Object- Aggravated assault, simple assault, intimidation, and forcible fondling are lesser included offenses of sexual assault with an object. Incest and statutory rape are mutually exclusive offenses and cannot occur with sexual assault with an object. The prior two offenses involve consent, while the latter involves forced action against the victim's will. 9) Forcible Fondling-Simple assault and intimidation are lesser included offenses of forcible fondling. Incest and statutory rape are mutually exclusive offenses and cannot occur with forcible fondling. The prior two offenses involve consent, while the latter involves forced action against the victim's will. Note: Forcible fondling is a lesser included offense of forcible rape, forcible sodomy, and sexual assault with an object. 10) Incest-Forcible rape, forcible sodomy, sexual assault with an object, and forcible fondling are mutually exclusive offenses. Incest involves consent, while the prior offenses involve forced sexual relations against the victim's will. 11) Statutory Rape-Forcible Rape, forcible sodomy, sexual assault with an object, and forcible fondling are mutually exclusive offenses. Statutory rape involves consent, while the prior offenses involve forced sexual relations against the victim's will. 12) Robbery-Aggravated assault, simple assault, intimidation, and all theft offenses (including motor vehicle theft) are lesser included offenses of robbery."),
	_479("479","Victim Segment","SIMPLE ASSAULT(13B) CANNOT HAVE MAJOR INJURIES","A Simple Assault (13B) was committed against a victim, but the victim had major injuries/trauma entered for Data Element 33 (Type Injury). Either the offense should have been classified as an Aggravated Assault (13A) or the victim's injury should not have been entered as major."),
	_480("480","Victim Segment","WHEN ASSAULT/HOMICIDE (31) IS 08, INCIDENT MUST HAVE TWO OR MORE OFFENSES","Data Element 31 (Aggravated Assault/Homicide Circumstances) has 08=Other Felony Involved but the incident has only one offense. For this code to be used, there must be an Other Felony. Either multiple entries for Data Element 6 (UCR Offense Code) should have been submitted, or multiple individual victims should have been submitted for the incident report."),
	_481("481","Victim Segment","VICTIM'S AGE MUST BE LESS THAN 18 FOR STATUTORY RAPE (36B)","Data Element 26 (Age of Victim) should be under 18 when Data Element 24 (Victim Connected to UCR Offense Code) is 36B=Statutory Rape."),
	_482("482","Victim Segment","LEOKA VICTIM MUST BE CONNECTED TO MURDER OR ASSAULT OFFENSE","Data Element 25 (Type of Victim) cannot be L=Law Enforcement Officer unless Data Element 24 (Victim Connected to UCR Offense Code) is one of the following: 09A=Murder & Non-negligent Manslaughter 13A=Aggravated Assault 13B=Simple Assault 13C=Intimidation"),
	_483("483","Victim Segment","VICTIM MUST BE LAW ENFORCEMENT OFFICER TO ENTER LEOKA DATA","Data Element 25A (Type of Officer Activity/Circumstance), Data Element 25B (Officer Assignment Type), Data Element 25C (Officer–ORI Other Jurisdiction), Data Element 26 (Age of Victim), Data Element 27 (Sex of Victim), Data Element 28 (Race of Victim), Data Element 29 (Ethnicity of Victim), Data Element 30 (Resident Status of Victim), and Data Element 34 (Offender Number to be Related) can only be entered when Data Element 25 (Type of Victim) is I=Individual or L=Law Enforcement Officer."),
	_484("484","Victim Segment","THIS VICTIM SEGMENT HAS A CONFLICTING LENGTH","Segment Length for the Victim Segment (Level 4) must be 129 characters (not reporting LEOKA) or 141 characters (reporting LEOKA). All Victim Segments in a submission must be formatted in only one of these two lengths."),
/**
 * 490 not implemented	
 */
	_490("490","Victim Segment","INCIDENT DATE/HOUR MUST CONTAIN A VALID ENTRY WITHEN VICTIM IS L=LAW ENFORCEMENT OFFICER","When Type of VIctim i L=Law Enforcement Officer and Data Element 24 (Victim Connected to UCR Offense Code) is one of the following: 09A=Murder & Non-negligent Manslaughter 13A=Aggrewavated Assault 13B=Simple Assault 13C=Intimidation Data Element 3 (Incident Date/Hour) must be populated with a valid hour (00-23) and cannot be blank "),
	_501("501","Offender Segment","MUST BE PRESENT— MANDATORY FIELD","The referenced data element in a Group A Incident Report must be populated with a valid data value and cannot be blank."),
/**
 *  502 not implemented
 */
	_502("502","Offender Segment","CONTAINS NONNUMERIC ENTRY","Data Element 36 (Offender Sequence Number) must contain numeric entry (00 through 99) with zero left-fill."),
	_504("504","Offender Segment","INVALID DATA VALUE","The referenced data element in a Group A Incident Report must be populated with a valid data value and cannot be blank."),
/**
 * 508 not implemented	
 */
	_508("508","Offender Segment","EXACT AGE MUST BE IN FIRST TWO POSITIONS","Data Element 37 (Age of Offender) contains data but is not left-justified. A single two-character age must be in positions 1 through 2 of the field."),
	_509("509","Offender Segment","CONTAINS NONNUMERIC ENTRY","Data Element 37 (Age of Offender) contains more than two characters indicating a possible age- range is being attempted. If so, the field must contain a numeric entry of four digits."),
	_510("510","Offender Segment","FIRST AGE MUST BE LESS THAN SECOND FOR AGE RANGE","Data Element 37 (Age of Offender) was entered as an age-range. Accordingly, the first age component must be less than the second age."),
/**
 * 515 not implemented	
 */
	_515("515","Offender Segment","CANNOT HAVE EMBEDDED BLANKS BETWEEN FIRST AND LAST NON-BLANK CHARACTERS","Must be blank right-fill if under 12 characters in length. Cannot have embedded blanks between the first and last characters entered."),
/** 
 * 516 not implemented	
 */
	_516("516","Offender Segment","MUST BE LEFT-JUSTIFIED– BLANK DETECTED IN FIRST POSITION","Must be left-justified with blank right-fill if under 12 characters in length."),
/**
 * 517 not implemented	
 */
	_517("517","Offender Segment","CANNOT HAVE CHARACTERS OTHER THAN A–Z, 0–9, AND/OR HYPHEN, AND/OR BLANKS","Must contain a valid character combination of the following: A–Z (capital letters only) 0–9 Hyphen Example: 11-123-SC is valid, but 11+123*SC is not valid."),
	_522("522","Offender Segment","AGE RANGE CANNOT HAVE '00' IN FIRST TWO POSITIONS","Data Element 37 (Age of Offender) was entered as an age-range. Therefore, the first age component cannot be 00 (unknown)."),
/**
 * 549 not in 3.1	
 */
	_549("549","Offender Segment","WARNING–OFFENDER IS SPOUSE, BUT AGE IS LESS THAN 18","Data Element 37 (Age of Offender) cannot be less than 18 years old when Data Element 35 (Relationship of Victim to Offender) contains a relationship of SE = Spouse."),
	_550__2_1("550","Offender Segment","OFFENDER IS SPOUSE, BUT AGE IS LESS THAN 10","Cannot be less than 10 years old when Data Element 35 (Relationship of Victim to Offender) contains a relationship of SE=Spouse."),
	_550__3_1("550","Offender Segment","OFFENDER IS SPOUSE, BUT AGE IS LESS THAN 13","Cannot be less than 10 years old when Data Element 35 (Relationship of Victim to Offender) contains a relationship of SE=Spouse."),
	_551("551","Offender Segment","DUPLICATE OFFENDER SEGMENT","When a Group 'A' Incident Report is submitted, the individual segments comprising the incident cannot contain duplicates. In this case, two Offender Segments were submitted having the same entry in Data Element 36 (Offender Sequence Number)."),
	_552("552","Offender Segment","CANNOT BE PRESENT WHEN OFFENDER NUMBER IS '00' UNKNOWN","Data Element 37 (Age of Offender) cannot be entered when Data Element 36 (Offender Sequence Number) is 00=Unknown."),
/**
 * 553 not in 3.1	
 */
	_553("553","Offender Segment","SEX OF VICTIM AND OFFENDER DOES NOT REFLECT THE RELATIONSHIP","Data Element 35 (Relationship of Victim to Offenders) has a relationship that is inconsistent with the offender's sex. The sex of the victim and/or offender must reflect the implied relationship. For example, if the relationship of the victim to offender is Homosexual Relationship, then the victim's sex must be the same as the offender's sex. The following relationships must reflect either the Same or Different sex codes depending upon this relationship: Relationship Sex Code BG=Victim was Boyfriend/Girlfriend Different XS=Victim was Ex-Spouse Different SE=Victim was Spouse Different CS=Victim was Common-Law Spouse Different HR=Homosexual Relationship Same"),
	_554("554","Offender Segment","AGE OF VICTIM AND OFFENDER DOES NOT REFLECT THE RELATIONSHIP","Data Element 35 (Relationship of Victim to Offenders) has a relationship that is inconsistent with the offender's age. The age of the victim and/or offender must reflect the implied relationship. For example, if the relationship of the victim to offender is PA=Parent, then the victim's age must be greater than the offender's age. The following relationships must be consistent with the victim's age in relation to the offender's age: Relationship Victim's Age Is CH=Victim was Child Younger PA=Victim was Parent Older GP=Victim was Grandparent Older GC=Victim was Grandchild Younger"),
	_555("555","Offender Segment","OFFENDER '00' EXISTS— CANNOT SUBMIT MORE OFFENDERS","When multiple Offender Segments are submitted, none can contain a 00=Unknown value because the presence of 00 indicates that the number of offenders is unknown. In this case, multiple offenders were submitted, but one of the segments contains the 00=Unknown value."),
	_556("556","Offender Segment","OFFENDER AGE MUST BE NUMERIC DIGITS","Data Element 37 (Age of Offender) must contain numeric entry of 00 through 99"),
	_557("557","Offender Segment","OFFENDER SEQUENCE NUMBER CANNOT BE UNKNOWN IF INCIDENT CLEARED EXCEPTIONALLY","Data Element 36 (Offender Sequence Number) contains 00 indicating that nothing is known about the offender(s) regarding number and any identifying information. In order to exceptionally clear the incident, the value cannot be 00. The incident was submitted with Data Element 4 (Cleared Exceptionally) having a value of A through E."),
	_558("558","Offender Segment","AT LEAST ONE OFFENDER MUST BE COMPLETELY KNOWN","None of the Offender Segments contain all known values for Age, Sex, and Race. When an Incident is cleared exceptionally (Data Element 4 contains an A through E), one offender must have all known values."),
	_559("559","Offender Segment","OFFENDER DATA MUST BE PRESENT IF OFFENSE CODE IS 09C, JUSTIFIABLE HOMICIDE","The incident was submitted with Data Element 6 (UCR Offense Code) value of 09C=Justifiable Homicide, but unknown information was submitted for all the offender(s). At least one of the offenders must have known information for Age, Sex, and Race."),
	_560("560","Offender Segment","VICTIM'S SEX CANNOT BE SAME FOR ALL OFFENDERS FOR OFFENSES OF RAPE","Segment Level 5 (Offender Segment) must contain a data value for at least one offender in Data Element 38 (Sex of Offender) that is not the same sex that is entered in Data Element 27 (Sex of Victim) when Data Element 6 (UCR Offense Code) is 11A=Rape."),
/**
 * 572 not in 3.1	
 */
	_572("572","Offender Segment","RELATIONSHIP UNKNOWN IF OFFENDER INFO MISSING","Data Element 37 (Age of Offender) If Data Element 37 (Age of Offender) is 00=Unknown, Data Element 38 (Sex of Offender) is U=Unknown, and Data Element 39 (Race of Offender) is U=Unknown then Data Element 35 (Relationship of Victim to Offender) must be RU=Relationship Unknown."),
	_584("584","Offender Segment","THIS OFFENDER SEGMENT HAS A CONFLICTING LENGTH","Segment Length for the Offender Segment (Level 5) must be 45 characters (not reporting Offender Ethnicity) or 46 characters (reporting Offender Ethnicity). All Offender Segments in a submission must be formatted in only one of these two lengths."),
	_601("601","Arrestee Segment","MUST BE PRESENT— MANDATORY FIELD","The referenced data element in a Group A Incident Report must be populated with a valid data value and cannot be blank."),
/**
 * 602 not implemented	
 */
	_602("602","Arrestee Segment","CONTAINS NONNUMERIC ENTRY","Data Element 40 (Arrestee Sequence Number) must be numeric entry of 01 to 99 with zero left- fill."),
	_604("604","Arrestee Segment","INVALID DATA VALUE -- NOT ON FBI VALIDATION TABLE","The referenced data element in a Group A Incident Report must be populated with a valid data value and cannot be blank."),
	_605("605","Arrestee Segment","INVALID ARREST DATE","Data Element 42 (Arrest Date) Each component of the date must be valid; that is, months must be 01 through 12, days must be 01 through 31, and year must include the century (i.e., 19xx, 20xx). In addition, days cannot exceed maximum for the month (e.g., June cannot have 31 days). The date cannot exceed the current date. The date cannot be later than that entered within the Month of Electronic submission and Year of Electronic submission fields on the data record. For example, if Month of Electronic submission and Year of Electronic submission are 06/1999, the arrest date cannot contain any date 07/01/1999 or later."),
	_606("606","Arrestee Segment","ERROR - DUPLICATE VALUE=[value]","Data Element 46 (Arrestee Was Armed With) The referenced data element in error is one that contains multiple data values. When more than one code is entered, none can be duplicate codes."),
	_607("607","Arrestee Segment","ERROR - MUTUALLY EXCLUSIVE VALUE=[value]","Data Element 46 (Arrestee Was Armed With) can have multiple data values and was entered with multiple values. However, the entry shown between the brackets in [value] above cannot be entered with any other data value."),
/**
 * 608 not implemented	
 */
	_608("608","Arrestee Segment","EXACT AGE MUST BE IN FIRST TWO POSITIONS","Data Element 47 (Age of Arrestee) contains data, but is not left-justified. A single two-character age must be in positions 1 through 2 of the field."),
	_609("609","Arrestee Segment","CONTAINS NONNUMERIC ENTRY","Data Element 47 (Age of Arrestee) contains more than two characters indicating a possible age- range is being attempted. If so, the field must contain a numeric entry of four digits."),
	_610("610","Arrestee Segment","FIRST AGE MUST BE LESS THAN SECOND FOR AGE RANGE","Data Element 47 (Age of Arrestee) was entered as an age-range. Accordingly, the first age component must be less than the second age."),
	_615("615","Arrestee Segment","CANNOT HAVE EMBEDDED BLANKS BETWEEN FIRST AND LAST NON-BLANK CHARACTERS","Must be blank right-fill if under 12 characters in length. Cannot have embedded blanks between the first and last characters entered."),
/**
 * 616 not implemented	
 */
	_616("616","Arrestee Segment","MUST BE LEFT-JUSTIFIED— BLANK DETECTED IN FIRST POSITION","Data Element 2 (Incident Number) and Data Element 41 (Arrest Transaction Number) must be left justified with blank right-fill when less than 12 characters in length."),
	_617("617","Arrestee Segment","CANNOT HAVE CHARACTERS OTHER THAN A-Z, 0-9, HYPHENS, AND/OR BLANKS","Data Element 2 (Incident Number) Must contain a valid character combination of the following: A–Z (capital letters only) 0–9 Hyphen Example: 11-123-SC is valid, but 11+123*SC is not valid."),
/**
 * 618 cannot be validated	
 */
	_618("618","Arrestee Segment","ARREST DATE CANNOT BE ON OR AFTER THE INACTIVE DATE OF THE ORI","Data Element 42 (Arrest Date) The UCR Program has determined that an ORI will no longer be submitting data to the FBI as of an inactive date. No arrest data from this ORI will be accepted after this date."),
	_622("622","Arrestee Segment","AGE RANGE CANNOT HAVE '00' IN FIRST TWO POSITIONS","Data Element 47 (Age of Arrestee) was entered as an age-range. Therefore, the first age component cannot be 00 (unknown)."),
/**
 * 623 not in 3.1	
 */
	_623("623","Arrestee Segment","CLEARANCE INDICATOR AND CLEARANCE OFFENSE CODE MUST BE BLANK","Clearance Indicator and Clearance Offense Code must be blank when Segment Action Type on Level 6 (Arrestee Segment) is I=Incident."),
/**
 * 640 not in 3.1	
 */
	_1640("640","Arrestee Segment","WARNING–NO DISPOSITION FOR POSSIBLE JUVENILE ARRESTEE","Data Element 52 (Disposition of Arrestee Under 18) was not entered, but Data Element 47 (Age of Arrestee) indicates an age-range for a juvenile. The low age is a juvenile and the high age is an adult, but the average age is a juvenile. Note: When an age-range is not entered and the age is a juvenile, then the disposition must be entered. These circumstances were flagged by the computer as a possible discrepancy between age and disposition and should be checked for possible correction by the participant."),
	_1641("641","Arrestee Segment","WARNING - ARRESTEE HAD AN AGE OF 99 OR OLDER","Data Element 47 (Age of Arrestee) was entered with a value of 99 which means the arrestee was over 98 years old. Verify that the submitter of data is not confusing the 99=Over 98 Years Old with 00=Unknown."),
	_652("652","Arrestee Segment","DISPOSITION MUST BE ENTERED WHEN AGE IS LESS THAN 18","Data Element 52 (Disposition of Juvenile) was not entered, but Data Element 47 (Age of Arrestee) is under 18. Whenever an arrestee's age indicates a juvenile, the disposition must be entered."),
	_653("653","Arrestee Segment","FOR AGE GREATER THAN 17 DISPOSITION SHOULD NOT BE ENTERED","Data Element 52 (Disposition of Juvenile) was entered, but Data Element 47 (Age of Arrestee) is 18 or greater. Whenever an arrestee's age indicates an adult, the juvenile disposition cannot be entered because it does not apply."),
	_654("654","Arrestee Segment","AUTOMATIC INDICATOR MUST BE BLANK OR 'A'","Data Element 46 (Arrestee Was Armed With) does not have A=Automatic or a blank in the third position of field."),
	_655("655","Arrestee Segment","WEAPON TYPE MUST=11, 12, 13, 14, OR 15 FOR AN 'A' IN THE AUTO INDICATOR","In Data Element 46 (Arrestee Was Armed With), A=Automatic is the third character of code. It is valid only with codes: 11=Firearm (Type Not Stated) 12=Handgun 13=Rifle 14=Shotgun 15=Other Firearm A weapon code other than those mentioned was entered with the automatic indicator. An automatic weapon is, by definition, a firearm."),
	_656("656","Arrestee Segment","THIS ARRESTEE EXCEEDED THE NUMBER OF OFFENDERS ON THE GROUP 'A' INCIDENT","A Group 'A' Incident Report was submitted with more arrestees than offenders. The number (nn) of offenders is shown within the message. The incident must be resubmitted with additional Offender Segments. This message will also occur if an arrestee was submitted and Data Element 36 (Offender Sequence Number) was 00=Unknown. The exception to this rule is when an additional arrest is reported for a pre-NIBRS incident. In this case, Segment Level 6 (Arrestee Segment) will contain A=Add, but the data value in Data Element 2 (Incident Number) will not match an incident already on file in the national UCR database. The segment will be processed, but used only for SRS purposes and will not be included in the agency's NIBRS figures."),
	_661("661","Arrestee Segment","ARRESTEE SEQUENCE NUMBER ALREADY EXISTS","Segment Level 6 (Arrestee Segment) cannot contain duplicate data values in Data Element 40 (Arrestee Sequence Number) when two or more Arrestee Segments are submitted for the same incident."),
/**
 * 664 not implemented	
 */
	_664("664","Arrestee Segment","ARRESTEE AGE MUST BE NUMERIC DIGITS","Data Element 47 (Age of Arrestee) does not contain a numeric entry of 00 through 99 for an exact age."),
	_665("665","Arrestee Segment","ARREST DATE CANNOT BE BEFORE THE INCIDENT START DATE","Data Element 42 (Arrest Date) cannot be earlier than Data Element 3 (Incident Date/Hour). A person cannot be arrested before the incident occurred."),
	_667("667","Arrestee Segment","INVALID ARRESTEE SEX VALUE","Data Element 48 (Sex of Arrestee) does not contain a valid code of M=Male or F=Female. Note: U=Unknown (if entered) is not a valid sex for an arrestee."),
	_669("669","Arrestee Segment","NO ARRESTEE RECORDS ALLOWED FOR A JUSTIFIABLE HOMICIDE","Group 'A' Incident Reports cannot have arrests when Data Element 6 (UCR Offense Code) is 09C=Justifiable Homicide. By definition a justifiable homicide never involves an arrest of the offender (the person who committed the justifiable homicide)."),
	_670("670","Arrestee Segment","JUSTIFIABLE HOMICIDE CANNOT BE AN ARREST OFFENSE CODE","Data Element 45 (UCR Arrest Offense Code) was entered with 09C=Justifiable Homicide. This is not a valid arrest offense"),
	_701("701","Group B Arrest Segment","MUST BE POPULATED WITH A VALID DATA VALUE– MANDATORY FIELD","The referenced data element in a Group B Arrest Report must be populated with a valid data value and cannot be blank."),
	_702("702","Group B Arrest Segment","CONTAINS NONNUMERIC ENTRY","Data Element 40 (Arrestee Sequence Number) must be numeric entry of 01 to 99 with zero left- fill."),
	_704("704","Group B Arrest Segment","INVALID DATA VALUE","The referenced data element in a Group A Incident Report must be populated with a valid data value and cannot be blank."),
	_705("705","Group B Arrest Segment","INVALID ARREST DATE","Data Element 42 (Arrest Date) Each component of the date must be valid; that is, months must be 01 through 12, days must be 01 through 31, and year must include the century (i.e., 19xx, 20xx). In addition, days cannot exceed maximum for the month (e.g., June cannot have 31 days). The date cannot exceed the current date. The date cannot be later than that entered within the Month of Electronic submission and Year of Electronic submission fields on the data record. For example, if Month of Electronic submission and Year of Electronic submission are 06/1999, the arrest date cannot contain any date 07/01/1999 or later."),
	_706("706","Group B Arrest Segment","ERROR - DUPLICATE VALUE=[value]","Data Element 46 (Arrestee Was Armed With) cannot contain duplicate data values although more than one data value is allowed."),
	_707("707","Group B Arrest Segment","ERROR - MUTUALLY EXCLUSIVE VALUE=[value]","Data Element 46 (Arrestee Was Armed With) can have multiple data values and was entered with multiple values. However, the entry shown between the brackets in [value] above cannot be entered with any other data value."),
/**
 * 708 not implemented	
 */
	_708("708","Group B Arrest Segment","EXACT AGE MUST BE IN FIRST TWO POSITIONS","Data Element 47 (Age of Arrestee) contains data, but is not left-justified. A single two-character age must be in positions 1 through 2 of the field."),
	_709("709","Group B Arrest Segment","CONTAINS NONNUMERIC ENTRY","Data Element 47 (Age of Arrestee) contains more than two characters indicating a possible age-range is being attempted. If so, the field must contain numeric entry of four digits."),
	_710("710","Group B Arrest Segment","FIRST AGE MUST BE LESS THAN SECOND FOR AGE RANGE","Data Element 47 (Age of Arrestee) was entered as an age-range. Accordingly, the first age component must be less than the second age."),
	_715("715","Group B Arrest Segment","CANNOT HAVE EMBEDDED BLANKS BETWEEN FIRST AND LAST NON-BLANK CHARACTERS","Must be blank right-fill if under 12 characters in length. Cannot have embedded blanks between the first and last characters entered."),
/**
 * 716 not implemented	
 */
	_716("716","Group B Arrest Segment","MUST BE LEFT-JUSTIFIED– BLANK DETECTED IN FIRST POSITION","Must be left-justified with blank right-fill if under 12 characters in length."),
	_717("717","Group B Arrest Segment","CANNOT HAVE CHARACTERS OTHER THAN A–Z, 0–9, AND/OR HYPHENS, AND/OR BLANKS","Must contain a valid character combination of the following: A–Z (capital letters only) 0–9 Hyphen Example: 11-123-SC is valid, but 11+123*SC is not valid."),
/**
 * 718 cannot be validated	
 */
	_718("718","Group B Arrest Segment","ARREST DATE CANNOT BE ON OR AFTER THE INACTIVE DATE OF THE ORI","Data Element 42 (Arrest Date) The UCR Program has determined that an ORI will no longer be submitting data to the FBI as of an inactive date. No arrest data from this ORI will be accepted after this date."),
/**
 * 720 not implemented	
 */
	_720("720","Group B Arrest Segment","ARREST DATE CANNOT PREDATE BASE DATE","Group 'B' Arrest Report (Level 7) submitted with a Segment Action Type of A=Add cannot have Data Element 42 (Arrest Date) earlier than the Base Date."),
	_722("722","Group B Arrest Segment","AGE RANGE CANNOT HAVE '00' IN FIRST TWO POSITIONS","Data Element 47 (Age of Arrestee) was entered as an age-range. Therefore, the first age component cannot be 00 (unknown)."),
/**
 * 740 not in 3.1	
 */
	_740("740","Group B Arrest Segment","WARNING–NO DISPOSITION FOR POSSIBLE JUVENILE ARRESTEE","Data Element 52 (Disposition of Arrestee Under 18) was not entered, but Data Element 47 (Age of Arrestee) indicates an age-range for a juvenile. The low age is a juvenile and the high age is an adult, but the average age is a juvenile. Note: When an age-range is not entered and the age is a juvenile, the disposition must be entered. These circumstances were flagged by the computer as a possible discrepancy between age and disposition and should be checked for possible correction by the participant"),
	_741("741","Group B Arrest Segment","WARNING–ARRESTEE IS OVER AGE 98","Data Element 47 (Age of Arrestee) was entered with a value of 99, which means the arrestee is over 98 years old. The submitter should verify that 99=Over 98 Years Old is not being confused the with 00=Unknown."),
/**
 * 751 not implemented
 */
	_751("751","Group B Arrest Segment","ARRESTEE SEQUENCE NUMBER ALREADY EXISTS","When a Group 'B' Arrest Report (Level 7) has two or more arrestees, the individual segments comprising the report cannot contain duplicates. In this case, two arrestee segments were submitted having the same entry in Data Element 40 (Arrestee Sequence Number)."),
	_752("752","Group B Arrest Segment","DISPOSITION MUST BE ENTERED WHEN AGE IS LESS THAN 18","Data Element 52 (Disposition of Juvenile) was not entered, but Data Element 47 (Age of Arrestee) is under 18. Whenever an arrestee's age indicates a juvenile, the disposition must be entered."),
	_753("753","Group B Arrest Segment","FOR AGE GREATER THAN 17 DISPOSITION SHOULD NOT BE ENTERED","Data Element 52 (Disposition of Juvenile) was entered, but Data Element 47 (Age of Arrestee) is 18 or greater. Whenever an arrestee's age indicates an adult, the juvenile disposition cannot be entered because it does not apply."),
	_754("754","Group B Arrest Segment","AUTOMATIC INDICATOR MUST BE BLANK OR 'A'","Data Element 46 (Arrestee Was Armed With) does not have A=Automatic or a blank in the third position of field."),
	_755("755","Group B Arrest Segment","WEAPON TYPE MUST=11, 12, 13, 14, OR 15 FOR AN 'A' IN THE AUTO INDICATOR","If Data Element 46 (Arrestee Was Armed With) weapon is an Automatic, add A as the third character of code; valid only with codes of: 11=Firearm (Type Not Stated) 12=Handgun 13=Rifle 14=Shotgun 15=Other Firearm A weapon code other than those mentioned was entered with the automatic indicator. An automatic weapon is, by definition, a firearm."),
/**
 * 757 not implemented	
 */
	_757("757","Group B Arrest Segment","ARRESTEE AGE MUST BE NUMERIC DIGITS","Data Element 47 (Age of Arrestee) does not contain a numeric entry of 00 through 99 for an exact age."),
	_758("758","Group B Arrest Segment","INVALID ARRESTEE SEX VALUE","Data Element 48 (Sex of Arrestee) does not contain a valid code of M=Male or F=Female. Note that U=Unknown (if entered) is not a valid sex for an arrestee."),
/**
 * 759 cannot be validated	
 */
	_759("759","Group B Arrest Segment","DUPLICATE GROUP 'B' ARREST REPORT SEGMENT ON FILE","The Group 'B' Arrest Report (Level 7) submitted as an Add is currently active in the FBI's database; therefore, it was rejected. If multiple arrestees are involved in the incident, ensure that Data Element 40 (Arrestee Sequence Number) is unique for each Arrestee Segment submitted so that duplication does not occur."),
	_760("760","Group B Arrest Segment","LEVEL 7 ARRESTS MUST HAVE A GROUP 'B' OFFENSE","Group 'B' Arrest Reports (Level 7) must contain a Group 'B' Offense Code in Data Element 45 (UCR Arrest Offense). The offense code submitted is not a Group 'B' offense code."),
/**
 * 761 not in 3.1	
 */
	_761("761","Group B Arrest Segment","ARRESTEE AGE MUST BE 01–17 FOR A RUNAWAY OFFENSE","Data Element 47 (Age of Arrestee) must be 01 through 17 for offense code of 90I=Runaway on a Group 'B' Arrest Report."),
/**
 *797 new to 3.1	
 */
	_797("797","Group B Arrest Segment","MISSING ARREST DATE FOR DELETE OF GROUP B ARREST","Data Element 42 (Arrest Date) is missing for a Group B Arrest Report with a Segment Action Type of D=Delete;must be populated with a valid data value and cannot be blank."),
/**
 *798 cannot be validated	
 */
	_798("798","Group B Arrest Segment","MISSING ARREST DATE FOR DELETE -ARREST TRANSACTION NUMBER MATCHES GROUP B ARREST REPORT WITH ARREST DATE OUTSIDE TWO YEAR WINDOW","Data Element 42 (Arrest Date) is missing for a Group B Arrest Report with a Segment Action Type of D=Delete; at least one Group B Arrest Report is on file that matches Data Element 41 (Arrest Transaction Number) with an Arrest Date outside the two year window."),
/**
 *799 new to 3.1	
 */
	_799("799","Group B Arrest Segment","MISSING ARREST DATE FOR DELETE -ARREST TRANSACTION NUMBER MATCHES MULTIPLE GROUP B ARREST REPORTS","Data Element 42 (Arrest Date) is missing for a Group B Arrest Report with a Segment Action Type of D=Delete; multiple Group B Arrest Reports are on file that match Data Element 41 (Arrest Transasction Number)."),	

	_001("001","Zero Report Segment","MUST BE POPULATED WITH A VALID DATA VALUE– MANDATORY FIELD","Mandatory Data Elements (Mandatory=Yes) must be populated with a valid data value and cannot be blank."),
/**
 * 090 not implemented	
 */
	_090_StructureCheck("090","Structure Check","ZERO-REPORTING MONTH IS NOT 01-12","A Segment Level 0 was submitted that had an invalid reporting month in positions 38 through 39. The data entered must be a valid month of 01 through 12."),
/**
 * 092 not implemented	
 */
	_092_StructureCheck("092","Structure Check","ZERO-REPORTING INCIDENT NUMBER MUST BE ALL ZEROS","A Segment Level 0 was submitted, but the incident number entered into positions 26 through 37 was not all zeros."),
/**
 * 093 cannot be validated	
 */
	_093_structurecheck("093","Structure Check","ZERO-REPORTING MONTH/YEAR WAS PRIOR TO THE BASE DATE","A Segment Level 0 was submitted with a month and year entered into positions 38 through 43 that is earlier than January 1 of the previous year or before the date the agency converted over to NIBRS."),
/**
 * 094 not implemented	
 */
	_094_structurecheck("094","Structure Check","ZERO-REPORTING MONTH/YEAR EXCEEDED MONTH/YEAR OF ELECTRONIC SUBMISSION","A Segment Level 0 was submitted with a month and year entered into positions 38 through 43 that was later than the Month of Electronic submission and Year of Electronic submission entered into positions 7 through 12. Note: Error Numbers 001, 015, 016, and 017 are also Zero-Reporting segment errors."),
/**
 * 090 Zero Report not implemented	
 */
	_090_ZeroReport("090","Zero Report Segment","ZERO REPORT MONTH IS NOT 01–12","Zero Report Month must be a valid month, data values 01 through 12."),
/**
 * 092 Zero Report not implemented	
 */
	_092_ZeroReport("092","Zero Report Segment","ZERO REPORT INCIDENT NUMBER MUST BE ALL ZEROS","Data Element 2 (Incident Number) in the Zero Report Segment (Level 0) must contain 12 zeros."),
/**
 * 093 Zero Report cannot be implemented
 */
	_093_ZeroReport("093","Zero Report Segment","ZERO REPORT MONTH/YEAR IS PRIOR TO AGENCY CONVERSION TO THE NIBRS","Zero Report Month and Zero Report Year must be later than the month and year in the date the LEA converted to the NIBRS."),
/**
 * 094 not implemented
 */
	_094_ZeroReport("094","Zero Report Segment","ZERO REPORT MONTH/YEAR EXCEEDED MONTH/YEAR OF SUBMISSION","Zero Report Month and Zero Report Year must be earlier than Month of Submission and Year of Submission.");
/**
 * 930 cannot be implemented
 */
	/**
	 * 931 cannot be implemented
	 */
	/**
	 * 932 cannot be implemented
	 */
	/**
	 * 933 cannot be implemented
	 */
	/**
	 * 934 cannot be implemented
	 */
	/**
	 * 952 cannot be implemented
	 */
	private String code;
	private String type;
	public String message;
	public String description;
	
	
	private NIBRSErrorCode(String code, String type, String message, String description) {
		this.setCode(code);
		this.setType(type);
		this.message = message;
		this.description = description;
	}


	public String getCode() {
		return code;
	}


	public void setCode(String code) {
		this.code = code;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}
	
}
