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
import java.util.List;
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
    
    private List<Bet> bets = new ArrayList<>();
    
    @Override
    public void run(String...args) throws Exception {
        
        
        try {
            //Read the user names from the text file and put them in an array list.
            List users = new ArrayList<>();
            try {
                File myObj = new File("c:\\Henk\\Users.txt");
                Scanner myReader = new Scanner(myObj);
                while (myReader.hasNextLine()) {
                    String user = myReader.nextLine();
                    System.out.println(user);
                    users.add(user);
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
                addBet(new Bet(entries[0], entries[1], Double.parseDouble(entries[2])));
            }
        } catch(Exception e) {
            logger.error("An error occurred", e);
        }
    }
    
    private void addBet(Bet bet) {
        synchronized(lock) {
            bets.add(bet);
        }
    }
    
    @Scheduled(fixedRate = 10000)
    private void runRoulette() {
        synchronized(lock) {
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
                    System.out.format("%-15s%11s%15s%18.2f%n", bet.getUsername(), bet.getBetNumber(), (win ? "WIN" : "LOOSE"),  winnings);
                }
                bets.clear();
            }
        }
    }
    
    private int getRandomNumberUsingNextInt(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }
}

class Bet {
    private String username;
    private double amount;
    private String betNumber;

    public Bet(String username, String betNumber, double amount) {
        this.username = username;
        this.amount = amount;
        this.betNumber = betNumber;
    }

    public String getUsername() {
        return username;
    }

    public double getAmount() {
        return amount;
    }

    public String getBetNumber() {
        return betNumber;
    }
}