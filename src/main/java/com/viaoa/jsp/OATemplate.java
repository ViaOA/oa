package com.viaoa.jsp;

import com.viaoa.hub.Hub;
import com.viaoa.jfc.report.OAHTMLConverter;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAProperties;

/**
 * 
 * Template builder that uses special "<%=name%> tags to insert and build an output text, using the following in the tags:
 * 1: OAObject/Hub properties and property paths to fill out tags
 * 2: name/values, where the tag name will use '$' for the name prefix, ex: '$myname' 
 * 3: callback
 * 
 *  
 * uses OAHTMLConverter, which includes special tags for processing (ex: looping) and conditional checks.
 * 
 * tags are in the form:
 * <%=xxx%> where 'xxx' is property, property path, or prefixed with '$'
 * if the tag uses '$' prefix, then the value will be retrieved from get the setProperty values, otherwise it will use the property path.
 * 
 * The callback method getValue(..) will be called to allow for overwriting the return value of a tag.
 * 
 *
 * example tags 
    <%=$HEADING%>
    <%=name%>
    <%=fullName%>
    <%=phone, '(###) ### ####'%>
    <%=manager.department.region.name%>
 
 *
 * note: the following tags will be included automatically
 * 
 * $datetime
 * $date
 * $time
 *
 * @author vvia
 *
 * @see OAHTMLConverter# for more details.
 */
public class OATemplate extends OAHTMLConverter {


    public void setTemplate(String template) {
        setHtmlTemplate(template);
    }
    public String getTemplate() {
        return getHtmlTemplate();
    }
    
    public String process(OAObject objRoot) {
        String s = process(objRoot, null, null);
        return s;
    }
    public String process(OAObject objRoot, OAProperties props) {
        String s = process(objRoot, null, props);
        return s;
    }

    public String process(Hub hub, OAProperties props) {
        String s = getHtml(null, hub, props);
        return s;
    }
    public String process(Hub hub) {
        String s = getHtml(null, hub, null);
        return s;
    }
    

    public String process(OAObject objRoot, Hub hub, OAProperties props) {
        String s = getHtml(objRoot, hub, props);
        return s;
    }
    

}


