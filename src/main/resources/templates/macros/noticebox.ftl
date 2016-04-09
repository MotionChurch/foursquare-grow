<#macro noticebox class="">
    <div id="middlebar">
        <div id="noticebox" class="${class}">
            <p>
                <img class="icon" src="${staticRoot}/images/noticeicon.png">
                <span><#nested></span>
            </p>
        </div>
    </div>
</#macro>
