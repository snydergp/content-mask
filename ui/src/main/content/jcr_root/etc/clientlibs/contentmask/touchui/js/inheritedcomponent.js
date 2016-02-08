var contentmask = window.contentmask || {};
contentmask.inheritedcomponent = {

    initTimeout: null,
    editableMap : {},
    childMap : {},

    setTarget : function(editable) {
        var editable = contentmask.inheritedcomponent.editableMap[editable.config.csp];
        Granite.author.DialogFrame.openDialog(editable);
    },

    editInherited : function(editable) {
        var target = contentmask.inheritedcomponent.childMap[editable.config.csp][0];
        var child = contentmask.inheritedcomponent.editableMap[target.config.csp];
        Granite.author.DialogFrame.openDialog(child);
    },

    revert : function(editable) {
        var target = contentmask.inheritedcomponent.childMap[editable.config.csp][0];
        var data = {
            ":operation": "revert-mask"
        };
        $.ajax(target.path, {
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
    },

    initInspectable : function(inspectable) {

        var csp = inspectable.config.csp;
        this.editableMap[csp] = inspectable;
        this.childMap[csp] = [];
    },

    init : function() {

        for (var property in this.editableMap) {
            if (this.editableMap.hasOwnProperty(property)) {
                var array = property.split('/');
                for (var i = 0; i < array.length; i++) {
                    var parentCsp = array.slice(0, i).join('/');
                    var parent = this.editableMap[parentCsp];
                    if (parent) {
                        var children = this.childMap[parentCsp];
                        children.push(this.editableMap[property]);
                    }
                }
            }
        }

    }

};

$(function() {

    $(document).on("cq-inspectable-added", function(event) {
        if (contentmask.inheritedcomponent.initTimeout) {
            clearTimeout(contentmask.inheritedcomponent.initTimeout)
        }

        contentmask.inheritedcomponent.initInspectable(event.inspectable);

        contentmask.inheritedcomponent.initTimeout = setTimeout(function(){contentmask.inheritedcomponent.init()}, 100);
    });

});
