package test.hifive.model.oa.filter;

import java.util.logging.Logger;

import test.hifive.model.oa.*;

import com.viaoa.hub.Hub;
import com.viaoa.hub.HubEvent;
import com.viaoa.hub.HubFilter;
import com.viaoa.hub.HubListener;
import com.viaoa.hub.HubListenerAdapter;
import com.viaoa.hub.HubMerger;
import com.viaoa.util.OAString;

/**
 * Used by AwardType.getAvailableItems() to populate the list of available items.
 * 
 * Items include the following:
 * 1: awardType.usesItems = true
 * 2: items from awardType.section (includes subsections)
 * 3: if min/max price, then they are compared with product cost.
 * 4: if awardType.useHifiveApprovedItems, then all items with item.hifiveRating = Approved
 * 5: awardType.includeItems list
 * 6: exclude all awardType.excludeItems
 * 7: exclude item.discontinuedDate
 * 
 */
public class AwardTypeAvailableItemFilter {
    private static Logger LOG = Logger.getLogger(AwardTypeAvailableItemFilter.class.getName());

    private AwardType awardType;
    private Hub<Item> hubAllItem;  // all items to be filtered 
    private Hub<Item> hubSectionItem;  // uses hubMerger to get all items from Catalog.sections.items
    private Hub<AwardType> hubThis;  // listen for changes to min/max price, and usesItems
    private HubFilter filter;  // filter that uses hubAllItem to populate hubAvailabeItems
    
    private static Hub<Product> hubApprovedProducts;
    private HubListener hubListenerProducts;
    
    private String logMsg;

    public AwardTypeAvailableItemFilter() {
        
    }
    
    public void setAwardType(AwardType at) {
        if (at == null) return;
        this.logMsg = "AwardType="+at.getName()+", id="+at.getId();
        LOG.fine("creating "+logMsg);
        this.awardType = at;
        hubThis = new Hub<AwardType>(AwardType.class);
        hubThis.add(awardType);
        hubThis.setPos(0);

        // all available items that are then filtered
        hubAllItem = new Hub<Item>(Item.class);

        
        // changes to min/max Price, usesItems, useHifiveApprovedItems, 
        hubThis.addHubListener(new HubListenerAdapter() {
            @Override
            public void afterPropertyChange(HubEvent e) {
                String prop = e.getPropertyName();
                if (prop == null) return;
                
                if (AwardType.PROPERTY_UsesItems.equals(prop)) {
                    setupApprovedProductListener();
                    if (filter != null) filter.refresh();
                }
                else if (AwardType.PROPERTY_MinimumItemPrice.equals(prop) || AwardType.PROPERTY_MaximumItemPrice.equals(prop)) {
                    setupApprovedProductListener();
                    if (filter != null) filter.refresh();
                }
                else if (AwardType.PROPERTY_UseHifiveApprovedItems.equals(prop)) {
                    setupApprovedProductListener();
                    if (filter != null) filter.refresh();
                }
            }
        });
        
        // sections.Items (recursive)
        hubSectionItem = new Hub<Item>(Item.class);
        new HubMerger(awardType, hubSectionItem, OAString.cpp(AwardType.PROPERTY_Section, Section.PROPERTY_Items) );
        hubSectionItem.addHubListener(new HubListenerAdapter() {
            @Override
            public void afterRemove(HubEvent e) {
                Item item = (Item) e.getObject();
                if (filter != null && !filter.isUsed(item)) {
                    hubAllItem.remove(item);
                }
            }
            @Override
            public void afterAdd(HubEvent e) {
                Item item = (Item) e.getObject();
                if (!hubAllItem.contains(item)) hubAllItem.add(item);
            }
        });
        for (Item item : hubSectionItem) {
            if (!hubAllItem.contains(item)) hubAllItem.add(item);
        }

        // add include items to AllItems
        awardType.getIncludeItems().addHubListener(new HubListenerAdapter() {
            @Override
            public void afterRemove(HubEvent e) {
                Item item = (Item) e.getObject();
                if (filter != null && !filter.isUsed(item)) {
                    hubAllItem.remove(item);
                }
            }
            @Override
            public void afterAdd(HubEvent e) {
                Item item = (Item) e.getObject();
                if (!hubAllItem.contains(item)) hubAllItem.add(item);
            }
        });
        for (Item item : awardType.getIncludeItems()) {
            if (!hubAllItem.contains(item)) hubAllItem.add(item);
        }

        // exclude items used by filter
        awardType.getExcludeItems().addHubListener(new HubListenerAdapter() {
            @Override
            public void afterRemove(HubEvent e) {
                Item item = (Item) e.getObject();
                if (filter != null && filter.isUsed(item)) {
                    if (!hubAllItem.contains(item)) hubAllItem.add(item);
                }
            }
            @Override
            public void afterAdd(HubEvent e) {
                Item item = (Item) e.getObject();
                if (filter != null) {
                    hubAllItem.remove(item);
                }
            }
        });
        
        awardType.getItemTypes().addHubListener(new HubListenerAdapter() {
            @Override
            public void afterRemove(HubEvent e) {
                if (filter != null) filter.refresh();
            }
            @Override
            public void afterAdd(HubEvent e) {
                if (filter != null) filter.refresh();
            }
        });
        
        filter = new HubFilter(hubAllItem, awardType.getAvailableItems(), 
                OAString.cpp(Item.PROPERTY_Products, Product.PROPERTY_Cost), 
                Item.PROPERTY_DiscontinuedDate, 
                Item.PROPERTY_ItemTypes) {
            @Override
            public boolean isUsed(Object object) {
                if (!awardType.getUsesItems()) return false;
                Item item = (Item) object;

                if (item.getDiscontinuedDate() != null) return false;
                
                if (awardType.getExcludeItems().contains(item)) return false;

                Hub<Item> h = awardType.getIncludeItems();
                if (h.getSize() > 0 && h.contains(item)) {
                    return true;
                }
                
                boolean bCheck = false;
                if (awardType.getUseHifiveApprovedItems()) {
                    bCheck = (item.getHifiveRating() == Item.HIFIVERATING_approved);
                }
                if (!bCheck) {
                    bCheck = hubSectionItem.contains(item);
                    if (!bCheck) {
                        return false;
                    }
                }

                double bdMin = awardType.getMinimumItemPrice();
                double bdMax = awardType.getMaximumItemPrice();
                
                if (bdMin > 0.0d) {
                    if (bdMax > 0.0d) {
                        if (!isCorrectPrice(item)) return false;
                    }
                }

                boolean bValid = (awardType.getItemTypes().getSize() == 0);
                for (ItemType awardItemType : awardType.getItemTypes()) {
                    if (item.getItemTypes().contains(awardItemType)) {
                        bValid = true;
                        break;
                    }
                }
                if (!bValid) return false;
                
                
                return true;
            }
        };

        filter.addDependentProperty(Item.PROPERTY_DiscontinuedDate);
        filter.addDependentProperty(Item.PROPERTY_ItemTypes);
        

        //****** use this for server side only code **********
        // filter.setServerSideOnly(true);
        
        // listen for changes to approved items
        setupApprovedProductListener();    
    }
    
    // add all Items from hifiveApproved list that are within the correct price range for this awardType
    protected void setupApprovedProductListener() {
        boolean b = awardType != null && awardType.getUseHifiveApprovedItems();
        if (hubListenerProducts != null) {
            if (b) return;
            getHifiveApprovedProducts().removeHubListener(hubListenerProducts);
            hubListenerProducts = null;
        }
        if (!b) return;
        if (!awardType.getUsesItems()) return;

        hubListenerProducts = new HubListenerAdapter() {
            @Override
            public void afterPropertyChange(HubEvent e) {
                String prop = e.getPropertyName();
                if (prop == null) return;
                if (!prop.equalsIgnoreCase(Product.PROPERTY_Cost)) return;

                Product prod = (Product) e.getObject();
                if (prod == null) return;

                Item item = prod.getItem();
                if (item == null) return;

                if (filter == null) return;
                if (filter.isUsed(item)) {
                    if (!hubAllItem.contains(item)) hubAllItem.add(item);
                }
                else {
                    hubAllItem.remove(item);
                }
            }
            @Override
            public void afterAdd(HubEvent e) {
                Product prod = (Product) e.getObject();
                if (prod == null) return;
                Item item = prod.getItem();
                if (item == null) return;

                if (filter != null && filter.isUsed(item)) {
                    if (!hubAllItem.contains(item)) hubAllItem.add(item);
                }
            }
            @Override
            public void afterRemove(HubEvent e) {
                Product prod = (Product) e.getObject();
                if (prod == null) return;
                Item item = prod.getItem();
                if (item == null) return;
                
                if (filter != null && !filter.isUsed(item)) {
                    hubAllItem.remove(item);
                }
            }
        };
        getHifiveApprovedProducts().addHubListener(hubListenerProducts);
        
        // go through all approved items to find Cost matches
/**        
        for (Item item : ModelDelegate.getHifiveApprovedItems()) {
            if (filter != null && filter.isUsed(item)) {
                if (!hubAllItem.contains(item)) hubAllItem.add(item);
            }
        }
*/        
    }
    
    private boolean isCorrectPrice(Item item) {
        for (Product prod : item.getProducts()) {
            if (isCorrectPrice(prod)) return true;
        }
        return false;
    }
    private boolean isCorrectPrice(Product prod) {
        double bdCost = prod.getCost();
        
        double bdMin = awardType.getMinimumItemPrice();
        if (bdMin > 0.0d) {
            if (bdCost < bdMin) return false;
        }
        double bdMax = awardType.getMaximumItemPrice();
        if (bdMax > 0.0d) {
            if (bdCost > bdMax) return false;
        }
        return true;
    }

    
    // single hub to track all hi5 approved items/products
    public synchronized static Hub<Product> getHifiveApprovedProducts() {
        if (hubApprovedProducts != null) return hubApprovedProducts;
        hubApprovedProducts = new Hub<Product>(Product.class);
        //new HubMerger(ModelDelegate.getHifiveApprovedItems(), hubApprovedProducts, OAString.cpp(Item.PROPERTY_Products), true );
        return hubApprovedProducts;
    }
    
}
