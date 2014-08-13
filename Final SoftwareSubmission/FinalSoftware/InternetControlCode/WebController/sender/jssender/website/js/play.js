function sendDweet(devname, command1) {
	dweetio.dweet_for(devname,{command:command1},function(err, dweet){
		console.log(dweet.content)
		if(dweet.content.command==command1){

			$("#result").text("Success: Sent "+ command1);
		}
	});
	// body...

}

var devicename="";
$( document ).ready(function(){
	devicename=GetURLParameter('devicename');
	$('#devname').text(devicename);
});
// var commandExecuter = {
// 	"up": sendDweet(name,"up"),
// 	"dn": sendDweet(name,"down"),
// 	"lt": sendDweet(name,"left"),
// 	"rt": sendDweet(name,"right"),
// 	"st": sendDweet(name,"stop")
// };
//w=119,a=97,s=115,d=100,x=120

$(document).keydown(function(e){
	if((e.which == 119) || (e.which == 38)){
		$('#up').click();
	//	alert("Up");
		return e.preventDefault();
	}
	if( (e.which == 97) || (e.which == 37)){
		$('#left').click();
	//	alert("left");
		return e.preventDefault();
	}
	if((e.which == 115) || (e.which == 40)){
		$('#down').click();
	//	alert("down");
		return e.preventDefault();
	}
	if((e.which == 100) || (e.which == 39)){
		$('#right').click();
	//	alert("right");
		return e.preventDefault();
	}
	if((e.keyCode == 120)){
		$('#stop').click();
	//	alert("stop");
		return e.preventDefault();
	}
});

$(document).keypress(function(e){
	if((e.which == 119) ){
		$('#up').click();
	//	alert("Up");
		return e.preventDefault();
	}
	if( (e.which == 97) ){
		$('#left').click();
	//	alert("left");
		return e.preventDefault();
	}
	if((e.which == 115) ){
		$('#down').click();
	//	alert("down");
		return e.preventDefault();
	}
	if((e.which == 100) ){
		$('#right').click();
	//	alert("right");
		return e.preventDefault();
	}
	if((e.keyCode == 120)){
		$('#stop').click();
	//	alert("stop");
		return e.preventDefault();
	}
});

// function checkDeviceName(){
// 	var st=$('#device-name').val()
// 	st=st.trim();
// 	if(st.match(/^([0-9]|[a-z])+([0-9a-z]+)$/i) == null)
// 	{
// 		alert("Enter Valid Device Name(Only Alphabets and Numbers allowed)");
// 		return false;
// 	}
// 	return true;
// }
function GetURLParameter(sParam)
{
	var sPageURL = window.location.search.substring(1);
	var sURLVariables = sPageURL.split('&');
	for (var i = 0; i < sURLVariables.length; i++)

	{

		var sParameterName = sURLVariables[i].split('=');

		if (sParameterName[0] == sParam)

		{

			return sParameterName[1];

		}

	}

}

$("#up").click( function() {
	sendDweet(devicename,"forward");
});
$("#down").click( function() {
	sendDweet(devicename,"backward");
});
$("#left").click( function() {
	sendDweet(devicename,"left");
});
$("#right").click( function() {
	sendDweet(devicename,"right");
});
$("#stop").click( function() {
	sendDweet(devicename,"stop");
});
