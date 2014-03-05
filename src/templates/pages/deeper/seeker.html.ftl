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

            <h3>Reading List</h3>
            <ul>
                <li><a href="http://www.amazon.com/dp/0310339308">Case for Christ - Lee Strobel</a></li>
                <li><a href="http://www.amazon.com/dp/0310339294">Case for Faith - Lee Strobel</a></li>
            </ul>
            <h3>Other</h3>
            <ul>
                <li><a href="http://www.myfoursquarechurch.com/connect/community-groups/">Join a Community Group</a></li>
            </ul>
        </article>
    </div>
</@commonpage>

