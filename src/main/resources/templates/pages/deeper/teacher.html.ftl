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

            <h3>Assessments</h3>
            <ul>
                <li>
                    <a href="http://tda.lifeway.com" target="_blank">Transformational Disciple Assessment</a>
                    <p>
                        The Transformational Discipleship Assessment (TDA)
                        is an advanced tool to dive more deeply into
                        opportunities for your customized discipleship
                        journey. This is an excellent tool to help you
                        take your spiritual formation to the next level.
                    </p>
                </li>
            </ul>
            <h3>Reading List</h3>
            <ul>
                <li><a href="http://www.amazon.com/dp/0800699327">How to Think Theologically - Howard W. Stone &amp; James O. Duke</a></li>
                <li><a href="http://www.amazon.com/dp/0061920622">Simply Christian - N.T. Wright</a></li>
            </ul>
            <h3>Other</h3>
            <ul>
                <li><a href="https://motionchrch.com/groups">Join a Community Group</a></li>
            </ul>
        </article>
    </div>
</@commonpage>
