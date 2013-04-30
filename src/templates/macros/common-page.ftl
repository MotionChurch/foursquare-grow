<#macro commonpage>
    <!doctype html>
    <html>
    <head>
        <title>Grow Process</title>

        <link rel="stylesheet" href="../style.css" />
        <script src="../scripts/jquery.min.js"></script>
        <script src="..//scripts/growth.js"></script>
    </head>
    <body>
    <div id="notfooter">
        <#include "/templates/nav.ftl">

        <#include "/templates/index-hero.ftl">

        <#nested>

        <div id="push"></div>
    </div>

    <#include "/templates/footer.ftl">

    </body>
    </html>
</#macro>
