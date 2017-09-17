package com.mattwilliams.abd;

import java.io.BufferedReader;

public class Main {

    public static void main(String [] args) {

        BufferedReader reader = null;

        try {
            FileParser parser = new FileParser(args[0]);
            parser.open();
            parser.processFile();
            parser.close();
        } catch (Exception e) {
            e.printStackTrace();
        }



    }


}