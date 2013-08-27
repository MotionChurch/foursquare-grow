#!/bin/sh

##
## This script deletes all Cassandra data and creates and populates the
## ColumnFamilies needed to start the Growth Process.
##

export TOOLS=`awk -F= '/jesterpm\.buildtools\.root/ { print $2 }' $HOME/.jesterpm-build-tools.properties`
export DEVFILES=$(dirname $0)

$TOOLS/scripts/setup-cassandra.sh

# Bootstrap keyspace
TEMPFILE=`mktemp`
cat $DEVFILES/cassandra-bootstrap.cql > $TEMPFILE

# Fill with questions
./compile-questions.sh >> $TEMPFILE

# Fill with videos
./compile-videos.sh >> $TEMPFILE

# GO!
#cat $TEMPFILE | less
cassandra-cli < $TEMPFILE
rm $TEMPFILE
