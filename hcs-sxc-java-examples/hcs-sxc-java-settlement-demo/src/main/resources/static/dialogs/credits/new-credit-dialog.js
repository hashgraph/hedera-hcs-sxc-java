function openNewCreditDialog(recipient) {


    /* Functions for the new credit dialog. () */
    newCreditDialog  = new mdc.dialog.MDCDialog(document.getElementById('new-credit-dialog'));
    event.preventDefault();
    styleTextFields();
    styleSwitches();
    $newCreditDialog = document.getElementById('new-credit-dialog');
    /*Clear styles and values to reuse fields*/
    $newCreditDialog.querySelectorAll(".mdc-text-field input").forEach(function(e){
        e.parentElement.classList.remove('mdc-text-field--invalid');
        e.value='';}
    );

    $newCreditDialog.querySelector("#new-credit-recipient").value = recipient;
    $newCreditDialog.querySelector("#send-new-credit-dialog-submit").onclick =  function(){
            $newCreditDialog = document.getElementById('new-credit-dialog');
            /* validate form */
            isFormValid = true;
            ls = $newCreditDialog.querySelectorAll(".mdc-text-field input");

            for (i=0; i< ls.length; i++ ){
                 if (! ls[i].checkValidity()){ls[i].focus();isFormValid=false;break;  }

            }
            var automate = (document.getElementById('new-credit-automatic').value === "on");
            automate = true;
            if(isFormValid){
                postBody = `{
                              "payerName"       : "${thisuserName}"
                            , "recipientName"   : "${document.getElementById('new-credit-recipient').value}"  
                            , "reference"       : "${document.getElementById('new-credit-service-reference').value}"
                            , "amount"          : ${document.getElementById('new-credit-amount').value * 1}
                            , "currency"        : "USD"
                            , "additionalNotes" :"${document.getElementById('new-credit-additional-notes').value}"
                            , "automatic"       : ${automate}
                            }`;

                newCreditDialog.close();
                el = document.getElementById(recipient);
                el.style.opacity=0.4;
                el.style.pointerEvents =  'none';
                el.querySelector(".mdc-linear-progress").classList.remove('mdc-linear-progress--closed');

                showSnackBarMessage("sending new credit to HH Network ...");
                fetch('credits', {
                    method: 'post',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },
                    body: postBody
                }).then(function(response) {
                    return response.json();
                }).then(function(data) {
                    showSnackBarMessage("New credit request sent");

                }).catch(function(res){
                    alert(res);
                });
            }      

    };

    newCreditDialog.open();
    return false;
}
       