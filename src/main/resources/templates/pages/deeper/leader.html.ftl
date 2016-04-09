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
                <li><a href="http://www.myfoursquarechurch.com/connect/community-groups/">Join a Community Group</a></li>
            </ul>
        </article>
    </div>
</@commonpage>
