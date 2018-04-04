#!/usr/bin/env python

# The purpose of this script is to update the video metadata for some videos.
#
# Usage:
#    scripts/rename-videos.py <csv-file>
#
# This script will read the csv file given on the command line.
# For each row, it excepts to find:
#   * video id (e.g. seeker-1),
#   * new filename (without extension),
#   * new duration.
# The script will find each video id in the videos/ directory and rename all
# the things.

import os,sys,csv,json
from glob import glob

BASE_URL = "https://d12xq7pqelpwt.cloudfront.net/videos-2018"

def find_video_json(id):
    '''Find the json file for a given video id.'''
    files = glob("videos/*/%s.json" % id)
    if len(files) != 1:
        raise Exception()
    return files[0]

def rename_videos(json_filename, video_filename, duration):
    '''
    Replace the video urls in the video metadata file.

    Parameters:
        json_filename:  The file to update
        video_filename: The basename of the new video (without extension)
        duration:       The duration of the new video
    '''
    with open(json_filename, 'r') as jsonfile:
        data = json.load(jsonfile)
    data['length'] = duration
    for url in data['urls']:
        ext = os.path.splitext(url['src'])[1]
        url['src'] = "%s/%s%s" % (BASE_URL, video_filename, ext)
    with open(json_filename, 'w') as jsonfile:
        json.dump(data, jsonfile, indent=4, separators=(',', ': '))

filename = sys.argv[1]
with open(filename, 'r') as csvfile:
    reader = csv.reader(csvfile)
    for (videoid, video_filename, duration) in reader:
        rename_videos(find_video_json(videoid), video_filename, int(duration))
