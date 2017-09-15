package com.mattwilliams.abd;

import java.time.LocalDateTime;

public class DataRow {

    enum Direction {
        Up,
        Down
    }

    private LocalDateTime dateTime;
    private double high;
    private double low;
    private double close;
    private Direction direction;

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public double getClose() {
        return close;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    @Override
    public String toString() {
        return dateTime.toString() + " " + high + " " + low + " " + close + (direction == Direction.Up ? "up"  : "down");
    }
}
