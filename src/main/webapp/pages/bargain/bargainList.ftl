<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8" />
<title>慈露系统-后台管理</title>
<link href="${rc.contextPath}/pages/js/jquery-easyui-1.4/themes/default/easyui.css" rel="stylesheet" type="text/css" />
<link href="${rc.contextPath}/pages/js/jquery-easyui-1.4/themes/icon.css" rel="stylesheet" type="text/css" />
<script src="${rc.contextPath}/pages/js/jquery-easyui-1.4/jquery.min.js"></script>
<script src="${rc.contextPath}/pages/js/jquery-easyui-1.4/jquery.easyui.min.js"></script>
<script src="${rc.contextPath}/pages/js/jquery-easyui-1.4/locale/easyui-lang-zh_CN.js"></script>


</head>
<body class="easyui-layout">

	<div data-options="region:'west',title:'menu',split:true" style="width: 180px;">
	<#include "../base/menu.ftl" ></div>

	<div data-options="region:'center'" style="padding: 5px;">
		<div id="cc" class="easyui-layout" data-options="fit:true" >  
	    	<div data-options="region:'north',title:'商品管理',split:true" style="height: 160px;">
	    	<form action="" method="post">
				<table class="search" >
				<tr>
					<td class="searchName" >时间：</td>
						<td class="searchText"  >
							<input name="time1" />
					</td>
					<td class="searchText"  >
						至<input name="time2"/>
					</td>
					<td class="searchName">砍价活动ID：</td>
					<td class="searchText"><input name="bargainId" placeholder="请输入数字" /></td>	
					<td class="searchName">砍价类型：</td>
					<td class="searchText">
						<select id="cutType" name="cutType">
							<option value="0">全部</option>
							<option value="1">一元砍</option>
							<option value="2">七刀砍</option>
						</select>
					</td>					
				</tr>
				<tr>
					<td class="searchName">商品ID：</td>
					<td class="searchText"><input name="productId" placeholder="请输入数字" /></td>
					<td></td>
					<td class="searchName">商品名称：</td>
					<td class="searchText"><input name="lname" placeholder="商品名称"/></td>
					<td class="searchName">砍价状态：</td>
					<td class="searchText">
						<select id="cutStatus" name="cutStatus">
							<option value="0">全部</option>
							<option value="1">进行中</option>
							<option value="2">已结束</option>
						</select>
					</td>
				</tr>								
				<tr>
				    <td></td>
					<td class="searchName"> <a  href="${rc.contextPath}/backBargain/toadd" class="easyui-linkbutton">新增商品</a> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </td>
					<td class="searchText"><a  href="javascript:cx_list();" class="easyui-linkbutton" iconCls="icon-search">查询</a> &nbsp; </td>					
				</tr>
			</table>
	    	</form>
	    	<form action="" id="hiddenForm" method="post" name="hiddenForm">
	    	     <input id="updateId" type="hidden" name="updateId" />
	    	     <input id="updateType" type="hidden" name="updateType" />
	    	</form>
	    	</div>  
	    	<div data-options="region:'center'" >
	    		<table id="productlist" style="width: 100%"></table>				
	    	</div>  
		</div>
	</div>
	
    <div id="easyUpdate" class="easyui-dialog" icon="icon-save" align="center" style="padding: 5px; width: 360px; height: 260px;">
		   <form action="${rc.contextPath}/backBargain/saveEasyUpdate" method="post">        
		          最高折扣:<input name="updatehigh" id="updatehigh"  style="width:100px">
		          最低折扣:<input name="updatelow" id="updatelow" style="width:100px"><br/>
		          最低价格:<input name="updatelowPrice" id="updatelowPrice" style="width:100px">
		          砍价次数：<input name="updatetimes" id="updatetimes" style="width:100px"><br/>
		          剩余库存：<input name="updateleftcount" id="updateleftcount" style="width:100px">
		          总库存：<input name="updatetotalcount" id="updatetotalcount"style="width:100px"><br/>
		   <input type="submit" value="保存" class="easyui-linkbutton"  style="width:60px;height: 20px"/>
		   <input id="easyupdateId" type="hidden" name="easyupdateId" />
    	   <input id="easyupdateProductId" type="hidden" name="easyupdateProductId" />  	
		   </form>       
	</div>


	<script type="text/javascript">
	
	    $(function(){  
       		 $("#easyUpdate").dialog("close");
		});	
	
		//网格初始化
		$('#productlist').datagrid(
						{
							url : '${rc.contextPath}/backBargain/list',
							rownumbers : true,
							pagination : true,
							fitColumns : true,   // 自适应宽度 
							pageSize: 10,        //设置默认分页大小
				            pageList:[ 10, 20, 30, 40, 50],   //设置分页大小
							queryParams:{source:0},
							columns : [ [
									{
										title : '商品主图 ',
										field : 'image1',
										width : 100,
										formatter : function(value, row, index) {
											return '<img src='+row.image1+' style="height:100px;width:160px;" />';
										}
									},
									{
										title : '基本信息 ',
										field : '11',
										width : 150,
										formatter : function(value, row, index) {
											var open = ' 商品ID：' + row.productId
													+ ' <br>商品名称：' + row.name
													+ '<br>商品规格：'+ row.netVolume
													+ '<br>开始时间：' + row.startTime
													+ '<br>结束时间：' + row.endTime
													+ '<br>';
											return open;
										}
									},
									{
										title : '详细信息 ',
										field : '22',
										width : 100,
										formatter : function(value, row, index) {
											var open = '品牌 '+ row.brandName
													+ '  <br>商家： '+ row.sellerName
													+ ' <br>发货地：'+ "杭州" 
													+' <br>生产日期：'+row.manufacturerDate
													+ '<br> 保质期：'+ row.durabilityPeriod;
											return open;
										}
									},
									{
										title : '砍价类型',
										field : '33',
										width : 100,align : 'center',
										formatter : function(value, row, index) {
						    					var opstr = "";var opst="";	
						    					if(row.type=="1")  opstr+='一元砍价 ';
						    					if(row.type=="2")  opstr+='七刀砍价 ';
						    					if(row.status=="0")  opst+='已结束';
						    					if(row.status=="1")  opst+='进行中';										
												return opstr+'<br>'+opst;
										}
									},
									{
										title : '折扣信息 ',
										field : '44',
										width : 100,
										formatter : function(value, row, index) {
											var open = '最高折扣 '+ row.highDiscount
													+ ' <br>最低折扣： '+ row.lowDiscount
													+ ' <br>最低价格：'+ row.lowPrice 
													+' <br>砍价次数：'+row.times;
											return open;
										}
									},
									{
										title : '销售信息  ',
										field : '55',
										width : 100,
										formatter : function(value, row, index) {
											var open = '剩余库存：' + row.leftCount
													+ ' <br>总库存： ' + row.stock
													+ ' <br>活动标题：'+row.title;
											return open;
										}
									},
									{
										title : '操作',
										field : "66",
										width : 100,align : 'center',
										formatter : function(value, row , index) {							
											var a ='';
											if(row.status == 0){
												a = '<a href="javaScript:;" onclick="startActivity(' + row.id + ')">开始活动</a><br/>';
											}
											if(row.status == 1 ){
												a = '<a href="javaScript:;" onclick="endActivity(' + row.id + ')">结束活动</a><br/>';
											}
											var opstr = '<a href="javaScript:;" onclick="updateActivity(' + index + ')">修改</a>&nbsp;&nbsp;';
											var opst = "<a href='${rc.contextPath}/backBargain/delete?productId="+row.productId+"' >删除</a><br/>";
											var ops = "<a href='${rc.contextPath}/backBargain/updatedetail?id="+row.id+"' >修改详细信息</a><br/>";											
											return a+opstr+opst+ops;
										}
									} ] ]
						});

		//查询
		function cx_list() {
			$('#productlist').datagrid('load', {
				source:0,
				id : $("input[name='bargainId']").val(),
		        productId : $("input[name='productId']").val(),
				name : $("input[name='lname']").val(),
				startTime : $("input[name='time1']").val(),
				endTime : $("input[name='time2']").val(),
				cutType : $('select[name="cutType"]').val(),
				cutStatus : $('select[name="cutStatus"]').val(),
			});
		}
		
		
		$(function(){					
			//日期输入框      //开始日期
  			$('input[name="time1"]'). datetimebox({
  				formatter: function(date){
  				   var y = date.getFullYear();
  				   var m = date.getMonth() + 1;
  				   var d = date.getDate();
  				   var h = date.getHours();
  				   var min=date.getMinutes();
  				   var s=date.getSeconds();
  				   return y + "-" + m + "-" + d +" "+h +":"+min +":"+s;
  				   
  				}
  			});
  			
  			//结束日期
  			$('input[name="time2"]'). datetimebox({
  				formatter: function(date){
  				   var y = date.getFullYear();
  				   var m = date.getMonth() + 1;
  				   var d = date.getDate();
  				 var h = date.getHours()
  				   var min=date.getMinutes(min)
  				   var s=date.getSeconds()
  				   return y + "-" + m + "-" + d +" "+h +":"+min +":"+s;
  				}
  			});
	})
		
		
	function startActivity(id){
		   var a=confirm("确定开始活动吗？");
		   $("#updateId").val(id);
		   $("#updateType").val(1);
		   if(a){
			   document.hiddenForm.action="${rc.contextPath}/backBargain/updateStatus";
			   document.hiddenForm.submit();
		   }
	}	
	
	
	function endActivity(id){
		   var a=confirm("确定结束活动吗？");
		   $("#updateId").val(id);
		   $("#updateType").val(2);
		   if(a){
			   document.hiddenForm.action="${rc.contextPath}/backBargain/updateStatus";
			   document.hiddenForm.submit();
		   }
	}	
	

	function updateActivity(index){
   	   var arr=$("#productlist").datagrid("getData");   	     	   
	   $("#updatehigh").val(arr.rows[index].highDiscount);
	   $("#updatelow").val(arr.rows[index].lowDiscount);
	   $("#updatelowPrice").val(arr.rows[index].lowPrice);
	   $("#updatetimes").val(arr.rows[index].times);
	   $("#updateleftcount").val(arr.rows[index].leftCount);
	   $("#updatetotalcount").val(arr.rows[index].stock);
	   $("#easyupdateId").val(arr.rows[index].id);
	   $("#easyupdateProductId").val(arr.rows[index].productId);
	   $("#easyUpdate").dialog("open");	
	}	
	
					
	</script>

</body>
</html>