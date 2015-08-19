package com.viaoa.jfc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.junit.Test;

import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;
import com.viaoa.TsactestDataGenerator;
import com.viaoa.hub.Hub;
import com.theice.tsactest.model.oa.*;

public class OAButtonTest extends OAUnitTest {

    
//qqqqqqqqqq Add option to enter a  OAButtonToolbar to
//qqqqq create a test that shows all of the features that can be used


    
    Hub<Site> hubSite;
    Hub<Environment> hubEnvironment;
    
    public JPanel createSamplePanel() {
        // JPanel pan = new JPanel(new FlowLayout());
        JPanel pan = new JPanel(new GridLayout(5,5));
        
        reset();
        TsactestDataGenerator data = new TsactestDataGenerator(modelTsac);
        data.createSampleData1();
        
        hubSite = modelTsac.getSites();
        hubEnvironment = hubSite.getDetailHub(Site.P_Environments);

        OAButton cmd = new OAButton(hubSite, "Plain") {
            @Override
            public boolean beforeActionPerformed() {
                System.out.println("beforeActionPerformed");
                return super.beforeActionPerformed();
            }
            @Override
            public boolean confirmActionPerformed() {
                System.out.println("confirmActionPerformed");
                return super.confirmActionPerformed();
            }
            @Override
            protected boolean onActionPerformed() {
                System.out.println("actionPerformed");
                return true;                
            }
            @Override
            public void afterActionPerformedSuccessful() {
                System.out.println("afterActionPerformedSuccessful");
                super.afterActionPerformedSuccessful();
            }
        };
        pan.add(cmd);

        cmd = new OAButton(hubSite, "success");
        cmd.setCompletedMessage("completed successfully");
        pan.add(cmd);
        
        cmd = new OAButton(hubSite, "confirm/success");
        cmd.setConfirmMessage("confirm");
        cmd.setCompletedMessage("completed successfully");
        pan.add(cmd);

        
        cmd = new OAButton(hubSite, "return false") {
            @Override
            protected boolean onActionPerformed() {
                return false;                
            }
        };
        pan.add(cmd);
        
        
        cmd = new OAButton(hubSite, "throwException") {
            @Override
            protected boolean onActionPerformed() {
                throw new RuntimeException("test error");
            }
        };
        pan.add(cmd);
        

        cmd = new OAButton(hubSite, "swingWorker") {
            @Override
            protected boolean onActionPerformed() {
                try {
                    Thread.sleep(2500);
                }
                catch (Exception e) {
                }
                return true;                
            }
        };
        cmd.setUseSwingWorker(true);
        cmd.setCompletedMessage("completed successfully");
        pan.add(cmd);
        
        cmd = new OAButton(hubSite, "swingWorker w/Msg") {
            @Override
            protected boolean onActionPerformed() {
                try {
                    Thread.sleep(2500);
                }
                catch (Exception e) {
                }
                return true;                
            }
        };
        cmd.setUseSwingWorker(true);
        cmd.setProcessingText("title here", "processing msg here");
        cmd.setCompletedMessage("completed successfully");
        pan.add(cmd);
        

        pan.add(new JLabel("Site:"));
        OALabel lbl = new OALabel(hubSite, Site.P_Name);
        pan.add(lbl);
        
        cmd = new OAButton(hubSite, OAButton.PREVIOUS, "prevSite");
        pan.add(cmd);
        
        cmd = new OAButton(hubSite, OAButton.NEXT, "nextSite");
        pan.add(cmd);

        
        pan.add(new JLabel("Env:"));
        lbl = new OALabel(hubEnvironment, Environment.P_Name);
        pan.add(lbl);

        cmd = new OAButton(hubEnvironment, OAButton.PREVIOUS, "prevEnv");
        pan.add(cmd);
        
        cmd = new OAButton(hubEnvironment, OAButton.NEXT, "nextEnv");
        pan.add(cmd);

        
        cmd = new OAButton(hubSite, "w/compDisplay") {
            @Override
            protected boolean onActionPerformed() {
                try {
                    Thread.sleep(2500);
                }
                catch (Exception e) {
                }
                return true;                
            }
        };
        cmd.setDisplayComponent(new JButton("DISPLAY component here qqqqqqqqqqqqq"));
        pan.add(cmd);

        
        cmd = new OAButton(hubSite, "w/compDisplay&Console") {
            @Override
            protected boolean onActionPerformed() {
                try {
                    for (int i=0; i<250 ;i++) {
                        hubSite.getAt(0).setName("test console "+i);
                        Thread.sleep(50);
                    }
                }
                catch (Exception e) {
                }
                return true;                
            }
        };
        cmd.setDisplayComponent(new JButton("DISPLAY component here qqqqqqqqqqqqq"));
        cmd.setConsoleProperty("name");
        pan.add(cmd);
        
        return pan;
    }

    public void test() throws Exception {
        JFrame frm = new JFrame();
        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frm.setBounds(new Rectangle(100,100,400,400));
        Container cont = frm.getContentPane();
        cont.setLayout(new BorderLayout());

        OAButtonTest test = new OAButtonTest();
        
        JPanel pan = createSamplePanel();
        cont.add(pan);

        String cmdName = "esc";
        pan.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0 ,false), cmdName);
        pan.getActionMap().put(cmdName, new AbstractAction() {
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
