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
        marketMap.put("BTC-MUSIC", "MUSIC");
        marketMap.put("BTC-NEO", "NEO");
        marketMap.put("BTC-OMG", "OMG");
        marketMap.put("BTC-ETH", "ETH");
        marketMap.put("BTC-BCC", "BCC");
        marketMap.put("BTC-CVC", "CVC");
        marketMap.put("BTC-STRAT", "STRAT");
        marketMap.put("BTC-QTUM", "QTUM");
        marketMap.put("BTC-PAY", "PAY");
        marketMap.put("BTC-LTC", "LTC");
        marketMap.put("BTC-XRP", "XRP");
        marketMap.put("BTC-BTS", "BTS");
        marketMap.put("BTC-BAT", "BAT");
        marketMap.put("BTC-XEL", "XEL");
        marketMap.put("BTC-WAVES", "WAVES");
        marketMap.put("BTC-XEM", "XEM");
        marketMap.put("BTC-DGB", "DGB");
        marketMap.put("BTC-LGD", "LGD");
        marketMap.put("BTC-GBYTE", "GBYTE");
        marketMap.put("BTC-SC", "SC");
        marketMap.put("BTC-GAME", "GAME");
        marketMap.put("BTC-MTL", "MTL");
        marketMap.put("BTC-LUN", "LUN");
        marketMap.put("BTC-XVG", "XVG");
        marketMap.put("BTC-CFI", "CFI");
        marketMap.put("BTC-ETC", "ETC");
        marketMap.put("BTC-ARK", "ARK");
        marketMap.put("BTC-EDG", "EDG");
        marketMap.put("BTC-SWIFT", "SWIFT");
        marketMap.put("BTC-LMC", "LMC");
        marketMap.put("BTC-AEON", "AEON");
        marketMap.put("BTC-VTR", "VTR");
        marketMap.put("BTC-XMG", "XMG");
        marketMap.put("BTC-XBB", "XBB");
        marketMap.put("BTC-NEOS", "NEOS");


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
            wrapper.setAuthKeysFromTextFile("keys.txt");
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
