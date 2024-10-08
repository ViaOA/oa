// Generated by OABuilder
package test.hifive.model.oa.propertypath;
 
import java.io.Serializable;

import test.hifive.model.oa.*;
 
public class ProgramPageInfoPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
    private ImageStorePPx imageStore;
    private PageInfoPPx pageInfo;
    private ProgramPPx program;
    private ProgramDocumentPPx programDocument;
     
    public ProgramPageInfoPPx(String name) {
        this(null, name);
    }

    public ProgramPageInfoPPx(PPxInterface parent, String name) {
        String s = null;
        if (parent != null) {
            s = parent.toString();
        }
        if (s == null) s = "";
        if (name != null) {
            if (s.length() > 0) s += ".";
            s += name;
        }
        pp = s;
    }

    public ImageStorePPx imageStore() {
        if (imageStore == null) imageStore = new ImageStorePPx(this, ProgramPageInfo.P_ImageStore);
        return imageStore;
    }

    public PageInfoPPx pageInfo() {
        if (pageInfo == null) pageInfo = new PageInfoPPx(this, ProgramPageInfo.P_PageInfo);
        return pageInfo;
    }

    public ProgramPPx program() {
        if (program == null) program = new ProgramPPx(this, ProgramPageInfo.P_Program);
        return program;
    }

    public ProgramDocumentPPx programDocument() {
        if (programDocument == null) programDocument = new ProgramDocumentPPx(this, ProgramPageInfo.P_ProgramDocument);
        return programDocument;
    }

    public String id() {
        return pp + "." + ProgramPageInfo.P_Id;
    }

    public String created() {
        return pp + "." + ProgramPageInfo.P_Created;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
