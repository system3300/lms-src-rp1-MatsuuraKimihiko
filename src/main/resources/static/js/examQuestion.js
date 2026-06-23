/**
 * 試験実施機能
 * 
 * @author 東京ITスクール
 */
$(function(){
	setInterval("timeCount()", 1000);
	setTimeout("timeUp()", limitTimeMin * 60 * 1000 - timeMSec + 1000);
});

function timeCount() {
	timeMSec += 1000;
	$('.sendTime').val(timeMSec);
}

function timeUp() {
	alert('制限時間となりました。回答を送信します。');
	$('.sendTime').val(limitTimeMin * 60 * 1000);
	$('#examQuestionForm').submit();
}
