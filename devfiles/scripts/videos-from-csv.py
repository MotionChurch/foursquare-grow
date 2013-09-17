#!/usr/bin/python

import sys,os,errno
import csv
import string
import urllib
from string import Template

BASE_URL="http://foursquaregrow.s3-website-us-east-1.amazonaws.com/"

def mkjson(chapter, number, title, length, image, pdf, videos):
    vtemplate = Template("""{
    "id": "$id",
    "number": "$number",
    "title": "$title",
    "length": $length,
    "image": "$image",
    "pdf": "$pdf",
    "urls": [""")

    urltemplate = Template("""{"src":"$src", "type":"$type"},""")

    directory = string.lower("videos/" + chapter)
    try:
        os.makedirs(directory)
    except OSError as exc:
        if exc.errno == errno.EEXIST and os.path.isdir(directory):
            pass
        else:
            raise
    filename = string.lower(directory + "/" + chapter + "-" + number + ".json")
    with open(filename, 'w') as outfile:
        outfile.write(vtemplate.substitute(dict(id=string.lower(chapter+"-"+number),
            chapter=chapter, number=number, title=title, length=length,
            image=BASE_URL + urllib.quote(image), pdf=BASE_URL + urllib.quote(pdf))))

        for type,src in videos.iteritems():
            outfile.write(urltemplate.substitute(dict(type=type, src=BASE_URL + urllib.quote(src))))

        outfile.seek(-1, 2)
        outfile.write("]\n}")

# This script reads lines from the given csv file and creates json files for
# each video in the videos/ directory.

filename = sys.argv[1];
with open(filename, 'rb') as csvfile:
    reader = csv.reader(csvfile)
    for row in reader:
        chapter = row[0]
        number = row[1]
        title = row[2]
        length = row[3]
        image = row[4]
        pdf = row[5]
        h264 = row[6]

        videos = { "video/mp4": h264 }

        mkjson(chapter, number, title, length, image, pdf, videos)

