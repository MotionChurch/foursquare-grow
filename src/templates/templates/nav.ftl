<#macro navLink href>
    <li><a
    <#if currentPage == href>
        class="current"
    </#if>
    href="${href}"><#nested></a></li>
</#macro>

<header>
    <h1><img src="${contentroot}/images/logo.png"> Grow Process</h1>

    <nav class="primary">
        <ul>
            <@navLink href="/index.html">Home</@navLink>
            <@navLink href="/about.html">About</@navLink>
            <@navLink href="/contact.html">Contact</@navLink>
            <#if user??>
                <@navLink href="/account/assessment">Take Assessment</@navLink>
            <#else>
                <@navLink href="/login.html">Login / Sign Up</@navLink>
            </#if>
        </ul>
    </nav>
</header>

