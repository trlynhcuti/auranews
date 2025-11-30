package com.example.auranews.models;

public class FunctionItem {
    private int icon;
    private String Text;

    public FunctionItem() {}

    public FunctionItem(int icon, String text) {
        this.icon = icon;
        Text = text;
    }

    public String getText() {
        return Text;
    }

    public void setText(String text) {
        Text = text;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
