# Dump Cassandra commands to setup questions
for i in $DEVFILES/questions/*.json; do
    id=`basename $i .json`
    echo "set strings['/questions/${id}']['value'] = '"
    cat $i
    echo "';"
done


