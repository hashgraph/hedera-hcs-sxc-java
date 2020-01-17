function openInitPayDialog(threadId, userName) {
    $initPayDialog = document.getElementById('init-pay-dialog');
    initPayDialog  = new mdc.dialog.MDCDialog($initPayDialog);
    event.preventDefault(); 
    styleTextFields();
    $initPayDialog.querySelectorAll(".mdc-text-field input").forEach(function(e){
        e.parentElement.classList.remove('mdc-text-field--invalid');
        e.value='';}
    );
    p = allAddresses.filter(a=>a.name===thisuserName)[0];
    r = allAddresses.filter(a=>a.name===userName)[0];
    payerAccountDetails = p.name + "- SortNo/AccNo: " + p.paymentAccountDetails;
    $initPayDialog.querySelector("#init-pay-payer-details").value = payerAccountDetails;
    recipientAccountDetails = r.name + "- SortNo/AccNo: " + r.paymentAccountDetails;
    $initPayDialog.querySelector("#init-pay-recipient-details").value = recipientAccountDetails;
    $initPayDialog.querySelector("#init-pay-now-send").onclick =  function(){
        postBody = `{
                     "threadId"          : "${threadId}"
                    ,"payerAccountDetails" : "${payerAccountDetails}"
                    ,"recipientAccountDetails" :"${recipientAccountDetails}"
                    ,"additionalNotes" : "${$initPayDialog.querySelector("#init-pay-additional-notes").value}"
                    }`;

        initPayDialog.close();
        el = document.getElementById(userName);
        el.style.opacity=0.4;
        el.style.pointerEvents =  'none';
        el.querySelector(".mdc-linear-progress").classList.remove('mdc-linear-progress--closed');

        showSnackBarMessage("sending channel details to HH Network ...");
        fetch('/settlements/paymentInit', {
            method: 'post',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: postBody
        }).then(function(response) {
            return response.json();
        }).then(function(data) {
            showSnackBarMessage("Payment init sent");

        }).catch(function(res){
            alert(res);
        });
    };

    initPayDialog.open();
    return false;
}
            