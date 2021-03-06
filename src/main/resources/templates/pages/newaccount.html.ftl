<#include "/macros/common.ftl">
<#include "/macros/common-page.ftl">

<@commonpage>
    <#if errorMessage??>
        <@noticebox class="visible">
            ${errorMessage?html}
        </@noticebox>
    <#else>
        <@noticebox>
        </@noticebox>
    </#if>

    <@content>
        <p>
            Fill out the form below to create a new Puyallup Foursquare InFellowship account.
        </p>
        <form action="${dynamicRoot}/newaccount.html" method="post">
        <p><label for="firstnameField">First Name:</label> <input id="firstnameField" type="text" name="firstname" /></p>
        <p><label for="lastnameField">Last Name:</label> <input id="lastnameField" type="text" name="lastname" /></p>
        <p><label for="emailField">Email:</label> <input id="emailField" type="text" name="email" /></p>
        <p><input type="submit" value="Create Account" /></p>
        </form>
    </@content>
</@commonpage>


