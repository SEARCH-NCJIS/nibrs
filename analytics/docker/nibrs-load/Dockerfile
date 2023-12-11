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
#

# Docker image definition for containers with an R environment needed to load NIBRS data

# Before building this image, you need to copy ../../../tools/r-packages/nibrs_1.0.x.tar.gz into the files/ directory

# To load local ICPSR data into a local staging db:

# 1. Be sure to run both compose files (nibrs-analytics-compose.yaml and nibrs-local-staging-compose.yaml)
# 2. docker run -it --rm --network docker_nibrs_analytics_nw \
#      --mount "type=bind,source=/opt/data/nibrs/ICPSR/ICPSR_36795,target=/nibrs" searchncjis/nibrs-load \
#      R -e 'dfs <- nibrs::loadICPSRRaw(conn=DBI::dbConnect(RMariaDB::MariaDB(), host="nibrs-staging-db", dbname="search_nibrs_staging"), dataDir="/nibrs", state="OH")'

# To load data from a staging db to dimensional (here we use a local staging db as an example, but the staging db can be anywhere as long as it's accessible by the specified
#   hostname from the container):

# docker run -it --rm --network docker_nibrs_analytics_nw searchncjis/nibrs-load \
#      R -e 'dfs <- nibrs::loadDimensionalFromStagingDatabase(stagingConn=DBI::dbConnect(RMariaDB::MariaDB(), host="nibrs-staging-db", dbname="search_nibrs_staging"),
#        dimensionalConn=DBI::dbConnect(RMariaDB::MariaDB(), host="nibrs-analytics-db", dbname="search_nibrs_dimensional", user="analytics", sampleFraction=.05))'

# (Note that if you want to write the whole db rather than a random sample of records, remove the sampleFraction= parameter)

FROM rocker/tidyverse:4.0.1

LABEL maintainer="SEARCH" \
  org.label-schema.description="Image that loads NIBRS data from source datasets into mariadb"

RUN apt-get update && apt-get install -y libmariadb-dev libmariadbclient-dev && R -e 'install.packages("RMariaDB")'
RUN R -e 'install.packages(c("furrr","sergeant"))'

COPY files/nibrs_1.0.2.tar.gz /tmp/
RUN R -e 'install.packages("/tmp/nibrs_1.0.2.tar.gz", repos = NULL, type="source")'
