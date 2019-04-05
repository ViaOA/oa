package test.xice.tsac.mrad.model;

/**
 * Describes the type of message about the Application, that is sent to tsac.
 * @author vvia
 *
 * @see Message#getMessageType() to get/set the type of message
 * @see MRADClient#addMessage(Message) to send message to tsac.
 * @see MRADClient#addMessage(String, MessageType) to have a mesasge created and sent to tsac.
 */
public enum MessageType {
    Info, Warn, Fatal;
}
