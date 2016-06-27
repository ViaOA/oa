// Generated by OABuilder
package test.hifive.model.oa.filter;

import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import test.hifive.model.oa.*;
import test.hifive.model.oa.propertypath.ItemPP;

import java.util.*;

@OAClass(useDataSource=false, localOnly=true)
@OAClassFilter(name = "HelpingHands", displayName = "Helping Hands", hasInputParams = false)
public class ItemHelpingHandsFilter extends OAObject implements CustomHubFilter {
    private static final long serialVersionUID = 1L;

    public static final String PPCode = ":HelpingHands()";
    private Hub<Item> hubMaster;
    private Hub<Item> hub;
    private HubFilter<Item> hubFilter;
    private OAObjectCacheFilter<Item> cacheFilter;
    private boolean bUseObjectCache;

    public ItemHelpingHandsFilter(Hub<Item> hub) {
        this(null, hub, true);
    }
    public ItemHelpingHandsFilter(Hub<Item> hubMaster, Hub<Item> hub) {
        this(hubMaster, hub, false);
    }
    public ItemHelpingHandsFilter(Hub<Item> hubMaster, Hub<Item> hubFiltered, boolean bUseObjectCache) {
        this.hubMaster = hubMaster;
        this.hub = hubFiltered;
        this.bUseObjectCache = bUseObjectCache;
        if (hubMaster != null) getHubFilter();
        if (bUseObjectCache) getObjectCacheFilter();
    }


    public void reset() {
    }

    public boolean isDataEntered() {
        return false;
    }
    public void refresh() {
        if (hubFilter != null) getHubFilter().refresh();
        if (cacheFilter != null) getObjectCacheFilter().refresh();
    }

    @Override
    public HubFilter<Item> getHubFilter() {
        if (hubFilter != null) return hubFilter;
        if (hubMaster == null) return null;
        hubFilter = new HubFilter<Item>(hubMaster, hub) {
            @Override
            public boolean isUsed(Item item) {
                return ItemHelpingHandsFilter.this.isUsed(item);
            }
        };
        hubFilter.addDependentProperty(ItemPP.itemTypes().pp);
        return hubFilter;
    }

    public OAObjectCacheFilter<Item> getObjectCacheFilter() {
        if (cacheFilter != null) return cacheFilter;
        if (!bUseObjectCache) return null;
        cacheFilter = new OAObjectCacheFilter<Item>(hubMaster) {
            @Override
            public boolean isUsed(Item item) {
                return ItemHelpingHandsFilter.this.isUsed(item);
            }
        };
        cacheFilter.addDependentProperty(ItemPP.itemTypes().pp);
        return cacheFilter;
    }

    public boolean isUsed(Item item) {
        // itemTypes
        Hub<ItemType> hubItemTypes = item.getItemTypes();
        for (ItemType itemType : hubItemTypes) {
            if (itemType.getType() == ItemType.TYPE_HELPINGHANDS) { return true; }
        }
        return false;
    }
    
}
