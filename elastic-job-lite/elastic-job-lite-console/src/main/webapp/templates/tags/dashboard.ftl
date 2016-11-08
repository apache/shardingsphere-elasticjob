<#macro confirmDialog id text>
    <div id="${id}" class="modal fade">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title">确认框</h4>
                </div>
                <div class="modal-body">
                    <p>${text}</p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
                    <button id="${id}-confirm-btn" type="button" class="btn btn-danger">确认</button>
                </div>
            </div>
        </div>
    </div>
</#macro>

<#macro successDialog id>
    <div id="${id}" class="modal fade">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title">信息框</h4>
                </div>
                <div class="modal-body">
                    <p>操作已成功完成</p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-success" data-dismiss="modal">关闭</button>
                </div>
            </div>
        </div>
    </div>
</#macro>

<#macro failureDialog id reason>
    <div id="${id}" class="modal fade">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title">信息框</h4>
                </div>
                <div class="modal-body">
                    <p>操作未成功，原因：${reason}</p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-danger" data-dismiss="modal">关闭</button>
                </div>
            </div>
        </div>
    </div>
</#macro>
