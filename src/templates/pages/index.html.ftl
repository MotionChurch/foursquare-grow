<#include "/macros/common.ftl">
<!doctype html>
<html>
<head>
    <title>Grow Process</title>

    <link href="http://fonts.googleapis.com/css?family=Arvo:400,700" rel="stylesheet">
    <link rel="stylesheet" href="${staticRoot}/style.css" />
    <script src="${staticRoot}/scripts/jquery.min.js"></script>
    <script src="${staticRoot}/scripts/jquery-ui.js"></script>
    <script src="${staticRoot}/scripts/growth.js"></script>
</head>
<body>
<div id="notfooter">
    <#include "/templates/banner.ftl">
    <header>
        <h1>
            <a href="http://myfoursquarechurch.com"><img src="${staticRoot}/images/foursquarelg.png"></a>
        </h1>

        <#include "/templates/nav.ftl">
    </header>

    <#include "/templates/index-hero.ftl">

    <@content>
        <h1>Welcome to GROW</h1>
        <p>
            GROW is an on-line web based spiritual formation
            process created by Foursquare Puyallup church with you in mind, to
            assist you in your journey to discover God, and to be an effective
            follower of Jesus Christ.
        </p>

    </@content>

    <div id="getstarted">
        <a class="greenbutton" href="learnmore.html">Learn More! &#x2799;</a>
    </div>

    <div id="push"></div>
</div>

<#include "/templates/footer.ftl">

</body>
</html>
