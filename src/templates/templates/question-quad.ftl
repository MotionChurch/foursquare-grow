<div class="quadQuestion question">
    <div class="top">${question.top}</div>
    <div class="middle">
        <div class="left">${question.left}</div>
        <div class="quad"><img src="${staticRoot}/images/quadselector.png" class="selector" /></div>
        <div class="right">${question.right}</div>
    </div>
    <div class="bottom">${question.bottom}</div>
</div>

<h1>${question.question}</h1>
<#if question.description??>
<p>
    ${question.description}
</p>
</#if>
