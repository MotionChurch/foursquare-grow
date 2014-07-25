Foursquare Grow Website
=========================

This is the source for the [Foursquare GROW](http://foursquaregrow.com) Website.

Requirements
--------------

* JDK 1.7
* Ant
* Ivy
* jesterpm-build-tools


Usage
-------

1. Download and bootstrap jesterpm-build-tools from http://github.com/jesterpm/jesterpm-build-tools
2. Copy devfiles/grow-server.properties.default to devfiles/grow-server.properties and insert your
   AWS and F1 credentials.
3. Run `ant server` to start the website on http://localhost:8085

The website defaults to running in dev mode which will only modify the dev Dynamo tables. You can
also run `ant server-prod` to cause the local website to access the production site's Dyanmo
tables.
