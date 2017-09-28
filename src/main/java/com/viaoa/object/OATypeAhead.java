/* Copyright 1999-2017 Vince Via vvia@viaoa.com Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed
 * to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License. */
package com.viaoa.object;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

import com.viaoa.hub.*;
import com.viaoa.model.oa.VString;
import com.viaoa.util.*;

/**
 * Bootstrap TypeAhead support, used by TextField and MultiValueTextField to do searches.
 * 
 * For styling, see oajsp.css
 * 
 */
public class OATypeAhead<F extends OAObject,T extends OAObject> {
    private static final long serialVersionUID = 1L;

    private static Logger LOG = Logger.getLogger(OATypeAhead.class.getName());

    // base hub
    protected Hub<F> hub;
    protected ArrayList<T> alTo;    
    /**
     *  pp from objects <F> to to objects <T>
     *  this is not used/needed if finder is not null.
     */
    protected String finderPropertyPath;
    protected OAPropertyPath ppFinder;
    
    /** 
     * property in <T> to match with search text.
     */
    protected String matchPropertyPath;
    protected OAPropertyPath ppMatch;

    /**
     * pp from <T> used for display value
     */
    protected String displayPropertyPath;
    protected OAPropertyPath ppDisplay;
    protected String displayFormat;    
    
    /**
     * pp from <T> used for sorting the matched objects.
     */
    protected String sortValuePropertyPath; 
    protected OAPropertyPath ppSortValue;
    protected String sortValueFormat;    

    /**
     * pp from <T> used for dropdown display
     */
    protected String dropDownDisplayPropertyPath;
    protected OAPropertyPath ppDropDownDisplay;
    protected String dropDownDisplayFormat;

    /**
     * additional custom finder for filtering <T> objects
     */
    protected OAFilter<T> filter;
    
    /**
     * To class <T> using finderPropertyPath
     */
    private Class<T> classTo;
    
    /**
     * used to get from hub<F>.activeObj to hub<T>
     */
    protected OAFinder<F,T> finder;

    protected String searchText;
    protected String[] searchTextSplit;
    
    protected int minInputLength = -1;
    protected int maxResults;
    protected boolean showHint=false;
    
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final AtomicInteger aiSearch = new AtomicInteger(); 
    private final HashSet<Integer> hsGuid = new HashSet<>();
    
    
    /**
     * Create a new TA using an array of strings, and update a String property.
     * Example:  to update an address.country from a list of countries.
     * String[] countries = ...
     * OATypeAhead ta = OATypeAhead.createTypeAhead(countries, hubAddress, AddressPP.country());
     * 
     * Note: this is treated as a freeform, where user can enter a value that is not in the list.
     * 
     * @param values array of values to choose from
     * @param hubTo hub where AO.propertyName will be updated with selected value from values.
     * @param propertyName name of property that is to be updated.
     */
    public static OATypeAhead createTypeAhead(String[] values) {
        if (values == null) values = new String[0];
        Hub<VString> hub = new Hub<>(VString.class);
        for (String s : values) {
            hub.add(new VString(s));
        }
        //hub.sort(VString.P_Value);
        
        OATypeAheadParams tap = new OATypeAheadParams();
        tap.matchPropertyPath = VString.P_Value;
        OATypeAhead<VString, VString> ta = new OATypeAhead<>(hub, tap);
        return ta;
    }
    
    
    /**
     * Helper class to enter all of the params.
     * @author vvia
     */
    public static class OATypeAheadParams<F extends OAObject,T extends OAObject> {
        public String finderPropertyPath;
        
        public String matchPropertyPath;
        
        public String displayPropertyPath; 
        public String displayFormat;    
        
        public String sortValuePropertyPath; 
        public String sortValueFormat;    
        
        public String dropDownDisplayPropertyPath;
        public String dropDownDisplayFormat;    

        public OAFilter<T> filter;
        
        public int minInputLength = -1;
        public int maxResults;
        
        /** flag to have TA show the full value on the textfield */
        public boolean showHint=false;
        
        void setup() {
            if (OAString.isEmpty(displayPropertyPath)) {
                displayPropertyPath = dropDownDisplayPropertyPath;
                displayFormat = dropDownDisplayFormat;
                if (OAString.isEmpty(displayPropertyPath)) {
                    displayPropertyPath = matchPropertyPath;
                    displayFormat = null;
                }                
            }
            if (OAString.isEmpty(dropDownDisplayPropertyPath)) {
                dropDownDisplayPropertyPath = displayPropertyPath;
                dropDownDisplayFormat = displayFormat;
            }
        }
    }
    

    /**
     * 
     * 
     */
    public OATypeAhead(ArrayList<T> arrayToUse) {
        alTo = arrayToUse;
    }
    

    /**
     * @param hub root hub used for searches, if there is a finderPropertyPath, then only the activeObject is used.
     * @param params
     */
    public OATypeAhead(Hub<F> hub, OATypeAheadParams params) {
        if (hub == null) throw new IllegalArgumentException("hub can not be null");
        this.hub = hub;
        if (params == null) throw new IllegalArgumentException("params can not be null");
        setup(params);
    }
    
    protected void setup(OATypeAheadParams params) {
        if (params == null) return;
        params.setup();
        
        this.finderPropertyPath = params.finderPropertyPath;
        classTo = (Class<T>) hub.getObjectClass();
        if (OAString.isNotEmpty(finderPropertyPath)) {
            ppFinder = new OAPropertyPath<F>(hub.getObjectClass(), finderPropertyPath);
            OALinkInfo[] lis = ppFinder.getLinkInfos();
            if (lis != null && lis.length > 0) {
                classTo = lis[lis.length-1].getToClass();
            }
        }

        this.minInputLength = params.minInputLength;
        this.maxResults = params.maxResults;
        this.showHint = params.showHint;
        
        if (ppFinder != null) {
            finder = new OAFinder<F,T>(this.finderPropertyPath) {
                @Override
                protected boolean isUsed(T obj) {
                    if (filter != null) {
                        if (!filter.isUsed(obj)) return false;
                    }
                    return OATypeAhead.this.isUsed(obj);
                }
            };
            finder.setMaxFound(params.maxResults);
        }
        
        
        this.matchPropertyPath = params.matchPropertyPath;
        if (OAString.isNotEmpty(matchPropertyPath)) {
            ppMatch = new OAPropertyPath<T>(classTo, matchPropertyPath);
        }

        this.displayPropertyPath = params.displayPropertyPath;
        this.displayFormat = params.displayFormat;
        if (OAString.isNotEmpty(displayPropertyPath)) {
            ppDisplay = new OAPropertyPath<T>(classTo, displayPropertyPath);
        }
        
        this.sortValuePropertyPath = params.sortValuePropertyPath;
        this.sortValueFormat = params.sortValueFormat;
        if (OAString.isNotEmpty(sortValuePropertyPath)) {
            ppSortValue = new OAPropertyPath<T>(classTo, sortValuePropertyPath);
        }

        this.dropDownDisplayPropertyPath = params.dropDownDisplayPropertyPath;
        this.dropDownDisplayFormat = params.dropDownDisplayFormat;
        if (OAString.isNotEmpty(dropDownDisplayPropertyPath)) {
            ppDropDownDisplay = new OAPropertyPath<T>(classTo, dropDownDisplayPropertyPath);
        }

        this.filter = params.filter;
    }

    public String getSearchText() {
        return this.searchText;
    }

    
    public ArrayList<T> search(String searchText) {
        this.searchText = searchText;
        try {
            final int cntSearch = aiSearch.incrementAndGet();
            if (finder != null) finder.stop();
            rwLock.writeLock().lock();
            hsGuid.clear();
            return _search(searchText, cntSearch);
        }
        finally {
            hsGuid.clear();
            rwLock.writeLock().unlock();
        }
    }
 
    
    protected ArrayList<T> _search(String searchText, final int cntSearch) {
        if (cntSearch != aiSearch.get()) return null;
        if (searchText == null) {
            searchTextSplit = null;            
        }
        else {
            String s = searchText.trim().toUpperCase();
            searchTextSplit = s.split(" ");
        }
        
        ArrayList<T> alToFound;
        
        if (finder == null) {
            alToFound = new ArrayList<T>();
            if (hub != null) {
                for (T obj : ((Hub<T>)hub)) {
                    if (cntSearch != aiSearch.get()) return null;
                    if (isUsed(obj)) {
                        alToFound.add(obj);
                        if (maxResults > 0 && alToFound.size() >= maxResults) break;
                    }
                }
            }
            else if (alTo != null) {
                for (T obj : alTo) {
                    if (cntSearch != aiSearch.get()) return null;
                    if (isUsed(obj)) {
                        alToFound.add(obj);
                        if (maxResults > 0 && alToFound.size() >= maxResults) break;
                    }
                }
            }
        }
        else {
            OAObject objFrom = hub.getAO();
            if (objFrom == null) return null;
            alToFound = finder.find(((F)objFrom));
        }
        
        if (cntSearch != aiSearch.get()) return null;
        // sort     
        if (ppSortValue != null) {
            Collections.sort(alToFound, new Comparator<T>() {
                @Override
                public int compare(T o1, T o2) {
                    String s1 = OATypeAhead.this.getSortValue(o1);
                    String s2 = OATypeAhead.this.getSortValue(o2);
                    
                    int x = OAString.compare(s1, s2);
                    return x;
                }
            });
        }
        return alToFound;
    }

    /**
     * minimum numbers of input chars before doing a search.
     * 0 allows to show all.  -1 is default.
     */
    public int getMinimumInputLength() {
        return minInputLength;
    }
    public void setMinimumInputLength(int x) {
        this.minInputLength = x;
    }

    /**
     * Maximum amount of results that will be returned at one time.
     */
    public int getMaxResults() {
        return maxResults;
    }
    public void setMaxResults(int x) {
        this.maxResults = x;
    }
    
    public void setShowHint(boolean b) {
        this.showHint = true;
    }
    public boolean getShowHint() {
        return this.showHint;
    }
    

   
    /** callback during search */
    protected boolean isUsed(T obj) {
        boolean b = _isUsed(obj);
        if (b) {
            if (hsGuid.contains(obj.getGuid())) b = false;
            else hsGuid.add(obj.getGuid());
        }
        return b;
    }

    /** callback during search to get the value to use for matching */
    protected String getMatchValue(T obj) {
        Object objCompare;
        if (ppMatch != null) {
            objCompare = ppMatch.getValue(obj);
        }
        else objCompare = obj;
        
        String str = OAConv.toString(objCompare);
        return str;
    }
        
    protected boolean _isUsed(T obj) {
        String str = getMatchValue(obj); 
        boolean b = isUsed(obj, str, getSearchText(), searchTextSplit);
        return b;
    }
    
    protected boolean isUsed(T obj, String objSearchValue, String searchText, String[] searchTextSplit) {
        // searchText is included in case this method is overwritten
        if (objSearchValue != null) objSearchValue = objSearchValue.toUpperCase();
        
        if (searchTextSplit == null || searchTextSplit.length == 0) {
            return true; // OAString.isEmpty(objSearchValue);
        }
        if (OAString.isEmpty(objSearchValue)) return false;
        
        for (String s : searchTextSplit) {
            if (objSearchValue.indexOf(s.toUpperCase()) < 0) return false;
        }
        return true;
    }
    
    
    /** callback during search to get the display value of a selected <T> object. */
    public String getDisplayValue(T obj) {
        String s;
        if (ppDisplay != null) {
            s = ppDisplay.getValueAsString(null, obj, displayFormat);
        }
        else {
            s = OAConverter.toString(obj, displayFormat);
        }
        return s;
    }
    /** callback during search to get the dropdown display value of matching <T> objects.*/
    public String getDropDownDisplayValue(T obj) {
        String s;
        if (ppDropDownDisplay != null) {
            s = ppDropDownDisplay.getValueAsString(null, obj, dropDownDisplayFormat);
        }
        else {
            s = OAConverter.toString(obj, dropDownDisplayFormat);
        }
        return s;
    }
    /** callback during search to get the sort value of matching <T> objects. */
    public String getSortValue(T obj) {
        String s;
        if (ppSortValue != null) {
            s = ppSortValue.getValueAsString(null, obj, sortValueFormat);
        }
        else {
            s = OAConverter.toString(obj, sortValueFormat);
        }
        if (s != null) s = s.toUpperCase();
        return s;
    }

    public Class getToClass() {
        return classTo;
    }

    public Hub getHub() {
        return hub;
    }
}

