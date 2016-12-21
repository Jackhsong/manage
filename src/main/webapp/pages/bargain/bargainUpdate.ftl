<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8" />
<title>砍价系统-后台管理</title>
<link href="${rc.contextPath}/pages/js/jquery-easyui-1.4/themes/default/easyui.css" rel="stylesheet" type="text/css" />
<link href="${rc.contextPath}/pages/js/jquery-easyui-1.4/themes/icon.css" rel="stylesheet" type="text/css" />
<script src="${rc.contextPath}/pages/js/jquery-easyui-1.4/jquery.min.js"></script>
<script src="${rc.contextPath}/pages/js/jquery-easyui-1.4/jquery.easyui.min.js"></script>
<script src="${rc.contextPath}/pages/js/jquery-easyui-1.4/locale/easyui-lang-zh_CN.js"></script>

<link rel="stylesheet" href="${rc.contextPath}/pages/kindeditor-4.1.10/themes/default/default.css" />
<link rel="stylesheet" href="${rc.contextPath}/pages/kindeditor-4.1.10/plugins/code/prettify.css" />
<script charset="utf-8" src="${rc.contextPath}/pages/kindeditor-4.1.10/kindeditor.js"></script>
<script charset="utf-8" src="${rc.contextPath}/pages/kindeditor-4.1.10/lang/zh_CN.js"></script>
<script charset="utf-8" src="${rc.contextPath}/pages/kindeditor-4.1.10/plugins/code/prettify.js"></script>


<style type="text/css">
.searchName {
	padding-right: 10px;
	text-align: right;
}

.searchText {
	padding-left: 10px;
	text-align: justify;
}

.search {
	width: 1100px;
	align: center;
}

a {
	text-decoration: none;
}
</style>
</head>
<body class="easyui-layout">

	<div data-options="region:'west',title:'menu',split:true" style="width: 180px;">
		<#include "../base/menu.ftl" ></div>
	</div>
	<div data-options="region:'center'"style="padding: 5px; max-height: 100%">
		<div>
		<form  method="post" name="form1" id="form1" >
			<table class="search" style=" width: 100%" >
				<tr>
					<td class="searchName">砍价商品编号：</td>
					<td class="searchText">
					<input type="hidden" name="id"  value="${data.id}" />
					<input  value="${data.id}"  disabled="true"/>
					</td>
					<td class="searchName">商品ID：</td><input type="hidden" name="productId"  value="${data.productId}" />
					<td class="searchText"><input value="${data.productId}" disabled="true"/></td>
					<td class="searchName">砍价类型：</td>
					<td class="searchText"><input name="typeName" value="${data.typeName}" disabled="true"/></td>				
				</tr>
				<tr>
					<td class="searchName">最高折扣：</td>
					<td class="searchText"><input name="highDiscount" value="${data.highDiscount }" /></td>
					<td class="searchName">最低折扣：</td>
					<td class="searchText"><input name="lowDiscount" value="${data.lowDiscount }"/></td>
					<td class="searchName">最低价格：</td>
					<td class="searchText"><input name="lowPrice"  value="${data.lowPrice }" /></td>
				</tr>
				<tr>
					<td class="searchName">砍价次数：</td>
					<td class="searchText"><input name="times"  value="${data.times }" /></td>
					<td class="searchName">砍价状态：</td>
					<td class="searchText"><input name="statusName" value="${data.statusName}" disabled="true"/></td>
					<td class="searchName">商品名称：</td>
					<td class="searchText"><input name="name" value="${data.name}" /></td>
				</tr>
				<tr>
					<td class="searchName">开始时间：</td>
					<td class="searchText"><input name="startTime"  value="${data.startTime }" /></td>
					<td class="searchName">结束时间：</td>
					<td class="searchText"><input name="endTime" value="${data.endTime}" /></td>
					<td class="searchName">卖点：</td>
					<td class="searchText"><input name="sellingPoint" value="${data.sellingPoint}" /></td>
				</tr>		
				<tr>
					<td class="searchName">商品描述：</td>
					<td class="searchText"><input name="desc"  value="${data.desc}" /></td>
					<td class="searchName">市场价：</td>
					<td class="searchText"><input name="marketPrice" value="${data.marketPrice }"  /></td>
					<td class="searchName">销售价：</td>
					<td class="searchText"><input name="salesPrice" value="${data.salesPrice }"   /></td>
				</tr>
				<tr>
					<td class="searchName">净含量：</td>
					<td class="searchText"><input name="netVolume"  value="${data.netVolume }" /></td>
					<td class="searchName">产地：</td>
					<td class="searchText"><input name="placeOfOrigin" value="${data.placeOfOrigin }"  /></td>
					<td class="searchName">成分：</td>
					<td class="searchText"><input name="storageMethod" value="${data.storageMethod }"   /></td>
				</tr>
				<tr>
					<td class="searchName">适用人群：</td>
					<td class="searchText"><input name="peopleFor" value="${data.peopleFor }"  /></td>
					<td class="searchName">食用方法：</td>
					<td class="searchText"><input name="foodMethod" value="${data.foodMethod }"   /></td>
					<td class="searchName">使用方法：</td>
					<td class="searchText"><input name="useMethod"  value="${data.useMethod }" /></td>
				</tr>		
				<tr>
					<td class="searchName">保质期：</td>
					<td class="searchText"><input name="durabilityPeriod" value="${data.durabilityPeriod }"  /></td>
					<td class="searchName">温馨提示：</td>
					<td class="searchText"><input name="tip" value="${data.tip }"/></td>
					<td class="searchName">备注：</td>
					<td class="searchText"><input name="remark"  value="${data.remark }" /></td>
				</tr>

				<tr>
					<td class="searchName">活动描述：</td>
					<td class="searchText"><input style="width:200px;" name="description" value="${data.description }" /></td>					
				</tr>
				<tr>
					<td class="searchName">活动标题：</td>
					<td class="searchText"><input name="title"  value="${data.title }" style="width:200px;"/></td>
					<td class="searchName">剩余库存：</td>
					<td class="searchText"><input name="leftCount" value="${data.leftCount }"/></td>
					<td class="searchName">总库存：</td>
					<td class="searchText"><input name="stock"  value="${data.stock }" /></td>
				</tr>

				<tr>				
					<td class="searchName">商品图片1：</td>
					<td class="searchName">
					<input type="text" id="image1" name="image1" style="width:200px;" value="${data.image1 }"/>
					<a onclick="picDialogOpen(1)" href="javascript:;" class="easyui-linkbutton">更改图片</a>
					</td>
					<td class="searchName">商品图片2：</td>
					<td class="searchName">
					<input type="text" id="image2" name="image2" style="width:200px;" value="${data.image2 }"/>
					<a onclick="picDialogOpen(2)" href="javascript:;" class="easyui-linkbutton">更改图片</a>
					</td>
				</tr>	
				<tr>
					<td class="searchName">商品图片3：</td>
					<td class="searchName">
					<input type="text" id="image3" name="image3" style="width:200px;" value="${data.image3 }"/>
					<a onclick="picDialogOpen(3)" href="javascript:;" class="easyui-linkbutton">更改图片</a>
					</td>	
					<td class="searchName">商品图片4：</td>
					<td class="searchName">
					<input type="text" id="image4" name="image4" style="width:200px;" value="${data.image4 }"/>
					<a onclick="picDialogOpen(4)" href="javascript:;" class="easyui-linkbutton">更改图片</a>
					</td>
				</tr>
				<tr>
					<td class="searchName">商品图片5：</td>
					<td class="searchName">
					<input type="text" id="image5" name="image5" style="width:200px;" value="${data.image5 }"/>
					<a onclick="picDialogOpen(5)" href="javascript:;" class="easyui-linkbutton">更改图片</a>
					</td>	
					<td class="searchName">砍价详情图：</td>
					<td class="searchName">
					<input type="text" id="pcDetail" name="pcDetail" style="width:200px;" value="${data.pcDetail}"/>
					<a onclick="allpicDialogOpen(6)" href="javascript:;" class="easyui-linkbutton">更改图片</a>
					</td>
				</tr>
				
				<tr>
					<td class="searchName" colspan="3"  style="height: 80px"  align="center">
					<input type="button" class="btn btn-primary"  id="saveProduct"  style="margin-left:15px;margin-bottom:30px;" value="保存修改">
					</td>
				</tr>
			</table>
			<input type="hidden" name="updateorsave" id="updateorsave" value="2">
		</form>
		</div>
	</div>
	
		<div id="picDia" class="easyui-dialog" icon="icon-save" align="center" style="padding: 5px; width: 300px; height: 150px;">
	    <form id="picForm" method="post" enctype="multipart/form-data">
			<input type="hidden" name="needWidth" id="needWidth" value="0">
	        <input type="hidden" name="needHeight" id="needHeight" value="0">
	        <input type="hidden" name="imageNumber" id="imageNumber" value="0">
	        <input id="picFile" type="file" name="picFile" />&nbsp;&nbsp;<br/><br/>
	        <a href="javascript:;" onclick="picUpload()" class="easyui-linkbutton" iconCls='icon-reload'>提交图片</a>
	    </form>
		</div>
	
		
		<div id="allPic" class="easyui-dialog" icon="icon-save" align="center" style="padding: 5px; width: 800px; height: 450px;">
			<form name="allImageForm" method="post" action="" id="allImageForm">
				<textarea name="content1" id="content1" cols="100" rows="8" style="width:800px;height:350px;visibility:hidden;"></textarea>
				<br/>
				<input type="button" name="button"  id="button"   value="提交图片" />
			</form>
		</div>
	
	
<script>
				
		$(function(){						
				//日期输入框
	  			//开始日期
	  			$('input[name="startTime"]'). datetimebox({
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
	  			$('input[name="endTime"]'). datetimebox({
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
		
	$("#button").click(function(){
	     var imageUrl=$("#content1").val().replace(/\"/g,"'");
	     $("#allPic").dialog("close");	     
	     $("#pcDetail").val(imageUrl);
	});
					
	//图片上传
    function picDialogOpen(inputId) {
        $("#needWidth").val("0");
        $("#needHeight").val("0");
		$("#imageNumber").val(inputId);
        $("#picDia").dialog("open");
    }
    
    function allpicDialogOpen(inputId) {
        $("#allPic").dialog("open");
    }
    
    $(function(){  
        $("#picDia").dialog("close");
        $("#allPic").dialog("close");
	});
    
    function picUpload() {
        $('#picForm').form('submit',{
            url:"${rc.contextPath}/pic/fileUpLoad",
            success:function(data){
                var res = eval("("+data+")")
                if(res.status == 1){
                    $.messager.alert('响应信息',"上传成功...",'info',function(){
                        var imageNumber=$("#imageNumber").val();
                        $("#picDia").dialog("close");
                        $("#image"+imageNumber).val(res.url);
                        return;
                    });
                } else{
                    $.messager.alert('响应信息',res.msg,'error',function(){
                        return ;
                    });
                }
            }
        })
    }
		
		
	
	 $("#saveProduct").click(function(){
			var name = $("#name").val();
			if(name== ""){
				$.messager.alert('提示','请填写商品名称','warning'); 
			}
			if(name!=""){
			      document.form1.action="${rc.contextPath}/backBargain/saveupdate" ;
				  document.form1.submit();
			}
    	});
		
		
	   KindEditor.ready(function(K) {
			var editor1 = K.create('textarea[name="content1"]', {
				cssPath : '${rc.contextPath}/pages/kindeditor-4.1.10/plugins/code/prettify.css',
				uploadJson : '${rc.contextPath}/pages/kindeditor-4.1.10/jsp/upload_json.jsp',
				fileManagerJson : '${rc.contextPath}/pages/kindeditor-4.1.10/jsp/file_manager_json.jsp',
				allowFileManager : true,
				afterCreate : function() {
					var self = this;
					K.ctrl(document, 13, function() {
						self.sync();
						document.forms['allImageForm'].submit();
					});
					K.ctrl(self.edit.doc, 13, function() {
						self.sync();
						document.forms['allImageForm'].submit();
					});
				},
				afterBlur: function(){this.sync();}
			});
			prettyPrint();
		});
		
		
		
	</script>			
</body>
</html>