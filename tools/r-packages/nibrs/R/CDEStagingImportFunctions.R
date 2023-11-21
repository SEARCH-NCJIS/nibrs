#' @import sergeant
#' @import purrr
#' @import dplyr
#' @importFrom furrr future_map
#' @import DBI
#' @export
loadMultiStateYearDataToParquetDimensional <- function(zipDirectory, codeTableList=NULL, zipFileSampleFraction=1, stateAbbreviationRegex=NULL, yearRegex=NULL, parallel=FALSE,
                                                       sharedCsvDir='/opt/data/nibrs/cde/csv',
                                                       drillHost='localhost',
                                                       drillPort=8047L,
                                                       writeProgressDetail=TRUE, cleanUpCsv=TRUE, reusePriorCsv=FALSE, accumListStorageDir='.') {

  writeLines('START HERE!!!')  # JD


  if (is.null(codeTableList)) {
    writeLines('Loading code tables')
    codeTableList <- loadCodeTables(quiet=TRUE)
    codeTableList$State <- loadStatesForDimensional()
  }

  pattern <- '.+\\.zip$'

  if (is.null(stateAbbreviationRegex)) {
    stateAbbreviationRegex <- '[A-Z]{2}'
  }

  if (is.null(yearRegex)) {
    yearRegex <- '[0-9]{4}'
  }

  pattern <- paste0('^(', stateAbbreviationRegex, ')\\-(', yearRegex, ')\\.zip$')

  files <- list.files(zipDirectory, pattern=pattern, full.names=TRUE)

  files <- sample(files, zipFileSampleFraction*length(files))

  files <- sort(files)

  if (writeProgressDetail) {
    writeLines(paste0('Will process the following CDE zip files:'))
    writeLines(files)
  }

  accumListFilePath <- file.path(accumListStorageDir, 'accumList.rds')

  mapf <- map

  keepTablesOfType <- function(dfList, type) {
    keep(dfList, function(tdf) {
      attr(tdf, "type")==type
    })
  }

  keepCodeTables <- function(dfList) keepTablesOfType(dfList, 'CT')
  keepFactTables <- function(dfList) keepTablesOfType(dfList, 'FT')

  if (!reusePriorCsv) {

    writeLines(paste0('Processing ', length(files), ' input CDE files'))

    accumList <- files %>% mapf(function(file) {

      state <- getStateCodeFromFile(file)
      yr <- as.integer(gsub(x=basename(file), pattern='.+\\-([0-9]{4})\\.zip$', replacement='\\1'))

      if (writeProgressDetail) writeLines(paste0('Processing NIBRS CDE file ', file, ', (state=', state, ', yr=', yr, ')'))

      directory <- tempfile(pattern='dir')
      unzip(file, exdir=directory, junkpaths=TRUE)
      sdfs <- loadCDEDataToStaging(directory, codeTableList, state, writeProgressDetail=writeProgressDetail)
      unlink(directory, TRUE)

      accumDfList <- NULL

      if (!is.null(sdfs)) {

        ddfs <- convertStagingTablesToDimensional(keepCodeTables(sdfs), keepFactTables(sdfs), DEFAULT_AGE_GROUP_FUNCTION_FACTORY, writeProgressDetail=writeProgressDetail)

        accumDfList <- list()
        accumDfList$DateType <- ddfs$DateType
        accumDfList$Agency <- ddfs$Agency %>% select(-Population)
        accumDfList$state <- state
        accumDfList$yr <- yr

        if (writeProgressDetail) writeLines(paste0('Writing out csvs for ', state, ' + ', yr, '...'))

        viewDir <- file.path(sharedCsvDir, state, yr)
        unlink(viewDir, recursive=TRUE)
        dir.create(viewDir, recursive=TRUE)

        factTables <- keepFactTables(ddfs)

        accumDfList$factTableNames <- names(factTables)

        accumDfList$CharFieldLengthTallyList <- factTables %>%
          imap(function(ftdf, tableName) {
            ret <- NULL
            if (nrow(ftdf)) {
              if (writeProgressDetail) writeLines(paste0('  writing fact table: ', tableName))
              write_csv(ftdf %>% mutate(CDEYear=yr), file.path(viewDir, paste0(tableName, '.csvh')), na='')
              if (writeProgressDetail) writeLines('  done')
              ret <- getCharFieldTypes(ftdf)
            } else {
              if (writeProgressDetail) writeLines(paste0('  skipping fact table ', tableName, ' for ', state, ' + ', yr, ' since table is empty'))
            }
            ret
          }) %>% set_names(names(factTables))

      } else {
        if (writeProgressDetail) writeLines(paste0('Cannot create parquet/drill data for ', file, ' because input data frame list is null'))
      }

      accumDfList

    })

    if (!cleanUpCsv) saveRDS(accumList, accumListFilePath)

  } else if (file.exists('accumList.rds')) {
    writeLines('Reusing CSV files written in a prior run')
    accumList <- readRDS(accumListFilePath)
  } else {
    stop('Cannot reuse prior csv if accumList object is not saved from the prior run')
  }

  writeLines(paste0('Removing empty accumulated (cross state/yr) objects'))
  pre <- length(accumList)
  accumList <- compact(accumList)
  writeLines(paste0('Removed ', (pre - length(accumList)), ' empty objects'))

  accumFactTableNames <- accumList %>%
    keep(function(member) { !is.null(member$factTableNames) }) %>%
    reduce(function(accum, value) {
      list(factTableNames=sort(unique(c(accum$factTableNames, value$factTableNames))))
    }, .init=list(factTableNames=c())) %>% unlist()

  writeLines(paste0('Accumulated fact table names: ', paste0(accumFactTableNames, collapse=', ')))

  factTableCharFieldTypes <- accumList %>%
    keep(function(member) { !is.null(member$CharFieldLengthTallyList) }) %>%
    reduce(function(accum, value) {
      state <- value$state
      yr <- value$yr
      writeLines(paste0('Accumulating char field types for state=', state, ', yr=', yr))
      value <- value$CharFieldLengthTallyList
      tableColList <- accumFactTableNames %>% map(function(tableName) {
        valueTable <- value[[tableName]]
        accumTable <- accum[[tableName]]
        ret <- NULL
        if (is.null(valueTable) && !is.null(accumTable)) {
          writeLines(paste0('No fact table for ', tableName, ' found for ', state, '+', yr))
          ret <- accumTable
        } else if (is.null(accumTable) && !is.null(valueTable)) {
          writeLines(paste0('Accumulated fact table is missing for ', tableName, ' while processing ', state, '+', yr))
          ret <- valueTable
        } else if (!is.null(accumTable) && !is.null(valueTable)) {
          ret <- map2_chr(valueTable, accumTable, function(valueCol, accumCol) {
            pp <- 'varchar\\(([0-9]+)\\)'
            valueValue <- as.integer(gsub(x=valueCol, pattern=pp, replacement='\\1'))
            accumValue <- as.integer(gsub(x=accumCol, pattern=pp, replacement='\\1'))
            ret <- case_when(valueValue > accumValue ~ valueCol, TRUE ~ accumCol)
            case_when(ret=='varchar(0)' ~ 'varchar(1)', TRUE ~ ret)
          }) %>% set_names(names(valueTable))
        } else {
          writeLines(paste0('Somehow both the accumulated fact table ', tableName, ' and the instance for ', state, '+', yr, ' are missing.'))
        }
        ret
      })
      tableColList %>% set_names(accumFactTableNames)
    }, .init=accumList[[1]]$CharFieldLengthTallyList)

  writeLines('Harvesting code tables from first non-null CDE file')

  enhancedCodeTableList <- NULL
  factTableNameList <- NULL
  typeSensitiveSelectColList <- NULL

  getTypeSensitiveSelectColList <- function(tableDf, tableName) {
    cns <- colnames(tableDf)
    charFieldTypes <- factTableCharFieldTypes[[tableName]]
    if (is.null(charFieldTypes)) {
      charFieldTypes <- getCharFieldTypes(tableDf)
    }
    otherFieldTypes <- character(0)
    otherFields <- setdiff(cns, names(charFieldTypes))
    if (length(otherFields) > 0) {
      otherFieldTypes <- tableDf %>%
      select(otherFields) %>%
      db_data_type(ANSI(), fields=.)
    }
    cns %>% map_chr(function(cn, types) {
      paste0("case when ", cn, "='' then cast(NULL as ", types[cn], ") else cast(", cn, " as ", types[cn], ") end as ", cn)
    }, c(charFieldTypes, otherFieldTypes)) %>% paste0(collapse=', ')
  }

  while (is.null(enhancedCodeTableList)) {
    for (file in files) {
      directory <- tempfile(pattern='dir')
      unzip(file, exdir=directory, junkpaths=TRUE)
      sdfs <- loadCDEDataToStaging(directory, codeTableList, getStateCodeFromFile(file), writeProgressDetail=writeProgressDetail)
      unlink(directory, TRUE)
      if (!is.null(sdfs)) {
        ddfs <- convertStagingTablesToDimensional(keepCodeTables(sdfs), keepFactTables(sdfs), DEFAULT_AGE_GROUP_FUNCTION_FACTORY, writeProgressDetail=writeProgressDetail)
        enhancedCodeTableList <- keepCodeTables(ddfs)
        factTableList <- keepFactTables(ddfs)
        factTableList <- factTableList %>%
          map(function(factTable) {
            factTable %>% mutate(CDEYear=1L) # doesn't matter what this value is as long as it's an integer...we are just determining column types here
          }) %>% set_names(names(factTableList))
        factTableNameList <- names(factTableList)
        typeSensitiveSelectColList <- c(
          imap_chr(enhancedCodeTableList, getTypeSensitiveSelectColList) %>% set_names(names(enhancedCodeTableList)),
          imap_chr(factTableList, getTypeSensitiveSelectColList) %>% set_names(names(factTableList))
        )
        break
      }
    }
  }

  enhancedCodeTableList <- enhancedCodeTableList[setdiff(names(enhancedCodeTableList), c('Agency', 'DateType'))]

  writeLines(paste0('Concatenating date/agency tables and removing char field length tally object'))
  accumList <- reduce(accumList, function(accumDfList, eachDfList) {
    accumDfList$DateType <- bind_rows(accumDfList$DateType, eachDfList$DateType) %>% distinct()
    accumDfList$Agency <- bind_rows(accumDfList$Agency, eachDfList$Agency) %>% distinct()
    accumDfList$CharFieldLengthTallyList <- NULL
    accumDfList$state <- NULL
    accumDfList$yr <- NULL
    accumDfList$factTableNames <- NULL
    accumDfList
  })

  accumList$Agency <- accumList$Agency %>%
    group_by(AgencyORI) %>%
    filter(!((AgencyName=='' | is.na(AgencyName)) & n() > 1)) %>%
    arrange(AgencyName, .by_group=TRUE) %>%
    filter(row_number()==1) %>%
    mutate(AgencyName=case_when(is.na(AgencyName) ~ 'NAME UNKNOWN', TRUE ~ AgencyName)) %>%
    mutate(AgencyName=paste0(toupper(AgencyName), ' (', AgencyORI, ')')) %>%
    ungroup()

  typeSensitiveSelectColList['Agency'] <- getTypeSensitiveSelectColList(accumList$Agency)
  typeSensitiveSelectColList['DateType'] <- getTypeSensitiveSelectColList(accumList$DateType)

  # make sure the the sharedCsvDir is mounted into the nibrs-drill container at /nibrs/csv

  db <- src_drill(host=drillHost, port=drillPort)

  reportResult <- function(resultDf, tableName) {
    if (nrow(resultDf) && ncol(resultDf)) writeLines(paste0('Wrote ', resultDf[[1,1]], ' ', tableName, ' records'))
  }

  createTableAndView <- function(tableName, partitionBy='', csvDirsPath='/', writeCTASSQL=FALSE) {

    getColList <- function(tablePath) {
      dbGetQuery(db$con, paste0('SELECT * FROM ', tablePath, ' limit 1')) %>%
        select(-matches('^dir[0-9]+$')) %>%
        colnames() %>%
        paste0(collapse=',')
    }

    writeLines(paste0('Creating aggregate parquet dataset for ', tableName))
    parquetFile <- paste0('dfs.nibrs.`/parquet/', tableName, '.parquet`')

    result <- dbGetQuery(db$con, paste0('DROP TABLE IF EXISTS ', parquetFile))
    result <- dbGetQuery(db$con, paste0('DROP VIEW IF EXISTS dfs.nibrs.', tableName))
    ctasSql <- paste0('CREATE TABLE ', parquetFile, ' ', partitionBy, ' AS SELECT ', typeSensitiveSelectColList[tableName], ' FROM dfs.nibrs.`/csv', csvDirsPath, tableName, '.csvh`')
    if (writeCTASSQL) writeLines(paste0('CTAS SQL: ', ctasSql))
    result <- dbGetQuery(db$con, ctasSql)
    reportResult(result, tableName)
    result <- dbGetQuery(db$con, paste0('CREATE VIEW dfs.nibrs.', tableName, ' as SELECT ', getColList(parquetFile), ' FROM ', parquetFile))

    invisible()

  }

  writeLines('Creating parquet datasets for fact tables')
  walk(factTableNameList, createTableAndView, partitionBy='PARTITION BY (StateID, CDEYear)', csvDirsPath='/*/*/')

  writeLines('Creating parquet datasets for static code tables')
  enhancedCodeTableList %>% iwalk(function(tdf, tableName) {
    write_csv(tdf, file.path(sharedCsvDir, paste0(tableName, '.csvh')), na='')
    createTableAndView(tableName)
  })

  writeLines('Creating parquet datasets for agency and date code tables')
  accumList %>% iwalk(function(tdf, tableName) {
    if (tableName %in% c('Agency', 'DateType')) {
      write_csv(tdf, file.path(sharedCsvDir, paste0(tableName, '.csvh')), na='')
      createTableAndView(tableName)
    }
  })

  if (cleanUpCsv) {
    writeLines('Cleaning up csv temp files from Drill...')
    list.files(sharedCsvDir, full.names=TRUE) %>% unlink(recursive=TRUE)
    writeLines('...csv cleanup done.')
  }

  invisible(c(factTableList, enhancedCodeTableList, accumList))

}

getStateCodeFromFile <- function(file) {
  gsub(x=basename(file), pattern='^(.{2})-.+', replacement='\\1')
}

#' Process a directory of FBI Crime Data Explorer extract zips
#'
#' Process a directory of FBI Crime Data Explorer extract zips.  We load each zip file into the staging format, then after doing that for all zip files, we
#' concatenate each staging table across input extracts to make one big list of tables in the staging format.
#'
#' @param zipDirectory the directory containing the CDE extract zips
#' @param codeTableList a list of staging code tables, or NULL to read them from the package
#' @param zipFileSampleFraction a value between 0 and 1 used to sample the input files (default is 1, to use all input files)
#' @param stateAbbreviationRegex a regex to select input files, as applied to the state abbreviation portion of the filename; it's advised to use non-capturing groups around the regex (e.g., (?:AR|AL|MI) would select
#' Arkansas, Alabama, and Michigan)
#' @param yearRegex a regex to select input files, as applied to the year portion of the filename
#' @param parallel whether to process in parallel on multiple threads/cores, via the furrr package
#' @return a list, containing the concatenated staging tables from the parsed input extracts
#' @import dplyr
#' @import purrr
#' @importFrom furrr future_map
#' @export
loadMultiStateYearDataToStaging <- function(zipDirectory, codeTableList=NULL, zipFileSampleFraction=1, stateAbbreviationRegex=NULL, yearRegex=NULL, parallel=FALSE, writeProgressDetail=TRUE) {

  if (is.null(codeTableList)) {
    writeLines('Loading code tables')
    codeTableList <- loadCodeTables(quiet=TRUE)
  }

  pattern <- '.+\\.zip$'

  if (is.null(stateAbbreviationRegex)) {
    stateAbbreviationRegex <- '[A-Z]{2}'
  }

  if (is.null(yearRegex)) {
    yearRegex <- '[0-9]{4}'
  }

  pattern <- paste0('^', stateAbbreviationRegex, '-', yearRegex, '\\.zip$')

  files <- list.files(zipDirectory, pattern=pattern, full.names=TRUE)

  files <- sample(files, zipFileSampleFraction*length(files))

  mapf <- map
  reducef <- reduce

  ret <- files %>% mapf(function(file) {
    state <- gsub(x=basename(file), pattern='^(.{2})-.+', replacement='\\1')
    year <- gsub(x=basename(file), pattern='.+\\-([0-9]{4})\\.zip$', replacement='\\1')
    writeLines(paste0('Processing NIBRS CDE file ', file, ', (state=', state, ', year=', year, ')'))
    directory <- tempfile(pattern='dir')
    unzip(file, exdir=directory, junkpaths=TRUE)
    sdfs <- loadCDEDataToStaging(directory, codeTableList, getStateCodeFromFile(file), writeProgressDetail=writeProgressDetail)
    unlink(directory, TRUE)
    if (!is.null(sdfs)) {
      sdfs$AdministrativeSegment <- sdfs$AdministrativeSegment %>% mutate(CDEYear=year)
      attr(sdfs$AdministrativeSegment, 'type') <- 'FT'
      sdfs$ArrestReportSegment <- sdfs$ArrestReportSegment %>% mutate(CDEYear=year)
      attr(sdfs$ArrestReportSegment, 'type') <- 'FT'
    }
    ret <- sdfs
  })

  writeLines(paste0('Removing empty inputs'))
  ret <- keep(ret, function(member) !is.null(member))

  nInputs <- length(ret)
  writeLines(paste0('Merging ', nInputs, ' state+year input datasets into single set of staging tables'))
  ret <- reducef(ret, function(accumList, theDfs) {
    if (is.null(accumList$accumDfs)) {
      accumList$accumDfs <- theDfs
      accumList$mergeCount = 1
    } else {
      accumList$accumDfs <- accumList$accumDfs %>%
        imap(function(accumDf, dfName, theDfs) {
          theDf <- theDfs[[dfName]]
          if (attr(theDf, 'type')=='FT' || dfName %in% c('Agency','DateType')) {
            ret <- accumDf %>% bind_rows(theDf)
            attr(ret, 'type') <- attr(theDf, 'type')
          } else {
            ret <- accumDf
          }
          ret
        }, theDfs) %>% set_names(names(accumList$accumDfs))
      accumList$mergeCount <- accumList$mergeCount + 1
    }
    writeLines(paste0('Merged input dataset ', accumList$mergeCount, ' of ', nInputs))
    accumList
  }, .init=list())

  ret <- ret$accumDfs
  ret$Agency <- ret$Agency %>% distinct()
  ret$DateType <- ret$DateType %>% distinct()

  ret

}

#' Load FBI Crime Data Explorer extracts into a list of tables in the staging db format
#' @param singleStateYearDirectory a directory containing the files in a single state+year extract downloaded from the CDE site
#' @param codeTableList a list of staging code tables, or NULL to read them from the package
#' @import stringr
#' @import dplyr
#' @import readr
#' @importFrom lubridate dmy
#' @export
loadCDEDataToStaging <- function(singleStateYearDirectory, codeTableList=NULL, state, writeProgressDetail=TRUE) {

  if (is.null(codeTableList)) {
    writeLines('Loading code tables')
    ret <- loadCodeTables(quiet=TRUE)
  } else {
    ret <- c(list(), codeTableList)
  }

  ret <- map(ret, function(ct) {
    # just in case they are passed in without the attributes set
    attr(ct, 'type') <- 'CT'
    ct
  })

  if (writeProgressDetail) writeLines('Loading agencies')

  # found out there are two different formats for the agencies table, depending on state/year

  if (file.exists(file.path(singleStateYearDirectory, 'agencies.csv'))) {

    # we spell out the cols for agencies.csv because there are columns with illegal names ("0")
    agencyDf <- read_csv(file.path(singleStateYearDirectory, 'agencies.csv'),
                         skip=1,
                         col_types='-i-cc-----cccc--cc------ci---------------------------------',
                         col_names=c('AGENCY_ID','ORI','LEGACY_ORI','UCR_AGENCY_NAME','NCIC_AGENCY_NAME','PUB_AGENCY_NAME','PUB_AGENCY_UNIT','STATE_NAME','STATE_ABBR','AGENCY_TYPE_NAME','POPULATION'))

  } else if (file.exists(file.path(singleStateYearDirectory, 'cde_agencies.csv'))) {

    stateLookup <- datasets::state.name %>% set_names(datasets::state.abb)
    agencyDf <- read_csv(file.path(singleStateYearDirectory, 'cde_agencies.csv'),
                         col_types=c('icccc-c-----c----------i--i-----------------')) %>%
      select(AGENCY_ID=agency_id,
             ORI=ori,
             NCIC_AGENCY_NAME=agency_name,
             AGENCY_TYPE_NAME=agency_type_name,
             STATE_ABBR=state_abbr,
             POPULATION=population
             ) %>%
      mutate(STATE_NAME=case_when(STATE_ABBR=='DC' ~ 'District of Columbia', TRUE ~ stateLookup[STATE_ABBR]))

  } else {
    writeLines('No agencies.csv or cde_agencies.csv file found, creating empty agency table')
    agencyDf <- tibble()
  }

  if (nrow(agencyDf)==0) {
    writeLines('No agency records found, skipping this state/year')
    return(NULL)
  } else {
    # there are occasionally duplicates of agencies, where some columns have different values for the same agency, but those aren't columns we care about
    agencyDf <- agencyDf %>%
      select(AGENCY_ID, ORI, NCIC_AGENCY_NAME, POPULATION, STATE_ABBR, STATE_NAME, AGENCY_TYPE_NAME) %>%
      distinct()
  }

  if (writeProgressDetail) writeLines(paste0('Loaded ', nrow(agencyDf), ' Agency records'))

  setFactTableAttribute <- function(tdf) {
    attr(tdf, 'type') <- 'FT'
    tdf
  }

  dupAgencies <- agencyDf %>% group_by(AGENCY_ID) %>% filter(n()>1)
  if (nrow(dupAgencies) > 0) {
    warning(paste0('Duplicate agencies found, agencyIDs: ', paste0(dupAgencies$AGENCY_ID, collapse=',')))
  }

  files <- list.files(singleStateYearDirectory)
  fixFilename <- function(s) s
  if ('nibrs_incident.csv' %in% files) {
    fixFilename <- tolower
  }

  ret$AdministrativeSegment <- loadCDEIncidentData(ret, agencyDf, singleStateYearDirectory, state, fixFilename, writeProgressDetail)
  ret <- c(ret, map(loadCDEVictimData(ret, agencyDf, singleStateYearDirectory, fixFilename, writeProgressDetail), setFactTableAttribute))
  ret <- c(ret, map(loadCDEOffenseData(ret, singleStateYearDirectory, fixFilename, writeProgressDetail), setFactTableAttribute))
  ret <- c(ret, map(loadCDEArresteeData(ret, singleStateYearDirectory, state, fixFilename, writeProgressDetail), setFactTableAttribute))
  ret$AdministrativeSegment <- ret$AdministrativeSegment %>% anti_join(ret$ArrestReportSegment, by=c('AdministrativeSegmentID'='ArrestReportSegmentID')) %>% setFactTableAttribute()
  ret$OffenderSegment <- loadCDEOffenderData(ret, singleStateYearDirectory, fixFilename, writeProgressDetail) %>% setFactTableAttribute()
  ret <- c(ret, map(loadCDEPropertyData(ret, singleStateYearDirectory, fixFilename, writeProgressDetail), setFactTableAttribute))

  ret$AgencyType <- bind_rows(ret$AgencyType,
                              tibble(AgencyTypeID=100L, StateCode='100', StateDescription='Other') %>% mutate(NIBRSCode=StateCode, NIBRSDescription=StateDescription))
  attr(ret$AgencyType, 'type') <- 'CT'

  ret$Agency <- agencyDf %>%
    rename(AgencyID=AGENCY_ID,
           AgencyORI=ORI,
           AgencyName=NCIC_AGENCY_NAME,
           Population=POPULATION,
           StateCode=STATE_ABBR,
           StateName=STATE_NAME) %>%
    mutate(AGENCY_TYPE_NAME=case_when(
      AGENCY_TYPE_NAME=='Tribal' ~ 'Tribal agencies',
      AGENCY_TYPE_NAME=='Other State Agency' ~ 'Other state agencies',
      AGENCY_TYPE_NAME=='University or College' ~ 'University or college',
      TRUE ~ AGENCY_TYPE_NAME
    )) %>%
    left_join(ret$AgencyType %>% select(NIBRSDescription, AgencyTypeID), by=c('AGENCY_TYPE_NAME'='NIBRSDescription')) %>%
    select(AgencyID, AgencyORI, AgencyName, AgencyTypeID, StateCode, StateName, Population)

  attr(ret$Agency, 'type') <- 'CT'

  allDates <- c(
    ret$AdministrativeSegment$IncidentDate,
    ret$PropertyType$RecoveredDate,
    ret$ArresteeSegment$ArrestDate,
    ret$ArrestReportSegment$ArrestDate
  ) %>% unique()

  minDate <- min(allDates, na.rm=TRUE)
  maxDate <- max(allDates, na.rm=TRUE)

  ret$DateType <- writeDateDimensionTable(minDate, maxDate, writeProgressDetail=writeProgressDetail)

  ret

}

loadCDEPropertyData <- function(tableList, directory, fixFilename, writeProgressDetail) {

  if (writeProgressDetail) writeLines('Loading Property Segment data')

  ret <- list()

  tdf <- read_csv(file.path(directory, fixFilename('NIBRS_PROPERTY.csv')), col_types=cols(.default=col_character()), progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    mutate_at(vars(matches('_ID$|_NUM$')), as.integer) %>%
    rename(NumberOfStolenMotorVehicles=STOLEN_COUNT,
           NumberOfRecoveredMotorVehicles=RECOVERED_COUNT,
           TypePropertyLossEtcTypeID=PROP_LOSS_ID
    ) %>%
    mutate_at(vars(ends_with('MotorVehicles')), as.integer) %>%
    mutate(TypePropertyLossEtcTypeID=case_when(
      TypePropertyLossEtcTypeID==8 ~ 99999L,
      is.na(TypePropertyLossEtcTypeID) ~ 99998L,
      TRUE ~ TypePropertyLossEtcTypeID
    ),
    SegmentActionTypeTypeID=99998L
    ) %>%
    select(PropertySegmentID=PROPERTY_ID,
           SegmentActionTypeTypeID,
           AdministrativeSegmentID=INCIDENT_ID,
           TypePropertyLossEtcTypeID,
           NumberOfStolenMotorVehicles,
           NumberOfRecoveredMotorVehicles
           )

  ret$PropertySegment <- tdf
  if (writeProgressDetail) writeLines(paste0('Loaded ', nrow(tdf), ' Property Segment records'))

  propertyTypeCt <- read_csv(file.path(directory, fixFilename('NIBRS_PROP_DESC_TYPE.csv')), col_types='icc', progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    select(PROP_DESC_ID, PROP_DESC_CODE) %>%
    inner_join(tableList$PropertyDescriptionType, by=c('PROP_DESC_CODE'='NIBRSCode')) %>%
    select(PROP_DESC_ID, PropertyDescriptionTypeID)

  tdf <- read_csv(file.path(directory, fixFilename('NIBRS_PROPERTY_DESC.csv')), col_types=cols(.default=col_character()), progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    mutate_at(vars(matches('_ID$')), as.integer) %>%
    mutate(PROPERTY_VALUE=as.double(PROPERTY_VALUE),
           DATE_RECOVERED=convertDate(DATE_RECOVERED)) %>%
    left_join(propertyTypeCt, by='PROP_DESC_ID') %>%
    mutate(PropertyDescriptionTypeID=case_when(is.na(PropertyDescriptionTypeID) ~ 99998L, TRUE ~ PropertyDescriptionTypeID)) %>%
    select(
      PropertyTypeID=NIBRS_PROP_DESC_ID,
      PropertySegmentID=PROPERTY_ID,
      PropertyDescriptionTypeID,
      ValueOfProperty=PROPERTY_VALUE,
      RecoveredDate=DATE_RECOVERED
    ) %>% mutate(RecoveredDateID=createKeyFromDate(RecoveredDate))

  ret$PropertyType <- tdf
  if (writeProgressDetail) writeLines(paste0('Loaded ', nrow(tdf), ' Property Type records'))

  drugTypeCt <- read_csv(file.path(directory, fixFilename('NIBRS_SUSPECTED_DRUG_TYPE.csv')), col_types='icc', progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    select(SUSPECTED_DRUG_TYPE_ID, SUSPECTED_DRUG_CODE) %>%
    inner_join(tableList$SuspectedDrugTypeType, by=c('SUSPECTED_DRUG_CODE'='NIBRSCode')) %>%
    select(SUSPECTED_DRUG_TYPE_ID, SuspectedDrugTypeTypeID)

  drugMeasureTypeCt <- read_csv(file.path(directory, fixFilename('NIBRS_DRUG_MEASURE_TYPE.csv')), col_types='icc', progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    select(DRUG_MEASURE_TYPE_ID, DRUG_MEASURE_CODE) %>%
    inner_join(tableList$TypeDrugMeasurementType, by=c('DRUG_MEASURE_CODE'='NIBRSCode')) %>%
    select(DRUG_MEASURE_TYPE_ID, TypeDrugMeasurementTypeID)

  tdf <- read_csv(file.path(directory, fixFilename('NIBRS_SUSPECTED_DRUG.csv')), col_types=cols(.default=col_character()), progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    mutate_at(vars(matches('_ID$')), as.integer) %>%
    mutate(EstimatedDrugQuantity=as.numeric(EST_DRUG_QTY)) %>%
    left_join(drugTypeCt, by='SUSPECTED_DRUG_TYPE_ID') %>%
    left_join(drugMeasureTypeCt, by='DRUG_MEASURE_TYPE_ID') %>%
    mutate(SuspectedDrugTypeTypeID=case_when(is.na(SuspectedDrugTypeTypeID) ~ 99998L, TRUE ~ SuspectedDrugTypeTypeID)) %>%
    mutate(TypeDrugMeasurementTypeID=case_when(is.na(TypeDrugMeasurementTypeID) ~ 99998L, TRUE ~ TypeDrugMeasurementTypeID)) %>%
    select(
      SuspectedDrugTypeID=NIBRS_SUSPECTED_DRUG_ID,
      PropertySegmentID=PROPERTY_ID,
      SuspectedDrugTypeTypeID,
      TypeDrugMeasurementTypeID,
      EstimatedDrugQuantity
    )

  ret$SuspectedDrugType <- tdf
  if (writeProgressDetail) writeLines(paste0('Loaded ', nrow(tdf), ' Drug records'))

  ret

}

loadCDEVictimData <- function(tableList, agencyDf, directory, fixFilename, writeProgressDetail) {

  if (writeProgressDetail) writeLines('Loading Victim Segment data')

  ret <- list()

  raceCt <- buildRaceLookup(tableList, directory, fixFilename)
  ethnicityCt <- buildEthnicityLookup(tableList, directory, fixFilename)
  ageCt <- buildAgeCodeLookup(tableList, directory, fixFilename)

  victimTypeCt <- read_csv(file.path(directory, fixFilename('NIBRS_VICTIM_TYPE.csv')), col_types='icc', progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    select(VICTIM_TYPE_ID, VICTIM_TYPE_CODE) %>%
    inner_join(tableList$TypeOfVictimType, by=c('VICTIM_TYPE_CODE'='NIBRSCode')) %>%
    select(VICTIM_TYPE_ID, TypeOfVictimTypeID)

  circumstanceTypeCt <- read_csv(file.path(directory, fixFilename('NIBRS_ACTIVITY_TYPE.csv')), col_types='icc', progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    select(ACTIVITY_TYPE_ID, ACTIVITY_TYPE_CODE) %>%
    inner_join(tableList$OfficerActivityCircumstanceType, by=c('ACTIVITY_TYPE_CODE'='NIBRSCode')) %>%
    select(ACTIVITY_TYPE_ID, OfficerActivityCircumstanceTypeID)

  assignmentTypeCt <- read_csv(file.path(directory, fixFilename('NIBRS_ASSIGNMENT_TYPE.csv')), col_types='icc', progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    select(ASSIGNMENT_TYPE_ID, ASSIGNMENT_TYPE_CODE) %>%
    inner_join(tableList$OfficerAssignmentTypeType, by=c('ASSIGNMENT_TYPE_CODE'='NIBRSCode')) %>%
    select(ASSIGNMENT_TYPE_ID, OfficerAssignmentTypeTypeID)

  tdf <- read_csv(file.path(directory, fixFilename('NIBRS_VICTIM.csv')), col_types=cols(.default=col_character()), progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    mutate_at(vars(matches('_ID$|_NUM$')), as.integer) %>%
    rename(
      VictimSequenceNumber=VICTIM_SEQ_NUM,
      AgeOfVictimMin=AGE_RANGE_LOW_NUM,
      AgeOfVictimMax=AGE_RANGE_HIGH_NUM) %>%
    mutate(
      SegmentActionTypeTypeID=99998L,
      SEX_CODE=case_when(is.na(SEX_CODE) ~ ' ', TRUE ~ SEX_CODE),
      RESIDENT_STATUS_CODE=case_when(is.na(RESIDENT_STATUS_CODE) ~ ' ', TRUE ~ RESIDENT_STATUS_CODE),
      AgeOfVictimMin=case_when(is.na(AgeOfVictimMin) ~ AGE_NUM, TRUE ~ AgeOfVictimMin)
    ) %>%
    left_join(raceCt, by='RACE_ID') %>%
    left_join(ethnicityCt, by='ETHNICITY_ID') %>%
    left_join(tableList$SexOfPersonType %>% select(NIBRSCode, SexOfPersonTypeID), by=c('SEX_CODE'='NIBRSCode')) %>%
    left_join(tableList$ResidentStatusOfPersonType %>% select(NIBRSCode, ResidentStatusOfPersonTypeID), by=c('RESIDENT_STATUS_CODE'='NIBRSCode')) %>%
    left_join(ageCt, by='AGE_ID') %>%
    left_join(victimTypeCt, by='VICTIM_TYPE_ID') %>%
    left_join(circumstanceTypeCt, by='ACTIVITY_TYPE_ID') %>%
    left_join(assignmentTypeCt, by='ASSIGNMENT_TYPE_ID') %>%
    mutate(
      EthnicityOfPersonTypeID=case_when(is.na(EthnicityOfPersonTypeID) ~ 99998L, TRUE ~ EthnicityOfPersonTypeID),
      RaceOfPersonTypeID=case_when(is.na(RaceOfPersonTypeID) ~ 99998L, TRUE ~ RaceOfPersonTypeID),
      TypeOfVictimTypeID=case_when(is.na(TypeOfVictimTypeID) ~ 99998L, TRUE ~ TypeOfVictimTypeID),
      OfficerActivityCircumstanceTypeID=case_when(is.na(OfficerActivityCircumstanceTypeID) ~ 99998L, TRUE ~ OfficerActivityCircumstanceTypeID),
      OfficerAssignmentTypeTypeID=case_when(is.na(OfficerAssignmentTypeTypeID) ~ 99998L, TRUE ~ OfficerAssignmentTypeTypeID),
      AgeNeonateIndicator=as.integer(NonNumericAge=='NN'),
      AgeFirstWeekIndicator=as.integer(NonNumericAge=='NB'),
      AgeFirstYearIndicator=as.integer(NonNumericAge=='BB')
    ) %>%
    left_join(agencyDf %>% select(AGENCY_ID, OfficerOtherJurisdictionORI=ORI), by=c('OUTSIDE_AGENCY_ID'='AGENCY_ID')) %>%
    select(
      VictimSegmentID=VICTIM_ID,
      SegmentActionTypeTypeID,
      AdministrativeSegmentID=INCIDENT_ID,
      VictimSequenceNumber,
      TypeOfVictimTypeID,
      OfficerActivityCircumstanceTypeID,
      OfficerAssignmentTypeTypeID,
      OfficerOtherJurisdictionORI,
      AgeNumVictim=AGE_NUM,
      starts_with('AgeOfVictim'),
      matches('^Age.+Indicator$'),
      NonNumericAge,
      ends_with('OfPersonTypeID')
    )

  circumstanceTypeCt <- read_csv(file.path(directory, fixFilename('NIBRS_CIRCUMSTANCES.csv')), col_types='i-c-', progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    select(CIRCUMSTANCES_ID, CIRCUMSTANCES_CODE) %>%
    mutate(CIRCUMSTANCES_CODE=str_pad(CIRCUMSTANCES_CODE, 2, 'left', '0')) %>%
    inner_join(tableList$AggravatedAssaultHomicideCircumstancesType, by=c('CIRCUMSTANCES_CODE'='NIBRSCode')) %>%
    select(CIRCUMSTANCES_ID, AggravatedAssaultHomicideCircumstancesTypeID)

  justTypeCt <- read_csv(file.path(directory, fixFilename('NIBRS_JUSTIFIABLE_FORCE.csv')), col_types='icc', progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    select(JUSTIFIABLE_FORCE_ID, JUSTIFIABLE_FORCE_CODE) %>%
    inner_join(tableList$AdditionalJustifiableHomicideCircumstancesType, by=c('JUSTIFIABLE_FORCE_CODE'='NIBRSCode')) %>%
    select(JUSTIFIABLE_FORCE_ID, AdditionalJustifiableHomicideCircumstancesTypeID)

  victimCircumstances <- read_csv(file.path(directory, fixFilename('NIBRS_VICTIM_CIRCUMSTANCES.csv')), col_types=cols(.default=col_character()), progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    mutate_at(vars(matches('_ID$')), as.integer) %>%
    rename(VictimSegmentID=VICTIM_ID)

  victimCircumstancesJ <- victimCircumstances %>%
    inner_join(justTypeCt, by='JUSTIFIABLE_FORCE_ID') %>%
    select(VictimSegmentID, AdditionalJustifiableHomicideCircumstancesTypeID) %>%
    group_by(VictimSegmentID)

  if (nrow(victimCircumstancesJ %>% filter(n() > 1)) > 0) {
    warn('Multiple Additional Justifiable Homicide Circumstances codes detected for at least one victim, selecting only the first one that occurs in the data (NIBRS Spec only allows 1)')
    victimCircumstancesJ <- filter(victimCircumstancesJ, row_number()==1)
  }

  victimCircumstancesJ <- ungroup(victimCircumstancesJ)

  tdf <- left_join(tdf, victimCircumstancesJ, by='VictimSegmentID') %>%
    mutate(AdditionalJustifiableHomicideCircumstancesTypeID=case_when(is.na(AdditionalJustifiableHomicideCircumstancesTypeID) ~ 99998L, TRUE ~ AdditionalJustifiableHomicideCircumstancesTypeID))

  ret$VictimSegment <- tdf
  if (writeProgressDetail) writeLines(paste0('Loaded ', nrow(tdf), ' Victim Segment records'))

  tdf <- victimCircumstances %>%
    inner_join(circumstanceTypeCt, by='CIRCUMSTANCES_ID') %>%
    select(VictimSegmentID, AggravatedAssaultHomicideCircumstancesTypeID) %>%
    ungroup() %>% mutate(AggravatedAssaultHomicideCircumstancesID=row_number())

  ret$AggravatedAssaultHomicideCircumstances <- tdf

  injuryTypeCt <- read_csv(file.path(directory, fixFilename('NIBRS_INJURY.csv')), col_types='icc', progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    select(INJURY_ID, INJURY_CODE) %>%
    inner_join(tableList$TypeInjuryType, by=c('INJURY_CODE'='NIBRSCode')) %>%
    select(INJURY_ID, TypeInjuryTypeID)

  tdf <- read_csv(file.path(directory, fixFilename('NIBRS_VICTIM_INJURY.csv')), col_types=cols(.default=col_character()), progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    mutate_at(vars(matches('_ID$')), as.integer) %>%
    rename(VictimSegmentID=VICTIM_ID) %>%
    inner_join(injuryTypeCt, by='INJURY_ID') %>%
    select(VictimSegmentID, TypeInjuryTypeID) %>%
    ungroup() %>% mutate(TypeInjuryID=row_number())

  ret$TypeInjury <- tdf

  ret$VictimOffenseAssociation <- read_csv(file.path(directory, fixFilename('NIBRS_VICTIM_OFFENSE.csv')), col_types=cols(.default=col_character()), progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    mutate_at(vars(matches('_ID$')), as.integer) %>%
    rename(VictimSegmentID=VICTIM_ID, OffenseSegmentID=OFFENSE_ID) %>%
    ungroup() %>% mutate(VictimOffenseAssociationID=row_number())

  relationshipTypeCt <- read_csv(file.path(directory, fixFilename('NIBRS_RELATIONSHIP.csv')), col_types='icc', progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    select(RELATIONSHIP_ID, RELATIONSHIP_CODE) %>%
    inner_join(tableList$VictimOffenderRelationshipType, by=c('RELATIONSHIP_CODE'='NIBRSCode')) %>%
    select(RELATIONSHIP_ID, VictimOffenderRelationshipTypeID)

  tdf <- read_csv(file.path(directory, fixFilename('NIBRS_VICTIM_OFFENDER_REL.csv')), col_types=cols(.default=col_character()), progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    mutate_at(vars(matches('_ID$')), as.integer) %>%
    rename(VictimSegmentID=VICTIM_ID, OffenderSegmentID=OFFENDER_ID) %>%
    inner_join(relationshipTypeCt, by='RELATIONSHIP_ID') %>%
    select(VictimSegmentID, OffenderSegmentID, VictimOffenderRelationshipTypeID) %>%
    ungroup() %>% mutate(VictimOffenderAssociationID=row_number())

  ret$VictimOffenderAssociation <- tdf

  ret

}

loadCDEOffenderData <- function(tableList, directory, fixFilename, writeProgressDetail) {

  if (writeProgressDetail) writeLines('Loading Offender Segment data')

  raceCt <- buildRaceLookup(tableList, directory, fixFilename)
  ethnicityCt <- buildEthnicityLookup(tableList, directory, fixFilename)
  ageCt <- buildAgeCodeLookup(tableList, directory, fixFilename)

  tdf <- read_csv(file.path(directory, fixFilename('NIBRS_OFFENDER.csv')), col_types=cols(.default=col_character()), progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    mutate_at(vars(matches('_ID$|_NUM$')), as.integer) %>%
    rename(
      AgeOfOffenderMin=AGE_RANGE_LOW_NUM,
      AgeOfOffenderMax=AGE_RANGE_HIGH_NUM) %>%
    mutate(
      SegmentActionTypeTypeID=99998L,
      SEX_CODE=case_when(is.na(SEX_CODE) ~ ' ', TRUE ~ SEX_CODE),
      AgeOfOffenderMin=case_when(is.na(AgeOfOffenderMin) ~ AGE_NUM, TRUE ~ AgeOfOffenderMin)
    ) %>%
    left_join(raceCt, by='RACE_ID') %>%
    left_join(ethnicityCt, by='ETHNICITY_ID') %>%
    left_join(tableList$SexOfPersonType %>% select(NIBRSCode, SexOfPersonTypeID), by=c('SEX_CODE'='NIBRSCode')) %>%
    left_join(ageCt, by='AGE_ID') %>%
    mutate(
      EthnicityOfPersonTypeID=case_when(is.na(EthnicityOfPersonTypeID) ~ 99998L, TRUE ~ EthnicityOfPersonTypeID),
      RaceOfPersonTypeID=case_when(is.na(RaceOfPersonTypeID) ~ 99998L, TRUE ~ RaceOfPersonTypeID),
    ) %>%
    select(OffenderSegmentID=OFFENDER_ID,
           AdministrativeSegmentID=INCIDENT_ID,
           OffenderSequenceNumber=OFFENDER_SEQ_NUM,
           AgeNumOffender=AGE_NUM,
           starts_with('AgeOfOffender'),
           NonNumericAge,
           ends_with('OfPersonTypeID'))

  if (writeProgressDetail) writeLines(paste0('Loaded ', nrow(tdf), ' Offender Segment records'))

  tdf

}

loadCDEArresteeData <- function(tableList, directory, state, fixFilename, writeProgressDetail) {

  if (writeProgressDetail) writeLines('Loading Arrestee Segment data')

  arrestTypeCt <- read_csv(file.path(directory, fixFilename('NIBRS_ARREST_TYPE.csv')), col_types='icc', progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    select(ARREST_TYPE_ID, ARREST_TYPE_CODE) %>%
    inner_join(tableList$TypeOfArrestType, by=c('ARREST_TYPE_CODE'='NIBRSCode')) %>%
    select(ARREST_TYPE_ID, TypeOfArrestTypeID)

  raceCt <- buildRaceLookup(tableList, directory, fixFilename)
  ethnicityCt <- buildEthnicityLookup(tableList, directory, fixFilename)
  ageCt <- buildAgeCodeLookup(tableList, directory, fixFilename)
  offenseTypeCt <- buildOffenseTypeLookup(tableList, directory, fixFilename, TRUE)

  tdf <- read_csv(file.path(directory, fixFilename('NIBRS_ARRESTEE.csv')), col_types=cols(.default=col_character()), progress=FALSE) %>%
    set_names(toupper(colnames(.)))

  if (!('ARREST_NUM' %in% colnames(tdf))) {
    tdf <- tdf %>% mutate(ARREST_NUM=str_pad(ARRESTEE_ID, 12, 'left', '0'))
  }

  tdf <- tdf %>%
    mutate_at(vars(matches('_ID$|AGE_.+_NUM$|_SEQ_NUM$')), as.integer) %>%
    rename(
      ArrestTransactionNumber=ARREST_NUM,
      ArresteeSequenceNumber=ARRESTEE_SEQ_NUM,
      AgeOfArresteeMin=AGE_RANGE_LOW_NUM,
      AgeOfArresteeMax=AGE_RANGE_HIGH_NUM) %>%
    mutate(
      SegmentActionTypeTypeID=99998L,
      SEX_CODE=case_when(is.na(SEX_CODE) ~ ' ', TRUE ~ SEX_CODE),
      RESIDENT_CODE=case_when(is.na(RESIDENT_CODE) ~ ' ', TRUE ~ RESIDENT_CODE),
      UNDER_18_DISPOSITION_CODE=case_when(is.na(UNDER_18_DISPOSITION_CODE) ~ ' ', TRUE ~ UNDER_18_DISPOSITION_CODE),
      MULTIPLE_INDICATOR=case_when(is.na(MULTIPLE_INDICATOR) ~ ' ', TRUE ~ MULTIPLE_INDICATOR),
      AgeOfArresteeMin=case_when(is.na(AgeOfArresteeMin) ~ as.integer(AGE_NUM), TRUE ~ AgeOfArresteeMin),
      ArrestDate=convertDate(ARREST_DATE),
      ArrestDateID=createKeyFromDate(ArrestDate)
    ) %>%
    left_join(offenseTypeCt, by='OFFENSE_TYPE_ID') %>%
    left_join(raceCt, by='RACE_ID') %>%
    left_join(ethnicityCt, by='ETHNICITY_ID') %>%
    left_join(arrestTypeCt, by='ARREST_TYPE_ID') %>%
    left_join(tableList$SexOfPersonType %>% select(NIBRSCode, SexOfPersonTypeID), by=c('SEX_CODE'='NIBRSCode')) %>%
    left_join(tableList$ResidentStatusOfPersonType %>% select(NIBRSCode, ResidentStatusOfPersonTypeID), by=c('RESIDENT_CODE'='NIBRSCode')) %>%
    left_join(tableList$DispositionOfArresteeUnder18Type %>% select(NIBRSCode, DispositionOfArresteeUnder18TypeID), by=c('UNDER_18_DISPOSITION_CODE'='NIBRSCode')) %>%
    left_join(tableList$MultipleArresteeSegmentsIndicatorType %>% select(NIBRSCode, MultipleArresteeSegmentsIndicatorTypeID), by=c('MULTIPLE_INDICATOR'='NIBRSCode')) %>%
    left_join(ageCt, by='AGE_ID') %>%
    mutate(
      TypeOfArrestTypeID=case_when(is.na(TypeOfArrestTypeID) ~ 99998L, TRUE ~ TypeOfArrestTypeID),
      EthnicityOfPersonTypeID=case_when(is.na(EthnicityOfPersonTypeID) ~ 99998L, TRUE ~ EthnicityOfPersonTypeID),
      RaceOfPersonTypeID=case_when(is.na(RaceOfPersonTypeID) ~ 99998L, TRUE ~ RaceOfPersonTypeID),
      UCROffenseCodeTypeID=case_when(is.na(UCROffenseCodeTypeID) ~ 99998L, TRUE ~ UCROffenseCodeTypeID),
    )

  weaponCt <- read_csv(file.path(directory, fixFilename('NIBRS_WEAPON_TYPE.csv')), col_types='ic--', progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    mutate(WEAPON_ID=as.integer(WEAPON_ID))

  weaponDf <- read_csv(file.path(directory, fixFilename('NIBRS_ARRESTEE_WEAPON.csv')), col_types=cols(.default=col_character()), progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    mutate_at(vars(matches('_ID$')), as.integer) %>%
    select(WEAPON_ID, ARRESTEE_ID) %>%
    left_join(weaponCt, by='WEAPON_ID') %>%
    mutate(WEAPON_CODE=case_when(is.na(WEAPON_CODE) ~ ' ', WEAPON_CODE=='95' ~ '99', TRUE ~ WEAPON_CODE),
           AutomaticWeaponIndicator=str_sub(WEAPON_CODE, 3, 3), WEAPON_CODE=str_sub(WEAPON_CODE, 1, 2)) %>%
    left_join(tableList$ArresteeWasArmedWithType %>% select(ArresteeWasArmedWithTypeID, NIBRSCode), by=c('WEAPON_CODE'='NIBRSCode')) %>%
    select(ARRESTEE_ID, ArresteeWasArmedWithTypeID, AutomaticWeaponIndicator)

  ret <- list()

  if (writeProgressDetail) writeLines('Separating Group A and Group B segment data')

  ret$ArresteeSegment <- tdf %>%
    filter(OffenseCategory1=='Group A') %>% select(-OffenseCategory1) %>%
    select(ArresteeSegmentID=ARRESTEE_ID,
           SegmentActionTypeTypeID,
           AdministrativeSegmentID=INCIDENT_ID,
           ArresteeSequenceNumber,
           ArrestTransactionNumber,
           starts_with('ArrestDate'),
           TypeOfArrestTypeID,
           MultipleArresteeSegmentsIndicatorTypeID,
           AgeNumArrestee=AGE_NUM,
           starts_with('AgeOfArrestee'),
           NonNumericAge,
           ends_with('OfPersonTypeID'),
           DispositionOfArresteeUnder18TypeID,
           UCROffenseCodeTypeID)

  ret$ArresteeSegmentWasArmedWith <- weaponDf %>%
    semi_join(ret$ArresteeSegment, by=c('ARRESTEE_ID'='ArresteeSegmentID')) %>%
    rename(ArresteeSegmentID=ARRESTEE_ID) %>%
    ungroup() %>% mutate(ArresteeSegmentWasArmedWithID=row_number())

  ret$ArrestReportSegment <- tdf %>%
    filter(OffenseCategory1=='Group B') %>% select(-OffenseCategory1) %>%
    inner_join(tableList$AdministrativeSegment %>% select(AdministrativeSegmentID, AgencyID, ORI), by=c('INCIDENT_ID'='AdministrativeSegmentID')) %>%
    mutate(StateCode=state) %>%
    select(ArrestReportSegmentID=ARRESTEE_ID,
           SegmentActionTypeTypeID,
           AgencyID, ORI,
           ArrestTransactionNumber,
           ArresteeSequenceNumber,
           starts_with('ArrestDate'),
           TypeOfArrestTypeID,
           MultipleArresteeSegmentsIndicatorTypeID,
           AgeNumArrestee=AGE_NUM,
           starts_with('AgeOfArrestee'),
           NonNumericAge,
           ends_with('OfPersonTypeID'),
           DispositionOfArresteeUnder18TypeID,
           UCROffenseCodeTypeID,
           StateCode)

  ret$ArrestReportSegmentWasArmedWith <- weaponDf %>%
    semi_join(ret$ArrestReportSegment, by=c('ARRESTEE_ID'='ArrestReportSegmentID')) %>%
    rename(ArrestReportSegmentID=ARRESTEE_ID) %>%
    ungroup() %>% mutate(ArrestReportSegmentWasArmedWithID=row_number())

  if (writeProgressDetail) writeLines(paste0('Loaded ', nrow(ret$ArresteeSegment), ' Group A Arrestee Segment records'))
  if (writeProgressDetail) writeLines(paste0('Loaded ', nrow(ret$ArrestReportSegment), ' Group B Arrest Report Segment records'))

  ret

}

buildRaceLookup <- function(tableList, directory, fixFilename) {
  read_csv(file.path(directory, fixFilename('REF_RACE.csv')), col_types='ic-----') %>%
    set_names(toupper(colnames(.))) %>%
    select(RACE_ID, RACE_CODE) %>%
    inner_join(tableList$RaceOfPersonType, by=c('RACE_CODE'='NIBRSCode')) %>%
    select(RACE_ID, RaceOfPersonTypeID)
}

buildEthnicityLookup <- function(tableList, directory, fixFilename) {

  ret <- read_csv(file.path(directory, fixFilename('NIBRS_ETHNICITY.csv')), col_types=cols(.default=col_character())) %>%
    set_names(toupper(colnames(.)))

  if ('HC_FLAG' %in% colnames(ret)) {
    ret <- select(ret, -HC_FLAG)
  }

  ret %>%
    select(ETHNICITY_ID, ETHNICITY_CODE) %>%
    mutate(ETHNICITY_ID=as.integer(ETHNICITY_ID)) %>%
    inner_join(tableList$EthnicityOfPersonType, by=c('ETHNICITY_CODE'='NIBRSCode')) %>%
    select(ETHNICITY_ID, EthnicityOfPersonTypeID)

}

buildAgeCodeLookup <- function(tableList, directory, fixFilename) {
  read_csv(file.path(directory, fixFilename('NIBRS_AGE.csv')), col_types='icc') %>%
    set_names(toupper(colnames(.))) %>%
    select(AGE_ID, AGE_CODE) %>% filter(AGE_CODE %in% c('NN','NB','BB','00')) %>%
    rename(NonNumericAge=AGE_CODE)
}

buildOffenseTypeLookup <- function(tableList, directory, fixFilename, includeGroupIndicatorField=FALSE) {
  ret <- read_csv(file.path(directory, fixFilename('NIBRS_OFFENSE_TYPE.csv')), col_types=cols(.default=col_character())) %>%
    set_names(toupper(colnames(.))) %>%
    select(OFFENSE_TYPE_ID, OFFENSE_CODE) %>%
    mutate_at(vars(ends_with('_ID')), as.integer) %>%
    inner_join(tableList$UCROffenseCodeType, by=c('OFFENSE_CODE'='NIBRSCode')) %>%
    select(OFFENSE_TYPE_ID, UCROffenseCodeTypeID, OffenseCategory1)
  if (!includeGroupIndicatorField) {
    ret <- ret %>% select(-OffenseCategory1)
  }
  ret
}

loadCDEOffenseData <- function(tableList, directory, fixFilename, writeProgressDetail) {

  if (writeProgressDetail) writeLines('Loading Offense Segment data')

  ret <- list()

  locationTypeCt <- read_csv(file.path(directory, fixFilename('NIBRS_LOCATION_TYPE.csv')), col_types='icc', progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    select(LOCATION_ID, LOCATION_CODE) %>%
    inner_join(tableList$LocationTypeType, by=c('LOCATION_CODE'='NIBRSCode')) %>%
    select(LOCATION_ID, LocationTypeTypeID)

  offenseTypeCt <- buildOffenseTypeLookup(tableList, directory, fixFilename)

  offenseTypes <- read_csv(file.path(directory, fixFilename('NIBRS_OFFENSE_TYPE.csv')), col_types=cols(.default=col_character())) %>%
    set_names(toupper(colnames(.))) %>%
    select(OFFENSE_TYPE_ID, OFFENSE_CODE) %>%
    mutate_at(vars(ends_with('_ID')), as.integer) %>%
    filter(OFFENSE_CODE %in% c('23*', '23H'))

  star23 <- offenseTypes %>% filter(OFFENSE_CODE=='23*') %>% .$OFFENSE_TYPE_ID
  h23 <- offenseTypes %>% filter(OFFENSE_CODE=='23H') %>% .$OFFENSE_TYPE_ID

  tdf <- read_csv(file.path(directory, fixFilename('NIBRS_OFFENSE.csv')), col_types=cols(.default=col_character()), progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    mutate_at(vars(ends_with('_ID')), as.integer) %>%
    mutate(NumberOfPremisesEntered=as.integer(NUM_PREMISES_ENTERED),
           SegmentActionTypeTypeID=99998L,
           OffenseSegmentID=OFFENSE_ID, AdministrativeSegmentID=INCIDENT_ID,
           OffenseAttemptedCompleted=ATTEMPT_COMPLETE_FLAG,
           OFFENSE_TYPE_ID=case_when(OFFENSE_TYPE_ID==star23 ~ h23, TRUE ~ OFFENSE_TYPE_ID), # 23* is not a valid nibrs offense code, 23H is closest per Becki
           METHOD_ENTRY_CODE=case_when(is.na(METHOD_ENTRY_CODE) ~ ' ', TRUE ~ METHOD_ENTRY_CODE)) %>%
    left_join(locationTypeCt, by='LOCATION_ID') %>%
    left_join(offenseTypeCt, by='OFFENSE_TYPE_ID') %>%
    left_join(tableList$MethodOfEntryType %>% select(NIBRSCode, MethodOfEntryTypeID), by=c('METHOD_ENTRY_CODE'='NIBRSCode')) %>%
    mutate(
      LocationTypeTypeID=case_when(is.na(LocationTypeTypeID) ~ 99998L, TRUE ~ LocationTypeTypeID),
      UCROffenseCodeTypeID=case_when(is.na(UCROffenseCodeTypeID) ~ 99998L, TRUE ~ UCROffenseCodeTypeID),
    ) %>%
    select(OffenseSegmentID, SegmentActionTypeTypeID, AdministrativeSegmentID, UCROffenseCodeTypeID, OffenseAttemptedCompleted, LocationTypeTypeID,
           NumberOfPremisesEntered, MethodOfEntryTypeID)

  ret$OffenseSegment <- tdf

  if (writeProgressDetail) writeLines(paste0('Loaded ', nrow(ret$OffenseSegment), ' Offense Segment records'))

  ct <- read_csv(file.path(directory, fixFilename('NIBRS_BIAS_LIST.csv')), col_types=cols(.default=col_character()), progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    mutate_at(vars(ends_with('_ID')), as.integer) %>%
    select(BIAS_ID, BIAS_CODE) %>%
    inner_join(tableList$BiasMotivationType, by=c('BIAS_CODE'='NIBRSCode')) %>%
    select(BIAS_ID, BiasMotivationTypeID)

  tdf <- read_csv(file.path(directory, fixFilename('NIBRS_BIAS_MOTIVATION.csv')), col_types=cols(.default=col_character()), progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    mutate_at(vars(ends_with('_ID')), as.integer) %>%
    left_join(ct, by='BIAS_ID') %>%
    select(OffenseSegmentID=OFFENSE_ID, BiasMotivationTypeID) %>%
    ungroup() %>% mutate(BiasMotivationID=row_number())

  ret$BiasMotivation <- tdf

  ct <- read_csv(file.path(directory, fixFilename('NIBRS_CRIMINAL_ACT_TYPE.csv')), col_types=cols(.default=col_character()), progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    select(CRIMINAL_ACT_ID, CRIMINAL_ACT_CODE) %>%
    mutate_at(vars(ends_with('_ID')), as.integer) %>%
    inner_join(tableList$TypeOfCriminalActivityType, by=c('CRIMINAL_ACT_CODE'='NIBRSCode')) %>%
    select(CRIMINAL_ACT_ID, TypeOfCriminalActivityTypeID)

  tdf <- read_csv(file.path(directory, fixFilename('NIBRS_CRIMINAL_ACT.csv')), col_types=cols(.default=col_character()), progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    mutate_at(vars(ends_with('_ID')), as.integer) %>%
    left_join(ct, by='CRIMINAL_ACT_ID') %>%
    select(OffenseSegmentID=OFFENSE_ID, TypeOfCriminalActivityTypeID) %>%
    ungroup() %>% mutate(TypeCriminalActivityID=row_number())

  ret$TypeCriminalActivity <- tdf

  ct <- read_csv(file.path(directory, fixFilename('NIBRS_USING_LIST.csv')), col_types=cols(.default=col_character()), progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    select(SUSPECT_USING_ID, SUSPECT_USING_CODE) %>%
    mutate_at(vars(ends_with('_ID')), as.integer) %>%
    inner_join(tableList$OffenderSuspectedOfUsingType, by=c('SUSPECT_USING_CODE'='NIBRSCode')) %>%
    select(SUSPECT_USING_ID, OffenderSuspectedOfUsingTypeID)

  tdf <- read_csv(file.path(directory, fixFilename('NIBRS_SUSPECT_USING.csv')), col_types=cols(.default=col_character()), progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    mutate_at(vars(ends_with('_ID')), as.integer) %>%
    left_join(ct, by='SUSPECT_USING_ID') %>%
    select(OffenseSegmentID=OFFENSE_ID, OffenderSuspectedOfUsingTypeID) %>%
    ungroup() %>% mutate(OffenderSuspectedOfUsingID=row_number())

  ret$OffenderSuspectedOfUsing <- tdf

  weaponCt <- read_csv(file.path(directory, fixFilename('NIBRS_WEAPON_TYPE.csv')), col_types=cols(.default=col_character()), progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    mutate(WEAPON_ID=as.integer(WEAPON_ID))

  ret$TypeOfWeaponForceInvolved <- read_csv(file.path(directory, fixFilename('NIBRS_WEAPON.csv')), col_types=cols(.default=col_character()), progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    mutate_at(vars(matches('_ID$')), as.integer) %>%
    select(WEAPON_ID, OFFENSE_ID) %>%
    left_join(weaponCt, by='WEAPON_ID') %>%
    mutate(WEAPON_CODE=case_when(is.na(WEAPON_CODE) ~ ' ', TRUE ~ WEAPON_CODE),
           AutomaticWeaponIndicator=str_sub(WEAPON_CODE, 3, 3), WEAPON_CODE=str_sub(WEAPON_CODE, 1, 2)) %>%
    left_join(tableList$TypeOfWeaponForceInvolvedType %>% select(TypeOfWeaponForceInvolvedTypeID, NIBRSCode), by=c('WEAPON_CODE'='NIBRSCode')) %>%
    select(OffenseSegmentID=OFFENSE_ID, TypeOfWeaponForceInvolvedTypeID, AutomaticWeaponIndicator) %>%
    ungroup() %>% mutate(TypeOfWeaponForceInvolvedID=row_number())

  ret

}

#' @importFrom lubridate dmy ymd_hms parse_date_time as_date
convertDate <- function(val) {
  parse_date_time(val, c('y-m-d H:M:S', 'd-b!-y!*')) %>% as_date()
}

loadCDEIncidentData <- function(tableList, agencyDf, directory, state, fixFilename, writeProgressDetail) {

  if (writeProgressDetail) writeLines('Loading Administrative Segment data')

  exceptionalClearanceCt <- read_csv(file.path(directory, fixFilename('NIBRS_CLEARED_EXCEPT.csv')), col_types=cols(.default=col_character()), progress=FALSE) %>%
    set_names(toupper(colnames(.))) %>%
    select(CLEARED_EXCEPT_ID, CLEARED_EXCEPT_CODE) %>%
    mutate_at(vars(ends_with('_ID')), as.integer) %>%
    inner_join(tableList$ClearedExceptionallyType, by=c('CLEARED_EXCEPT_CODE'='NIBRSCode')) %>%
    select(CLEARED_EXCEPT_ID, ClearedExceptionallyTypeID)

  ret <- read_csv(file.path(directory, fixFilename('NIBRS_incident.csv')), col_types=cols(.default=col_character()), progress=FALSE) %>%
    set_names(toupper(colnames(.)))

  ret <- ret %>%
    mutate_at(vars(ends_with('_ID')), as.integer) %>%
    inner_join(agencyDf %>% select(ORI, AGENCY_ID), by='AGENCY_ID') %>%
    rename(AgencyID=AGENCY_ID,
           IncidentHour=INCIDENT_HOUR,
           ReportDateIndicator=REPORT_DATE_FLAG) %>%
    mutate(IncidentNumber=str_pad(INCIDENT_ID, 12, 'left', '0'),
           SegmentActionTypeTypeID=99998L,
           StateCode=state,
           IncidentDate=convertDate(INCIDENT_DATE),
           ExceptionalClearanceDate=convertDate(CLEARED_EXCEPT_DATE),
           IncidentDateID=createKeyFromDate(IncidentDate),
           ExceptionalClearanceDateID=createKeyFromDate(ExceptionalClearanceDate),
           CARGO_THEFT_FLAG=case_when(is.na(CARGO_THEFT_FLAG) ~ ' ', TRUE ~ CARGO_THEFT_FLAG)) %>%
    left_join(exceptionalClearanceCt, by='CLEARED_EXCEPT_ID') %>%
    left_join(tableList$CargoTheftIndicatorType %>% select(NIBRSCode, CargoTheftIndicatorTypeID), by=c('CARGO_THEFT_FLAG'='NIBRSCode')) %>%
    select(AdministrativeSegmentID=INCIDENT_ID, SegmentActionTypeTypeID, StateCode, ORI, AgencyID, IncidentNumber,
           IncidentDate, IncidentDateID, ReportDateIndicator, IncidentHour, ClearedExceptionallyTypeID, ExceptionalClearanceDate, ExceptionalClearanceDateID, CargoTheftIndicatorTypeID)

  if (writeProgressDetail) writeLines(paste0('Loaded ', nrow(ret), ' Administrative Segment records'))

  ret

}
