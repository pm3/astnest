<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@page import="java.util.ArrayList"%>
<%@page import="com.aston.utils.servlet.meta.ALinkMeta"%>
<%@page import="java.util.Collections"%>
<%
java.util.List<ALinkMeta> l = new ArrayList<ALinkMeta>(); 
l.addAll(ALinkMeta.metaInfo(com.aston.nest.servlet.UserServlet.class));
l.addAll(ALinkMeta.metaInfo(com.aston.nest.servlet.RoomManagerServlet.class));
l.addAll(ALinkMeta.metaInfo(com.aston.nest.servlet.AdminServlet.class));
request.setAttribute("links", l);
Collections.sort(l);
%>
<!doctype html>
<html lang="us">
<head>
	<meta charset="utf-8">
	<title>API Services</title>
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<link href="css/bootstrap.min.css" rel="stylesheet">
	<style type="text/css">
		.response {
			height: 200px;
			background-color: lightyellow;
			overflow: auto;
		}

		.status {
			font-family: Menlo,Monaco,Consolas,"Courier New",monospace;
			font-weight: bold;
			color: green;
		}

		.status.error {
			color: red;
		}
	</style>
	<script src="js/jquery-1.10.2.js"></script>
	<script src="js/bootstrap.min.js"></script>
	<script type="text/javascript">
	$(document).ready(function(){

		window.location.contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
		$("form").submit(function(e){
			e.preventDefault();
			var $form = $(this);
				action = window.location.contextPath + $form.attr("action"),
				formId = "#" + $form.attr("id"),
				data = {};

			var xhr = new XMLHttpRequest();
			xhr.onreadystatechange = function(ev){
				 if (xhr.readyState==4){
					if(xhr.status == "200") {
						$(formId + " .status").removeClass("error");
					} else {
						$(formId + " .status").addClass("error");
					}
					$(formId + " .status").text(xhr.status + " " + xhr.statusText);
					$(formId + " .response").text(xhr.responseText);
				 };
			};
			xhr.open('POST', action, true);
			data = new FormData();
			$form.find("input, select, textarea").each(function(){
				var $input = $(this),
					name = $input.attr("name"),
					type = $input.attr("type");
					if ($input.val() != "") {
						if ($input.is("textarea")) {
							var val = $input.val();
							val = val.split("\n");
							for (var i = 0; i < val.length; i++) {
								if(val[i] != "") {
									data.append(name,val[i]);
								}
							}
						} else if (type == "text") {
							data.append(name, $input.val());	
						} else if (type == "checkbox") {
							data.append(name, $input.is(':checked'));
						} else if (type == "file") {
							var files = $input.prop('files');
							data.append(name, files[0]);	
						} else {
							data.append(name, $input.val());	
						}
					};			
			});
			xhr.send(data);
		});

	});
	</script>
</head>
<body>
	
	<div class="container">
		<h1>API Sevices</h1>

		<ul>
<c:forEach var="link" items="${links}">
			<li><a href="#${link.id}">${link.path}</a></li>
</c:forEach>		
		</ul>

			
<c:forEach var="link" items="${links}">
		<div class="row">
			<hr>
			<form id="${link.id}" action="${link.path}" class="form-horizontal">
				<div class="col-sm-5">
					<h2>${link.path}</h2>
<c:forEach var="p" items="${link.params}">
<c:if test="${p.type eq 'String' || p.type eq 'long' || p.type eq 'Long' || p.type eq 'int' || p.type eq 'Integer'}">
					<div class="form-group">
						<label class="col-sm-4 control-label">${p.name} : ${p.type}</label>
						<div class="col-sm-8">
							<input class="form-control" type="text" name="${p.name}">
						</div>
					</div>
</c:if>
<c:if test="${p.type eq 'Part'}">
					<div class="form-group">		
						<label class="col-sm-4 control-label">${p.name} : ${p.type}</label>
						<div class="col-sm-8">
							<input class="form-control" type="file" name="${p.name}">
						</div>
					</div>		
</c:if>
<c:if test="${p.type eq 'boolean'}">
					<div class="form-group">	
					 	<div class="col-sm-offset-4 col-sm-8">
					      <div class="checkbox">
					        <label>
					          <input name="${p.name}" type="checkbox"> ${p.name} : ${p.type}
					        </label>
					      </div>
					    </div>
					</div>		
</c:if>
<c:if test="${p.type eq 'RoomAccess'}">
					<div class="form-group">
						<label class="col-sm-4 control-label">${p.name} : ${p.type}</label>
						<div class="col-sm-8">
							<select class="form-control" name="${p.name}">
								<option></option>
								<option>read</option>
								<option>edit</option>
								<option>manage</option>
							</select>
						</div>
					</div>
</c:if>
<c:if test="${p.type eq 'String[]'}">
					<div class="form-group">
						<label class="col-sm-4 control-label">${p.name} : ${p.type}</label>
						<div class="col-sm-8">
							<textarea rows="5" style="width: 100%" name="${p.name}"></textarea>
						</div>
					</div>
</c:if>
</c:forEach>
					<div class="form-group">
						<div class="col-sm-8 col-sm-offset-4">		
							<input class="btn btn-primary" type="submit" value="Odoslať">
						</div>
					</div>
				</div>
				<div class="col-sm-7">
					<h3>Response</h3>
					<div>Status: <span class="status"></span></div>
					<pre class="response"></pre>
				</div>
			</form>
		</div>
</c:forEach>		

	</div>
</body>
</html>