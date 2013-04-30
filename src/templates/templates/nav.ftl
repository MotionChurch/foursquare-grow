<#macro navLink href>
    <li><a
    <#if currentPage == href>
        class="current"
    </#if>
    href="${href}"><#nested></a></li>
</#macro>

<header>
    <h1><img src="../images/logo.png"> Grow Process</h1>

    <nav class="primary">
        <ul>
            <@navLink href="index.html">Home</@navLink>
            <@navLink href="about.html">About</@navLink>
            <@navLink href="contact.html">Contact</@navLink>
            <@navLink href="login.html">Login / Sign Up</@navLink>
        </ul>
    </nav>
</header>

