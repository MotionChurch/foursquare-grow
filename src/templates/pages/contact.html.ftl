<#include "/macros/common.ftl">
<#include "/macros/common-page.ftl">

<@commonpage>
    <@noticebox>
    </@noticebox>

    <@content class="text">
        <h1>Contact Us</h1>

        <p>
            If you have any questions about GROW, please complete the following
            form and we will be in touch soon, or call us at 253-848-9111.
        </p>

        <form action="${dynamicRoot}/contactus" method="post">
        <p><label for="firstnameField">First Name:</label> <input id="firstnameField" type="text" name="firstname" /></p>
        <p><label for="lastnameField">Last Name:</label> <input id="lastnameField" type="text" name="lastname" /></p>
        <p><label for="emailField">Email:</label> <input id="emailField" type="text" name="email" /></p>
        <p><label for="phoneField">Phone:</label> <input id="phoneField" type="text" name="phone" /></p>
        <p>
            Foursquare Member:
            <label><input type="radio" name="member" value="yes" id="memberYes"> Yes</label>
            <label><input type="radio" name="member" value="no" id="memberNo"> No</label>
        </p>
        <p>
            Question:<br />
            <textarea name="question" rows="10" cols="80"></textarea>
        </p>
        <p><input type="submit" value="Send" /></p>
        </form>

    </@content>

</@commonpage>


