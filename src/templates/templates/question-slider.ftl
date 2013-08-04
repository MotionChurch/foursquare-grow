<h1>${question.question}</h1>
<#if question.description??>
<p>
    ${question.description}
</p>
</#if>

<div class="sliderQuestion question">
    <div class="sliderbar"><img src="${staticRoot}/images/slider.png" class="slider" /></div>
    <div class="answers">
    <#list question.answers?keys as answerid>
        <div id="${answerid}" class="label">${question.answers[answerid].text}</div>
    </#list>
        <span class="stretch"></span>
    </div>
</div>
