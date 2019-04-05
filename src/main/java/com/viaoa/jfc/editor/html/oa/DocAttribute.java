/*  Copyright 1999-2015 Vince Via vvia@viaoa.com
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.viaoa.jfc.editor.html.oa;

import com.viaoa.annotation.OAClass;
import com.viaoa.object.*;


/**
 * OAObject used for capturing html attributes from html document.
 * @author vvia
 *
 */
@OAClass(addToCache=false, initialize=false, useDataSource=false, localOnly=true)
public class DocAttribute extends OAObject {
    private static final long serialVersionUID = 1L;
    protected String id;
	protected String name;
	
	protected DocElement docElement;

	
    public String getId() {
        return id;
    }
    
    public void setId(String newId) {
        String old = id;
        this.id = newId;
        firePropertyChange("id", old, id);
    }
	
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
        String old = this.name;
        this.name = name;
        firePropertyChange("name", old, name);
	}

	public DocElement getDocElement() {
		if (docElement == null) docElement = (DocElement) getObject("docElement");
		return docElement;
	}
    public void setDocElement(DocElement newValue) {
        DocElement old = this.docElement;
        this.docElement = newValue;
        firePropertyChange("docElement", old, this.docElement);
    }
	
	
	
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
        
        // OALinkInfo(property, toClass, ONE/MANY, cascadeSave, cascadeDelete, reverseProperty, allowDelete, owner)
        oaObjectInfo.addLink(new OALinkInfo("docElement", DocElement.class, OALinkInfo.ONE, false, false, "docAttributes"));
        
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
        
        oaObjectInfo.addRequired("id");
    }


}
