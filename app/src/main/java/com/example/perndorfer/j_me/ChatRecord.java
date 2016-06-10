package com.example.perndorfer.j_me;

import java.util.HashMap;

/**
 * Created by Perndorfer on 07.04.2016.
 */
public class ChatRecord
{
    private int chatId;
    private HashMap<String, String> messages;

    public ChatRecord(int chatId, String who)
    {
        this.chatId = chatId;
        this.messages = new HashMap<String,String>();
    }

    public void addMessage(String who, String val)
    {
        messages.put(who,val);
    }

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public HashMap<String, String> getMessages() {
        return messages;
    }

    public void setMessages(HashMap<String, String> messages) {
        this.messages = messages;
    }

}
