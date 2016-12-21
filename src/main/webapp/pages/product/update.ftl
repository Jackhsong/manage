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
.input-xxlarge{
	width: 400px;
	height: 20px;
}
.input-xlarge{
	width:300px;
	height: 20px;
}
.input-mini{
	width:50px;
	height: 15px;
}
.selectStyle{
	width:200px;
	height:20px;
}
textarea{ 
 	resize:none;
}                                                               
</style>
</head>
<body class="easyui-layout">
	<div data-options="region:'center',title:'添加商品信息'" style="padding:5px;">
		<form id="productBaseForm" action="post">
			<fieldset>
				<legend>商品基本信息</legend>
				<input type="hidden" value="1" name="saveType" id="saveType"/>
				<input type="hidden" value="<#if product.id?exists && (product.id != 0)>${product.id?c}</#if>" id="editId" name="id" />
				<table id="baseInfo">
					<tr>
						<td><label for="barcode">商品条码：</label><br/><br/></td>
						<td>
							<input type="text" class="input-xxlarge" id="barcode" name="barcode" maxlength="20" value="<#if product.barcode?exists>${product.barcode}</#if>"/><br/><br/>
						</td>
					</tr>
					<tr>
						<td><label for="productName">商品名称：</label><br/><br/></td>
						<td>
							<input type="text" class="input-xxlarge" id="productName" name="name" maxlength="44" value="<#if product.name?exists>${product.name}</#if>"/>
							<font color="red" size="2px">必填</font>
						<br/><br/>
						</td>
					</tr>
					<tr>
						<td><label for="remark">商家商品备注：</label><br/><br/></td>
						<td>
							<input type="text" class="input-xxlarge" id="remark" name="remark" maxlength="100" value="<#if product.remark?exists>${product.remark}</#if>"/>
						<br/><br/></td>
					</tr>
					<tr><td colspan="2"><hr/></td></tr>
					
					<tr>
						<td><label for="code">商品编码：</label><br/><br/></td>
						<td>
							<input maxlength="22" type="text" id="code" name="code" class="input-xlarge" value="<#if product.code?exists>${product.code}</#if>" <#if (canEdit?exists && (canEdit>0))>readonly="readonly"</#if>/><font color="red">必填</font>
							
							<span id="autoCreateCodeTips" style="color: red"></span>&nbsp;注：不同的商家可能用不同的商品编码，可使用%x实现单件编码组合成多件销售
						<br/><br/></td>
					</tr>
					<tr>
						<td><label>商品结算：</label><br/><br/></td>
						<td>
							<input type="radio" name="submitType" id="submitType1" value="1" <#if product.submitType?exists && (product.submitType==1)>checked</#if>> 供货价 
							<input type="number" class="input-mini" name="wholesalePrice" id="wholesalePrice" maxlength="10" value="<#if wholesalePrice?exists>${wholesalePrice}</#if>"/>&nbsp;&nbsp;
							<input type="radio" name="submitType" id="submitType2" value="2" <#if product.submitType?exists && (product.submitType==2)>checked</#if>> 扣点
							<input type="number" class="input-mini" name="deduction" id="deduction" maxlength="10" value="<#if deduction?exists>${deduction}</#if>"/> %&nbsp;
							建议价：<input type="number" class="input-mini" name="proposalPrice" id="proposalPrice" maxlength="10" value="<#if proposalPrice?exists>${proposalPrice}</#if>" />
							<input type="radio" name="submitType" id="submitType3" value="3" <#if product.submitType?exists && (product.submitType==3)>checked</#if>> 自营采购价
							<input type="number" class="input-mini" name="selfPurchasePrice" id="selfPurchasePrice" maxlength="10" value="<#if selfPurchasePrice?exists>${selfPurchasePrice}</#if>"/>
							&nbsp;&nbsp;<a href="javascript:void(0);" onclick="showLogList();">供货价历史记录</a>
						<br/><br/></td>
					</tr>
					<#if product.id?exists && (product.id == 0)>
					<tr>
						<td><label for="totalStock">商品库存：</label><br/><br/></td>
						<td>
							<input type="number" class="input-xxlarge" name="totalStock" id="totalStock" maxlength="11" onblur="intValidator(this)" value="<#if totalStock?exists>${totalStock}</#if>"/>
							<font color="red" size="2px">必填</font>
							<br/><br/></td>
					</tr>
					</#if>
					<tr>
						<td><label>是否自动调库存：</label><br/><br/></td>
						<td>
							<input type="radio" name="isAutomaticAdjustStock" id="isAutomaticAdjustStock1" value="1" <#if product.isAutomaticAdjustStock?exists && (product.isAutomaticAdjustStock==1)>checked</#if>/>是&nbsp;&nbsp;&nbsp;
							<input type="radio" name="isAutomaticAdjustStock" id="isAutomaticAdjustStock0" value="0" <#if product.isAutomaticAdjustStock?exists && (product.isAutomaticAdjustStock==0)>checked</#if>/>否
						</td>
					</tr>
					<tr>
						<td><label>建议市场价：</label><br/><br/></td>
						<td>
							<input type="number" class="input-xxlarge" name="proposalMarketPrice" id="proposalMarketPrice" value="<#if proposalMarketPrice?exists>${proposalMarketPrice}</#if>"/><br/><br/>
						</td>
					</tr>
					<tr>
						<td><label>建议特卖价：</label><br/><br/></td>
						<td>
							<input type="number" class="input-xxlarge" name="proposalSalesPrice" id="proposalSalesPrice" value="<#if proposalSalesPrice?exists>${proposalSalesPrice}</#if>"/><br/><br/>
						</td>
					</tr>
                    <tr>
                        <td><label>心动慈露
						供货价：</label><br/><br/></td>
                        <td>
                            <input type="number" class="input-xxlarge" name="hqbsWholesalePrice" id="hqbsWholesalePrice" value="<#if product.hqbsWholesalePrice?exists>${product.hqbsWholesalePrice}</#if>"/><br/><br/>
                        </td>
                    </tr>

					<tr><td colspan="2"><hr/></td></tr>

					<tr>
						<td><label for="netVolume">净含量：</label><br/><br/></td>
						<td><input type="text" class="input-xxlarge" name="netVolume" id="netVolume" maxlength="15" value="<#if product.netVolume?exists>${product.netVolume}</#if>"/><br/><br/></td>
					</tr>
					<tr>
						<td><label for="placeOfOrigin">产地：</label><br/><br/></td>
						<td><input type="text" class="input-xxlarge" name="placeOfOrigin" id="placeOfOrigin" maxlength="20" value="<#if product.placeOfOrigin?exists>${product.placeOfOrigin}</#if>"/><br/><br/></td>
					</tr>
					<tr>
						<td><label for="storageMethod">储藏方法：</label><br/><br/></td>
						<td><input type="text" class="input-xxlarge" name="storageMethod" id="storageMethod" maxlength="50" value="<#if product.storageMethod?exists>${product.storageMethod}</#if>"/><br/><br/></td>
					</tr>
					<tr>
						<td><label for="manufacturerDate">生产日期：</label><br/><br/></td>
						<td><input type="text" class="input-xxlarge" name="manufacturerDate" id="manufacturerDate" maxlength="20" value="<#if product.manufacturerDate?exists>${product.manufacturerDate}</#if>"/>
						<!-- <font color="red" size="2px">必填</font> -->
						<br/><br/></td>
					</tr>
					<tr>
						<td><label for="durabilityPeriod">保质期：</label><br/><br/></td>
						<td><input type="text" class="input-xxlarge" name="durabilityPeriod" id="durabilityPeriod" maxlength="15" value="<#if product.durabilityPeriod?exists>${product.durabilityPeriod}</#if>"/>
						<!-- <font color="red" size="2px">必填</font> -->
						<br/><br/></td>
					</tr>
					<tr>
						<td><label for="expireDate">过期提醒时间：</label><br/><br/></td>
						<td><input type="text" class="input-xxlarge" name="expireDate" id="expireDate" maxlength="15" value="<#if product.expireDate?exists>${product.expireDate}</#if>" onClick="WdatePicker({dateFmt: 'yyyy-MM-dd'})"/>
						<!-- <font color="red" size="2px">必填</font> -->
						<br/><br/></td>
					</tr>
					<tr>
						<td><label for="peopleFor">适用人群：</label><br/><br/></td>
						<td><input type="text" class="input-xxlarge" name="peopleFor" id="peopleFor" maxlength="50" value="<#if product.peopleFor?exists>${product.peopleFor}</#if>"/><br/><br/></td>
					</tr>
					<tr>
						<td><label for="foodMethod">食用方法：</label><br/><br/></td>
						<td><input type="text" class="input-xxlarge" name="foodMethod" id="foodMethod" maxlength="50" value="<#if product.foodMethod?exists>${product.foodMethod}</#if>"/><br/><br/></td>
					</tr>
					<tr>
						<td><label for="useMethod">使用方法：</label><br/><br/></td>
						<td><input type="text" class="input-xxlarge" name="useMethod" id="useMethod" maxlength="50" value="<#if product.useMethod?exists>${product.useMethod}</#if>"/><br/><br/></td>
					</tr>
					<tr>
						<td><label for="sellingPoint">卖点</label><br/><br/></td>
						<td><input type="text" class="input-xxlarge" name="sellingPoint" id="sellingPoint" maxlength="50" value="<#if product.sellingPoint?exists>${product.sellingPoint}</#if>"/><br/><br/></td>
					</tr>
					<tr>
						<td><label for="tip">温馨提示：</label><br/><br/></td>
						<td>
							<textarea name="tip" id="tip" onkeydown="checkEnter(event)" style="height: 60px;width: 400px"><#if product.tip?exists>${product.tip}</#if></textarea>
							&nbsp;&nbsp;（长度&lt; 100；当前字数：<font id="tipCounter" color="red">0</font>）<br/><br/>
						</td>
					</tr>
					
					<tr>
						<td><label for="deliverAreaDesc">配送地区描述：</label><br/><br/></td>
						<td>
							<input type="text" class="input-xxlarge" name="deliverAreaDesc" id="deliverAreaDesc" maxlength="50" value="<#if product.deliverAreaDesc?exists>${product.deliverAreaDesc}</#if>"/>
							<input type="hidden" name="deliverAreaType" id="deliverAreaType" value="<#if product.deliverAreaType?exists>${product.deliverAreaType?c}</#if>"/>
							<br/><br/>
						</td>
					</tr>
					
					<tr><td colspan="2"><hr/></td></tr>
					<tr>
						<td><label for="gegeSay">介绍描述：</label><br/><br/></td>
						<td>
							<textarea id="gegeSay" name="gegeSay" onkeydown="checkEnter(event)" style="height: 60px;width: 400px"><#if product.gegeSay?exists>${product.gegeSay}</#if></textarea>
							&nbsp;&nbsp;长度（&lt; 140）：<span style="color:red" id="counter">0 字</span><br/><br/></td>
					</tr>
					<tr><td colspan="2"><hr/></td></tr>
					<tr>
						<td style="font-size: 20px;">商品图片</td>
					</tr>
					<tr>
						<td><label for="image1">商品详情主图1</label><br/><br/></td>
						<td>
							<input type="text" id="pic_1" name="image1" style="width: 450px;" class="input-xlarge" value="<#if product.image1?exists>${product.image1}</#if>" />
							<a onclick="picDialogOpen(this)" href="javascript:;" class="easyui-linkbutton">上传图片</a>
							<font color="red" size="2px" style="italtic">图片大小不得超过400KB</font>
						<br/><br/></td>
					</tr>
					<tr>
						<td><label for="image2">商品详情主图2</label><br/><br/></td>
						<td>
							<input type="text" id="pic_2" name="image2" style="width: 450px;" class="input-xlarge" value="<#if product.image2?exists>${product.image2}</#if>" />
							<a onclick="picDialogOpen(this)" href="javascript:;" class="easyui-linkbutton">上传图片</a>
						<br/><br/></td>
					</tr>
					<tr>
						<td><label for="image3">商品详情主图3</label><br/><br/></td>
						<td>
							<input type="text" id="pic_3" name="image3" style="width: 450px;" class="input-xlarge" value="<#if product.image3?exists>${product.image3}</#if>" />
							<a onclick="picDialogOpen(this)" href="javascript:;" class="easyui-linkbutton">上传图片</a><br/><br/>
					</tr>
					<tr>
						<td><label for="image4">商品详情主图4</label><br/><br/></td>
						<td>
							<input type="text" id="pic_4" name="image4" style="width: 450px;" class="input-xlarge" value="<#if product.image4?exists>${product.image4}</#if>" />
							<a onclick="picDialogOpen(this)" href="javascript:;" class="easyui-linkbutton">上传图片</a><br/><br/>
					</tr>
					<tr>
						<td><label for="image5">商品详情主图5</label><br/><br/></td>
						<td>
							<input type="text" id="pic_5" name="image5" style="width: 450px;" class="input-xlarge" value="<#if product.image5?exists>${product.image5}</#if>" />
							<a onclick="picDialogOpen(this)" href="javascript:;" class="easyui-linkbutton">上传图片</a>
							<br/><br/>
						</td>
					</tr>
					<tr><td colspan="2"><hr/></td></tr>
				</table>
				<table id="imageDetails">
				
				</table>
				<table>
					<tr>
						<td><label for="isAvailable">商品使用状态</label></td>
						<td>
							<input type="radio" name="isAvailable" id="isAvailable1" value="1" <#if product.isAvailable?exists && (product.isAvailable==1)>checked</#if>> 可用
							<input type="radio" name="isAvailable" id="isAvailable0" value="0" <#if product.isAvailable?exists && (product.isAvailable==0)>checked</#if>> 停用
						</td>
					</tr>
					<tr>
						<td><input style="width: 150px;cursor:pointer" type="button" id="saveButton1" value="保存"/></td>
					</tr>
				</table>
			</fieldset>
		</form>
	</div>


	<div id="picDia" class="easyui-dialog" icon="icon-save" align="center" style="padding: 5px; width: 300px; height: 150px;">
	    <form id="picForm" method="post" enctype="multipart/form-data">
	        <input id="picFile" type="file" name="picFile" /><br/><br/>
	        <a href="javascript:;" onclick="picUpload()" class="easyui-linkbutton" iconCls='icon-reload'>提交图片</a>
	    </form>
	</div>
	
	<!-- 基本商品供货价修改列表begin -->
	<div id="showHistoryListDiv" style="width:500px;height:600px;padding:20px 20px;">
		<table id="s_historyData"></table>
	</div>
	<!-- 基本商品供货价修改列表end -->
	
<script type="text/javascript">
	
	function intValidator(obj){
		var reg = /^[1-9]\d+$/;
		var value = $.trim($(obj).val());
		if(!reg.test(value)){
			$.messager.alert('提示',"请输入正整数",'info');
			return false;
		}
		$(obj).val(value);		
	}

	
    $(function(){
        $('#picDia').dialog({
            title:'又拍图片上传窗口',
            collapsible:true,
            closed:true,
            modal:true
        });
        

        $("#code").focus(function(){
        	$("#autoCreateCodeTips").html('');
        });
        
        //自动生成商品编号
        $("#autoCreateCode").click(function(){
        	$("#autoCreateCodeTips").html();
        	$.ajax({
        		url: '${rc.contextPath}/productBase/autoCreateCode',
	            type: 'post',
	            dataType: 'json',
	            success: function(data){
	            	if(data.status == 1){
	            		$("#code").val(data.code);
	            	}else{
	            		$("#autoCreateCodeTips").html('自动生成失败，请手动输入或重试');
	            	}
	            },
	            error: function(xhr){
	            	$("#autoCreateCodeTips").html('自动生成失败，请手动输入或重试');
	            }
        	});
        });

		$('#s_historyData').datagrid({
            nowrap: false,
            striped: true,
            collapsible:true,
            idField:'id',
            url:'${rc.contextPath}/productBase/jsonWholeSalePriceHistory',
            loadMsg:'正在装载数据...',
            fitColumns:true,
            remoteSort: true,
            pageSize:30,
            columns:[[
            	{field:'createTime',  title:'修改时间', width:80, align:'center'},
                {field:'username',    title:'修改人', width:30, align:'center'},
                {field:'oldPrice',    title:'修改前', width:30, align:'center'},
                {field:'newPrice',    title:'修改后', width:30, align:'center'},
            ]]
        });
		
		$('#showHistoryListDiv').dialog({
    		title:'供货价历史记录',
    		collapsible:true,
    		closed:true,
    		modal:true,
    		buttons:[{
                text:'取消',
                align:'left',
                iconCls:'icon-cancel',
                handler:function(){
                    $('#showHistoryListDiv').dialog('close');
                    $('#s_historyData').datagrid('reload');
                }
            }]
		});
    });

    function showLogList(){
    	var id = $.trim($("#editId").val());
    	$('#s_historyData').datagrid('reload',{proudctBaseId:id});
		$('#showHistoryListDiv').dialog('open');
    }
    var $obj;
    function picDialogOpen(obj) {
    	$obj = $(obj).prev();
        $("#picDia").dialog("open");
        $("#yun_div").css('display','none');
    }

    function picUpload() {
        $('#picForm').form('submit',{
            url:"${rc.contextPath}/pic/fileUpLoad",
            success:function(data){
                var res = eval("("+data+")")
                if(res.status == 1){
                    $.messager.alert('响应信息',"上传成功...",'info',function(){
                        $("#picDia").dialog("close");
                        if($obj != null && $obj!='' && $obj !=undefined) {
                        	$obj.val(res.url);
                            $("#picFile").val("");
                        }
                        return
                    });
                } else{
                    $.messager.alert('响应信息',res.msg,'error',function(){
                        return ;
                    });
                }
            }
        })
    }
    
    function addImageDetailsRow(obj){
    	var row= $("#imageDetails").find("tr").first().clone();
    	$(row).find("input[name='detailId']").val('');
    	$(row).find("input[name='content']").val('');
    	$(obj).parent().parent().after(row);
    }
    
    //批量上传图片后回调
    function addImageDetailsRowAuto(content){
    	var row= $("#imageDetails").find("tr").first().clone();
    	$(row).find("input[name='detailId']").val('');
    	$(row).find("input[name='content']").val(content);
    	$("#imageDetails").find("tr").last().after(row);
    }
    
    function removeImageDetailsRow(obj){
    	$(obj).parent().parent().remove();
    }
  
</script>

<script>

	$(document).keyup(function() { 
		var text=$("#gegeSay").val(); 
		var counter=text.length;
		$("#counter").text(counter+" 字");
	});
	
	$(function(){
		$("#tip").keyup(function(){
			var text = $("#tip").val();
			$("#tipCounter").text(text.length);
		});
	});

	function checkEnter(e){
		var et=e||window.event;
		var keycode=et.charCode||et.keyCode;
		if(keycode==13){
			if(window.event)
				window.event.returnValue = false;
			else
				e.preventDefault();//for firefox
		}
	}
	
	//清空所有图片
	function clearImage(){
		for (var i=1;i<8;i++)
		{
			$("#pic_"+i).val("");
		}	
		var row= $("#imageDetails").find("tr").first().clone();
    	$(row).find("input[name='detailId']").val('');
    	$(row).find("input[name='content']").val('');   	
    	$("#imageDetails").find("tr").remove();  	
    	$("#imageDetails").append(row);		
	}
	
	function filterNullParameter(form){
		var params = "";
		$.each(form.split("&"),function(i, item){
			var arryStr = item.split("=");
			if($.trim(arryStr[1])!=''){
				params +=item +"&";
			}
		});
		var mIds = $("input[name='detailId']");
		var mContents = $("input[name='content']");
		var mIdAndContent = "";
		$.each(mIds,function(i,item){
			var id = $(mIds[i]).val();
			var content = $(mContents[i]).val();
			mIdAndContent +=(id+","+content+";");
		});
		mIdAndContent = mIdAndContent.substring(0,mIdAndContent.length-1);
		params += ("mIdAndContent="+mIdAndContent);
		
		var categoryIds = "";
		$("#categoryTab").find("tr").each(function(){
			 var id = $(this).find("td").eq(0).text();
			 var firstId = $(this).find("td").eq(1).find("select[name='categoryFirstId']").val();
			 var secondId = $(this).find("td").eq(2).find("select[name='categorySecondId']").val();
			 var thirdId = $(this).find("td").eq(3).find("select[name='categoryThirdId']").val();
			 categoryIds+=id+","+firstId+","+secondId+","+thirdId+";";
		});
		params = params+"&categoryIds="+categoryIds;

		return params;
	}

	function sortNumber(a, b)
	{
		return a - b;
	}
	
	function submitForm(saveType){	
		$("#saveType").val(saveType);	
		var submitType= $("input[name='submitType']:checked").val();
		var deduction = $("#deduction").val();
		var proposalPrice = $("#proposalPrice").val();
		var selfPurchasePrice =$("#selfPurchasePrice").val();
    	var wholesalePrice = $("#wholesalePrice").val();
    	var editId = $("#editId").val();
    	var name = $("#productName").val(); 	
    	var code = $("#code").val();
    	var totalStock = $("#totalStock").val();
    	var manufacturerDate = $("#manufacturerDate").val();
    	var durabilityPeriod = $("#durabilityPeriod").val();
    	var tip = $("#tip").val();
    	var gegeSay = $("#gegeSay").val();
    	var image1 = $("#pic_1").val();    	
		var counter=gegeSay.length; 	
		if(submitType == undefined ||(submitType == 1 && wholesalePrice == '') || (submitType == 2 && (deduction == '' || proposalPrice == '')) || (submitType == 3 && selfPurchasePrice == '')){
			$.messager.alert("提示","请填写商品结算信息","info");
		}else if(Number(deduction) >= 100){
            $.messager.alert("提示","扣点需要小于100%","info");
        }
		else if($.trim(name) == ""){
			$.messager.alert("提示","请输入商品名称","info");
		}else if($.trim(code) == ""){
    		$.messager.alert("提示","请输入商品编码","info");
		}else if(totalStock == ''){
			$.messager.alert("提示","请输入库存","info");
		}else if(tip.length > 99){
    		$.messager.alert("提示","温馨提示字数不得超过100","info");
    	}else if(counter > 140){
    		$.messager.alert("提示","格格说字数不得超过140","info");
    	}else{
    		$.messager.progress();
    		$.post(
    			"${rc.contextPath}/productBase/checkCodeAndBarCode",
    			{code:code,productBaseId:editId},
    			function(data){
    				if(data.status==1){
    					$.ajax({
    		    			url: '${rc.contextPath}/productBase/saveOrUpdate',
    		    			data:filterNullParameter($("#productBaseForm").serialize()),
    		    			type:'post',
    		    			dataType:'JSON',
    		    			success: function(data) {
    		    				$.messager.progress('close');
    		    				if (data.status == 1) {
    		    					window.location.href = "${rc.contextPath}/productBase/list";
    		    				} else {
    		    					$.messager.alert("提示", data.msg, "error");
    		    				}
    		    			},
    		    			error:function(data, textStatus){
    		    				$.messager.progress('close');
    		    			}
    		    		});
    				}else{
    					$.messager.alert("提示","商品编码重复，请重新生成","info");
    					$.messager.progress('close');
    				}
    			},
    			'json'
    		);
    		
    	} 
	}
	
	$(function(){
		
	    $("#saveButton1").click(function(){
	    	submitForm(1);
    	});
	    
	    $("#saveButton2").click(function(){
	    	var image1 = $.trim($("#pic_1").val());
	    	var image2 = $.trim($("#pic_2").val());
	    	var image3 = $.trim($("#pic_3").val());
	    	var image4 = $.trim($("#pic_4").val());
	    	var image5 = $.trim($("#pic_5").val());
	    	var contents = $("input[name='content']");
	    	var imageDetails = '';
	    	$.each(contents,function(i,item){
				imageDetails +=$.trim($(contents[i]).val());
			});
	    	var isEmpty = true;
	    	if((image1 == '' && image2 == '' && image3 == '' && image4 == '' && image5 == '') || imageDetails == '' ){
	    		$.messager.alert('提示','图片不能全部为空',"error");
	    	}else{
		    	$.messager.confirm('提示','确定把信息同步至特卖/商城商品吗？',function(b){
		    		if(b){
				    	submitForm(2);
		    		}
		    	});
	    	}
    	});
	    
		$("#freightTemplate").change(function(){
			$("#freight_muban").prop("checked", true);
			$("#freight_baoyou").prop("checked", false);
		});
				

		


		$('#code').change(function (){
			var code = $('#code').val();
            var index = code.lastIndexOf("%");
            if(index != -1){
            	var num = code.substring(index+1);
                var reg = new RegExp("^[0-9]*$");
    			if(index == (code.length-1)){
                    $.messager.alert("确认信息","您输入了一个以%结尾的商品编码哦。"+num,"info");
    			}else{
                    if(reg.test (num)){
                        $.messager.alert("确认信息","该商品发货时数量会默认乘以"+num+"哦，请确认。","warn");
                    }
    			}
            }
		});
		
		var submitType = $("input[name='submitType']:checked").val();
		var wholesalePrice = $("#wholesalePrice").val();
		var deduction = $("#deduction").val();
		var proposalPrice = $("#proposalPrice").val();
		var selfPurchasePrice =$("#selfPurchasePrice").val();
		$("#submitType1").change(function(){
			if($(this).is(":checked")){
				$("#deduction").val('');
				$("#proposalPrice").val('');
				$("#selfPurchasePrice").val('');
				if(submitType == 1){
					$("#wholesalePrice").val(wholesalePrice);
				}
			}
		});
		
		$("#submitType2").change(function(){
			if($(this).is(":checked")){
				$("#wholesalePrice").val('');
				$("#selfPurchasePrice").val('');
				if(submitType == 2){
					$("#deduction").val(deduction);
					$("#proposalPrice").val(proposalPrice);
				}
			}
		});
		
		$("#submitType3").change(function(){
			if($(this).is(":checked")){
				$("#wholesalePrice").val('');
				$("#deduction").val('');
				$("#proposalPrice").val('');
				if(submitType == 3){
					$("#selfPurchasePrice").val(selfPurchasePrice);
				}
			}
		});
	});
	
</script>

</body>
</html>