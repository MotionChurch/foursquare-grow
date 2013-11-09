# Dump video data into Cassandra form.

for i in $DEVFILES/videos/*; do
    level=`basename $i`
    if [ "$level" != "playlist.json" ]; then
        for j in $i/*.json; do
            id=`basename $j .json`
            echo "set strings['/training/${level}']['${id}'] = '"
            cat $j|sed "s/'/\\\'/g"
            echo "';"
        done
    fi
done

# Default Playlist
echo "set strings['defaultPlaylist']['value'] = '"
cat $DEVFILES/videos/playlist.json
echo "';"
