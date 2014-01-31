<#include "/macros/common.ftl">
<#include "/macros/common-page.ftl">
<#include "/macros/hms.ftl">

<@commonpage>
    <@noticebox>
        The Grow Process focuses on the topic that you want to learn
        about.  Our 'Assessment' test will give you the right courses
        fit for your level.
    </@noticebox>

    <div id="progressbar">
        <div class="progress" style="width: ${overallProgress}%"></div>
    </div>

    <div id="content">
        <nav>
            <#list chapters as x>
                <#if isChapterAllowed[x]>
                    <a href="${dynamicRoot}/account/training/${x}" <#if x == chapter>class="current"</#if>>${x?capitalize}</a>
                <#else>
                    <span class="disabled">${x?capitalize}</span>
                </#if>

                <#if x_has_next> - </#if>
            </#list>
        </nav>

        <div id="chapterprogress">
            "${chapter?capitalize} Chapter Progress:"
            <div class="progressbar"><div class="progress" style="width: ${chapterProgress}%"></div></div>
            <div class="progresslabel" style="left:${chapterProgress}%">${chapterProgress}%</div>
        </div>

        <div id="videos">
        <#assign allowed = true>
        <#list videos as video>
            <article>
            <div class="image <#if video.completed>completed</#if> <#if allowed>allowed</#if>" id="${video.id}"><a href="#" onclick="playVideo('${video.id}'); return false"><img src="${video.image!staticRoot+"/images/videoimage.jpg"}" alt="${video.title}" /></a></div>
                <h2><#if video.number != "0">${video.number}. </#if>${video.title}</h2>
                <span class="duration"><@hms seconds=video.length /></span>
                <#if (video.pdf!"") != "">
                    <span class="pdf"><a href="${video.pdf}" target="_blank">Outline</a></span>
                </#if>
                <#if !allowUserToSkip && allowed && !video.completed>
                    <#assign allowed = false>
                </#if>
            </article>
        </#list>
        </div>
    </div>

    <div id="videoplayer">
        <div class="close"><a href="#" onclick="closeVideo(); return false"><img src="${staticRoot}/images/close.png" alt="Close Video" title="Close" /></a></div>
        <div class="video">
        <video width="720" height="405" controls="controls">
            This video is not playable in your browser.
            <a href="http://google.com/chrome">Try Chrome?</a>
        </video>
        </div>
    </div>

</@commonpage>


