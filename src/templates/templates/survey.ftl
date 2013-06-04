<#include "/macros/common.ftl">
<#include "/macros/common-page.ftl">
<#include "/macros/surveycontent.ftl">

<@commonpage>
    <@noticebox>
        The Grow Process focuses on the topic that you want to learn
        about.  Our 'Assessment' test will give you the right courses
        fit for your level.
    </@noticebox>

    <div id="progressbar">
        <div id="progress"></div>
    </div>

    <div id="content">
        <form id="questionForm" action="/account/assessment/question/${question.id}" method="post">
        <input id="answerField" type="hidden" name="answer" value="${selectedAnswerId!}" />
        <div id="previous">
            <a href="#" onclick="previousQuestion();return false;">
                <img src="${contentroot}/images/previous.png" alt="Previous Question" />
            </a>
        </div>
        <div id="next">
            <a href="#" onclick="nextQuestion();return false;">
                <img src="${contentroot}/images/next.png" alt="Next Question" />
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
            </#switch>
        </article>
        </form>
    </div>
</@commonpage>

