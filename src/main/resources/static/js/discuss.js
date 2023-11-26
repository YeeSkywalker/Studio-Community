function like(btn, entityType, entityId, targetId, postId) {
    $.post(
        CONTEXT_PATH + "/like",
        {"entityType":entityType, "entityId":entityId, "targetId":targetId, "postId":postId},
        (data) => {
            data = $.parseJSON(data);
            if (data.code == 200) {
                console.log(data)
                $(btn).children("i").text(data.count);
                $(btn).children("b").text(data.status==1?"Liked":"Like");
            } else {
                alert(data.msg);
            }
        }
    )
}