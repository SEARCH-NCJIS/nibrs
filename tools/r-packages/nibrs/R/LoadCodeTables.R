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

#' @importFrom DBI dbClearResult dbSendQuery dbWriteTable
#' @importFrom readxl read_excel
#' @import dplyr
#' @import purrr
#' @import tibble
#' @export
loadCodeTables <- function(spreadsheetFile=NULL, conn=NULL, quiet=FALSE) {

  writeLines(paste0('Loading code tables package name ', getPackageName()))
  if (is.null(spreadsheetFile)) {
    spreadsheetFile <- system.file("raw", "NIBRSCodeTables.xlsx", package=getPackageName())
  }

  TOC <- read_excel(spreadsheetFile, sheet='TOC') %>%
    filter(Table != 'NibrsErrorCodeType')

  map2(TOC$Table, TOC$Tab, function(codeTableName, tabName) {
    ct <- read_excel(spreadsheetFile, sheet=tabName) %>%
      select(-starts_with('X_')) %>% as_tibble()
    if (!quiet) writeLines(paste0("Loading code table: ", codeTableName))
    if (!is.null(conn)) dbClearResult(dbSendQuery(conn, paste0("truncate ", codeTableName)))
    if (codeTableName=='UCROffenseCodeType') {
      colnames(ct) <- c('UCROffenseCodeTypeID', 'StateCode', 'StateDescription', 'OffenseCategory1',
                        'OffenseCategory2', 'OffenseCategory3', 'OffenseCategory4')
    } else if (codeTableName=='BiasMotivationType') {
      colnames(ct) <- c('BiasMotivationTypeID', 'StateCode', 'StateDescription', 'BiasMotivationCategory')
    } else if (codeTableName=='TypeOfWeaponForceInvolvedType') {
      colnames(ct) <- c('TypeOfWeaponForceInvolvedTypeID', 'StateCode', 'StateDescription', 'TypeOfWeaponForceInvolvedCategory')
    } else if (codeTableName=='AgencyType') {
      colnames(ct) <- c('AgencyTypeID', 'StateCode', 'StateDescription')
    } else {
      colnames(ct) <- c(paste0(codeTableName, 'ID'), 'StateCode', 'StateDescription')
    }
    ct <- ct %>% mutate(NIBRSCode=StateCode, NIBRSDescription=StateDescription) %>%
      mutate_at(vars(ends_with('ID')), as.integer) %>%
      mutate_at(vars(-ends_with('ID')), as.character) %>%
      mutate(
        StateCode=case_when(is.na(StateCode) & StateDescription=='Blank' ~ ' ', TRUE ~ StateCode),
        NIBRSCode=case_when(is.na(NIBRSCode) & NIBRSDescription=='Blank' ~ ' ', TRUE ~ NIBRSCode)
      )
    if (!is.null(conn)) dbWriteTable(conn=conn, value=ct, name=codeTableName, append=TRUE, row.names = FALSE)
    attr(ct, 'type') <- 'CT'
    ct
  }) %>% set_names(TOC$Table)

}

#' @importFrom readr read_csv
loadStatesForDimensional <- function() {
  ret <- read_csv(system.file("raw", "states.csv", package=getPackageName()), col_types='icc')
  attr(ret, 'type') <- 'CT'
  ret
}
