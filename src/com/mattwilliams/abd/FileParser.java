package com.mattwilliams.abd;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class FileParser {

    private String inputFileName;
    private String outputFileLong;
    private String outputFileShort;
    private BufferedReader reader = null;
    private static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss.SSS");
    private static DateTimeFormatter hourOnly = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00");

    FileParser(String inputFileName) {

        this.inputFileName = inputFileName;
        this.outputFileLong = inputFileName.replace(".csv", "-long.csv");
        this.outputFileShort = inputFileName.replace(".csv", "-short.csv");
    }

    void open() throws FileNotFoundException {
        File file = new File(inputFileName);
        reader = new BufferedReader(new FileReader(file));
    }

    void close() throws IOException {
        reader.close();
        reader = null;
    }

    void processFile() {

        try (BufferedWriter longWriter = new BufferedWriter(new FileWriter(outputFileLong))) {
            try (BufferedWriter shortWriter = new BufferedWriter(new FileWriter(outputFileShort))) {
                LocalDateTime lastTimestamp = null;
                double lowBidOfHour = Double.MAX_VALUE;
                double highBidOfHour = 0;
                double lowAskOfHour = Double.MAX_VALUE;
                double highAskOfHour = 0;
                double closeAsk = 0;
                double closeBid = 0;
                double lastHourCloseAsk = -1;
                double lastHourCloseBid = -1;

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] array = line.split(",");
                    String currency = array[0];
                    LocalDateTime thisTimestamp = LocalDateTime.parse(array[1], dateFormat);
                    double bid = Double.parseDouble(array[2]);
                    double ask = Double.parseDouble(array[3]);

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
                        closeAsk = ask;
                        closeBid = bid;

                    } else {

                        if (lastHourCloseBid >= 0) {
                            String bidLabel = bid > lastHourCloseBid ? "up" : "down";
                            longWriter.append(currency).append(",").append(lastTimestamp.format(hourOnly))
                                    .append(",").append(String.valueOf(highBidOfHour)).append(",").append(String.valueOf(lowBidOfHour))
                                    .append(",").append(String.valueOf(closeBid)).append(",").append(bidLabel);
                            longWriter.newLine();
                        }

                        if (lastHourCloseAsk >= 0) {
                            String askLabel = ask > lastHourCloseAsk ? "up" : "down";
                            shortWriter.append(currency).append(",").append(lastTimestamp.format(hourOnly))
                                    .append(",").append(String.valueOf(highAskOfHour)).append(",").append(String.valueOf(lowAskOfHour))
                                    .append(",").append(String.valueOf(closeAsk)).append(",").append(askLabel);

                            shortWriter.newLine();
                        }
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

    private static boolean sameHour(LocalDateTime date1, LocalDateTime date2) {
        return date1.format(hourOnly).equals(date2.format(hourOnly));
    }
}
