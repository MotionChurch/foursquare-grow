#!/bin/sh

##
## This script clears the strings ColumnFamily and then rebuilds it.
##

export TOOLS=`awk -F= '/jesterpm\.buildtools\.root/ { print $2 }' $HOME/.jesterpm-build-tools.properties`
export DEVFILES=$(dirname $0)

TEMPFILE=`mktemp`

cat > $TEMPFILE << EOF
use GROW;

drop column family strings;

create column family strings
    with key_validation_class = 'UTF8Type'
    and comparator = 'UTF8Type'
    and default_validation_class = 'UTF8Type';
EOF

# Fill with questions
./compile-questions.sh >> $TEMPFILE


# Fill with videos
./compile-videos.sh >> $TEMPFILE

# GO!
cassandra-cli < $TEMPFILE
rm $TEMPFILE
