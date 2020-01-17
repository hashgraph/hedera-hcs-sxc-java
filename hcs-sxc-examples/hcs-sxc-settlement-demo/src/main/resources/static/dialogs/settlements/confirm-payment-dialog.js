function openAcceptPaymentDialog (threadId, otherUserId){
    event.preventDefault();
    styleTextFields();
    confirmPaymentDialog  = new mdc.dialog.MDCDialog(document.getElementById('confirm-payment-dialog'));

    $confirmPaymentDialog = document.getElementById('confirm-payment-dialog');
    $confirmPaymentDialog.querySelector("#thread-id").value=threadId;
    clickFunction = function(){

        showSnackBarMessage("sending confirmation " + threadId + " ...");
        confirmPaymentDialog.close();

        el = document.getElementById(otherUserId);
        el.style.opacity=0.4;
        el.style.pointerEvents =  'none';
        el.querySelector(".mdc-linear-progress").classList.remove('mdc-linear-progress--closed');

        fetch('settlements/paymentInit/ack/'+threadId, {
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
    $confirmPaymentDialog.querySelector("#confirm-payment-send").onclick = clickFunction;
    confirmPaymentDialog.open();
    return false;
}