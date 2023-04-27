$(document).ready(function(){
    loadPages();
    loadData(0);
});

function loadPages() {
    $.getJSON('/users', function(data) {
        var pageCount = (data.count / data.pageSize) +
            (data.count % data.pageSize > 0 ? 1 : 0);
        var i;

        for (i = 1; i <= pageCount; i++) {
            $('#pages').append(
                $('<li>').attr('class', 'page-item').append(
                    $('<a>').attr('class', 'page-link').attr('id', i - 1)
                        .append('Page ' + i))
            );
        }
    });

    $("#pages").on("click", ".page-link", function(event) {
        loadData(event.target.id);
    });
}

function loadData(page) {
    $("#data > tbody").empty();

    $.getJSON('/allusers?page=' + page, function(data) {
        var i;

        for (i = 0; i < data.length; i++) {
            $('#data > tbody:last-child').append(
                $('<tr>')
                    .append($('<td>').append(data[i].chatId))
                    .append($('<td>').append(data[i].name))
                    .append($('<td>').append(data[i].admin))
                    .append($('<td>').append(data[i].phone))
                    .append($('<td class="del" onclick="del(event)">').append('delete'))
            );
        }
    });
}
function del(e){
    const a = e.target.previousSibling;
    console.log(a.innerHTML);
    $.ajax({
        type: 'GET',
        url: '/delete?phone='+a.innerHTML,
        error : function() {
            console.log("error");
        },
        success: function () {
            location.reload()
        }
    });
};