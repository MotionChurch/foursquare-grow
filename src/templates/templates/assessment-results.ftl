<#include "/macros/common.ftl">
<#include "/macros/common-page.ftl">

<@commonpage>
    <@noticebox>
    </@noticebox>

    <@content class="text">
        <h1>Congratulations!</h1>

        <p>Congratulations for completing your GROW assessment!</p>

        <p>Based on your responses you have been identified as a ${stage?cap_first}.</p>

        <p>
            So what's next?  Now you begin the process of GROWing. The button
            below will take you to the ${stage?cap_first} page.
        </p>

        <p>Here you will find everything you need to begin the GROW process and start your journey.</p>

        <p>
            We are genuinely excited for you. Each phase of the GROW process
            will produce positive quantifiable and quality results in your life, as
            you learn, and then apply this learning in your life.
        </p>

        <h2>GROW Classes</h2>

        <p>
            We want to launch GROW with great momentum, and personally provide
            you with great momentum.  As a part of the launch of GROW we will
            be having GROW classes starting in March.  Please take a moment and
            complete the short form below, and help us plan and prepare to give
            you the best possible experience.
        </p>

        <form id="classform" action="http://www.myfoursquarechurch.com/grow-classes/#response" method="post">
            <input type="hidden" name="formid" value="136">

            <div>
                <label for="lastname">Last Name</label>:
                <input type="text" name="r[1]" id="lastname" value="">
            </div>

            <div>
                <label for="firstname">First Name</label>:
                <input type="text" name="r[2]" id="firstname" value="">
            </div>

            <div>
                <label for="phone">Phone</label>:
            <input type="text" name="r[3]" id="phone" value="">
            </div>

            <div>
                <label for="email">Email</label>:
                <input type="text" id="email" name="r[11]" value="">
            </div>

            <div>
                <p>
                    Will you be attending the GROW classes beginning the week of March 3rd?
                </p>

                <div class="grouping">
                    <div class="groupingitem">
                        <label class="check"><input type="radio" name="r[6]" value="Yes">Yes</label>
                    </div>

                    <div class="groupingitem">
                        <label class="check"><input type="radio" name="r[6]" value="No">No</label>
                    </div>
                </div>
            </div>

            <h3>GROW Class Schedule</h3>

            <p>
                If you selected YES to attend the GROW classes in March,
                please select the class you are most likely to attend:
            </p>

            <div>
                <label class="check"><input type="checkbox" name="r[10]" value="Tuesday Evening - 7pm">Tuesday Evening - 7pm</label>
            </div>

            <div>
                <label class="check"><input type="checkbox" name="r[10]" value="Saturday Morning - 9am">Saturday Morning - 9am</label>
            </div>

        </form>
    </@content>

    <div id="getstarted">
        <a class="greenbutton" onclick="return submitClassForm()" href="${dynamicRoot}/account/training/introduction">Begin GROWing &#x2799;</a>
    </div>
</@commonpage>


