<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<style>
    	.wrap{
    		height:100%;
    		margin: 0;
    	}
        .body{
        	
            float : left;
            width : 65%;
            box-sizing : border-box;
           
        }
       .memberList{
           width: 560px;
           height: 600px;
           border: 1px #d3d3d3 solid;
           float: left;
           padding: 20px; border-radius: 15px;
           margin-right: 20px;
       }
       .messageSend{
           width: 600px;
           height: 600px;
           float: left;
           border: 1px lightgray solid;
           padding-top: 50px;
            border-radius: 15px;
           
       }
       .search{
            width: 340px; 
            height: 40px; 
            border: 1px lightgray solid;
            border-radius: 5px;
       }
       #search-btn{
            background: rgba(170, 143, 211, 0.673); 
            color: white; 
            width: 80px; 
            height: 40px; 
            border: 0ch;
            border-radius: 5px;
        }
        .type{
            width: 80px; 
            height: 40px; 
            border: 1px lightgray solid;
            border-radius: 5px;
        }
        #table2{
            width: 100%;
            text-align: center;
           
        }
        #table2 th{
            background:#C6B3E1;
            height: 40px;
            color:white;
            position: sticky;
    		top: 0px;
    
        }
        
        #table2 td{
            height: 60px;
            border-top: 1px solid lightgray;
            border-bottom: 1px solid lightgray;
            font-size: 15px;
            color:gray;
        }
        #table2 tr td:first-child{
            visibility: hidden;
        }
        .textarea{
            width: 500px;
            height: 430px;
            border-radius: 5px;
            resize: none;
        	border-color:rgba(170, 143, 211, 0.473); 
            font-size: 30px;
            padding: 15px;
            
        }
        #messageSend-btn{
            background: rgba(170, 143, 211, 0.673); 
            color: white; 
            width: 500px; 
            height: 60px; 
            border: 0ch;
            border-radius: 5px;
        }
        .textarea:focus{
         	outline:rgba(170, 143, 211, 0.473);
         	border-color:rgba(170, 143, 211, 0.8); 
         }
         .search:focus{
         	outline:rgba(170, 143, 211, 0.473);
         	border-color:rgba(170, 143, 211, 0.8); 
         }
         .type:focus{
         	outline:rgba(170, 143, 211, 0.473);
         	border-color:rgba(170, 143, 211, 0.8); 
         }
         #search-btn:focus{
         	outline:rgba(170, 143, 211, 0.473);
         	border-color:rgba(170, 143, 211, 0.8); 
         }
         
       
</style>
</head>
<body>
<form action="message.do" method="get">
	<div id="hide">
		<input type="hidden" id="keyword-hidden" name="keyword" value="">
		<input type="hidden" id="type-hidden" name="type" value="">
		<button type="submit" id="form-btn">??????</button>
	</div>
</form>
    <div class="wrap">
    	<%@ include file="/WEB-INF/views/admin/sidebar.jsp" %>
    	<form action="message.up" method="post" onsubmit="return chkchk(this)">
        <div class="body">
        
            <div style=" margin: 60px; width: 1400px;">
                <div class="memberList">
                    <div align="center">
                        
                            <input type="text" class="search" id="keyword" >
                            <select id="type" class="type">
                                <option value="MEMBER_NAME">??????</option>
                                <option value="MEMBER_ID">?????????</option>
                                <option value="MEMBER_NICK">?????????</option>
                                <option value="MEMBER_PHONE">????????????</option>
                            </select>
                            <button type="button" id="search-btn">??????</button>
                        
                    </div><br>
                    
                    <div style="overflow: auto; height: 500px; border-top: 1px solid lightgray;">
                        <table id="table2" >
                            <tr>
                                <th style="width:1px"></th>
                                <th>???????????????</th>
                                <th>?????????</th>
                                <th>??????</th>
                                <th>?????????</th>
                                <th><input type="checkbox" id="checkall" style="width:50px;"></th>
                            </tr>
                            <c:choose>
                               <c:when test="${ empty memberList }">
                                    <tr>
                                        <td colspan="6">??????????????? ????????????.</td>
                                       </tr>
                               </c:when>
                               <c:otherwise>
                                       <c:forEach var="b" items="${ memberList }">
	                                        <tr>
	                                            <td class="memberNo">${ b.memberNo }</td>
	                                            <td>${ b.gradeName }</td>
	                                            <td>${ b.memberId }</td>
	                                            <td>${ b.memberName }</td>
	                                            <td>${ b.memberPhone }</td>
	                                            <td><input type="checkbox" value="${ b.memberNick }" name="check" class="checkBox" id="check"></td>
	                                        </tr>
                                    	</c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </table>
                		</div>
                    </div>
                    
	                <div class="messageSend">
		               <div align="center">
		                    <textarea class="textarea" name="content" required></textarea><br><br>
		                    <input type="submit" id="messageSend-btn" value="??????" >
		               </div>
		            </div>
                	
                </div>
            </div>
        </form>
        </div>
    
    <script>
    $('#form-btn').hide();
	    $(document).ready(function(){
	            //????????? ???????????? ??????
	            $("#checkall").click(function(){
	                //??????????????????
	                if($("#checkall").prop("checked")){
	                    //input????????? name??? chk??? ???????????? ????????? checked????????? true??? ??????
	                    $("input[name=check]").prop("checked",true);
	                    //????????? ???????????????
	                }else{
	                    //input????????? name??? chk??? ???????????? ????????? checked????????? false??? ??????
	                    $("input[name=check]").prop("checked",false);
	                }
	            })
	        });
	    
	    $('#search-btn').click(function() {
		    	$(function(){
		    	    
		    	    	var keyword = $('#keyword').val();
		    	    	var type = $('#type').val();
		    	    	
		    	    	
		    	        $('#keyword-hidden').val(keyword);
		    	        $('#type-hidden').val(type);
		    	        $("#form-btn").click();
		    	});
	    });
	    
	    
	    
	   
    </script>
    <script>
	    function chkchk (form){ 
	        var arr_form = document.getElementsByName('check'); 
	        var num = 0; 
	        for(var i=0; i<arr_form.length; i++){ 
	            if(arr_form[i].checked){ 
	                num++; 
	            } 
	        } 
	        if(!num){ 
	            alert('???????????? ????????? ????????? ??????????????????.'); 
	            return false; 
	        } 
	    } 
	    </script>
	    
   
</body>
</html>