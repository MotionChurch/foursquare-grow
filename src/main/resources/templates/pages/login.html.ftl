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
            <div><img src="${staticRoot}/images/foursquarechurch.png" alt="Puyallup Foursquare" /></div>
            <p>Login using your Foursquare Community Groups / Online Giving login.</p>
            <div id="login-area">
                <form action="${dynamicRoot}/account/authenticate?redirect=${redirect!""}" method="post">
                    <label>Username</label>
                    <input id="emailField" type="text" name="email" tabindex="1" />
                    <label>Password</label>
                    <input id="passwordField" type="password" name="password" tabindex="2" />
                    <input type="submit" value="Login" class="login-button" />
                </form>
                <a href="https://myfoursquarechurch.ccbchurch.com/w_password.php">Forgot username or password?</a>
            </div>
            <div style="font-weight: bold;">
                <a href="https://myfoursquarechurch.ccbchurch.com/w_sign_up.php">Sign Up</a>
            </div>
        </div>
    </div>
</@commonpage>

