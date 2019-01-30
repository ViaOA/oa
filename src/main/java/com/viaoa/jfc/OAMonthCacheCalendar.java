package com.viaoa.jfc;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingWorker;

import com.viaoa.ds.OASelect;
import com.viaoa.hub.Hub;
import com.viaoa.object.OAFinder;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectCacheFilter;
import com.viaoa.util.OADate;
import com.viaoa.util.OAString;


/**
 * A month calendar that uses objects from cache.
 * @author vvia
 *
 * @param <F>
 */
public class OAMonthCacheCalendar<F extends OAObject> extends OAMonthCalendar {

    protected final ArrayList<OAFinder> alFinder = new ArrayList<>();
    protected OAObjectCacheFilter cacheFilterCalendar; 
    private final AtomicInteger aiSelectCnt = new AtomicInteger(); 
    
    public OAMonthCacheCalendar(Hub<F> hub, String propertyPath, String[] datePropertyPaths) {
        super(hub, propertyPath, datePropertyPaths);
        if (hub.getMasterHub() != null) throw new RuntimeException("can not use a detail hub for cache calendar");
        onNewMonth();
    }
    
    public OAMonthCacheCalendar(Hub<F> hub, String datePropertyPath, Hub hubDetail) {
        super(hub, datePropertyPath, hubDetail);
        setAllowCreateNew(true);
        onNewMonth();
    }
    
    
    @Override
    protected void setup() {
        super.setup();
        
        cacheFilterCalendar = new OAObjectCacheFilter<F>(hub) {
            @Override
            public boolean isUsed(F obj) {
                if (aiSelectCnt == null || aiSelectCnt.get() > 0) return false;
                if (alFinder == null) return false;
                for (OAFinder finder : alFinder) {
                    if (finder.findFirst(obj) != null) {
                        return true;
                    }
                }
                return false;
            }
        };
        for (String pp : datePropertyPaths) {
            cacheFilterCalendar.addDependentProperty(pp, false);
        }
        
    }
    
    
    @Override
    protected void onNewMonth() {
        // create finders for each pp
        if (hub == null || alFinder == null) return;
        hub.clear();
        alFinder.clear();
        OAFinder finder;

        OADate d1  = getBeginDate();
        OADate d2 = getEndDate();
        
        String query = "";
        for (String pp : datePropertyPaths) {
            query = OAString.concat(query, String.format("(%s >= ? AND %s < ?)", pp, pp), " OR ");
            finder = new OAFinder();
            finder.addGreaterOrEqualFilter(pp, d1);
            finder.addLessOrEqualFilter(pp, d2);
            alFinder.add(finder);
        }
        final String q = query;

        final Object[] params = new Object[datePropertyPaths.length * 2];
        for (int i=0; i<datePropertyPaths.length*2; i+=2) {
            params[i] = d1;
            params[i+1] = d2;
        }

        SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
            ArrayList al = new ArrayList();
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    aiSelectCnt.incrementAndGet();
                    OASelect sel = new OASelect(getHub().getObjectClass(), q, params, "");
                    for (; ;) {
                        Object objx = sel.next();
                        if (objx == null) break;
                        al.add(objx);
                    }
                }
                finally {
                    aiSelectCnt.decrementAndGet();
                }
                return null;
            }
            @Override
            protected void done() {
                cacheFilterCalendar.refresh();
                al.clear();
            }
        };
        sw.execute();
    }
}
