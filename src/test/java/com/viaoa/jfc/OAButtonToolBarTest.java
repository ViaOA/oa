package com.viaoa.jfc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
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

import test.theice.tsac3.model.oa.*;

public class OAButtonToolBarTest extends OAUnitTest {

    
    Hub<Site> hubSite;
    public OAButtonToolBar create() {

        reset();
        TsactestDataGenerator data = new TsactestDataGenerator(modelTsac);
        data.createSampleData1();
        
        hubSite = modelTsac.getSites();

        OAButtonToolBar tb = new OAButtonToolBar(hubSite, Site.P_Name, 10) {
            @Override
            public void onButtonClick(int pos) {
                blink(pos, Color.yellow);
            }
        };
        
        return tb;
    }

    public void randomize() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i=0 ;; i++) {
                    try {
                        Thread.sleep(450);
                    }
                    catch (Exception e) {
                    }
                    Site site;
                    int x = hubSite.getSize(); 
                    if (x < 10 && i%3==0) {
                        site = new Site();
                        site.setName("newSite");
                        hubSite.add(site);
                    }
                    else if (x > 0 && (i%4==0)) {
                        hubSite.removeAt(i%10);
                    }
                    else {
                        site = hubSite.getAt(i%10);
                        if (site != null) site.setName("change."+i);
                    }
                }
                
            }
        });
        t.start();
            
    }
    

    public void test() throws Exception {
        JFrame frm = new JFrame();
        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frm.setBounds(new Rectangle(100,100,400,400));
        Container cont = frm.getContentPane();
        cont.setLayout(new BorderLayout());

        OAButtonToolBarTest test = new OAButtonToolBarTest();
        
        OAButtonToolBar tb = create();
        cont.add(tb, BorderLayout.SOUTH);

        String cmd = "esc";
        tb.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0 ,false), cmd);
        tb.getActionMap().put(cmd, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        frm.setVisible(true);
        randomize();
        for (int i=0 ;; i++) {
            tb.blink(i % 10);
            Thread.sleep(2000);
        }
    }
    

    public static void main(String[] args) throws Exception {

        OAButtonToolBarTest test = new OAButtonToolBarTest();
        test.test();
    }
    
}
