<#include "header.ftl">

<div class="row">

    <div class="col-md-12 mt-1">
        <div class="float-xs-center" >
            <form class="form-inline" action="/register" method="post">
             <h1 class="display-4">${context.title}</h1>
                <div class="form-group">
                    <input type="text" class="form-control" id="username" name="username"  placeholder="username">
                </div>
               <div>&nbsp</div>
                <div  class="form-group">
                    <input type="password" class="form-control" id="password" name="password"   placeholder="password">
                </div>
                <div>&nbsp</div>
                 <div  class="form-group">
                    <input type="password" class="form-control" id="conformPassword" name="conformPassword"   placeholder="conformPassword">
                </div>
				<div>&nbsp</div>
                <div  class="form-group">
                    <button type="submit" class="btn btn-primary">register</button>
                </div>
            </form>

        </div>
    </div>

</div>

<#include "footer.ftl">
