<footer>
    <div class="left">
        <a href="${dynamicRoot}/index.html">Home</a>
        <a href="${dynamicRoot}/learnmore.html">Learn More</a>
        <a href="${dynamicRoot}/contact.html">Contact</a>
    </div>

    <div class="right">
        &copy;2013 <a href="http://motionchrch.com">Foursquare Church</a>
        <#if config.getDomain() != "prod">
        - <#include "/templates/gitversion.ftl" parse=false>
        </#if>
    </div>

</footer>

