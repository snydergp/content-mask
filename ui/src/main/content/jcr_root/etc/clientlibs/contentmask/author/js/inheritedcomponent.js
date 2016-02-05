contentmask.inheritedcomponent = {

    setTarget : function() {
        var editable = CQ.WCM.getEditable(this.path);
        CQ.wcm.EditBase.showDialog(editable, CQ.wcm.EditBase.EDIT);
    },

    editInherited : function() {
        var targetPath = CQ.WCM.getNestedEditables(this.path)[0];
        var editable = CQ.WCM.getEditable(targetPath);
        CQ.wcm.EditBase.showDialog(editable, CQ.wcm.EditBase.EDIT);
    },

    revert : function() {
        var targetPath = CQ.WCM.getNestedEditables(this.path)[0];
        var data = {
            ":operation": "revert-mask"
        };
        $.ajax(targetPath, {
            'data': data,
            'method': 'POST',
            'success': function() {location.reload()}
        });
    },

    hideInheritedEditables : function() {
        $('.js-inherited-component').each(function(index, e) {
            var targetPath = $(e).attr('data-inherited-component');
            if (targetPath) {
                CQ.WCM.toggleEditables(false, targetPath);

            }
        });
    }

};

CQ.WCM.on('editablesready', function() {

    contentmask.inheritedcomponent.hideInheritedEditables();

});