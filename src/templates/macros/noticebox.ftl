<#macro noticebox class="">
    <div id="middlebar">
        <div id="noticebox" class="${class}">
            <img class="icon" src="${staticRoot}/images/noticeicon.png">
            <p>
                <#nested>
            </p>
        </div>
    </div>
</#macro>
