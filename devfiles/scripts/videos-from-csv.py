#!/usr/bin/python

import sys,os,errno
import csv
import string
import urllib
from string import Template

#BASE_URL="http://foursquaregrow.s3-website-us-east-1.amazonaws.com/"
BASE_URL="http://d12xq7pqelpwt.cloudfront.net/"

"""
Create the json file for a particular video. The fields are:
    chapter: The chapter (i.e. Seeker, Believer, etc)
    videoNumber: The number of the video in the chapter, starting with 1
    number: The video number to display to the user. 0 to hide number.
    title: The video title.
    length: The video length in seconds.
    image: The url of the image to display.
    pdf: The url of the outline pdf.
    videos: A dictionary of mime type to video urls
"""
def mkjson(chapter, videoNumber, number, title, length, image, pdf, videos):
    vtemplate = Template("""{
    "id": "$id",
    "number": "$number",
    "title": "$title",
    "length": $length,
    "image": "$image",
    "pdf": "$pdf",
    "urls": [""")

    # NB we seek backwards to trim the comma after the loop
    urltemplate = Template("""{"src":"$src", "type":"$type"},\n""")

    directory = string.lower("videos/" + chapter)
    try:
        os.makedirs(directory)
    except OSError as exc:
        if exc.errno == errno.EEXIST and os.path.isdir(directory):
            pass
        else:
            raise
    filename = string.lower(directory + "/" + chapter + "-" + str(videoNumber) + ".json")
    with open(filename, 'w') as outfile:
        outfile.write(vtemplate.substitute(dict(id=string.lower(chapter+"-"+str(videoNumber)),
            chapter=chapter, number=number, title=title, length=length,
            image=BASE_URL + urllib.quote(image), pdf=BASE_URL + urllib.quote(pdf))))

        for type,src in videos.iteritems():
            outfile.write(urltemplate.substitute(dict(type=type, src=BASE_URL + urllib.quote(src))))

        outfile.seek(-2, 2)
        outfile.write("]\n}")

# This script reads lines from the given csv file and creates json files for
# each video in the videos/ directory.

filename = sys.argv[1];
with open(filename, 'rb') as csvfile:
    videoNumbers = dict()

    reader = csv.reader(csvfile)
    for row in reader:
        chapter = row[0]
        number = row[1]
        title = row[2]
        length = row[3]
        image = row[4]
        pdf = row[5]
        h264 = row[6]
        webm = row[7]

        # Number the videos in the chapter starting with 1
        videoNumber = 1
        if chapter in videoNumbers:
            videoNumber = videoNumbers[chapter]

        # Update the number for the next video
        videoNumbers[chapter] = videoNumber + 1

        videos = { "video/mp4": h264, "video/webm": webm }

        mkjson(chapter, videoNumber, number, title, length, image, pdf, videos)

