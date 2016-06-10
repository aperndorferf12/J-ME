package com.example.perndorfer.j_me;

import java.io.Serializable;

/**
 * Created by Perndorfer on 07.04.2016.
 */
public class Contact implements Serializable
{
    private int id;
    private String name;
    private String number;
    private int chat_id;

    public Contact(int id, String name,String number)
    {
        this.id = id;
        this.name = name;
        this.number = number;
        this.chat_id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getChat_id() {
        return chat_id;
    }

    public void setChat_id(int chat_id) {
        this.chat_id = chat_id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return name;
    }
}
