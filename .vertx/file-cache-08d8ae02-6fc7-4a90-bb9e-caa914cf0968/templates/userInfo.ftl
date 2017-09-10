<#include "header.ftl">
<div class="col-md-12 mt-1">
<#list context.userList>
    <h2>${context.title}:</h2>
    <ul>
        <#items as page>
            <li>${page}</li>
        </#items>
    </ul>

</#list>
</div>

<#include "footer.ftl">