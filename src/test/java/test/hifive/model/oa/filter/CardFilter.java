// Generated by OABuilder
package test.hifive.model.oa.filter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import test.hifive.model.oa.*;

import java.util.*;

@OAClass(addToCache=false, initialize=true, useDataSource=false, localOnly=true)
public class CardFilter extends OAObject {
    private static final long serialVersionUID = 1L;

    public static final String PROPERTY_DigitalCard = "DigitalCard";
    public static final String PROPERTY_DigitalCardUseNull = "DigitalCardUseNull";
    protected boolean digitalCard;
    protected boolean digitalCardUseNull;
    protected transient Hub<CardFilter> hub;

    public CardFilter() {
        getHub();
    }

    private int changingCnt;;
    private int changeCnt;
    private int holdChangeCnt;
    private void setChanging(boolean b) {
        if (b) changingCnt++;
        else changingCnt--;
        if (changingCnt == 1) holdChangeCnt = changeCnt;
        if (changingCnt == 0 && holdChangeCnt != changeCnt) update();
    }

    public boolean getDigitalCard() {
        return digitalCard;
    }
    
    public void setDigitalCard(boolean newValue) {
        setChanging(true);
        fireBeforePropertyChange(PROPERTY_DigitalCard, this.digitalCard, newValue);
        boolean old = digitalCard;
        this.digitalCard = newValue;
        firePropertyChange(PROPERTY_DigitalCard, old, this.digitalCard);
        setChanging(false);
    }
    
      
    public boolean getDigitalCardUseNull() {
        return digitalCardUseNull;
    }
    public void setDigitalCardUseNull(boolean newValue) {
        setChanging(true);
        boolean old = this.digitalCardUseNull;
        this.digitalCardUseNull = newValue;
        firePropertyChange(PROPERTY_DigitalCardUseNull, old, this.digitalCardUseNull);
        setChanging(false);
    }

    public void reset() {
        setChanging(true);
        setDigitalCard(false);
        setNull(PROPERTY_DigitalCard);
        setDigitalCardUseNull(false);
        setChanging(false);
    }

    private transient ArrayList<WeakReference<HubFilter>> alFilter;
    private void add(HubFilter filter) {
        if (alFilter == null) alFilter = new ArrayList<WeakReference<HubFilter>>(5);
        alFilter.add(new WeakReference<HubFilter>(filter));
    }
    private void update() {
        for (WeakReference<HubFilter> ref : alFilter) {
            HubFilter filter = ref.get();
            if (filter != null) filter.refresh();
        }
    }
    public Hub<CardFilter> getHub() {
        if (hub != null) return hub;
        hub = new Hub<CardFilter>(CardFilter.class);
        hub.add(this);
        hub.setPos(0);
        hub.addHubListener(new HubListenerAdapter() {
            @Override
            public void afterPropertyChange(HubEvent e) {
                changeCnt++;
            }
        });
        return hub;
    }
    public HubFilter createDigitalOnlyFilter(Hub<Card> hubMaster, Hub<Card> hub) {
        return createDigitalOnlyFilter(hubMaster, hub, false);
    }
    public HubFilter createDigitalOnlyFilter(final Hub<Card> hubMaster, Hub<Card> hub, boolean bAllHubs) {
        HubFilter filter = new HubFilter(hubMaster, hub) {
            @Override
            public boolean isUsed(Object object) {
                Card card = (Card) object;
                return isUsedForDigitalOnlyFilter(card);
            }
        };
        filter.addDependentProperty(Card.PROPERTY_DigitalCard);
        add(filter);
 
        if (!bAllHubs) return filter;
        filter.setServerSideOnly(true); 
        // need to listen to all Card
        OAObjectCacheHubAdder hubCacheAdder = new OAObjectCacheHubAdder(hubMaster);
        return filter;
    }

    public boolean isUsedForDigitalOnlyFilter(Card card) {
        // digitalCard
        return card.getDigitalCard();
    }
    
}
