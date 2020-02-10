function openSelectPaychannelDialog(threadId, userName) {
    $paychannelDialog = document.getElementById('select-paychannel-dialog');
    paychannelDialog  = new mdc.dialog.MDCDialog($paychannelDialog);
    event.preventDefault(); 
    styleTextFields();
    $paychannelDialog.querySelectorAll(".mdc-text-field input").forEach(function(e){
        e.parentElement.classList.remove('mdc-text-field--invalid');
        e.value='';}
    );
    mdcList =  $paychannelDialog.querySelector(".mdc-list");
    mdcList.innerHTML = "";
    paymentChannels.forEach(c => {
        clonedListItem = $paychannelDialog.querySelector("#mdc-list-item-template").content.cloneNode(true);
        clonedListItem.querySelector("li").innerHTML=c.name;
        clonedListItem.querySelector("li").setAttribute("data-value",c.name);
        mdcList.appendChild(clonedListItem);
    });

    let selectedPaymentChannelName=paymentChannels[0].name;
    //$paychannelDialog.querySelector(".mdc-list").classList.add("mdc-list-item--selected");
    $paychannelDialog.querySelector(".mdc-list").setAttribute("data-value",selectedPaymentChannelName);                
    $paychannelDialog.querySelector(".mdc-select__selected-text").innerHTML=selectedPaymentChannelName;

    const mdcSelect  =  new mdc.select.MDCSelect( $paychannelDialog.querySelector(".mdc-select"));

    mdcSelect.listen('MDCSelect:change', () => {
        selectedPaymentChannelName = mdcSelect.value;
      });
    $paychannelDialog.querySelector("#channel-now-send").onclick =  function(){

        postBody = `{
                     "threadId"          : "${threadId}"
                    , "paymentChannelName" :"${selectedPaymentChannelName}"
                    , "additionalNotes" :"${$paychannelDialog.querySelector('#channel-additional-notes').value}"
                    }`;

        paychannelDialog.close();
        el = document.getElementById(userName);
        el.style.opacity=0.4;
        el.style.pointerEvents =  'none';
        el.querySelector(".mdc-linear-progress").classList.remove('mdc-linear-progress--closed');

        showSnackBarMessage("sending channel details to HH Network ...");
        fetch('/settlements/proposechannel', {
            method: 'post',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: postBody
    	}).then(async function(response) {
    		if (response.status===200){
    			showSnackBarMessage("New channel proposal sent");
    			await renderSettlementsPanel(userName);
    			return response.json();
    		}
        }).catch(function(res){
            alert(res);
        });
    };

    paychannelDialog.open();
    return false;
}