package com.limethecoder.model;

import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Simulator {
    private Map<Position, City> cities = new HashMap<>();
    private Map<Integer, Country> countries = new HashMap<>();
    private Map<City, List<City>> citiesNeighbours = new HashMap<>();

    public void simulate() {
        int count = 0;
        initNeighbors();

        while (!isFinish(count)) {
            count++;
            cities.values().forEach(City::setAmountsToPay);
            doTransactionsWithNeighbours();
            tearUp();
        }

    }

    public void addCountry(Country country){
        int id = countries.size() + 1;
        country.setId(id);
        countries.put(id,country);

        Position lowerLeft = country.getLowerLeft();
        Position upperRight = country.getUpperRight();

        for(int y = lowerLeft.getY(); y <= upperRight.getY(); y++) {
            for (int x = lowerLeft.getX(); x <= upperRight.getX(); x++) {
                City city = new City(new Position(x, y), country);
                cities.put(city.getPosition(), city);
            }
        }
    }

    public boolean isCoordinatesValid() {
        return countries.values().stream().allMatch(Country::isPositionValid);
    }

    public void showResults(PrintWriter writer) {
        countries.values().stream()
                .sorted(Comparator.comparing(Country::getFinishDay).thenComparing(Country::getName))
                .forEach(writer::println);
    }

    private void tearUp() {
        cities.values().forEach(city -> {
            city.fillBalances();
            city.clearIncoming();
        });
    }

    private boolean isFinish(int day){
        for(Country country : countries.values()){
            if(country.getFinishDay() < 0){
                country.setFinishDay(day);
            }
        }
        boolean allCitiesDone = true;
        for(City city : cities.values()){
            if(!city.isComplete(countries.values())){
                allCitiesDone = false;
                city.getCountry().setFinishDay(-1);
            }
        }
        return allCitiesDone;
    }

    private void doTransactionsWithNeighbours() {
        for (City city : citiesNeighbours.keySet()) {
            for (City neighbour : citiesNeighbours.get(city)) {
                for (Country country : countries.values()) {
                    sendMoney(city, neighbour, country);
                }
            }
        }
    }

    private void sendMoney(City debit, City credit, Country country){
        int amount = debit.withdrawalCoins(country);
        credit.acceptCoins(country,amount);
    }

    private void initNeighbors(){
        for(City city : cities.values()) {
            Position position = city.getPosition();
            Stream.of(position.north(), position.south(), position.east(), position.west())
                    .map(cities::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.collectingAndThen(Collectors.toList(),
                            neighbours -> citiesNeighbours.put(city, neighbours)));
        }
    }
}
