<#assign bannerResult = get("bannerData", "riap://component/backend/banner")>
<#if bannerResult.succeeded == true>
    <#if (bannerData.html!"") != "">
<div id="banner">${bannerData.html}</div>
    </#if>
</#if>
