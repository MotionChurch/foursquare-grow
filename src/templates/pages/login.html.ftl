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

    <div id="content">
        <div id="foursquarelogin">
            <div><img src="${staticRoot}/images/foursquarechurchlogin.png" alt="Puyallup Foursquare" /></div>
            <div class="inside">
                <div class="left">
                    <form action="${dynamicRoot}/account/authenticate?redirect=${redirect!""}" method="post">
                        <p>Login using your Foursquare Community Groups / Online Giving login.</p>
                        <p>
                            <div><label for="emailField">Login</label></div>
                            <div><input id="emailField" type="text" name="email" tabindex="1" /></div>
                        </p>
                        <p>
                            <div><label for="passwordField">Password &mdash; <a href="https://pfseawa.infellowship.com/UserLogin/ForgotPassword" target="_blank">forgot?</a></label></div>
                            <div><input id="passwordField" type="password" name="password" tabindex="2" /></div>
                        </p>
                        <p class="submit"><input type="submit" value="Sign in" tabindex="3" /></p>
                        <p id="noaccount">Don't have an account? <a href="https://pfseawa.infellowship.com/UserLogin/New">Sign up!</a></p>
                    </form>
                </div>
                <div class="right">
                    <div><img src="${staticRoot}/images/acts242.png" alt="Acts 2:42" /></div>
                    <div><img src="${staticRoot}/images/leadershipdev.png" alt="Leadership Development" /></div>
                </div>
            </div>
        </div>
    </div>
</@commonpage>

