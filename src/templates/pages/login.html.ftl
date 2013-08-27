<#include "/macros/common.ftl">
<#include "/macros/common-page.ftl">

<@commonpage>
    <@noticebox>
        <#if errorMessage??>
            ${errorMessage?html}
        <#else>
            Welcome!
        </#if>
    </@noticebox>

    <@content>
        <p>Welcome! You will need to login with your Foursquare Church InFellowship login.</p>
        <p>If you do not already have an account,
            <a href="${dynamicRoot}/newaccount.html">create one here</a>.</p>
        <form action="${dynamicRoot}/account/authenticate?redirect=${redirect!""}" method="post">
        <p><label for="emailField">Email:</label> <input id="emailField" type="text" name="email" /></p>
        <p><label for="passwordField">Password:</label> <input id="passwordField" type="password" name="password" /></p>
        <p><input type="submit" value="Login" /></p>
        </form>
    </@content>
</@commonpage>

