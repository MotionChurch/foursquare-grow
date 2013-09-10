<#macro hms seconds>
    <#assign h = (seconds / 3600)?int />
    <#assign m = (seconds % 3600 / 60)?int />
    <#assign s = (seconds % 3600 % 60)?int />

    <#if (h < 10)>
        <#assign h = "0${h}" />
    </#if>

    <#if (m < 10)>
        <#assign m = "0${m}" />
    </#if>

    <#if (s < 10)>
        <#assign s = "0${s}" />
    </#if>

    <#if (seconds >= 3600)>
        ${h}:${m}:${s}
    <#elseif (seconds >= 60)>
        ${m}:${s}
    <#else>
        ${s} seconds
    </#if>
</#macro>
