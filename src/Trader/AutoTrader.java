package Trader;

import Trader.BittrexWrapper.Bittrex;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static Trader.Analyzer.analyzer;
import static Trader.Log.log;
import static Trader.Trader.tradeBuy;
import static com.sun.corba.se.impl.util.Utility.printStackTrace;


/**
 * Created by trash on 14.08.2017.
 */
public class AutoTrader {
    private static Bittrex wrapper = new Bittrex();


    public static void main (String args[]) {
        boolean analyzMarket = false;

        HashMap<String, String> marketMap = new HashMap<>();

        marketMap.put("BTC-ARK", "ARK");
        marketMap.put("BTC-ARK", "ARK");
        marketMap.put("BTC-EDG", "EDG");
        marketMap.put("BTC-DGB", "DGB");
        marketMap.put("BTC-WAVES", "WAVES");

        while(true) {
            for (String key : marketMap.keySet()) {
                Market market = new Market(key, marketMap.get(key));

                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (Exception e) {
                    printStackTrace();
                }

                actions(market);
            }
        }
    }

    static void actions (Market market) {
        if(analyzer(market, "first", true)) {
            wrapper.setAuthKeysFromTextFile("C:\\keys.txt");
            /////////////////////КОСТЫЛЬ//////////////////////////////////////////
            market = new Market(market.name, market.ALT);
            /////////////////////КОСТЫЛЬ//////////////////////////////////////////

            double profit = tradeBuy(market);
            log("Выставил на покупку");
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (Exception e) {
                printStackTrace();
            }

            /////////////////////КОСТЫЛЬ//////////////////////////////////////////
            String rawOpenOrders = wrapper.getOpenOrders(market.name);
            market.openOrders = Bittrex.getMapsFromResponse(rawOpenOrders);
            /////////////////////КОСТЫЛЬ//////////////////////////////////////////

            if (!(market.openOrders.get(0) == null )) { // && (market.availableALT * (market.topOrderAsks - 0.00000001) < 0.0005))
                for (HashMap<String, String> map : market.openOrders) {
                    if (map.get("OrderType").equals("LIMIT_BUY")) {
                        log("Купить не удалось.");
                        wrapper.cancelOrder(map.get("OrderUuid"));
                        if (analyzer(market, "first", false)) {
                            log("Новая попытка");
                            actions(new Market(market.name, market.ALT));
                        }
                    } else {
                        CalculatorProfit.calculatorProfit(market, profit);
                    }
                }
            } else {
                CalculatorProfit.calculatorProfit(market, profit);
            }
        }
    }
}
