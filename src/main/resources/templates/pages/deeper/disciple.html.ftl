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
                    <a href="http://tda.lifeway.com" target="_blank">Transformational Disciple Assessment</a>
                    <p>
                        The Transformational Discipleship Assessment (TDA)
                        is an advanced tool to dive more deeply into
                        opportunities for your customized discipleship
                        journey. This is an excellent tool to help you
                        take your spiritual formation to the next level.
                    </p>
                </li>
            </ul>
            <h3>Reading List</h3>
            <ul>
                <li><a href="http://www.amazon.com/dp/1576830276">Spiritual Disciplines for the Spiritual Life - Donald Whitney</a></li>
                <li><a href="http://www.amazon.com/dp/0849944244">Search for Significance ­ Robert McGee</a></li>
                <li><a href="http://www.amazon.com/dp/031033750X">The Purpose Driven Life - Rick Warren</a></li>
                <li><a href="http://www.amazon.com/dp/0446691097">Battlefield of the Mind - Joyce Myer</a></li>
                <li><a href="http://www.amazon.com/dp/0892765054">Bodily Healing and the Atonement - Dr. T.J. McCrossan</a></li>
                <li><a href="http://www.amazon.com/dp/1599792583">Fasting - Jentzen Franklin</a></li>
                <li><a href="http://www.amazon.com/dp/0830768785">The Pursuit of God - A.W. Tozer</a></li>
                <li><a href="http://www.amazon.com/dp/0875084885">What Shall This Man Do - Watchman Nee</a></li>
                <li><a href="http://www.amazon.com/dp/B00ARFNII4">The Walk of the Spirit The Walk of Power - Dave Robertson</a></li>
            </ul>
            <h3>Other</h3>
            <ul>
                <li><a href="http://www.myfoursquarechurch.com/connect/community-groups/">Join a Community Group</a></li>
            </ul>
        </article>
    </div>
</@commonpage>
