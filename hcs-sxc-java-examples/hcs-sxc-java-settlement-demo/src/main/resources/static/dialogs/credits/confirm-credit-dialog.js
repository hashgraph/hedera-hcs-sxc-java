function openConfirmCreditDialog(threadId,otherUserId) {
                event.preventDefault();
                styleTextFields();
                confirmCreditDialog  = new mdc.dialog.MDCDialog(document.getElementById('confirm-credit-dialog'));

                $confirmCreditDialog = document.getElementById('confirm-credit-dialog');
                $confirmCreditDialog.querySelector("#thread-id").value=threadId;
                clickFunction = function(){
                    $confirmCreditDialog = document.getElementById('confirm-credit-dialog');
                    threadId = $confirmCreditDialog.querySelector("#thread-id").value;
                    showSnackBarMessage("sending confirmation " + threadId + " ...");
                    confirmCreditDialog.close();

                    el = document.getElementById(otherUserId);
                    el.style.opacity=0.4;
                    el.style.pointerEvents =  'none';
                    el.querySelector(".mdc-linear-progress").classList.remove('mdc-linear-progress--closed');

                    fetch('credits/ack/'+threadId, {
                            method: 'post',
                            headers: {
                                'Accept': 'application/json',
                                'Content-Type': 'application/json'
                            }
                    	}).then(async function(response) {
                    		if (response.status===200){
                                showSnackBarMessage("Confirmation sent");
                    			await renderCreditsPanel(otherUserId);
                    		}
                        }).catch(function(res){
                            alert(res);
                        });
        
                };
                $confirmCreditDialog.querySelector("#confirm-credit-send").onclick = clickFunction;
                confirmCreditDialog.open();
                return false;
            }