package com.ankit;

import java.util.ArrayList;
import java.util.List;

public class TestMainOther {
    private static List<String> list = new ArrayList<>(5);

    static {
        list.add("ABC");
        list.add("ABC-1");
        list.add("ABC-2");
        list.add("ABC-3");
        list.add("ABC-4");
    }


    public static void main(String[] args) {
        String unique = getUniqueName("ABC", 0);
        String uniqueName = getUnique("ABC", 0);

        System.out.println(unique);
        System.out.println(uniqueName);
    }

    private static String getUniqueName(String name, long next) {
        boolean yes = list.contains(name);
        if(yes) {
            long nextV = next + 1;
            name = name + nextV;
            getUniqueName(name,nextV);
        } else {
            return name;
        }
        return name;
    }

    private static String getUnique(String name, long next){
        String orginal = name;
        boolean dup = list.contains(name);
        while(dup) {
             next = next + 1;

            name = name +"-"+ next;
            dup = list.contains(name);
            name = name.split("-")[0];
            name = name +"-"+ next;
        }
        return name;

    }
}
