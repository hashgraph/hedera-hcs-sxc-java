   function openSettleNowDialog(otherUserId){
        /* Functions for settlement dialog in credit tab */
        settleNowDialog  = new mdc.dialog.MDCDialog(document.getElementById('settle-now-dialog'));
        event.preventDefault();
        styleTextFields();
        styleSwitches();
        $settleNowDialog = document.getElementById('settle-now-dialog');

        $settleNowDialog.querySelector("#settle-now-additional-notes").removeAttribute("disabled");
        $settleNowDialog.querySelector("#settle-now-send").removeAttribute("disabled");
        $settleNowDialog.querySelector(".warning").style.visibility='hidden';

        /*
         * Calculate the net amount.
         */

        var thisUserPays = 0;
        var otherUserPays = 0;
        var payerName = "";
        var recipientName = "";
        var threadIds=[];    
        document.querySelectorAll("#"+otherUserId+" .mdc-data-table__row").forEach(
            r=>{
                 if(r.cells.settlebox.querySelector("input").checked){
                     threadIds.push(r.cells.threadId.title);
                     rowAmount = 1*r.cells.amount.getAttribute("data-amount");
                     if (r.cells.payer.textContent!==otherUserId){
                        thisUserPays += rowAmount ;
                    } else {
                        otherUserPays += rowAmount ; 
                    }
                 }
             }
        );

        payerName = thisUserPays > otherUserPays? thisuserName : otherUserId;
        recipientName = thisUserPays < otherUserPays? thisuserName : otherUserId;
        netValue = Math.abs(thisUserPays - otherUserPays);


        $settleNowDialog.querySelector("#settle-now-thread-ids").value=threadIds;
        $settleNowDialog.querySelector("#settle-now-payer").value= payerName;
        $settleNowDialog.querySelector("#settle-now-dialog #settle-now-recipient").value= recipientName;
        $settleNowDialog.querySelector("#settle-now-dialog #settle-now-net-value").value= netValue;

        clickFunction = function(){
            additionalNotes = $settleNowDialog.querySelector("#settle-now-additional-notes").value;
            //alert(thisUserPays + " " + otherUserPays);
            settleNowDialog.close();

            el = document.getElementById(otherUserId);
            el.style.opacity=0.4;
            el.style.pointerEvents =  'none';
            el.querySelector(".mdc-linear-progress").classList.remove('mdc-linear-progress--closed');

            var automate = (document.getElementById('new-credit-automatic').checked);
            
            postBody = `{
                          "payerName"       : "${payerName}"
                        , "recipientName"   : "${recipientName}"  
                        , "netValue"        : ${netValue}
                        , "currency"        : "USD"
                        , "threadIds"       : ${JSON.stringify(threadIds)}
                        , "additionalNotes" : "${additionalNotes}"
                        , "automatic"       : ${automate}
                        }`;

            showSnackBarMessage("sending new settlement request to HH Network ...");
            fetch('settlements', {
                method: 'post',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                body: postBody
            }).then(function(response) {
                if (response.status===200){
                    showSnackBarMessage("Settlement request sent");
                } else {
                    alert("Failed to send message to HH network");
                }
            }).catch(function(res){
                alert(res);
            });


        };
        if (payerName === thisuserName) {
            $settleNowDialog.querySelector("#settle-now-send").onclick =  clickFunction;
        }   else {
            $settleNowDialog.querySelector("#settle-now-additional-notes").setAttribute("disabled","disabled");
            $settleNowDialog.querySelector("#settle-now-send").setAttribute("disabled","disabled");
            $settleNowDialog.querySelector(".warning").style.visibility='visible';
        }

        settleNowDialog.open();
        return false;
    }