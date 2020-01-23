function openConfirmSettlementDialog(threadId,otherUserId) {
    event.preventDefault();
    styleTextFields();
    confirmSettlementDialog  = new mdc.dialog.MDCDialog(document.getElementById('confirm-settlement-dialog'));

    $confirmSettlementDialog = document.getElementById('confirm-settlement-dialog');
    $confirmSettlementDialog.querySelector("#thread-id").value=threadId;
    clickFunction = function(){
        $confirmSettlementDialog = document.getElementById('confirm-settlement-dialog');
        threadId = $confirmSettlementDialog.querySelector("#thread-id").value;
        showSnackBarMessage("sending confirmation " + threadId + " ...");
        confirmSettlementDialog.close();

        el = document.getElementById(otherUserId);
        el.style.opacity=0.4;
        el.style.pointerEvents =  'none';
        el.querySelector(".mdc-linear-progress").classList.remove('mdc-linear-progress--closed');

        fetch('settlements/ack/'+threadId, {
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
    $confirmSettlementDialog.querySelector("#confirm-settlement-send").onclick = clickFunction;
    confirmSettlementDialog.open();
    return false;
}