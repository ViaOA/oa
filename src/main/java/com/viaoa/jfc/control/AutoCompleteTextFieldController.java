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
package com.viaoa.jfc.control;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.*;
import com.viaoa.hub.*;
import com.viaoa.jfc.text.autocomplete.AutoCompleteList;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAString;
import com.viaoa.util.OATemplate;

/**
 * Controller OAAutoComplete.
 * @author vvia
 *
 */
public class AutoCompleteTextFieldController extends OAJfcController {
    private final JTextField txt;
    
    protected String searchTemplate;
    private OATemplate templateSearch;
    
    private AutoCompleteList autoCompleteList;
    private int maxResults = 35;
    
    public AutoCompleteTextFieldController(Hub hub, JTextField txt, String propertyPath) {
        super(hub, null, propertyPath, txt, HubChangeListener.Type.HubNotEmpty, true, true);
        this.txt = txt;
        init();
    }

    public AutoCompleteTextFieldController(Hub hub, JTextField txt) { 
        super(hub, null, null, txt, HubChangeListener.Type.HubNotEmpty, false, true);
        this.txt = txt;
        init();
    }

    protected void init() {
        getAutoCompleteList();
    }

    public void setMaxResults(int x) {
        this.maxResults = x;
    }
    public int getMaxResults() {
        return this.maxResults;
    }
    
    
    public Component getTableRenderer(JLabel label, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component comp = super.getTableRenderer(label, table, value, isSelected, hasFocus, row, column);
        return comp;
    }
    

    @Override
    public void update() {
        try {
            _update(); 
        }
        finally {
        }
        super.update();
    }
    
    protected void _update() {
        if (txt == null) return;

        // only update the txt.text if the hub is link. 
        String text = null;
        if (!OAString.isEmpty(getPropertyPath()) && hub != null && hub.getLinkHub() != null) {
            Object obj = hub.getAO();
            if (obj != null || bIsHubCalc) {
                text = getValueAsString(obj, getFormat());
            }
            if (text == null) {
                text = getNullDescription();
                if (text == null) text = "";
            }
            txt.setText(text);
        }
        
    }
    
    /**
     * Template used to match/find text for an object.
     */
    public void setSearchTemplate(String s) {
        this.searchTemplate = s;
        templateSearch = null;
    }
    public String getSearchTemplate() {
        return searchTemplate;
    }
    public OATemplate getTemplateForSearch() {
        if (OAString.isNotEmpty(getSearchTemplate())) {
            if (templateSearch == null) templateSearch = new OATemplate<>(getSearchTemplate());
        }
        return templateSearch;
    }

    
    public AutoCompleteList getAutoCompleteList() {
        if (autoCompleteList != null) return autoCompleteList; 
        final JList jlist = new JList();
    
        autoCompleteList = new AutoCompleteList(this.txt, jlist, true) {  // true=exact match only
            ArrayList<TreeSearchItem> alList = new ArrayList<TreeSearchItem>();
            
            // data structure used for search data
            class TreeSearchItem implements Comparable {
                Object obj;
                String searchValue;
                String displayValue;
                String toolTip;
                public TreeSearchItem(String searchValue, String displayValue, Object obj) {
                    this.searchValue = searchValue;
                    this.displayValue = displayValue;
                    this.obj = obj;
                }
                
                @Override
                public int compareTo(Object o) {
                    if (searchValue != null && o instanceof TreeSearchItem) {
                        return this.searchValue.compareTo( ((TreeSearchItem)o).searchValue);
                    }
                    return -1;
                }
            }
            
            private AtomicInteger aiCnt = new AtomicInteger();
            private void _search(Hub h, String text, final int cnt) {
                for (Object obj : h) {
                    if (cnt != aiCnt.get()) break;
                    String searchValue;
                    
                    if (obj instanceof OAObject) {
                        if (getTemplateForSearch() != null) searchValue = getTemplateForSearch().process((OAObject) obj);
                        else if (getPropertyPath() != null) searchValue = ((OAObject)obj).getPropertyAsString(getPropertyPath());
                        else searchValue = obj.toString();
                    }
                    else searchValue = obj.toString();

                    int pos = searchValue.toUpperCase().indexOf(text);
                    if (pos < 0) continue;
                    
                    String displayValue;
                    
                    if (!(obj instanceof OAObject) || getTemplateForDisplay()==null) displayValue = searchValue;
                    else {
                        getTemplateForDisplay().setHiliteOutputText(text);
                        displayValue = getTemplateForDisplay().process((OAObject)obj);
                    }
                    if (displayValue != null && displayValue.toLowerCase().indexOf("html") < 0) displayValue = "<html>" + displayValue;
                    TreeSearchItem tsi = new TreeSearchItem(searchValue, displayValue, obj);
                    alList.add(tsi);
                    if (alList.size() == maxResults) break;
                }
            }
     
            @Override
            protected String[] getSearchData(String text, int offset) {
                final int cnt = aiCnt.incrementAndGet();
                alList.clear();
                
                if (offset == 0) return null;
                if (text == null || text.length() < 1) return null;
                
                if (offset < text.length()) {
                    text = text.substring(0, offset);  // cursor could be in middle of text
                }
                
                text = text.toUpperCase();
                _search(getHub(), text, cnt);
                if (cnt != aiCnt.get()) return null;
                Collections.sort(alList);
                int x = alList.size();
                boolean bMaxed = (x >= maxResults);
                String[] ss = new String[x+(bMaxed?1:0) ];
                for (int i=0; i<x; i++) {
                    TreeSearchItem tsi = alList.get(i);
                    ss[i] = tsi.displayValue;
                }
                if (alList.size() >= maxResults) ss[x] = "<html><i>Limited to "+x+" max results</i>";
                return ss;
            }
            @Override
            protected String getClosestMatch(String text) {
                return text;
            }
            
            @Override
            protected String getTextForSelectedValue(int pos, String value) {
                if (pos < 0 || pos >= alList.size()) return value;
                TreeSearchItem tsi = alList.get(pos);
                if (tsi == null) return value;
                return tsi.searchValue;
            }
            @Override
            protected void onValueSelected(int pos, String value) {
                String s = isValidHubChangeAO(hub.getAt(pos));
                if (OAString.isNotEmpty(s)) {
                    JOptionPane.showMessageDialog(AutoCompleteTextFieldController.this.txt, s, "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                else if (!confirmHubChangeAO(hub.getAt(pos))) {
                    return;
                }
                
                if (pos < 0) {
                    getHub().setAO(null);
                }
                else if (pos >= 0 && pos < alList.size()) {
                    TreeSearchItem tsi = alList.get(pos);
                    getHub().setAO(tsi.obj);
                    super.onValueSelected(pos, value);
                }
            }
            @Override
            protected String getToolTipText(int pos) {
                if (pos < 0 || pos >= alList.size()) return null;
                TreeSearchItem tsi = alList.get(pos);
                if (tsi == null) return null;
                if (OAString.isEmpty(tsi.toolTip)) {
                    if (getTemplateForToolTipText() == null) return tsi.displayValue;
                    if (tsi.obj instanceof OAObject) tsi.toolTip = getTemplateForToolTipText().process((OAObject) tsi.obj);
                    if (tsi.toolTip != null && tsi.toolTip.toLowerCase().indexOf("html") < 0) tsi.toolTip = "<html>" + tsi.toolTip;
                }
                return tsi.toolTip;
            }
        };
        autoCompleteList.setShowOne(true);        
        return autoCompleteList;
    }

}
