<header>
    <h1>
    <#if user??>
        <a href="${dynamicRoot}/account"><img src="${staticRoot}/images/logo.png"> Grow Process</a>
    <#else>
        <a href="${dynamicRoot}/index.html"><img src="${staticRoot}/images/logo.png"> Grow Process</a>
    </#if>
    <#if config.getDomain() != "prod">
        <span class="versiontag">${config.getDomain()}</span>
    </#if>
        <a id="foursquarefloat" href="http://myfoursquarechurch.com"><img src="${staticRoot}/images/foursquaresm.png" alt="Foursqaure Church" /></a>
    </h1>

    <#include "/templates/nav.ftl">
</header>
