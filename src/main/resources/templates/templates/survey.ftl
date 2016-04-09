<#include "/macros/common.ftl">
<#include "/macros/common-page.ftl">

<@commonpage>
    <@noticebox>
        The Grow Process focuses on the topic that you want to learn
        about.  Our 'Assessment' test will give you the right courses
        fit for your level.
    </@noticebox>

    <div id="progressbar">
        <div class="progress" style="width: ${percentComplete!"0"}%"></div>
    </div>

    <div id="content">
        <form id="questionForm" action="${dynamicRoot}/account/assessment/question/${question.id}" method="post">
        <input id="direction" type="hidden" name="direction" value="next" />
        <input id="answerField" type="hidden" name="answer" value="${selectedAnswerId!}" />
        <div id="previous">
            <#if question.previousQuestion??>
            <a href="#" onclick="previousQuestion();return false;">
                <img src="${staticRoot}/images/previous.png" alt="Previous Question" />
            </a>
            </#if>
        </div>
        <div id="next">
            <a href="#" onclick="nextQuestion();return false;">
                <img src="${staticRoot}/images/next.png" alt="Next Question" />
            </a>
        </div>
        <article>
            <#switch question.type>
                <#case "text">
                    <#include "/templates/question-text.ftl">
                    <#break>
                <#case "image">
                    <#include "/templates/question-image.ftl">
                    <#break>
                <#case "slider">
                    <#include "/templates/question-slider.ftl">
                    <#break>
                <#case "quad">
                    <#include "/templates/question-quad.ftl">
                    <#break>
                <#case "circle">
                    <#include "/templates/question-circle.ftl">
                    <#break>
            </#switch>
        </article>
        </form>
    </div>
</@commonpage>

