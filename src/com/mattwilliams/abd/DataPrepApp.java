package com.mattwilliams.abd;

import java.io.IOException;

/**
 * The DataPrepApp class reads an input currency data
 * file with bid and ask values, and outputs a long matrix
 * and a short matrix.
 *
 * Program arguments are:
 * 1) Path to the input file
 * 2) (Optional) output path of long matrix
 * 3) (Optional) output path of short matrix
 *
 * @author Matt Williams
 * @version 9/20/2017
 */
public class DataPrepApp {

    public static void main(String [] args) {

        // Must have between 1 and 3 arguments
        if (args.length < 1 || args.length > 3) {
            printUsage();

        } else {

            try {
                FileParser parser = new FileParser(args[0]);

                if (args.length >= 2) {
                    parser.setOutputFileLong(args[1]);
                }

                if (args.length >= 3) {
                    parser.setOutputFileShort(args[2]);
                }

                parser.open();
                parser.processFile();
                System.out.println("Wrote file " + parser.getOutputFileLong());
                System.out.println("Wrote file " + parser.getOutputFileShort());
                parser.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Print usage information
     */
    private static void printUsage() {
        System.err.println("Usage: java -jar abd1.jar inputFile [longMatrixPath] [shortMatrixPath]");
    }


}