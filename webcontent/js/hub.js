
function Hub() {
    this.hubData = new HubData();
    this.hubDataUnique = new HubDataUnique();
    this.hubDataActive = new HubDataActive();
    this.hubDataMaster = new HubDataMaster();
}

function HubData() {
    this.array = [];
}
HubData.prototype.getSize = function() {
    return array.length;
}
function HubDataUnique() {
    this.listeners = null;
}
function HubDataActive() {
    this.activeObject = null;
}
function HubDataMaster() {
}

Hub.prototype.add = function(obj) {
    this.hubData.array.push(obj);
    if (obj._hubs == null) {
        obj._hubs = [];
    }
    obj._hubs.push(this);

    if (this.hubDataUnique.listeners == null) { return; }
    for ( var i = 0; i < this.hubDataUnique.listeners.length; i++) {
        var hl = this.hubDataUnique.listeners[i];
        if (hl.onAdd != undefined) {
            hl.onAdd(this, obj);
        }
    }
}
Hub.prototype.firePropertyChange = function(obj, property, oldValue, newValue) {
    if (this.hubDataUnique.listeners == null) { return; }
    for ( var i = 0; i < this.hubDataUnique.listeners.length; i++) {
        var hl = this.hubDataUnique.listeners[i];
        if (hl.onPropertyChange != undefined) {
            hl.onPropertyChange(this, obj, property, oldValue, newValue);
        }
    }
}
Hub.prototype.addListener = function(hubListener) {
    if (this.hubDataUnique.listeners == null) this.hubDataUnique.listeners = [];
    this.hubDataUnique.listeners.push(hubListener);
};

// qqqqqqqqqqqqqqqqqqq
function HubListener() {
};
HubListener.prototype.onAdd = function(hub, obj) {
};
HubListener.prototype.onPropertyChange = function(hub, object, property, oldValue, newValue) {
};





