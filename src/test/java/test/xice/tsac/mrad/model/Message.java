package test.xice.tsac.mrad.model;

import java.io.Serializable;


/**
 * Used to send messages to tsac about the Application.
 * @author vvia
 * 
 * 
 * @see MRADClient#addMessage(Message) to send message to tsac.
 * @see MRADClient#addMessage(String, MessageType) to have a mesasge created and sent to tsac.
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    protected String text;
    
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    
    
    public MessageType getMessageType() {
        return messageType;
    }
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
    protected MessageType messageType;
}
