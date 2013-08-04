<div class="imageQuestion question">
    <#list question.answers?keys as answerid>
        <#if selectedAnswerId?? && answerid == selectedAnswerId>
            <img class="answer selected" id="${answerid}" onclick="selectAnswer(this)" src="${staticRoot}/images/${question.id}-${answerid}-hover.jpg" />
        <#else>
            <img class="answer" id="${answerid}" onclick="selectAnswer(this)" src="${staticRoot}/images/${question.id}-${answerid}.jpg" />
        </#if>
    </#list>
</div>

<h1>${question.question}</h1>
<#if question.description??>
<p>
    ${question.description}
</p>
</#if>
