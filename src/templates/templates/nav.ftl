<#macro navLink href>
    <li><a
    <#if (currentPage!"") == href>
        class="current"
    </#if>
    href="${href}"><#nested></a></li>
</#macro>

<header>
    <h1>
    <#if user??>
        <a href="${dynamicRoot}/account"><img src="${staticRoot}/images/logo.png"> Grow Process</a>
    <#else>
        <a href="${dynamicRoot}/index.html"><img src="${staticRoot}/images/logo.png"> Grow Process</a>
    </#if>
    </h1>

    <nav class="primary">
        <ul>
            <@navLink href="${dynamicRoot}/index.html">Home</@navLink>
            <@navLink href="${dynamicRoot}/learnmore.html">Learn More</@navLink>
            <@navLink href="${dynamicRoot}/contact.html">Contact</@navLink>
            <#if user??>
                <@navLink href="${dynamicRoot}/account">Keep Growing</@navLink>
                <@navLink href="${dynamicRoot}/account/logout">Logout</@navLink>
            <#else>
                <@navLink href="${dynamicRoot}/login.html">Login / Sign Up</@navLink>
            </#if>
            <a href="http://myfoursquarechurch.com">Foursquare Church</a>
        </ul>
    </nav>
</header>

