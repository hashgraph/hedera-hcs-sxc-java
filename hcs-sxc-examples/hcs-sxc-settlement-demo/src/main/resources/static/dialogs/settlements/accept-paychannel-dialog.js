function openAcceptPaychannelDialog (threadId, otherUserId, channelName){
    event.preventDefault();
    styleTextFields();
    confirmChannelDialog  = new mdc.dialog.MDCDialog(document.getElementById('confirm-pay-channel-dialog'));

    $confirmChannelDialog = document.getElementById('confirm-pay-channel-dialog');
    $confirmChannelDialog.querySelector("#channel-name").value=channelName;

    clickFunction = function(){

        showSnackBarMessage("sending confirmation " + threadId + " ...");
        confirmChannelDialog.close();

        el = document.getElementById(otherUserId);
        el.style.opacity=0.4;
        el.style.pointerEvents =  'none';
        el.querySelector(".mdc-linear-progress").classList.remove('mdc-linear-progress--closed');

        fetch('settlements/proposechannel/ack/'+threadId, {
                method: 'post',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                }
            }).then(function(response) {
                return response.json();
            }).then(function(data) {
                showSnackBarMessage("confirmation sent");

            }).catch(function(res){
                alert(res);
            });

    };
    $confirmChannelDialog.querySelector("#confirm-channel-send").onclick = clickFunction;
    confirmChannelDialog.open();
    return false;
}