<h1>${question.question}</h1>
<#if question.description??>
<p>
    ${question.description}
</p>
</#if>

<p>Move the white dot to answer the question.</p>

<div class="quadQuestion question">
    <div class="above">
        <span class="left">${question.topleft}</span>
        <span class="right">${question.topright}</span>
    </div>
    <div class="middle">
        <div class="quad"><img src="${staticRoot}/images/quadselector.png" class="selector" /></div>
    </div>
    <div class="below">
        <span class="left">${question.bottomleft}</span>
        <span class="right">${question.bottomright}</span>
    </div>
</div>

