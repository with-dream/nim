package com.example.imlib;

import com.alibaba.fastjson.JSON;
import com.example.imlib.utils.L;

import java.util.*;

public class Test {
    public static void main(String[] args) {
        List<String> ll = new ArrayList<>();
        ll.add("aa");
        ll.add("bb");
        ll.add("bb");
        ll.add("bb");
        Set<String> ss = new HashSet<>();
        ss.addAll(ll);
        L.p("==>" + ss);
    }
}
