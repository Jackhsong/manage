<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8" />
<title>心动慈露-后台管理</title>
<link href="${rc.contextPath}/pages/js/jquery-easyui-1.4/themes/default/easyui.css" rel="stylesheet" type="text/css" />
<link href="${rc.contextPath}/pages/js/jquery-easyui-1.4/themes/icon.css" rel="stylesheet" type="text/css" />
<script src="${rc.contextPath}/pages/js/jquery-easyui-1.4/jquery.min.js"></script>
<script src="${rc.contextPath}/pages/js/jquery-easyui-1.4/jquery.easyui.min.js"></script>
<script src="${rc.contextPath}/pages/js/jquery-easyui-1.4/locale/easyui-lang-zh_CN.js"></script>
<style type="text/css">
.searchName{
	padding-right:10px;
	text-align:right;
}
.searchText{
	padding-left:10px;
	text-align:justify;
}

</style>
</head>
<body class="easyui-layout">

	<div data-options="region:'west',title:'menu',split:true" style="width: 180px;">
	<input type="hidden" value="${nodes!0}" id="nowNode"/>
	<#include "../base/menu.ftl" ></div>

	<div data-options="region:'center'" style="padding: 5px;">
		<div id="cc" class="easyui-layout" data-options="fit:true" >
			<div data-options="region:'north',title:'订单管理',split:true" style="height: 240px;">
				<form action="${rc.contextPath}/order/export" id="searchForm" method="post" >
						<table class="search">
							<tr>
								<td class="searchName">付款时间：</td>
								<td>				
									<input value="" id="startTime1" name="startTime1"/>
			 						~
			 						<input value="" id="endTime1" name="endTime1" />
								</td>
																
								<td class="searchName">商品名称：</td>
								<td class="searchText">
									<input id="productName" name="productName" value="" />	
								</td>
							</tr>
							<tr>
								<td class="searchName">订单状态：</td>
								<td class="searchText">
									<select id="orderStatus" name="orderStatus" style="width: 170px;">
										<option value="0">全部</option>
										<option value="1">未付款</option>
										<option value="2">待发货</option>
										<option value="3">已发货</option>
										<option value="4">交易成功</option>
										<option value="5">用户取消</option>
										<option value="6">超时取消</option>
									</select>
								</td>
								<td class="searchName">用户ID：</td>
								<td class="searchText"><input id="accountId" name="accountId" value="${accountId!''}" /></td>	
							</tr>
							<tr>
								<td class="searchName">订单编号：</td>
								<td class="searchText">
									<input id="orderNumber" name="orderNumber" value="" />
								</td>
								<td class="searchName">商品id：</td>
								<td class="searchText">
									<input id="productId" name="productId" value="" />
								</td>

							</tr>
							
							<tr>
								<td class="searchName">收货手机号：</td>
								<td class="searchText"><input id="reveivePhone" name="reveivePhone" value="" /></td>
								<td class="searchName">商家：</td>
								<td class="searchText"><input id="sellerId" name="sellerId" value=""/></td>

							</tr>
							<tr>
								<td class="searchName">收货人姓名：</td>
								<td class="searchText"><input id="receiveName" name="receiveName" value="" /></td>
								<td class="searchName">用户名：</td>
								<td class="searchText"><input id="accountName" name="accountName" value="" /></td>	
							</tr>
							<tr>
								<td class="searchName">物流单号：</td>
								<td class="searchText"><input id="logisticsNumber" name="logisticsNumber" value="" /></td>
								<td class="searchName">分销订单：</td>
								<td class="searchText">
									<input type="radio" name="fx" value="0" checked />全部订单
									<input type="radio" name="fx" value="1" />分销订单
								</td>	
							</tr>
							<tr>
								<input type=hidden id="exportType" name="exportType" value="" />
								<td class="searchName">客服备注：</td>
								<td class="searchText">
									<select id="remark2Type" name="remark2Type" style="width:173px;">
										<option value="-1">全部</option>
										<option value="1">有</option>
										<option value="0">无</option>
									</select>
								</td>
								<td  class="searchName"></td>
								<td class="searchText" style="padding-top: 5px">
									<a id="searchBtn" onclick="searchOrder()" href="javascript:;" class="easyui-linkbutton" data-options="iconCls:'icon-search'">查询</a>&nbsp;						
									<a id="exportResult" onclick="exportResult()" href="javascript:;" class="easyui-linkbutton" data-options="iconCls:'icon-print'">导出结果</a>&nbsp;												
								</td>
							</tr>
						</table>
					</form>
			</div>
			<div data-options="region:'center'" >
				<table id="s_data">

				</table>
				
				<!-- 填写发货所需信息的div begin -->
				<div id="sendOrderDiv" class="easyui-dialog" style="width:350px;height:240px;padding:20px 20px;">
	            	<form id="sendOrderForm" method="post">
	            		<input id="orderId" type="hidden" name="orderId" />
	            		<input type="radio" id="sendType1" name="sendType" value="1" checked /> 有物流
	            		<input type="radio" id="sendType0" name="sendType" value="0"/> 无物流
	            		<div id="haveChannel">
		            		<table cellpadding="5">
		                		<tr>
		                    		<td>物流渠道:</td>
		                    		<td><input id="channel" type="text" name="channel" ></input></td>
		                		</tr>
		                		<tr>
		                    		<td>物流单号:</td>
		                    		<td><input id="number" type="text" name="number" ></input></td>
		                		</tr>
		                		<tr>
		                    		<td>物流运费:</td>
		                    		<td><input id="money" type="text" name="money" ></input></td>
		                		</tr>
		            		</table>
	            		</div>
	        		</form>
	        	</div>
			</div>
		</div>
	</div>

	<script>
		$(function(){
			$(document).keydown(function(e){
				if (!e){
				   e = window.event;  
				}  
				if ((e.keyCode || e.which) == 13) {  
				      $("#searchBtn").click();  
				}
			});
		});	
		
		function searchOrder() {
			$('#s_data').datagrid('load', {
				orderNumber : $("#orderNumber").val(),
				orderStatus : $("#orderStatus").val(),		
				productId : $("#productId").val(),
				productName : $("#productName").val(),			
				sellerId : $("input[name='sellerId']").val(),
				accountName : $("#accountName").val(),
				accountId : $("#accountId").val(),
				receiveName : $("#receiveName").val(),
				reveivePhone : $("#reveivePhone").val(),
				logisticsNumber : $("#logisticsNumber").val(),				
				startTime1 : $("input[name='startTime1']").val(),
				endTime1 :$("input[name='endTime1']").val(),		
				fx : $('input:radio[name=fx]:checked').val(),			
				remark2Type:$("#remark2Type").val(),				
			});
		}
		
		function exportResult(){
			$('#exportType').val("result");
			$('#searchForm').submit();
		}
		
		function sendOrder(index) {
			$("#sendType1").prop("checked", true);
			$("#sendType1").change();
        	$('#channel').combobox('clear');
        	$('#money').val("");
        	$('#number').val("");
			var arr=$("#s_data").datagrid("getData");
			$('#orderId').val(arr.rows[index].id);
			$('#sendOrderDiv').dialog('open');
		}
		
		function editSendOrder(index) {
			$("#sendType1").prop("checked", true);
			$("#sendType1").change();
        	$('#channel').combobox('clear');
        	$('#money').val("");
        	$('#number').val("");
        	$('#orderId').val("");
			var arr=$("#s_data").datagrid("getData");
			var channel = arr.rows[index].ologChannel;
			$('#channel').combobox('setValue', channel);
        	$('#money').val(arr.rows[index].ologMoney);
        	$('#number').val(arr.rows[index].ologNumber);
			$('#orderId').val(arr.rows[index].id);
			$('#sendOrderDiv').dialog('open');
		}
		
		function uploadOrder() {
        	$('#uploadOrderForm').submit();
		}
		
		function toDetail(id){
    		window.open("${rc.contextPath}/order/detail/"+id,"_blank");
		}

		$(function() {
			$('#sendType1').change(function(){
				if($('#sendType1').is(':checked')){
					$('#haveChannel').show();
				}
			});
			$('#sendType0').change(function(){
				if($('#sendType0').is(':checked')){
					$('#haveChannel').hide();
				}
			});
			
			$('#orderFileBox').filebox({
				buttonText: '选择文件',
				buttonAlign: 'right'
			})
	
			$('#channel').combobox({   
			    url:'${rc.contextPath}/order/jsonCompanyCode',   
			    valueField:'code',   
			    textField:'text'  
			});
			
			$('#uploadOrderForm').form({
		    	url:'${rc.contextPath}/order/batchSendOrder',   
				onSubmit: function(){   
					$.messager.progress();
		    	},   
				success:function(data){
		    		$.messager.progress('close');
		    		var data = eval('(' + data + ')');  
		        	if (data.status == 1){
		        		$.messager.alert("提示","only test","info");
		            	window.location.href = "${rc.contextPath}/order/list";
		        	}else{
		        		$.messager.alert("提示",data.msg,"error");
		        	}
		    	}   
			});
			
			$('#sendOrderDiv').dialog({
            title:'物流信息',
            collapsible:true,
            closed:true,
            modal:true,
            buttons:[{
                text:'发货',
                iconCls:'icon-ok',
                handler:function(){
                    $('#sendOrderForm').form('submit',{
                        url:"${rc.contextPath}/order/sendOrder",
                        success:function(data){
                            var res = eval("("+data+")")
                            if(res.status == 1){
                                $('#sendOrderDiv').dialog('close');
                                $.messager.alert('响应信息',"保存成功",'info',function(){
                                    $('#s_data').datagrid('reload');
                                });$('#sendOrderDiv').dialog('close');
                            } else if(res.status == 2){
                                $.messager.alert('响应信息',"该订单已经发货",'info',function(){
                                });$('#sendOrderDiv').dialog('close');
                            }else if(res.status == 3){
                                $.messager.alert('响应信息',res.msg,'info',function(){
                                });$('#sendOrderDiv').dialog('close');
                            } 
                            else{
                                $.messager.alert('响应信息',"发货失败",'error',function(){
                                });
                            }
                        }
                    })
                }
            },{
                text:'取消',
                align:'left',
                iconCls:'icon-cancel',
                handler:function(){
                    $('#sendOrderDiv').dialog('close');
                }
            }]
        })
		
		
			$('#s_data') .datagrid(
			{
				nowrap : false,
				striped : true,
				collapsible : true,
				idField : 'id',
				url : '${rc.contextPath}/order/jsonOrderInfo',
				queryParams: {
					accountId: '${accountId!""}'
				},
				loadMsg : '正在装载数据...',
				fitColumns : true,
				remoteSort : true,
				singleSelect : true,
				pageSize : 10,
				pageList : [ 10, 20 , 30, 40, 50 ],
				columns : [ [
						{field : 'createTime', title : '下单时间', width : 90, align : 'center'},
						{field : 'payTime', title : '付款时间', width : 90, align : 'center'},
						{field : 'sendTime', title : '发货时间', width : 90, align : 'center'},
						{field : 'orderChannel', title : '客户端类型', width : 50, align : 'center'},
                    	{field : 'id', title : '订单ID', width : 30, align : 'center'},
						{field : 'number', title : '订单编号', width : 70, align : 'center',
							formatter : function(value, row, index) {
								var a = '<a target="_blank" href="${rc.contextPath}/order/detail/'+row.id+'">'+row.number+'</a>';
								return a;
							}
						},
						{field : 'oStatusDescripton', title : '订单状态', width : 50, align : 'center'},
						{field : 'totalPrice', title : '订单总价', width : 40, align : 'center'},
						{field : 'realPrice', title : '实付金额', width : 40, align : 'center'},
						{field : 'raFullName', title : '收货人', width : 40, align : 'center'},
						{field : 'raMobileNumber', title : '收货手机', width : 60, align : 'center'},
						{field : 'sSellerName', title : '商家', width : 70, align : 'center'},
						{field : 'sSendAddress', title : '发货地', width : 70, align : 'center'},
						{field : 'ologChannel', title : '物流公司', width : 60, align : 'center'},
						{field : 'ologNumber', title : '运单号', width : 70, align : 'center'},
						{field : 'hidden', title : '操作', width : 80, align : 'center',
							formatter : function(value, row, index) {
								var a = '';
								if(row.status == 2){
									a = '<a href="javaScript:;" onclick="sendOrder(' + index + ')">发货</a>';
								}else if(row.status == 3 && row.ologNumber != ''){
									a = '<a href="javaScript:;" onclick="editSendOrder(' + index + ')">修改发货信息</a>';
								}
								return a;
							}
						} ] ],
				pagination : true,
				rownumbers : true
			});
			
				$(function(){	
						//日期输入框      //开始日期
			  			$('input[name="startTime1"]'). datetimebox({
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
			  			$('input[name="endTime1"]'). datetimebox({
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
		})
	</script>

</body>
</html>