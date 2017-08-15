package Trader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static Trader.Utilites.percentageCalculator;

/**
 * Created by trash on 14.08.2017.
 */

class ReadHistoryMarket {

    static boolean readHistoryMarket (Market market,String params, boolean log) {

        double avgPriceAsks;
        double avgPriceBids;

        int countSell = 0;
        int count5Sell = 0;
        int count25Sell = 0;
        int countBuy = 0;
        int count5Buy = 0;
        int count25Buy = 0;
        int count = 0;

        double sumSellBTC = 0;
        double sumBuyBTC = 0;

        double sumSell25BTC = 0;
        double sumBuy25BTC = 0;

        double sumSell5BTC = 0;
        double sumBuy5BTC = 0;

        ArrayList<Double> allPriceAsks = new ArrayList<>();
        ArrayList<Double> allPriceBids = new ArrayList<>();

        ArrayList<String> time = new ArrayList<>();
        int timeCount = 0;

        for (HashMap<String, String> map : market.marketHistory) {

            if (count < 5) {
                if (map.get("OrderType").equals("SELL")) {
                    sumSell5BTC = sumSell5BTC + Double.parseDouble(map.get("Total"));
                    count5Sell++;
                }
                if (map.get("OrderType").equals("BUY")) {
                    sumBuy5BTC = sumBuy5BTC + Double.parseDouble(map.get("Total"));
                    count5Buy++;
                }
            }

            if (count < 25) {
                if (map.get("OrderType").equals("SELL")) {
                    sumSell25BTC = sumSell25BTC + Double.parseDouble(map.get("Total"));
                    count25Sell++;
                }
                if (map.get("OrderType").equals("BUY")) {
                    sumBuy25BTC = sumBuy25BTC + Double.parseDouble(map.get("Total"));
                    count25Buy++;
                    time.add(map.get("TimeStamp"));
                }
            }

            if (map.get("OrderType").equals("SELL")) {
                sumSellBTC = sumSellBTC + Double.parseDouble(map.get("Total"));
                allPriceAsks.add(Double.parseDouble(map.get("Price")));
                countSell++;
            }
            if (map.get("OrderType").equals("BUY")) {
                sumBuyBTC = sumBuyBTC + Double.parseDouble(map.get("Total"));
                allPriceBids.add(Double.parseDouble(map.get("Price")));
                countBuy++;
            }
            count++;
        }
        /////////////////Находим средние значения///////
        double sum = 0;
        double intervalX = 1;

        for (int i = 0; i < allPriceAsks.size(); i++) {
            sum = sum + allPriceAsks.get(i);
        }
        avgPriceAsks = sum / allPriceAsks.size();

        sum = 0;
        for (int i = 0; i < allPriceBids.size(); i++) {
            sum = sum + allPriceBids.get(i);
        }
        avgPriceBids = sum / allPriceBids.size();

        ///////////////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////////////////
        ArrayList<Coordinate> coordinateAsksCoordinate1 = createCoordinates(allPriceAsks, intervalX);
        ArrayList<Coordinate> coordinateBidsCoordinate1 = createCoordinates(allPriceAsks, intervalX);

        double ABAsks[] = Approximation(coordinateAsksCoordinate1);
        double ABBids[] = Approximation(coordinateBidsCoordinate1);

        /////////////////////////прямая y = A*x+B определяет изменения курса за последние 200 сделок////////////
        /////////////////////////но что с этим делать я пока хз D://////////////////////////////////////////////


        if(params.equals("first")) {
            if (avgPriceBids < market.topOrderBids) {
                if(log) System.out.println("Непрошел тест на среднее");
                return false;
            }
        }
        /////////////////АНАЛИЗ ХАЙПА///////////////
        if (countSell < countBuy && sumSellBTC < sumSellBTC) {
            double haip = percentageCalculator(countSell, countBuy);
            if (haip > 40) {
                if(log) {
                    System.out.println("------------------------- Ситация 1 ----------------------------");
                    Log.log("------------------------- Ситация 1 ----------------------------");
                }
                return true;
            }
        }

        if (count25Sell < count25Buy && sumSell25BTC < sumBuy25BTC) {
            double haip2 = percentageCalculator(count25Sell, count25Buy);
            if (haip2 > 70) {
                if(log) {
                    System.out.println("------------------------- Ситация 2 ----------------------------");
                    Log.log("------------------------- Ситация 2 ----------------------------");
                }
                return true;
            }
        }

        for (int i = 0; i < time.size(); i++) {
            if (timeCount < Collections.frequency(time, time.get(i))) {
                timeCount = Collections.frequency(time, time.get(i));
            }
        }

        if (timeCount > 6) {
            if(log) {
                System.out.println("------------------------- Ситация 3 ----------------------------");
                Log.log("------------------------- Ситация 3 ----------------------------");
            }
            return true;
        }

        if (count25Sell > count25Buy && sumSell25BTC > sumBuy25BTC) {
            if (count25Sell > 15 && count5Buy > 3) {
                if(log) {
                    System.out.println("------------------------- Ситация 4 ----------------------------");
                    Log.log("------------------------- Ситация 4 ----------------------------");
                }
                return true;
            }
        }

        return false;
    }

    private static ArrayList<Coordinate> createCoordinates(ArrayList<Double> y, double intervalX){
        ArrayList<Coordinate> coordinatesXY = new ArrayList<>();
        for(int i = 0; i < y.size(); i++){
            coordinatesXY.add(new Coordinate((double)i*intervalX,y.get(i)));
        }
        return  coordinatesXY;
    }

    private static class Coordinate {
        double x;
        double y;
        private Coordinate (double x, double y){
            this.x = x;
            this.y = y;
        }
    }
    private static double[] Approximation (ArrayList<Coordinate> coordinates){
        double A;
        double B;
        double sumXiYi = 0;
        double sumXi2 = 0;
        double sumX = 0;
        double sumY = 0;
        double[] rateAB = new double[2];

        for (Coordinate point: coordinates) {
            sumXiYi = sumXiYi + (point.x * point.y);
            sumXi2 = sumXi2 + (point.x * point.x);
            sumX=sumX + (point.x);
            sumY=sumY + (point.y);
        }

        A = (coordinates.size()*sumXiYi-(sumX*sumY))/(coordinates.size()*sumXi2-(sumX*sumX));
        B = (sumY - (A*sumX))/coordinates.size();

        rateAB[0] = A;
        rateAB[1] = B;
        return rateAB;
    }
}