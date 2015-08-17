package com.viaoa.jfc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import org.junit.Test;

import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;
import com.viaoa.TsactestDataGenerator;
import com.viaoa.hub.Hub;
import com.theice.tsactest.model.oa.*;

public class OAButtonTest extends OAUnitTest {

    
    Hub<Site> hubSite;
    
    public OAButton create() {

        reset();
        TsactestDataGenerator data = new TsactestDataGenerator(modelTsac);
        data.createSampleData1();
        
        hubSite = modelTsac.getSites();

        OAButton cmd = new OAButton(hubSite, "Test Button Here") {
        };
        hubSite.setPos(0);
        return cmd;
    }

    public void test() throws Exception {
        JFrame frm = new JFrame();
        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frm.setBounds(new Rectangle(100,100,400,400));
        Container cont = frm.getContentPane();
        cont.setLayout(new FlowLayout());

        OAButtonTest test = new OAButtonTest();
        
        OAButton cmd = create();
        cont.add(cmd);

        String cmdName = "esc";
        cmd.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0 ,false), cmdName);
        cmd.getActionMap().put(cmd, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        frm.setVisible(true);
        for (int i=0 ;; i++) {
            Thread.sleep(2000);
        }
    }
    

    public static void main(String[] args) throws Exception {

        OAButtonTest test = new OAButtonTest();
        test.test();
    }
    
}
