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

<div data-options="region:'west',title:'menu',split:true" style="width:180px;">
<input type="hidden" value="${nodes!0}" id="nowNode"/>
	<#include "../base/menu.ftl" >
</div>

<div data-options="region:'center'" style="padding:5px;">
	<div id="cc" class="easyui-layout" data-options="fit:true" >
		<div data-options="region:'north',title:'提现管理',split:true" style="height: 150px;">
			<form id="searchForm" method="post">
				<table cellpadding="15">
					<tr>
	                	<td>用户昵称：</td>
	                	<td class="searchText">
							<input id="searchTitle" name="searchTitle" value="" placeholder="用户昵称" />
						</td>
	                    <td>用户ID：</td>
	                    <td class="searchText">
							<input id="searchAccountId" name="searchAccountId" value="" placeholder="请输入数字" />
						</td>
		                <td>
							<a id="searchBtn" onclick="searchList()" href="javascript:void(0)" class="easyui-linkbutton" data-options="iconCls:'icon-search',width:'80px'">查 询</a>
	                	</td>
	                </tr>
				</table>
			</form>
		</div>
		
		<div data-options="region:'center'" >
			<!--数据表格-->
    		<table id="s_data" style="width: 100%"></table>
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


	function refuse(id,type){
	    $.messager.confirm("提示信息","确定拒绝么？",function(re){
	        if(re){
	            $.messager.progress();
	            $.post("${rc.contextPath}/qqbsCash/refuse",{id:id,type:type},function(data){
	                $.messager.progress('close');
	                if(data.status == 1){
	                    $.messager.alert('响应信息',"拒绝成功...",'info',function(){
	                        $('#s_data').datagrid('reload');
	                    });
	                } else{
	                    $.messager.alert('响应信息',data.msg,'error',function(){
	                    });
	                }
	            })
	        }
	    })
	}
	
	function accept(id,type){
    	$.messager.confirm("提示信息","确定打款么？",function(re){
        	if(re){
	            $.messager.progress();
	            $.post("${rc.contextPath}/qqbsCash/accept",{id:id,type:type},function(data){
	                $.messager.progress('close');
	                if(data.status == 1){
	                    $.messager.alert('响应信息',"打款成功...",'info',function(){
	                        $('#s_data').datagrid('reload');
	                    });
	                } else{
	                    $.messager.alert('响应信息',data.msg,'error',function(){
	                    });
	                }
	            })
        	}
    	})
	}
	// 搜索按钮
	function searchList() {
		$('#s_data').datagrid('load', {
			name : $("#searchTitle").val(),
			accountId:$('#searchAccountId').val()
		});
	}

	// 跟新gird行数据
	function updateActions(){
		var rowcount = $('#s_data').datagrid('getRows').length;
		for(var i=0; i<rowcount; i++){
			$('#s_data').datagrid('updateRow',{
		    	index:i,
		    	row:{}
			});
		}
	}
	
		$('#s_data').datagrid({
            nowrap: false,
            striped: true,
            collapsible:true,
            idField:'id',
            url:'${rc.contextPath}/qqbsCash/listInfo',
            loadMsg:'正在装载数据...',
            singleSelect:false,
            fitColumns:true,
            remoteSort: true,
            pageSize:10,
            pageList:[10,20,30,40,50],
            columns:[[
            	{field:'id',    title:'ID', width:15, align:'center',checkbox:true},
            	{field:'index',    title:'提现编号', width:10, align:'center'},
            	{field:'nickname',    title:'昵称', width:20, align:'center'},
                {field:'accountId',    title:'用户Id', width:20, align:'center'},
                {field:'withdrawals',    title:'提现金额', width:20, align:'center'},
                {field:'hidden',  title:'操作', width:40, align:'center',
					formatter : function(value, row, index) {
							var a = '';
							var b = '';
							var c = '';
							var d = '';
							if (row.status == 2){
								a = '已打款';
							}else if(row.status == 3){
								b = '已拒绝';
							}else{
								c = '<a href="javascript:void(0);" onclick="accept(' + row.id + ',' +1+ ')">打款</a>&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;';
								d = '<a href="javascript:void(0);" onclick="refuse(' + row.id + ','+2+')">拒绝</a>';
							}
							return a+b+c + d;
					}
				} ] ],				
			onBeforeEdit:function(index,row){
            	row.editing = true;
            	updateActions();
        	},
        	onAfterEdit:function(index,row){
            	row.editing = false;
            	updateActions();
            	updateActivity(row);
        	},
        	onCancelEdit:function(index,row){
            	row.editing = false;
            	updateActions();
        	},
			pagination : true,
			onLoadSuccess:function(data){
    	  		if (data.rows.length > 0) { 
            		//循环判断操作为新增的不能选择 
                 	for (var i = 0; i < data.rows.length; i++) { 
	                     //根据operate让某些行不可选 
	                     if (data.rows[i].status == 2 || data.rows[i].status == 3) { 
	                         $("input[type='checkbox']")[i + 1].disabled = true;
                     		 $("#s_data").datagrid("unselectRow", i);
	                     } 
                 	} 
             	} 
            },
            onClickRow:function(rowIndex, rowData){
	             //加载完毕后获取所有的checkbox遍历 
	             $("input[type='checkbox']").each(function(index, el){ 
	                 //如果当前的复选框不可选，则不让其选中 
	                 if (el.disabled == true) { 
	                     $("#s_data").datagrid("unselectRow", index-1);
	                 } 
	             }) 
         	}, 
	         onCheckAll:function(rows){ 
	            //加载完毕后获取所有的checkbox遍历 
	             $("input[type='checkbox']").each(function(index, el){
	                 //如果当前的复选框不可选，则不让其选中 
	                 if (el.disabled == true) { 
	                 	$("#s_data").datagrid("unselectRow", index-1);
	                 } 
	             }) 
	         }
		});

		
</script>

</body>
</html>