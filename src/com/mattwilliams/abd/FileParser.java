package com.mattwilliams.abd;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The FileParser class is responsible for reading the currency
 * input file and writing the long and short matrix files.
 *
 * @author Matt Williams
 * @version 9/20/2017
 */
class FileParser {

    /**
     * The input file path
     */
    private String inputFileName;

    /**
     * Path to the long matrix
     */
    private String outputFileLong;

    /**
     * Path to the short matrix
     */
    private String outputFileShort;

    /**
     * Input file reader
     */
    private BufferedReader reader = null;

    /**
     * Format for reading dates from input data file
     */
    private static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss.SSS");

    /**
     * Format for writing and comparing dates - only goes up to the hour
     */
    private static DateTimeFormatter hourOnly = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00");

    /**
     * Create a new FileParser that will read the given input file. By default
     * the long matrix will be written to a file with -long appended to it, and the
     * short matrix will be written to a file with -short appended to it.
     *
     * @param inputFileName - a CSV formatted currency raw data file
     */
    FileParser(String inputFileName) {
        this.inputFileName = inputFileName;
        this.outputFileLong = inputFileName.replace(".csv", "-long.csv");
        this.outputFileShort = inputFileName.replace(".csv", "-short.csv");
    }

    /**
     * Open the currency input data file for reading
     * @throws FileNotFoundException if could not open input file
     */
    void open() throws FileNotFoundException {
        File file = new File(inputFileName);
        reader = new BufferedReader(new FileReader(file));
    }

    void close() throws IOException {
        reader.close();
        reader = null;
    }

    void processFile() {

        if (reader == null) {
            System.err.println("Call open() first");
            return;
        }

        try (BufferedWriter longWriter = new BufferedWriter(new FileWriter(outputFileLong))) {

            try (BufferedWriter shortWriter = new BufferedWriter(new FileWriter(outputFileShort))) {

                // Keep track of hourly data
                LocalDateTime lastTimestamp = null;
                double lowBidOfHour = Double.MAX_VALUE;
                double highBidOfHour = 0;
                double lowAskOfHour = Double.MAX_VALUE;
                double highAskOfHour = 0;
                double closeAsk = 0; // this hour's closing ask
                double closeBid = 0; // this hour's closing bid
                double lastHourCloseAsk = -1; // last hour's closing ask
                double lastHourCloseBid = -1; // last hour's closing bid

                // Start reading the file line-by-line
                String line;

                while ((line = reader.readLine()) != null) {
                    String[] array = line.split(",");
                    String currency = array[0];
                    LocalDateTime thisTimestamp = LocalDateTime.parse(array[1], dateFormat);
                    double bid = Double.parseDouble(array[2]);
                    double ask = Double.parseDouble(array[3]);

                    // If this is the first line, or if this line is in the same hour as
                    // the last line, compare and update the hourly data
                    if (lastTimestamp == null || sameHour(thisTimestamp, lastTimestamp)) {

                        if (bid > highBidOfHour) {
                            highBidOfHour = bid;
                        }

                        if (bid < lowBidOfHour) {
                            lowBidOfHour = bid;
                        }

                        if (ask > highAskOfHour) {
                            highAskOfHour = ask;
                        }

                        if (ask < lowAskOfHour) {
                            lowAskOfHour = ask;
                        }
                        closeAsk = ask; // in case this is last of hour
                        closeBid = bid;

                    } else {

                        // Otherwise, this is the start of a new hour. Write a line for the previous hour
                        // into the long and short output files
                        boolean firstLine = (lastHourCloseBid < 0);

                        if (!firstLine) {
                            String bidLine = getLine(currency, lastHourCloseBid, lastTimestamp, highBidOfHour,
                                    lowBidOfHour, closeBid);
                            longWriter.append(bidLine);
                            longWriter.newLine();

                            String askLine = getLine(currency, lastHourCloseAsk, lastTimestamp, highAskOfHour,
                                    lowAskOfHour, closeAsk);
                            shortWriter.append(askLine);
                            shortWriter.newLine();

                        }

                        // Update the closing info for the hour
                        lastHourCloseBid = closeBid;
                        lastHourCloseAsk = closeAsk;
                        lowBidOfHour = bid;
                        highBidOfHour = bid;
                        closeBid = bid;
                        lowAskOfHour = ask;
                        highAskOfHour = ask;
                        closeAsk = ask;
                    }
                    lastTimestamp = thisTimestamp;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the path of the short matrix file
     * @return - the path of the short matrix file
     */
    public String getOutputFileShort() {
        return outputFileShort;
    }

    /**
     * Set the path of the short matrix file
     * @param outputFileShort - the path of the short matrix file
     */
    public void setOutputFileShort(String outputFileShort) {
        this.outputFileShort = outputFileShort;
    }

    /**
     * Get the path of the long matrix file
     * @return - the path of the long matrix file
     */
    public String getOutputFileLong() {
        return outputFileLong;
    }

    /**
     * Set the path of the long matrix file
     * @param outputFileLong - the path of the long matrix file
     */
    public void setOutputFileLong(String outputFileLong) {
        this.outputFileLong = outputFileLong;
    }

    /**
     * Compares two dates and returns <code>true</code> if they are in the same hour
     * @param date1
     * @param date2
     * @return <code>true</code> if date1 and date2 have the same hour
     */
    private static boolean sameHour(LocalDateTime date1, LocalDateTime date2) {
        return date1.format(hourOnly).equals(date2.format(hourOnly));
    }

    /**
     * Return a label characterizing the difference between two values
     * @param current - the current value
     * @param previous - the previous value
     * @return "UP" or "DOWN" depending on direction of change
     */
    private static String getLabel(double current, double previous) {
        return current > previous ? "UP" : "DOWN";
    }

    /**
     * Build a line of text to be output into a long or short matrix file
     * @param currency - currency pair string
     * @param previousClose - previous hour closing bid/ask
     * @param timeStamp - this hour's timestamp
     * @param high - high bid/ask for this hour
     * @param low - low bid/ask for this hour
     * @param close - this hour's closing bid/ask
     * @return a delimited line
     */
    private static String getLine(String currency, double previousClose, LocalDateTime timeStamp, double high,
                                  double low, double close) {

        double change = close - previousClose;
        String label = getLabel(close, previousClose);
        return String.join(",", currency, timeStamp.format(hourOnly),
                String.valueOf(high), String.valueOf(low),
                String.valueOf(close), String.format("%.3f", change), label);

    }
}
