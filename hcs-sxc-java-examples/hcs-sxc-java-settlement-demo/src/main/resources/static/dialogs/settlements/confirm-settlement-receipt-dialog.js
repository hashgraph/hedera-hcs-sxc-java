function openAcceptSettlePaymentMade (threadId, otherUserId){
    event.preventDefault();
    styleTextFields();
    acceptSettlePaymentMadeDialog  = new mdc.dialog.MDCDialog(document.getElementById('accept-settle-payment-made-dialog'));

    $acceptSettlePaymentMadeDialog = document.getElementById('accept-settle-payment-made-dialog');
    $acceptSettlePaymentMadeDialog.querySelector("#thread-id").value=threadId;
    clickFunction = function(){

        showSnackBarMessage("sending confirmation " + threadId + " ...");
        acceptSettlePaymentMadeDialog.close();

        el = document.getElementById(otherUserId);
        el.style.opacity=0.4;
        el.style.pointerEvents =  'none';
        el.querySelector(".mdc-linear-progress").classList.remove('mdc-linear-progress--closed');

        fetch('/settlements/paid/ack/'+threadId, {
                method: 'post',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                }
	    	}).then(async function(response) {
	    		if (response.status===200){
	    			showSnackBarMessage("Confirmation sent");
	    			await renderSettlementsPanel(otherUserId);
	    			return response.json();
	    		}
            }).catch(function(res){
                alert(res);
            });

    };
    $acceptSettlePaymentMadeDialog.querySelector("#accept-settlement-payment-made-send").onclick = clickFunction;
    acceptSettlePaymentMadeDialog.open();
    return false;
}