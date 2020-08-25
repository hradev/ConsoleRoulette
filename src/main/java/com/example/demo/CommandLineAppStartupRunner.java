/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CommandLineAppStartupRunner implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(CommandLineAppStartupRunner.class);
    
    private Object lock = new Object();
    
    //private List<Bet> bets = new ArrayList<>();
    
    private Map<User, List<Bet>> users = new HashMap<>();
    
    @Override
    public void run(String...args) throws Exception {
        
        
        try {
            //Read the user names from the text file and put them in an array list.
            try {
                File myObj = new File("c:\\Henk\\Users.txt");
                Scanner myReader = new Scanner(myObj);
                while (myReader.hasNextLine()) {
                    String userLine = myReader.nextLine();
                    String details[] = userLine.split(" ");
                    
                    if(details.length == 3) {
                        double totalWinnings = Double.parseDouble(details[1]);
                        double totalBets = Double.parseDouble(details[2]);
                        users.put(new User(totalWinnings, totalBets, details[0]), new ArrayList<>());
                    } else {
                        users.put(new User(0, 0, userLine), new ArrayList<>());
                    }
                }
                myReader.close();
            } catch (FileNotFoundException e) {
              System.out.println("An error occurred.");
              e.printStackTrace();
            }

            String command = "";
            while (!"exit".equals(command)) {
                BufferedReader reader =
                           new BufferedReader(new InputStreamReader(System.in));
                command = reader.readLine();
                String [] entries = command.split(" ");
                addBet(new Bet(entries[1], Double.parseDouble(entries[2])), entries[0]);
            }
        } catch(Exception e) {
            logger.error("An error occurred", e);
        }
    }
    
    private void addBet(Bet bet, String userName) {
        synchronized(lock) {
            User userTmp = new User(0, 0, userName);
            users.get(userTmp).add(bet);
        }
    }
    
    @Scheduled(fixedRate = 10000)
    private void runRoulette() {
        synchronized(lock) {
            for (Map.Entry<User,List<Bet>> userEntry : users.entrySet()) {
                List<Bet> bets = userEntry.getValue();
                User user = userEntry.getKey();
                if (bets.size() > 0) {
                    int randomNumber = getRandomNumberUsingNextInt(1, 36);
                    System.out.println("Number: " + randomNumber);
                    System.out.println("Player                 Bet        Outcome          Winnings");
                    System.out.println("---");
                    for(Bet bet : bets) {
                        double winnings = 0;
                        boolean win = false;
                        if("EVEN".equals(bet.getBetNumber())) {
                            if (randomNumber %2 == 0) {
                                win = true;
                                winnings = bet.getAmount() * 2;
                            }
                        } else if("ODD".equals(bet.getBetNumber())) {
                            if (randomNumber % 2 != 0) {
                                win = true;
                                winnings = bet.getAmount() * 2;
                            }
                        } else {
                            int numberBet = Integer.parseInt(bet.getBetNumber());
                            if(numberBet == randomNumber) {
                                win = true;
                                winnings = bet.getAmount() * 36;
                            }
                        }
                        System.out.format("%-15s%11s%15s%18.2f%n", user.getUserName(), bet.getBetNumber(), (win ? "WIN" : "LOOSE"),  winnings);
                        //Add winnings to the user details.
                        user.setTotalWinnings(user.getTotalWinnings() + winnings);
                        //Add bet amount to user details.
                        user.setTotalBet(user.getTotalBet() + bet.getAmount());
                    }
                    bets.clear();
                }
            }
            
            System.out.println("Player                 Total Win              Total Bet");
            
            for (Map.Entry<User,List<Bet>> userEntry : users.entrySet()) {
                User user = userEntry.getKey();
                System.out.format("%-15s%18.2f%18.2f%n", user.getUserName(), user.getTotalWinnings(), user.getTotalBet());
            }
        }
    }
    
    private int getRandomNumberUsingNextInt(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }
}

class Bet {
    private double amount;
    private String betNumber;

    public Bet(String betNumber, double amount) {
        this.amount = amount;
        this.betNumber = betNumber;
    }

    public double getAmount() {
        return amount;
    }

    public String getBetNumber() {
        return betNumber;
    }
}

class User {
    private double totalWinnings;
    private double totalBet;
    private String userName;

    public User(double totalWinnings, double totalBet, String userName) {
        this.totalWinnings = totalWinnings;
        this.totalBet = totalBet;
        this.userName = userName;
    }
    
    public double getTotalWinnings() {
        return totalWinnings;
    }

    public void setTotalWinnings(double totalWinnings) {
        this.totalWinnings = totalWinnings;
    }

    public double getTotalBet() {
        return totalBet;
    }

    public void setTotalBet(double totalBet) {
        this.totalBet = totalBet;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.userName);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final User other = (User) obj;
        if (!Objects.equals(this.userName, other.userName)) {
            return false;
        }
        return true;
    }
    
    
}