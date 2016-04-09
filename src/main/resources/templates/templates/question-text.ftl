<h1>${question.question}</h1>
<#if question.description??>
<p>
    ${question.description}
</p>
</#if>

<div class="textQuestion question">
    <#list question.answers?keys as answerid>
        <#if selectedAnswerId?? && answerid == selectedAnswerId>
            <div id="${answerid}" onclick="selectAnswer(this)" class="answer selected">${question.answers[answerid].text}</div>
        <#else>
            <div id="${answerid}" onclick="selectAnswer(this)" class="answer">${question.answers[answerid].text}</div>
        </#if>
    </#list>
</div>

