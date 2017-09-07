package com.viaoa.jsp;

import com.viaoa.hub.Hub;
import com.viaoa.jfc.report.OAHTMLConverter;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAProperties;

/**
 * 
 * Template builder that uses special tags to insert and build an output html template:
 *
 * @see OAHTMLConverter# for more details.
 */
public class OATemplate<F extends OAObject> extends OAHTMLConverter {


    public void setTemplate(String template) {
        setHtmlTemplate(template);
    }
    public String getTemplate() {
        return getHtmlTemplate();
    }
    
    public String process(F objRoot) {
        String s = process(objRoot, null, null);
        return s;
    }
    public String process(F objRoot, OAProperties props) {
        String s = process(objRoot, null, props);
        return s;
    }

    public String process(Hub<F> hub, OAProperties props) {
        String s = getHtml(null, hub, props);
        return s;
    }
    public String process(Hub<F> hub) {
        String s = getHtml(null, hub, null);
        return s;
    }
    
    public String process(F objRoot, Hub hub, OAProperties props) {
        String s = getHtml(objRoot, hub, props);
        return s;
    }
}

