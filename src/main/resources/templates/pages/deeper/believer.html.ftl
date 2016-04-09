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
        <article class="text">
            <#include "/templates/deeperheader.ftl">

            <h3>Assessments</h3>
            <ul>
                <li>
                    <a href="http://www.churchgrowth.org/cgi-cg/gifts.cgi?intro=1" target="_blank">Spiritual Gifts Assessment</a>
                    <p>
                        This is a spiritual gifts assessment.  We all have
                        gifts from God to be understood and utilized and used
                        for Him. This link is a tool to help you identify your
                        spiritual gifts so that you can better understand your
                        fit in the body of Christ.
                    </p>
                </li>
            </ul>
            <h3>Reading List</h3>
            <ul>
                <li><a href="http://www.amazon.com/dp/0060652926">Mere Christianity - C.S. Lewis</a></li>
                <li><a href="http://www.amazon.com/dp/0310339308">Case for Christ - Lee Strobel</a></li>
                <li><a href="http://www.amazon.com/dp/0310339294">Case for Faith - Lee Strobel</a></li>
                <li><a href="http://www.amazon.com/dp/0849944244">Search for Significance ­ Robert McGee</a></li>
                <li><a href="http://www.amazon.com/dp/031033750X">The Purpose Driven Life - Rick Warren</a></li>
                <li><a href="http://www.amazon.com/dp/0446691097">Battlefield of the Mind - Joyce Myer</a></li>
                <li><a href="http://www.amazon.com/dp/1576830276">Spiritual Disciplines for the Spiritual Life - Donald Whitney</a></li>
                <li><a href="http://www.amazon.com/dp/0892765054">Bodily Healing and the Atonement - Dr. T.J. McCrossan</a></li>
                <li><a href="http://www.amazon.com/dp/1577700163">The Bible in Light of our Redemption - E.W. Kenyon</a></li>
                <li><a href="http://www.amazon.com/dp/0310246954">The Life You Always Wanted - John Ortberg</a></li>
                <li><a href="http://www.amazon.com/dp/0785261265">Good Morning Holy Spirit - Benny Hinn</a></li>
            </ul>
            <h3>Other</h3>
            <ul>
                <li><a href="http://www.myfoursquarechurch.com/connect/community-groups/">Join a Community Group</a></li>
            </ul>
        </article>
    </div>
</@commonpage>
