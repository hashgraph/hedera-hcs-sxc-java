function openSettlementCompletedDialog(threadId, userName) {
    $settlementCompletedDialog = document.getElementById('settlement-completed-dialog');
    settlementCompletedDialog  = new mdc.dialog.MDCDialog($settlementCompletedDialog);
    event.preventDefault(); 
    styleTextFields();
    $settlementCompletedDialog.querySelectorAll(".mdc-text-field input").forEach(function(e){
        e.parentElement.classList.remove('mdc-text-field--invalid');
        e.value='';}
    );
    $settlementCompletedDialog.querySelector("#thread-id").value=threadId;
    $settlementCompletedDialog.querySelector("#settlement-completed-send").onclick =  function(){
        postBody = `{
                     "threadId"        : "${threadId}"
                    ,"additionalNotes" : "${$settlementCompletedDialog.querySelector("#settlement-completed-additional-notes").value}"
                    }`;

        settlementCompletedDialog.close();
        el = document.getElementById(userName);
        el.style.opacity=0.4;
        el.style.pointerEvents =  'none';
        el.querySelector(".mdc-linear-progress").classList.remove('mdc-linear-progress--closed');

        showSnackBarMessage("sending settlement completed to HH Network ...");
        fetch('/settlements/complete', {
            method: 'post',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: postBody
    	}).then(async function(response) {
    		if (response.status===200){
                showSnackBarMessage("Settlement paid Sent");
    			await renderSettlementsPanel(userName);
    			return response.json();
    		}
        }).catch(function(res){
            alert(res);
        });
    };

    settlementCompletedDialog.open();
    return false;
}



