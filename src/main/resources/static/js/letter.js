$(function(){
	$("#sendBtn").click(send_dm);
	$(".close").click(delete_msg);
});

function send_dm() {
	$("#sendModal").modal("hide");

	var toName = $("#recipient-name").val();
	var content = $("#message-text").val();
	$.post(
	    CONTEXT_PATH + "/dm/send",
	    {"targetUsername":toName, "content":content},
	    function(data) {
	        data = $.parseJSON(data);
	        if(data.code == 200) {
	            $("#hintBody").text("Send successfully!")
	        } else {
	            $("#hintBody").text(data.msg)
	        }

	        $("#hintModal").modal("show");
            setTimeout(() => {
                $("#hintModal").modal("hide");
                location.reload();
            }, 2000);
	    }
	);
}

function delete_msg() {
	// TODO 删除数据
	$(this).parents(".media").remove();
}