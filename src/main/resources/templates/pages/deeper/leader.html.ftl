<#include "/macros/common.ftl">
<#include "/macros/common-page.ftl">

<@commonpage>
    <#if errorMessage??>
        <@noticebox class="visible">
            ${errorMessage?html}
        </@noticebox>
    <#else>
        <@noticebox>
        </@noticebox>
    </#if>

    <div id="content">
        <article class="text">
            <#include "/templates/deeperheader.ftl">

            <ul>
                <li><a href="https://motionchrch.com/groups">Join a Community Group</a></li>
            </ul>
        </article>
    </div>
</@commonpage>
