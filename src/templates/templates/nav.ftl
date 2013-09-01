<#macro navLink href>
    <li><a
    <#if currentPage!"" == href>
        class="current"
    </#if>
    href="${href}"><#nested></a></li>
</#macro>

<header>
    <h1><img src="${staticRoot}/images/logo.png"> Grow Process</h1>

    <nav class="primary">
        <ul>
            <@navLink href="${dynamicRoot}/index.html">Home</@navLink>
            <@navLink href="${dynamicRoot}/about.html">About</@navLink>
            <@navLink href="${dynamicRoot}/contact.html">Contact</@navLink>
            <#if user??>
                <@navLink href="${dynamicRoot}/account/assessment">Take Assessment</@navLink>
            <#else>
                <@navLink href="${dynamicRoot}/login.html">Login / Sign Up</@navLink>
            </#if>
        </ul>
    </nav>
</header>

