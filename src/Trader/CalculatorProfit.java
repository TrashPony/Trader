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

    static void calculatorProfit(Market market, double profit) {
        double difference;
        double oldDifference = 0;
        boolean successfulSell = false;
        double startDifference;
        double startProfit = profit;


        wrapper.setAuthKeysFromTextFile("keys.txt");
        String uuidOrder;
        try {
            while (!successfulSell) {

                if (market.topOrderAsks > profit) {
                    startDifference = Utilites.percentageCalculator(startProfit, market.topOrderAsks);
                    profit = market.topOrderAsks;
                    Log.log("СП: " + startProfit + " НП: " + profit + " up " + startDifference + " % ", "debug");
                } else {
                    difference = Utilites.percentageCalculator(profit, market.topOrderAsks);
                    startDifference = Utilites.percentageCalculator(startProfit, market.topOrderAsks);

                    if (difference != oldDifference) {
                        Log.log("СП: " + startProfit + " Changed " + startDifference + " % ", "debug");
                        Log.log("НП: " + profit + " Changed " + difference + " % ", "debug");
                    }
                    if (difference < -2 && (Utilites.percentageCalculator(profit, market.secondOrderAsk)) < -2) {
                        market = new Market(market.name, market.ALT);
                        Log.log("Цена упала на - 2% относительно второго заказа: " + Utilites.percentageCalculator(profit, market.secondOrderAsk), "debug");
                        Log.log("Экстренный перезакуп!!!", "debug");
                        uuidOrder = Trader.tradeSell(market, startProfit, true);
                    }

                    if (!ReadHistoryMarket.secondReadHistrory(market, "", false) && startDifference > 0.3) {
                        Log.log("Алгоритм посчитал что рынок больше не эффективен", "debug");
                        uuidOrder = Trader.tradeSell(market, startProfit, false);
                    }
                    oldDifference = difference;
                }

                TimeUnit.SECONDS.sleep(5);

                if (!(market.openOrders.get(0) == null)) {
                    for (HashMap<String, String> map : market.openOrders) {
                        if (map.get("OrderType").equals("LIMIT_SELL")) {
                            uuidOrder = map.get("OrderUuid");
                            wrapper.cancelOrder(uuidOrder);
                        }
                    }
                }

                /////////////////////КОСТЫЛЬ//////////////////////////////////////////
                market = new Market(market.name, market.ALT);

                String rawOpenOrders = wrapper.getOpenOrders(market.name);
                market.openOrders = Bittrex.getMapsFromResponse(rawOpenOrders);

                String rawAvailableALT = wrapper.getBalance(market.ALT);
                HashMap<String, String> walletALT = Utilites.readResponse(rawAvailableALT);

                if (!(walletALT.get("Available") == null)) {
                    market.availableALT = Double.parseDouble(walletALT.get("Available"));
                } else {
                    market.availableALT = 0;
                }
                /////////////////////КОСТЫЛЬ//////////////////////////////////////////

                if (market.availableALT * (market.topOrderAsks - 0.00000001) < 0.0005 && market.openOrders.get(0) == null) {
                    Log.log("Монеты на продажу кончились " + (market.availableALT * (market.topOrderAsks - 0.00000001)), "debug");
                    successfulSell = true;
                }
            }
        } catch (Exception e) {
            System.err.println(e);
            CalculatorProfit.calculatorProfit(market, profit);
        }
    }
}
