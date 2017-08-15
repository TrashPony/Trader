package Trader;

import Trader.BittrexWrapper.Bittrex;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static com.sun.corba.se.impl.util.Utility.printStackTrace;

/**
 * Created by trash on 14.08.2017.
 */

class CalculatorProfit {
    private static Bittrex wrapper = new Bittrex();

    static void calculatorProfit (Market market, double profit) {
        double difference;
        double oldDifference = 0;
        boolean successfulSell = false;
        double startDifference;
        double startProfit = profit;


        wrapper.setAuthKeysFromTextFile("keys.txt");
        String uuidOrder;

        while(!successfulSell) {

            if(market.topOrderAsks > profit) {
                startDifference = Utilites.percentageCalculator(startProfit, market.topOrderAsks);
                System.out.print("Стартовый профит: " + startProfit);
                Log.log("Стартовый профит: " + startProfit);
                profit = market.topOrderAsks;
                System.out.println(" новый профит составляет: " + profit + " он поднялся на " + startDifference + " % ");
                Log.log(" новый профит составляет: " + profit + " он поднялся на " + startDifference + " % ");
            } else {
                difference = Utilites.percentageCalculator(profit, market.topOrderAsks);
                startDifference = Utilites.percentageCalculator(startProfit, market.topOrderAsks);

                if (difference != oldDifference) {
                    System.out.println("Стартовый профит: " + startProfit + " он изменился относительно успешной продажи на " + startDifference + " % ");
                    System.out.println("Нарастающий профит составляет " + profit + " он упал на " + difference + " % ");
                    Log.log("Стартовый профит: " + startProfit + " он изменился относительно успешной продажи на " + startDifference + " % ");
                    Log.log("Нарастающий профит составляет " + profit + " он упал на " + difference + " % ");
                }
               if(difference < -2 && (Utilites.percentageCalculator(profit, market.secondOrderAsk)) < -2){
                    market = new Market(market.name, market.ALT);
                    System.out.println("Цена упала на - 2% относительно второго заказа: " + Utilites.percentageCalculator(profit, market.secondOrderAsk));
                    System.out.println("Экстренный перезакуп!!!");
                    Log.log("Цена упала на - 2% относительно второго заказа: " + Utilites.percentageCalculator(profit, market.secondOrderAsk));
                    Log.log("Экстренный перезакуп!!!");
                    uuidOrder = Trader.tradeSell(market, startProfit);
                }

                if(!Analyzer.analyzer(market,"",false) && startDifference > 0.3){
                    System.out.println("Алгоритм посчитал что рынок больше не эффективен");
                    Log.log("Алгоритм посчитал что рынок больше не эффективен");
                    uuidOrder = Trader.tradeSell(market, startProfit);
                }
                oldDifference = difference;
            }

            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (Exception e) {
                printStackTrace();
            }

            if(!(market.openOrders.get(0) == null)) {
                for (HashMap<String, String> map : market.openOrders) {
                    if (map.get("OrderType").equals("LIMIT_SELL")) {
                        uuidOrder = map.get("OrderUuid");
                        wrapper.cancelOrder(uuidOrder);
                    }
                }
            }

            /////////////////////КОСТЫЛЬ//////////////////////////////////////////
            market= new Market(market.name,market.ALT);

            String rawOpenOrders = wrapper.getOpenOrders(market.name);
            market.openOrders = Bittrex.getMapsFromResponse(rawOpenOrders);

            String rawAvailableALT = wrapper.getBalance(market.ALT);
            HashMap<String, String>  walletALT = Utilites.readResponse(rawAvailableALT);

            if(!(walletALT.get("Available") == null)) {
                market.availableALT = Double.parseDouble(walletALT.get("Available"));
            } else {
                market.availableALT = 0;
            }
            /////////////////////КОСТЫЛЬ//////////////////////////////////////////

            if(market.availableALT * (market.topOrderAsks - 0.00000001) < 0.0005 && market.openOrders.get(0) == null){
                System.out.println("Монеты на продажу кончились " + (market.availableALT * (market.topOrderAsks - 0.00000001)));
                Log.log("Монеты на продажу кончились " + (market.availableALT * (market.topOrderAsks - 0.00000001)));
                successfulSell = true;
            }
        }
    }
}