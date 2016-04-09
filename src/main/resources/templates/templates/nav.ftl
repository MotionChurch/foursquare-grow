<#macro navLink href>
    <li><a
    <#if (currentPage!"") == href>
        class="current"
    </#if>
    href="${href}"><#nested></a></li>
</#macro>

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
    </ul>
</nav>
