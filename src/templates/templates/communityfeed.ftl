<div id="thefeed">
    <h2>The Feed</h2>

    <#assign threads = feeddata.getThreads(chapter)>
    <#list threads as thread>
        <#assign messages = feeddata.getMessages(chapter, thread.id)>
        <article>
            <div class="question" id="${thread.id}">
                Q: ${thread.message.message!""}
                <div><a class="reply" href="#">Answer</a></div>
            </div>
            <#list messages as msg>
                <div class="answer" id="${msg.id}">
                    A: ${msg.message!""}
                    <a class="readmore" href="#">(continue)</a>
                </div>
            </#list>
        </article>
    </#list>
</div>

