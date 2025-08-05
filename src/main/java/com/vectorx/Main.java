package com.vectorx;

import com.vectorx.VectorX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        try {
            String token = "pankaj:CgYO9pfG4UB1nqLieUMu39HP7HMGnB3w:india-west-1"; // Replace with actual token
            VectorX vx = new VectorX(token);

            Index index = vx.getIndex("testing_index");

            System.out.println(index);

            System.out.println(vx.deleteIndex("testing_index"));



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
