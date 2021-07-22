package org.cloudbus.cloudsim.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class test1 {

    public static void main(String[] args) {
        List<Integer> arr1 = new ArrayList<Integer>();
        for (int i = 0; i < 10; i++) {
            arr1.add(i);
        }
        System.out.println(arr1);

        List<Integer> arr2 = arr1.stream().collect(Collectors.toList());
        System.out.println(arr2);
        arr1.remove(5);
        System.out.println(arr2);
        System.out.println(arr1);

    }



}
