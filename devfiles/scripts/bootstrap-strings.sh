#!/bin/sh

##
## This script clears the strings ColumnFamily and then rebuilds it.
## If given a file name, it will put the commands into the file and not run it.
##

export TOOLS=`awk -F= '/jesterpm\.buildtools\.root/ { print $2 }' $HOME/.jesterpm-build-tools.properties`
export DEVFILES=$(dirname $0)/..

SAVEFILE="$1"
TEMPFILE="$SAVEFILE"
if [ -z "$SAVEFILE" ]; then
    TEMPFILE=`mktemp`
fi

cat > $TEMPFILE << EOF
use GROW;

drop column family strings;

create column family strings
    with key_validation_class = 'UTF8Type'
    and comparator = 'UTF8Type'
    and default_validation_class = 'UTF8Type';
EOF

# Fill with questions
$DEVFILES/scripts/compile-questions.sh >> $TEMPFILE


# Fill with videos
$DEVFILES/scripts/compile-videos.sh >> $TEMPFILE

# GO!
if [ -z "$SAVEFILE" ]; then
    cassandra-cli < $TEMPFILE
    rm $TEMPFILE
fi
