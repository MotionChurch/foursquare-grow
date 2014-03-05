<#include "/macros/common.ftl">
<#include "/macros/common-page.ftl">

<@commonpage>
    <@noticebox>
    </@noticebox>

    <@content class="text">
        <h1>Congratulations!</h1>

        <p>Congratulations for completing your GROW assessment!</p>

        <p>Based on your responses you have been identified as a <strong>${stage?cap_first}.</strong></p>

        <p>
            So what's next?  Now you begin the process of GROWing. The button
            below will take you to the ${stage?cap_first} page.
        </p>

        <p>Here you will find everything you need to begin the GROW process and start your journey.</p>

        <p>
            We are genuinely excited for you. Each phase of the GROW process
            will produce positive quantifiable and quality results in your life, as
            you learn, and then apply this learning in your life.
        </p>
    </@content>

    <div id="getstarted">
        <a class="greenbutton" onclick="return submitClassForm()" href="${dynamicRoot}/account/training/introduction">Begin GROWing &#x2799;</a>
    </div>
</@commonpage>


