<div class="imageQuestion question">
    <#list question.answers?keys as answerid>
        <#if selectedAnswerId?? && answerid == selectedAnswerId>
            <a href="#" class="answer" id="${answerid}" onclick="selectAnswer(this)" class="selected"><img src="images/${question.id}-${answerid}.png" alt="${question.answers[answerid]!}" /></a>
        <#else>
            <a href="#" class="answer" id="${answerid}" onclick="selectAnswer(this)" class="answer"><img src="images/${question.id}-${answerid}.png" alt="${question.answers[answerid]!}" /></a>
        </#if>
    </#list>
</div>

<h1>${question.question}</h1>
<#if question.description??>
<p>
    ${question.description}
</p>
</#if>
