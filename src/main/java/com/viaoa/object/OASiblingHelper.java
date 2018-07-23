package com.viaoa.object;

import java.util.ArrayList;

import com.viaoa.annotation.OACalculatedProperty;
import com.viaoa.hub.Hub;
import com.viaoa.util.OAPropertyPath;
import com.viaoa.util.OAString;


/**
 * Used in OAThreadLocal to be able to help findSiblings by tracking propertyPaths from 
 * calls to OAObject.getObject/Hub
 * 
 * @author vvia
 */
public class OASiblingHelper<F extends OAObject> {

    private Hub<F> hub;
    private final Node nodeRoot;  // known paths from the root (hub)
    
    
    public OASiblingHelper(Hub<F> hub) {
        this.hub = hub;
        nodeRoot = new Node(null);
        nodeRoot.oi = hub.getOAObjectInfo();
    }

    // tree nodes for propertyPaths from hub
    protected class Node {
        public Node(Node parent) {
            this.nodeParent = parent;
        }
        Node nodeParent;
        OAObjectInfo oi;
        OALinkInfo li;
        ArrayList<Node> alChildren;
    }

    public Hub<F> getHub() {
        return hub;
    }
    
    /*
     * add a known pp to the list.
     */
    public void add(String ppFromHub) {
        if (OAString.isEmpty(ppFromHub)) return;
        add(ppFromHub, 0);
    }
    private void add(final String ppFromHub, final int cnt) {
        OAPropertyPath<F> pp = new OAPropertyPath<F>(hub.getObjectClass(), ppFromHub);
        OALinkInfo[] lis = pp.getLinkInfos();
        
        if (lis != null) {
            Node node = nodeRoot;
            for (OALinkInfo li : lis) {
                Node nodex = _add(node, li.getName());
                if (nodex == null) break;
                node = nodex;
            }
        }
        
        // see if last is a calc prop, and check dependent prop paths
        if (pp.isLastPropertyLinkInfo()) return;
        if (cnt > 3) return;
        
        OACalculatedProperty calc = pp.getOACalculatedPropertyAnnotation();
        if (calc == null) return;

        String[] dependProps = calc.properties();
        if (dependProps == null) return;
        
        String ppPrefix = "";
        if (lis != null) {
            for (OALinkInfo li : lis) {
                if (ppPrefix.length() == 0) ppPrefix = li.getName();
                else ppPrefix += "." + li.getName();
            }
            if (ppPrefix.length() > 0) ppPrefix += ".";
        }
        
        for (String s : dependProps) {
            add(ppPrefix + s, cnt+1);
        }
    }
    
    private Node _add(Node node, String prop) {
        // returns the node node that has this prop
        Node nodeFound = null;
        if (node.alChildren == null) node.alChildren = new ArrayList<>();
        else {
            for (Node nodeChild : node.alChildren) {
                if (prop.equalsIgnoreCase(nodeChild.li.getName())) {
                    nodeFound = nodeChild;
                    break;
                }
            }
        }        
        if (nodeFound == null) {
            OALinkInfo li = node.oi.getLinkInfo(prop);
            if (li != null && !li.getPrivateMethod()) {
                nodeFound = new Node(node);
                nodeFound.oi = li.getToObjectInfo();
                nodeFound.li = li;
                node.alChildren.add(nodeFound);
            }
        }
        return nodeFound;
    }

    
    
    /**
     * Called by oaobj.getObject/Hub so that this helper can build internal pp/Nodes for getting sibling data.
     */
    public void onGetReference(final OAObject obj, final String prop) {
        if (obj == null || prop == null) return;
        _onGetReference(nodeRoot, obj, prop);
    }
    private Node _onGetReference(final Node node, final OAObject obj, final String prop) {
        final Class cz = obj.getClass();
        if (node.oi.getForClass().equals(cz)) {
            Node nodex = _add(node, prop);
            return nodex;
        }

        
        if (node.alChildren != null) {
            for (Node nodeChild : node.alChildren) {
                Node nodex = _onGetReference(nodeChild, obj, prop);
                if (nodex != null) return nodex;
            }
        }

        // see if there is a link prop match
        for (OALinkInfo li : node.oi.getLinkInfos()) {
            if (li.getToClass().equals(cz) && !li.getPrivateMethod()) {
                Node nodex = _add(node, li.getName());
                nodex = _add(nodex, prop);
                return nodex;
            }
        }
        
        return null;
    }
    

    /**
     * Called by getSilbing to find root hub and pp.
     * @param obj object that is being used
     * @param prop property/reference that is getting
     * @return a hub and propertyPath that can be used to find siblings.
     */
    public String getPropertyPath(OAObject obj, String prop) {
        Node node = _findNode(nodeRoot, obj, prop, false);
        if (node == null) {
            node = _findNode(nodeRoot, obj, prop, true);
        }

        String pp = null;
        for ( ;node != null && node != nodeRoot; node=node.nodeParent) {
            if (pp == null) pp = node.li.getName();
            else pp = node.li.getName() + "." + pp;
        }
        return pp;
    }

    private Node _findNode(final Node node, final OAObject obj, final String prop, final boolean bRetry) {
        final Class cz = obj.getClass();
        
        if (node.oi.getForClass().equals(cz)) {
            if (node.alChildren != null) {
                for (Node nodeChild : node.alChildren) {
                    if (prop.equalsIgnoreCase(nodeChild.li.getName())) return nodeChild;
                }
            }
            
            for (OALinkInfo li : node.oi.getLinkInfos()) {
                if (li.getName().equalsIgnoreCase(prop) && !li.getPrivateMethod()) {
                    Node nodex = _add(node, li.getName());
                    return nodex;
                }
            }
        }

        if (bRetry) {
            // see if there is a link prop match
            for (OALinkInfo li : node.oi.getLinkInfos()) {
                if (!li.getToClass().equals(cz) || li.getPrivateMethod()) continue;
                if (li.getCalculated()) continue;
                Node nodex = _add(node, li.getName());
                nodex = _add(nodex, prop);
                return nodex;
            }
        }
        
        if (node.alChildren != null) {
            for (Node nodeChild : node.alChildren) {
                Node nodex = _findNode(nodeChild, obj, prop, bRetry);
                if (nodex != null) return nodex;
            }
        }
        
        return null;
    }
    
}
