function openSettlementPaymentMadeDialog(threadId, userName) {
    $settlementPaymentMadeDialog = document.getElementById('settlement-payment-made-dialog');
    settlementPaymentMadeDialog  = new mdc.dialog.MDCDialog($settlementPaymentMadeDialog);
    event.preventDefault(); 
    styleTextFields();
    $settlementPaymentMadeDialog.querySelectorAll(".mdc-text-field input").forEach(function(e){
        e.parentElement.classList.remove('mdc-text-field--invalid');
        e.value='';}
    );
    $settlementPaymentMadeDialog.querySelector("#thread-id").value=threadId;
    $settlementPaymentMadeDialog.querySelector("#settlement-paymend-made-send").onclick =  function(){
        postBody = `{
                     "threadId"        : "${threadId}"
                    ,"additionalNotes" : "${$settlementPaymentMadeDialog.querySelector("#settlement-paymend-made-additional-notes").value}"
                    }`;

        settlementPaymentMadeDialog.close();
        el = document.getElementById(userName);
        el.style.opacity=0.4;
        el.style.pointerEvents =  'none';
        el.querySelector(".mdc-linear-progress").classList.remove('mdc-linear-progress--closed');

        showSnackBarMessage("sending channel details to HH Network ...");
        fetch('/settlements/paid', {
            method: 'post',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: postBody
    	}).then(async function(response) {
    		if (response.status===200){
    			showSnackBarMessage("Settlement paid sent");
    			await renderSettlementsPanel(userName);
    			return response.json();
    		}
        }).catch(function(res){
            alert(res);
        });
    };

    settlementPaymentMadeDialog.open();
    return false;
}