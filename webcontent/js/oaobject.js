
function OAObject() {
}
OAObject.prototype._hubs = null;
OAObject.prototype.properties = [];
// oaKey, etc

OAObject.prototype.firePropertyChange = function(property, oldValue, newValue) {
    if (this._hubs == null) { return; }
    //qqq needs to get sharedHub listeners
    for ( var i = 0; i < this._hubs.length; i++) {
        this._hubs[i].firePropertyChange(this, property, oldValue, newValue);
    }
    //needs to adjust link property, add/remove with Hubs, etc
};

OAObject.prototype.getProperty = function(prop) {
    if (prop == null) return null;
    var props = prop.split(".");
    var obj = this;
    for ( var i = 0; i < props.length; i++) {
        var p = props[i];
        p = p.charAt(0).toUpperCase() + p.substring(1);

        var val = obj["get" + p];
        if (val == undefined) return null;
        if (val == null) return null;
        val = val.call(obj);
        if (val == null) return null;
        if (val == undefined) return null;
        obj = val;
    }
    return obj;
}

OAObject.prototype.getObject = function(prop) {
    return this.properties[prop];
}

OAObject.prototype.getHub = function(prop) {
     this.properties[prop];
     return new Hub();
//    if (h == null) h = new Hub();
//    return h;    
}



 // create a subclass of OAObject
 OA = {};
 OA.derive = function (subclass) {
     function subproto() {}
     subproto.prototype = OAObject.prototype;
     subclass.prototype = new subproto();
     subclass.prototype.constructor = subclass;
 };






