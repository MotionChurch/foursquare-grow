<#include "/macros/common.ftl">
<#include "/macros/common-page.ftl">

<@commonpage>
    <@noticebox>
    </@noticebox>

    <@content class="text">
        <h1>Welcome</h1>

        <p>
            Welcome to the "Leader" portion of GROW.  This portion of the GROW
            process is intended for those who either:
        </p>

        <ul>
            <li>Want to lead in the church, or,</li>
            <li>Just want to continue to grow personally and be prepared if
                called by God to lead</li>
        </ul>

        <p>
            This section of GROW contains two elements: Teacher and Group Leader
        </p>

        <ol>
            <li>Teacher: If you are considering leading in the church in any
            capacity; on a team in ministry, or as a Community Group leader you must
            complete Teacher</li>

            <li>Group Leader: If you are considering leading a Community Group,
            you must complete BOTH Teacher and Group Leader</li>
        </ol>

        <p>
            We trust you will enjoy these teachings, learn and grow and be better
            equipped to serve God regardless of how or where you serve or lead.
        </p>

    </@content>

    <#if nextstage??>
    <div id="getstarted">
        <a class="greenbutton" href="${dynamicRoot}/account/training/${nextstage}">Continue GROWing &#x2799;</a>
    </div>
    </#if>
</@commonpage>
