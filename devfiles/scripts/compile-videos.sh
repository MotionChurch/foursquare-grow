# Dump video data into Cassandra form.

for i in $DEVFILES/videos/*; do
    level=`basename $i`
    for j in $i/*.json; do
        id=`basename $j .json`
        echo "set strings['/training/${level}']['${id}'] = '"
        cat $j|sed "s/'/\\\'/g"
        echo "';"
    done
done

