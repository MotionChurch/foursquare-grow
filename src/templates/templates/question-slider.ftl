<h1>${question.question}</h1>
<#if question.description??>
<p>
    ${question.description}
</p>
</#if>

<p>Slide the slider to answer the question.</p>

<div class="sliderQuestion question">
    <div class="sliderbar"><div class="slider noselect"></div></div>
    <div class="answers">
    <#list question.answers?keys as answerid>
        <div id="${answerid}" class="label">${question.answers[answerid].text}</div>
    </#list>
        <span class="stretch"></span>
    </div>
</div>
