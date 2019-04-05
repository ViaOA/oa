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
package com.viaoa.jfc.tree;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.lang.reflect.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import com.viaoa.hub.*;
import com.viaoa.jfc.*;

/** 
    @deprecated See OATree for description on setting Listeners
    @see OATree
*/
public interface OATreeNodeListener {
    public Component getTreeCellRendererComponent(Component comp,JTree tree,Object value,boolean selected,boolean expanded,boolean leaf,int row,boolean hasFocus);
}
