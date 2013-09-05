# Dump Cassandra commands to setup questions

FIRST=""
COUNT=0

for i in $DEVFILES/questions/*.json; do
    id=`basename $i .json`
    if [ -z "$FIRST" ]; then
        FIRST=$id
    fi
    echo "set strings['/questions/${id}']['value'] = '"
    cat $i|sed "s/'/\\\'/g"
    echo "';"
    COUNT=$((COUNT + 1))
done

# Create Summary
cat << EOF
set strings['/questions']['value'] = '
{
    "first": "$FIRST",
    "count": $COUNT
}
';
EOF

