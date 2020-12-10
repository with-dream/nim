package com.example.imlib;

public class Test implements Cloneable {
    public String test;

    public Test clone() throws CloneNotSupportedException {
        Test t = (Test) super.clone();
        t.test = new String(test);
        return t;
    }
}
