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

getChildSegmentID <- function(incidentID, childSegmentSequenceNumber) {
  # different implementation needed if there were ever more than 10 child sequence numbers in NIBRS...but that's unlikely
  (incidentID*10) + (childSegmentSequenceNumber - 1)
}

processingMessage <- function(df, name) {
  writeLines(paste0('Processing ', nrow(df), ' raw ', name, ' records.'))
  df
}

#' @importFrom readr read_delim
getColumnSpecs <- function(fileName) {

  df <- read_delim(file=fileName, delim=" ", col_names=c("name", "pos", "type"), col_types="ccc")
  p <- "(.+)\\-(.+)"
  start <- gsub(pattern=p, x = df$pos, replacement="\\1")
  end <- gsub(pattern=p, x = df$pos, replacement="\\2")

  df$start <- as.integer(start)
  df$end <- as.integer(end)

  df

}

TypeOfWeaponForceInvolvedTranslationDf <- tibble(
  icpsrCode=c(1,110,111,120,121,130,131,140,141,150,151,200,300,350,400,500,600,650,700,850,900,990),
  AutomaticWeaponIndicator=c("N","N","Y","N","Y","N","Y","N","Y","N","Y","N","N","N","N","N","N","N","N","N","N","N")
)

#' @import stringr
#' @import DBI
#' @import dplyr
#' @import purrr
#' @importFrom readr write_delim
#' @export
writeDataFrameToDatabase <- function(conn, x, tableName, append = TRUE, viaBulk = FALSE, localBulk = TRUE,
                                     writeToDatabase=TRUE, forceConnectionType=NULL) {

  executeSQL <- function(sql) {
    if (!writeToDatabase) {
      writeLines(sql)
    } else {
      tryCatch(
        dbClearResult(dbSendStatement(conn, sql)),
        error = function(e) {
          writeLines(paste0("Attempted SQL: ", sql))
          stop(e)
        })
    }
  }

  formatValue <- function(value) {
    ret <- "NULL"
    if (!is.na(value)) {
      if (is.character(value) | is.factor(value)) {
        ret <- paste0("'", value, "'")
      } else if (class(value) == "Date") {
        ret <- paste0("'", format(value, "%Y-%m-%d"), "'")
      } else {
        ret <- as.character(value)
      }
    }
    ret
  }

  if (!is.null(x)) {

    if (!dbExistsTable(conn, tableName)) {
      dbWriteTable(conn, tableName, x %>% head(0), field.types=getCharFieldTypes(x))
    }

    if (viaBulk) {
      cc <- class(conn)
      if (!is.null(forceConnectionType)) {
        cc <- forceConnectionType
      }
      f <- NULL
      if (Sys.info()['sysname'] == 'Windows') {
        f <- gsub(x=tempfile(tmpdir='C:/dev', pattern=tableName), pattern='\\\\', replacement='/')
      } else {
        f <- tempfile(tmpdir = "/tmp", pattern = tableName)
      }
      if ('MariaDBConnection'==cc) {
        if (nrow(x) > 0) {
          if (!append) {
            executeSQL(paste0("truncate ", tableName))
          }

          write_delim(x=x %>% mutate_if(is.logical, as.integer), file=f, na="\\N", delim="|", col_names=FALSE)
          cn <- colnames(x)
          dateCols <- as.vector(sapply(x, function(col) {inherits(col, "Date")}))
          cne <- cn
          cne[dateCols] <- paste0('@', cne[dateCols])
          setString <- ""
          fieldList <-  paste0("(", paste0(cne, collapse=','), ")")
          if (any(dateCols)) {
            setString <- paste0("set ", paste0(cn[dateCols], "=str_to_date(", cne[dateCols], ", '%Y-%m-%d')", collapse=","))
            fieldList <-  paste0("(", paste0(cne, collapse=','), ")")
          }

          localQualifier <- ''
          if (!localBulk) {
            localQualifier <- ' local '
          }

          sql <- paste0("load data ", localQualifier, " infile '", f, "' into table ", tableName,
                        " fields terminated by \"|\" ", fieldList, " ", setString)
          executeSQL(sql)
          if (writeToDatabase) {
            file.remove(f)
          }
        }
      } else if ('SQLServerConnection'==cc) {
        if (nrow(x) > 0) {
          if (!append) {
            executeSQL(paste0("delete from ", tableName))
          }

          x <- select_(x, .dots=dbListFields(conn, tableName))

          if (any(sapply(x, is.logical))) {
            x <- mutate_if(x, is.logical, "as.integer")
          }

          # To avoid the scientific notation. Ideally, we should use the options(scipen=999)
          # but in readr version 1.0.0, it does not for write_delim -HW.

          x <- mutate_if(x, function(col) is.numeric(col), function(v) ifelse(is.na(v), NA, trimws(format(v, scientific=FALSE))))

          write_delim(x=x, file=f, na="", delim="|", col_names=FALSE)

          sql <- paste0("BULK INSERT ", tableName, " FROM '" , f, "' WITH ( KEEPIDENTITY, FIELDTERMINATOR ='|', ROWTERMINATOR ='\n' ) ")
          executeSQL(sql)
          if (writeToDatabase) {
            file.remove(f)
          }
        }
      } else {
        stop(paste0("Bulk loading on unsupported database: ", cc))
      }
    }
    else {

      x <- as.data.frame(x)
      if (nrow(x) > 0) {
        if (!append) {
          executeSQL(paste0("truncate ", tableName))
        }
        colNames <- colnames(x)
        colCount <- length(colNames)
        for (r in 1:nrow(x)) {
          sql <- paste0("insert into ", tableName, " (", paste0(colNames, collapse=","), ") values (")
          for (c in 1:colCount) {
            sql <- paste0(sql, formatValue(x[r,c]), ifelse(c == colCount, ")", ","))
          }
          executeSQL(sql)
        }
      }
    }
  }

  invisible()

}

getCharFieldTypes <- function(adf) {
  fieldTypes <- NULL
  charCols <- adf %>% select_if(is.character)
  if (ncol(charCols)) {
    fieldTypes <- colnames(charCols) %>% map_chr(function(colName, charCols) {
      maxLength <- 1
      if (any(!is.na(charCols[[colName]]))) {
        maxLength <- max(str_length(na.omit(charCols[[colName]])))
      }
      paste0('varchar(', maxLength, ')')
    }, charCols) %>% set_names(colnames(charCols))
  }
  fieldTypes
}
