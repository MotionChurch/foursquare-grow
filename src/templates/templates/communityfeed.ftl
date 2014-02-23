<div id="thefeed">
    <h2>The Feed</h2>

    <#assign threads = feeddata.getThreads(chapter)>
    <#list threads as thread>
        <#assign messages = feeddata.getMessages(chapter, thread.id)>
        <article>
            <div class="question" id="${thread.id}">
                Q: ${thread.message.message!""}
                <div><a class="reply" href="#" onclick="answerQuestion('${thread.id}'); return false;">Answer</a></div>
            </div>
            <div class="answer hidden" id="answer-${thread.id}">
                <form action="${dynamicRoot}/account/feed/${chapter}/${thread.id}" method="post">
                    <textarea name="question" rows="5" defaultValue="Write your reply."></textarea>
                    <div><a class="send" href="#" onclick="$(this).closest('form').submit(); return false;">Send</a></div>
                </form>
            </div>
            <#list messages as msg>
                <div class="answer slider" id="${msg.id}">
                    A: ${msg.message!""}
                    <#if msg_has_next && msg_index == 0>
                        <div><a class="readmore" href="#" onclick="showAnswers(this); return false;">(show more)</a></div>
                    </#if>
                </div>
            </#list>
        </article>
    </#list>
    <article>
        <div class="question">
            <form action="${dynamicRoot}/account/feed/${chapter}" name="newquestion" method="post">
                <textarea name="question" rows="5" defaultValue="Ask your own question."></textarea>
                <div><a class="send" href="#" onclick="$(this).closest('form').submit(); return false;">Send</a></div>
            </form>
        </div>
    </article>
</div>

