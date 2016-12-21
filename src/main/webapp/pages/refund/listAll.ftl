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


</head>
<body class="easyui-layout">

	<div data-options="region:'west',title:'menu',split:true" style="width: 180px;">
	<input type="hidden" value="${nodes!0}" id="nowNode"/>
	<#include "../base/menu.ftl" ></div>

	<div data-options="region:'center'" style="padding: 5px;">
		<div id="cc" class="easyui-layout" data-options="fit:true" >  
	    	<div data-options="region:'north',title:'退款管理',split:true" style="height: 160px;">
	    	<form action="${rc.contextPath}/refund/exportResult" method="post">
	    		<table>
	    					<tr>
	    				<td>
	    				订单编号 <input class="easyui-numberbox" name="number" data-options="prompt:'只能输入数字'" style="width:160px">&nbsp;&nbsp;&nbsp;&nbsp;
	    				</td>
	    				<td>
	    				退款状态 <select name="status" id="status" style="width:80px">
	    					<option value="0">全部</option>
        					<option value="1">申请中</option>
        					<option value="3">待退款</option>
        					<option value="4">退款成功</option>
        					<option value="5">退款关闭</option>
        					<option value="6">退款取消</option>
	    				</select>
	    				</td>
	    				<td>
		    				打款状态 
		    				<select name="moneyStatus" id="moneyStatus" style="width:80px;">
		    					<option value="0">全部</option>
	        					<option value="1">已打款</option>
	        					<option value="2">未打款</option>
		    				</select>&nbsp;&nbsp;&nbsp;&nbsp;
	    				</td>
	    				<td>
	    				分摊状态 
	    				<select name="refundProportionStatus" id="refundProportionStatus" style="width:80px;">
	    					<option value="-1">全部</option>
        					<option value="1">已分摊</option>
        					<option value="0">未分摊</option>
	    				</select>&nbsp;&nbsp;&nbsp;&nbsp;
	    				</td>
	    		   </tr>
	    			<tr>    				
	    				 <td >订单状态：
                            <select id="orderStatus" name="orderStatus" style="width: 80px;">
                                <option value="0">全部</option>
                                <option value="1">未付款</option>
                                <option value="2">待发货</option>
                                <option value="3">已发货</option>
                                <option value="4">交易成功</option>
                                <option value="5">用户取消</option>
                                <option value="6">超时取消</option>
						</td>
						<td>收货手机 <input class="easyui-numberbox" data-options="prompt:'只能输入数字'" name="mobileNumber" style="width:100"></td>   
						<td>用户名 <input class="easyui-textbox" name="name" style="width:100">&nbsp;&nbsp;</td>
	    			   <td>收货人姓名 <input class="easyui-textbox" name="receiveName" style="width:100">&nbsp;</td>
	    			    
	    			</tr>
	    		   <tr>
	    				<td>
	    				退款申请时间 <input name="startTime" />&nbsp;至
	    				</td>
	    				<td>
			 			<input name="endTime" />
	    				</td>
	    			</tr>
	    			<tr>
	    				<td>
	    				&nbsp;&nbsp;
	    					<a onclick="searchInfo()" href="javascript:;" class="easyui-linkbutton" >查询</a>&nbsp;&nbsp;
	    					<input type="submit" value="导出查询结果" class="easyui-linkbutton" style="width:100px;height: 30px"/>
	    					</td>
	    				<td></td>
	    			</tr>
	    		</table>
	    		</form>
	    	</div>  
	    	<div data-options="region:'center'" >
	    		<table id="s_data"  style="width: 100%">
					
				</table>
				
				<!-- 确认收货div begin -->
				<div id="receiveGoodsDiv" class="easyui-dialog" style="width:400px;height:200px;padding:20px 20px;">
	            	<form id="receiveGoodsForm" method="post">
	            		<input type="hidden" name="receiveGoodsForm_id" id="receiveGoodsForm_id" value=""/>
	            		备注：<textarea rows="4" cols="40" name="receiveGoodsForm_remark" id="receiveGoodsForm_remark"></textarea>
	        		</form>
	        	</div>
	        	<!-- 确认收货div end -->
	    	</div>  
		</div>
	</div>

<script>
function receiveIt(id){
	$("#receiveGoodsForm_id").val('');
	$("#receiveGoodsForm_remark").val('');
	$("#receiveGoodsForm_id").val(id);
	$("#receiveGoodsDiv").dialog('open');
}

function dealWithApply(id) {
	var urlStr = "${rc.contextPath}/refund/dealRefund/"+id;
	window.open(urlStr,"_blank");
}


function searchInfo() {
	$('#s_data').datagrid('load', {
		source:1,
		number : $("input[name='number']").val(),
		status : $("#status").val(),
        financialAffairsCardId : $("#financialAffairsCardId").val(),
		type : $("#type").val(),
		startTime : $("input[name='startTime']").val(),
		endTime : $("input[name='endTime']").val(),
		moneyStatus : $("#moneyStatus").val(),
		receiveType : $("#receiveType").val(),
		name : $("input[name='name']").val(),
		receiveName : $("input[name='receiveName']").val(),
		mobileNumber : $("input[name='mobileNumber']").val(),
	});
}
	
	$(function (){
		
		$('#s_data') .datagrid({
			nowrap : false,
			striped : true,
			collapsible : true,
			idField : 'id',
			url : '${rc.contextPath}/refund/jsonAll',
			queryParams:{source:1},
			loadMsg : '正在装载数据...',
			fitColumns : true,
			remoteSort : true,
			singleSelect : true,
			pageSize : 50,
			pageList : [ 50, 60 ],
			columns : [ [
					{field : 'orderNumber', title : '订单号', width : 35, align : 'center'},
					{field : 'username', title : '用户名', width : 30, align : 'center'},
					{field : 'createTime', title : '退款发起时间', width : 40, align : 'center'},
					{field : 'updateTime', title : '最新更新时间', width : 40, align : 'center'},
					{field : 'type', title : '退货需求', width : 25, align : 'center'},
					{field : 'statusStr', title : '退款状态', width : 20, align : 'center'},
					{field : 'moneyStatus', title : '打款状态', width : 25, align : 'center'},
					{field : 'receiveStatus', title : '收货状态', width : 25, align : 'center'},
					{field : 'realMoney', title : '退款金额', width : 30, align : 'center'},
					{field : 'count', title : '退款数量', width : 20, align : 'center'},
					{field : 'productId', title : '退款商品ID', width : 30, align : 'center'},
					{field : 'productName', title : '商品名称', width : 50, align : 'center'},
					{field : 'productCount', title : '购买数量', width : 20, align : 'center'},
					{field : 'fullName', title : '收货人', width : 30, align : 'center'},
					{field : 'mobileNumber', title : '收货手机', width : 30, align : 'center'},
					{field : 'hidden', title : '操作', width : 80, align : 'center',
						formatter : function(value, row, index) {
							//只显示打款（待退款）和确认收货（type=2 && status=3、4 && logistics.isRes=0）
							var a = '';
							if(row.status == 3){
								a += '<a href="javaScript:;" onclick="dealWithApply(' + row.id + ')">'+row.moneyTip+'</a>  ';
							}
							if((row.status == 3 || row.status == 4) && row.canReceive == 1){
								a += ' | <a href="javaScript:;" onclick="receiveIt(' + row.id + ')">确认收货</a>';
							}
							return a;
						}
					} ] ],
			pagination : true
		}); 
		
		$('#receiveGoodsDiv').dialog({
	        title:'确认收货',
	        collapsible:true,
	        closed:true,
	        modal:true,
	        buttons:[{
	            text:'保存',
	            iconCls:'icon-ok',
	            handler:function(){
	                $.messager.confirm("提示信息","确定收到货了吗？",function(re){
	                    if(re){
	                        $.messager.progress();
	                        var id = $('#receiveGoodsForm_id').val();
	    	            	var remark = $.trim($('#receiveGoodsForm_remark').val());
	    	            	if(remark.length>100){
	    	            		$.messager.alert("提示","备注字数必须小于100","info");
	    	            		return false;
	    	            	}
	                        $.ajax({
	            			    url: '${rc.contextPath}/refund/receiveGoods',
	            			    type: 'post',
	            			    dataType: 'json',
	            			    data: {'id':id,'remark':remark},
	            			    success: function(data){
	            			    	 $.messager.progress('close');
	            			        if(data.status == 1){
	            			        	$('#receiveGoodsDiv').dialog('close');
	            			        	$('#s_data') .datagrid("load");
	            			        }else{
	            			        	$.messager.alert("提示",data.msg,"info");
	            			        }
	            			    },
	            			    error: function(xhr){
	            			    	 $.messager.progress('close');
	            			    	$.messager.alert("提示",'服务器忙，请稍后再试。('+xhr.status+')',"info");
	            			    }
	            			});
	                    }
	                });
	            }
	        },{
	            text:'取消',
	            align:'left',
	            iconCls:'icon-cancel',
	            handler:function(){
	                $('#receiveGoodsDiv').dialog('close');
	            }
	        }]
	    });
	});
	
		$(function(){	
						//日期输入框      //开始日期
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
</script>

</body>
</html>