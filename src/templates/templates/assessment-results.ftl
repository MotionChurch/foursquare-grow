<#include "/macros/common.ftl">
<#include "/macros/common-page.ftl">

<@commonpage>
    <@noticebox>
    </@noticebox>

    <@textcontent>
        <p>Congratulations for completing your GROW assessment!</p>

        <p>Based on your responses you have been identified as a ${stage?cap_first}.</p>

        <p>
            So what’s next?  Now you begin the process of GROWing. The button
            below will take you to the ${stage?cap_first} page.
        </p>

        <p>Here you will find everything you need to begin the GROW process and start your journey.</p>

        <p>
            We are genuinely excited for you. Each phase of the GROW process
            will produce positive quantifiable and quality results in your life, as
            you learn, and then apply this learning in your life.
        </p>
    </@textcontent>

    <div id="getstarted">
        <a class="greenbutton" href="${dynamicRoot}/account/training/${stage?lower_case}">Begin GROWing &#x2799;</a>
    </div>
</@commonpage>


