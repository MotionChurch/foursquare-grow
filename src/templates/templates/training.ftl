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
        <#switch chapter>
            <#case "seeker"><#assign overallProgress = 0><#break>
            <#case "believer"><#assign overallProgress = 25><#break>
            <#case "disciple"><#assign overallProgress = 50><#break>
            <#case "teacher"><#assign overallProgress = 75><#break>
        </#switch>
        <div class="progress" style="width: ${overallProgress}%"></div>
    </div>

    <div id="content">
        <nav>
            <#assign chapters = ["seeker", "believer", "disciple", "teacher"]>
            <#list chapters as x>
                <a href="${dynamicRoot}/account/training/${x}" <#if x == chapter>class="current"</#if>>${x?capitalize}</a>
                <#if x_has_next> - </#if>
            </#list>
        </nav>

        <div id="chapterprogress">
            "${chapter?capitalize} Chapter Progress:"
            <div class="progressbar"><div class="progress" style="width: ${chapterProgress}%"></div></div>
            <div class="progresslabel" style="left:${chapterProgress}%">${chapterProgress}%</div>
        </div>

        <div id="videos">
        <#list videos as video>
            <article>
                <div class="image <#if video.completed>completed</#if>" id="${video.id}"><a href="#" onclick="playVideo('${video.id}'); return false"><img src="${video.image!staticRoot+"/images/videoimage.jpg"}" alt="${video.title}" /></a></div>
                <h2>${video.title}</h2>
                <span class="duration"><@hms seconds=video.length /></span>
                <#if (video.pdf!"") != "">
                    <span class="pdf"><a href="${video.pdf}">Outline</a></span>
                </#if>
            </article>
        </#list>
        </div>

        <div id="thefeed">
            <h2>The Feed</h2>
            <article>
                <div class="question">
                    Q: How has God worked in your life...
                    <div><a class="reply" href="#">Answer</a></div>
                </div>
                <div class="answer">
                    A: God has worked in an amazing way in my own life. I constantly
                    <a class="readmore" href="#">(continue)</a>
                </div>
            </article>
            <article>
                <div class="question">
                    Q: How has God worked in your life...
                    <div><a class="reply" href="#">Answer</a></div>
                </div>
                <div class="answer">
                    A: God has worked in an amazing way in my own life. I constantly
                    <a class="readmore" href="#">(continue)</a>
                </div>
            </article>
        </div>
    </div>

    <div id="videoplayer">
        <div class="close"><a href="#" onclick="closeVideo(); return false"><img src="${staticRoot}/images/close.png" alt="Close Video" /></a></div>
        <div class="video">
        <video width="720" height="405" controls="controls">
            This video is not playable in your browser.
            <a href="http://google.com/chrome">Try Chrome?</a>
        </video>
        </div>
    </div>

</@commonpage>


