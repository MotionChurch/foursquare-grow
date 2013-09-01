<#macro hms seconds>
    <#assign h = (seconds / 3600)?int />
    <#assign m = (seconds % 3600 / 60)?int />
    <#assign s = (seconds % 3600 % 60)?int />

    <#if (seconds >= 3600)>
        ${h}:${m}:${s}
    <#elseif (seconds >= 60)>
        ${m}:${s}
    <#else>
        ${s} seconds
    </#if>
</#macro>
