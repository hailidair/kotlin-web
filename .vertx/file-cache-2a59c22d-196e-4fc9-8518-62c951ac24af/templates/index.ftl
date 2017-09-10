<#include "header.ftl">

<div class="row">

    <div class="col-md-12 mt-1">
        <div class="float-xs-center" >
          <#if '${context.islogin}' =='true'>  
          <div>${context.username}</div>
          
        	 	
        	 	 <a href="/userInfo?username=${context.username}">user Info</a>
      	  <#else>
         		 <a href="/loginPage">Login</a>
         		 <a href="/registerPage">register</a>
      	  </#if>
       
        </div>
    </div>

</div>

<#include "footer.ftl">
