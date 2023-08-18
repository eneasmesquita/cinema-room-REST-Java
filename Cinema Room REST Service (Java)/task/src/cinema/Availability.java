package cinema;

import java.util.ArrayList;

public class Availability {
    private int total_rows;
    private int total_columns;
    private ArrayList<Seat> available_seats;

    public Availability(int total_rows, int total_columns, ArrayList<Seat> available_seats) {
        this.total_rows = total_rows;
        this.total_columns = total_columns;
        this.available_seats = available_seats;
    }

    public int getTotal_rows() {
        return total_rows;
    }

    public void setTotal_rows(int total_rows) {
        this.total_rows = total_rows;
    }

    public int getTotal_columns() {
        return total_columns;
    }

    public void setTotal_columns(int total_columns) {
        this.total_columns = total_columns;
    }

    public ArrayList<Seat> getAvailable_seats() {
        return available_seats;
    }

    public void setAvailable_seats(ArrayList<Seat> available_seats) {
        this.available_seats = available_seats;
    }

    public void setAvailable_seats(Seat seat){
        this.available_seats.add(seat);
    }
}
