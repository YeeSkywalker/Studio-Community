$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");

    // Before sending request, preset csrf
   // var token = $("meta[name='_csrf']").attr("content");
    //var header = $("meta[name='_csrf_header']").attr("content");
   // $(document).ajaxSend(function(e, xhr, options) {
    //    xhr.setRequestHeader(header, token);
    //})

	// Get title
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();

	// Post request
	$.post (
	    CONTEXT_PATH + "/discuss/add",
	    {"title":title, "content":content},
	    (data) => {
	        data = $.parseJSON(data);
	        $("#hintBody").text(data.msg);

	        // Show hint frame
	        $("#hintModal").modal("show");
            setTimeout(() => {
                $("#hintModal").modal("hide");
                if (data.code == 200) {
                    window.location.reload();
                }
            }, 2000);
	    }
	);
}