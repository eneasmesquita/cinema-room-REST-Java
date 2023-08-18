package cinema;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.*;

@RestController
@RequestMapping
public class RoomController {

    ArrayList<Seat> seats = new ArrayList<>();
    ArrayList<Purchase> purchases = new ArrayList<>();
    Availability availability;
    int current_income;

    public RoomController() {
        int price;
        for (int i = 1; i <= 9 ; i++){ //row loop
            //determining the price
            if (i <= 4){
                price = 10;
            } else {
                price = 8;
            }
            for (int j = 1; j <= 9 ; j++){  //column loop
                seats.add(new Seat(i,j,price));
            }
        }
        int total = seats.size() / 9;
        availability = new Availability(total,total,seats);
    }

    @GetMapping("/seats")
    public Availability getAvailableSeats(){
        return availability;
    }

    @PostMapping("/purchase")
    public ResponseEntity<String> purchaseTicket(@RequestBody Seat seat) {

        int totalColumns = availability.getTotal_columns();
        int totalRows = availability.getTotal_rows();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        if (isSeatOutOfBounds(seat, totalRows, totalColumns)) {
            Map<String,String> errorMessage = new HashMap<>();
            errorMessage.put("error", "The number of a row or a column is out of bounds!");
            String errorMessageJSON = gson.toJson(errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessageJSON);
        }

        Seat purchasedSeat = findAndPurchaseSeat(seat, availability.getAvailable_seats());

        if (purchasedSeat == null) {
            Map<String,String> errorMessage = new HashMap<>();
            errorMessage.put("error", "The ticket has been already purchased!");
            String errorMessageJSON = gson.toJson(errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessageJSON);
        }

        UUID uuid = UUID.randomUUID(); //token generation UUID v.4
        String token = uuid.toString();
        Purchase purchaseResponse = new Purchase(token,purchasedSeat);
        purchases.add(purchaseResponse);

        String purchaseResponseJSON = gson.toJson(purchaseResponse);
        return ResponseEntity.ok(purchaseResponseJSON);
    }

    @PostMapping("/return")
    public ResponseEntity<String> refund(@RequestBody String token){

        String[] code = token.split("\"token\":\\s*\"|\"\\s*}"); //extract just the token code

        Refund refund = new Refund(findTokenAndMakeRepayment(code[1]));
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        if (refund.getReturned_ticket() == null){
            Map<String,String> errorMessage = new HashMap<>();
            errorMessage.put("error", "Wrong token!");
            String errorMessageJSON = gson.toJson(errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessageJSON);
        }

        availability.setAvailable_seats(refund.getReturned_ticket()); //the seat is available again

        String refundResponseJSON = gson.toJson(refund);
        return ResponseEntity.ok(refundResponseJSON);
    }

    @GetMapping("/stats")
    public ResponseEntity<String> getStats(@RequestParam(value = "password", required = false) String password){

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        if (password == null || !password.equals("super_secret")){
            Map<String,String> errorMessage = new HashMap<>();
            errorMessage.put("error", "The password is wrong!");
            String errorMessageJSON = gson.toJson(errorMessage);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMessageJSON);
        }

        Statistic stats = new Statistic(current_income, availability.getAvailable_seats().size(), purchases.size());
        String statsJSON = gson.toJson(stats);
        return ResponseEntity.ok(statsJSON);
    }

    private boolean isSeatOutOfBounds(Seat seat, int totalRows, int totalColumns) {
        return seat.getColumn() >= totalColumns || seat.getColumn() < 0 ||
                seat.getRow() >= totalRows || seat.getRow() < 0;
    }

    private Seat findTokenAndMakeRepayment(String token){
        for (Iterator<Purchase> iterator = purchases.iterator(); iterator.hasNext();) {
            Purchase p = iterator.next();
            if (p.getToken().equals(token)){
                iterator.remove(); //repayment
                current_income -= p.getTicket().getPrice();
                return p.getTicket();
            }
        }
        return null;
    }
    private Seat findAndPurchaseSeat(Seat seat, ArrayList<Seat> availableSeats) {
        for (Iterator<Seat> iterator = availableSeats.iterator(); iterator.hasNext();) {
            Seat s = iterator.next();
            if (seat.getRow() == s.getRow() && seat.getColumn() == s.getColumn()) {
                iterator.remove(); // Remove purchased seat from available seats
                current_income += s.getPrice();
                return s; // Return the purchased seat
            }
        }
        return null; // Seat is not available
    }

}





