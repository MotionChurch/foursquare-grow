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
        <p>Welcome! You will need to login with your Foursquare Church InFellowship login.</p>
        <p>
            If you do not already have an account,
            <a href="https://pfseawa.infellowship.com/UserLogin/New">create one here</a>.
            After you receive your confirmation email, come back to this site to begin the
            assessment.
        </p>
        <form action="${dynamicRoot}/account/authenticate?redirect=${redirect!""}" method="post">
        <p><label for="emailField">Email:</label> <input id="emailField" type="text" name="email" /></p>
        <p><label for="passwordField">Password:</label> <input id="passwordField" type="password" name="password" /></p>
        <p><input type="submit" value="Login" /></p>
        </form>
    </@content>
</@commonpage>

