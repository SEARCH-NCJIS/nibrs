#' Apply segment edits to raw staging data and (optionally) load the edited data into an "intermediate" staging db
#'
#' Apply segment edits to raw staging data and (optionally) load the edited data into an "intermediate" staging db.
#' @importFrom DBI dbReadTable
#' @import dplyr
#' @import purrr
#' @import tibble
#' @param rawConn connection to the raw staging database
#' @param intermediateConn connection to the intermediate/edited staging database
#' @param writeToDatabase whether to write to the dimensional database or not
#' @return a list with all the staging database tables, as tibbles
#' @export
applyStagingEdits <- function(
  rawConn=DBI::dbConnect(RMariaDB::MariaDB(), host="localhost", dbname="search_nibrs_staging", username="root"),
  intermediateConn=DBI::dbConnect(RMariaDB::MariaDB(), host="localhost", dbname="search_nibrs_stagingi", username="root"),
  writeToDatabase=TRUE) {

  # note: the FACT_TABLES and DIMENSION_TABLES vectors are defined in LoadDimensional.R

  writeLines('Reading dimension tables from raw staging')
  dimensionTables <- map(DIMENSION_TABLES, function(tableName) {
    dbReadTable(rawConn, tableName) %>% as_tibble()
  }) %>% set_names(DIMENSION_TABLES)

  writeLines('Reading fact tables from raw staging')
  factTables <- map(FACT_TABLES, function(tableName) {
    dbReadTable(rawConn, tableName) %>% as_tibble()
  }) %>% set_names(FACT_TABLES)

  # find IncidentNumbers on administrative segments where the segment action type is != 3
  # group by IncidentNumber

  editedIncidents <- factTables$AdministrativeSegment %>%
#    filter(SegmentActionTypeTypeID != 3) %>%
    select(IncidentNumber, AgencyID) %>% distinct() %>%
    inner_join(factTables$AdministrativeSegment %>%
                 select(AdministrativeSegmentID, IncidentNumber, ReportTimestamp, SegmentActionTypeTypeID, AgencyID), by=c('IncidentNumber', 'AgencyID')) %>%
    group_by(IncidentNumber, AgencyID)

  writeLines(paste0("editedIncidents row count: ", nrow(editedIncidents)))
  latestEdits <- editedIncidents %>%
    filter(ReportTimestamp==max(ReportTimestamp)) %>%
    # it's possible that timestamps don't have adequate resolution to produce a single "latest record". when this happens, we select
    # from among the ties using the higher PK ID value (which will work if the table uses auto-incremented PKs)
    group_by(IncidentNumber, AgencyID, ReportTimestamp) %>%
    filter(AdministrativeSegmentID==max(AdministrativeSegmentID)) %>%
    ungroup()

  writeLines(paste0("latestEdits row count: ", nrow(latestEdits)))
  editedIncidents <- editedIncidents %>% ungroup()

  # where the latest (by ReportTimestamp) admin segment record has a action type of 2 (delete), then
  # delete all fact table records associated with that incident number (by AdministrativeSegmentID, and child segment IDs)

  pureDeletes <- editedIncidents %>% inner_join(latestEdits %>% select(IncidentNumber, SegmentActionTypeTypeID) %>% filter(SegmentActionTypeTypeID==2), by='IncidentNumber') %>% select(AdministrativeSegmentID)
  writeLines(paste0("Removing ", nrow(pureDeletes), " records where latest edit is action type D"))

  # where the latest admin segment record has an action type of 3 (incident report) or 4 (replace), then
  # delete all fact table records associated with the admin segment id of the *other* records for that incident number

  deleteEdits <- latestEdits %>% filter(SegmentActionTypeTypeID %in% 3:4)

  replaceDeletes <- editedIncidents %>%
    inner_join(deleteEdits %>% select(IncidentNumber, SegmentActionTypeTypeID), by='IncidentNumber') %>%
    anti_join(deleteEdits, by='AdministrativeSegmentID') %>% select(AdministrativeSegmentID)
  writeLines(paste0("Removing ", nrow(replaceDeletes), " records where latest edit is action type I or R (keeping ", nrow(deleteEdits %>% select(IncidentNumber) %>% distinct()), " incidents)"))

  factTables <- deleteIncidentsFrom(factTables, bind_rows(pureDeletes, replaceDeletes))

  # remove deleted Group Bs

  deletedGroupB <- factTables$ArrestReportSegment %>% filter(SegmentActionTypeTypeID==2) %>% select(ArrestReportSegmentID)
  writeLines(paste0("Deleting ", nrow(deletedGroupB), " Group B records with action type of D"))

  factTables$ArrestReportSegment <- factTables$ArrestReportSegment %>% anti_join(deletedGroupB, by='ArrestReportSegmentID')
  factTables$ArrestReportSegmentWasArmedWith <- factTables$ArrestReportSegmentWasArmedWith %>% anti_join(deletedGroupB, by='ArrestReportSegmentID')

  tables <- c(dimensionTables, factTables)

  if (writeToDatabase) {
    tables %>% iwalk(function(ddf, tableName) {
      writeLines(paste0("Writing edited table ", tableName, " to intermediate staging database"))
      writeDataFrameToDatabase(intermediateConn, ddf, tableName, viaBulk=TRUE, localBulk=FALSE, append=FALSE)
    })
  }

  tables

}

deleteIncidentsFrom <- function(factTables, deletes) {

  factTables$OffenderSuspectedOfUsing <- factTables$OffenderSuspectedOfUsing %>% anti_join(factTables$OffenseSegment %>% semi_join(deletes, by='AdministrativeSegmentID'), by='OffenseSegmentID')
  factTables$TypeCriminalActivity <- factTables$TypeCriminalActivity %>% anti_join(factTables$OffenseSegment %>% semi_join(deletes, by='AdministrativeSegmentID'), by='OffenseSegmentID')
  factTables$BiasMotivation <- factTables$BiasMotivation %>% anti_join(factTables$OffenseSegment %>% semi_join(deletes, by='AdministrativeSegmentID'), by='OffenseSegmentID')
  factTables$TypeOfWeaponForceInvolved <- factTables$TypeOfWeaponForceInvolved %>% anti_join(factTables$OffenseSegment %>% semi_join(deletes, by='AdministrativeSegmentID'), by='OffenseSegmentID')

  factTables$TypeInjury <- factTables$TypeInjury %>% anti_join(factTables$VictimSegment %>% semi_join(deletes, by='AdministrativeSegmentID'), by='VictimSegmentID')
  factTables$AggravatedAssaultHomicideCircumstances <- factTables$AggravatedAssaultHomicideCircumstances %>% anti_join(factTables$VictimSegment %>% semi_join(deletes, by='AdministrativeSegmentID'), by='VictimSegmentID')
  factTables$VictimOffenderAssociation <- factTables$VictimOffenderAssociation %>% anti_join(factTables$VictimSegment %>% semi_join(deletes, by='AdministrativeSegmentID'), by='VictimSegmentID')
  factTables$VictimOffenseAssociation <- factTables$VictimOffenseAssociation %>% anti_join(factTables$VictimSegment %>% semi_join(deletes, by='AdministrativeSegmentID'), by='VictimSegmentID')

  factTables$PropertyType <- factTables$PropertyType %>% anti_join(factTables$PropertySegment %>% semi_join(deletes, by='AdministrativeSegmentID'), by='PropertySegmentID')
  factTables$SuspectedDrugType <- factTables$SuspectedDrugType %>% anti_join(factTables$PropertySegment %>% semi_join(deletes, by='AdministrativeSegmentID'), by='PropertySegmentID')

  factTables$ArresteeSegmentWasArmedWith <- factTables$ArresteeSegmentWasArmedWith %>% anti_join(factTables$ArresteeSegment %>% semi_join(deletes, by='AdministrativeSegmentID'), by='ArresteeSegmentID')

  factTables$AdministrativeSegment <- factTables$AdministrativeSegment %>% anti_join(deletes, by='AdministrativeSegmentID')
  factTables$OffenseSegment <- factTables$OffenseSegment %>% anti_join(deletes, by='AdministrativeSegmentID')
  factTables$VictimSegment <- factTables$VictimSegment %>% anti_join(deletes, by='AdministrativeSegmentID')
  factTables$OffenderSegment <- factTables$OffenderSegment %>% anti_join(deletes, by='AdministrativeSegmentID')
  factTables$PropertySegment <- factTables$PropertySegment %>% anti_join(deletes, by='AdministrativeSegmentID')
  factTables$ArresteeSegment <- factTables$ArresteeSegment %>% anti_join(deletes, by='AdministrativeSegmentID')

  factTables

}
