<#include "header.ftl">

<div class="row">

  <div class="col-md-12 mt-1">
   <h1 class="display-4">${context.title}</h1>
    <div>
      <form class="form-inline" action="/login" method="post">
        <div class="form-group">
          <div><input type="text" class="form-control" id="username" name="username" placeholder="username"></div>
         	<div>&nbsp</div>
          <div><input type="password" class="form-control" id="password" name="password" placeholder="password"></div>
          <div>&nbsp</div>
        </div>
        <div>
        	<a href="/registerPage" class="btn btn-primary">register</a>
        	<button type="submit" class="btn btn-primary">Login</button>
        <div>
      </form>
    </div>
   
  </div>
</div>

<#include "footer.ftl">
