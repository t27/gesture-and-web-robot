function checkDeviceName(){
	var st=$('#device-name').val()
	st=st.trim();
	if(st.match(/^([0-9]|[a-z])+([0-9a-z]+)$/i) == null)
	{
		alert("Enter Valid Device Name(Only Alphabets and Numbers allowed)");
		return false;
	}
	return true;
}

$("#play").click( function() {
if(checkDeviceName()){
	document.location.href = "./play.html?devicename="+$('#device-name').val();
}

});