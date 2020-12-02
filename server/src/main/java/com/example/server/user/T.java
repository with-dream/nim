package com.example.server.user;

class A {
}

class B extends A {
}

public class T {

    public static void main(String[] args) {
        A a = new B();
        System.out.println("==>" + a.getClass());
    }
}
