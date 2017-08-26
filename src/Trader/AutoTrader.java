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

        marketMap.put("BTC-BCC", "BCC");
        marketMap.put("BTC-XRP", "XRP");
        marketMap.put("BTC-ETH", "ETH");
        marketMap.put("BTC-ETC", "ETC");

        marketMap.put("BTC-LTC", "LTC");
        marketMap.put("BTC-NEO", "NEO");
        marketMap.put("BTC-LSK", "LSK");

        marketMap.put("BTC-QTUM", "QTUM");
        marketMap.put("BTC-VTC", "VTC");
        marketMap.put("BTC-KORE", "KORE");

        try {

            while (true) {
                for (String key : marketMap.keySet()) {
                    Market market = new Market(key, marketMap.get(key));

                    TimeUnit.SECONDS.sleep(3);

                    actions(market);
                }
            }
        } catch (Exception e) {
            System.err.println(e);
            main(null);
        }
    }

    static void actions (Market market) {
        try {
            boolean extra = false;
            double profit;

            if (analyzer(market, "first", false)) {
                wrapper.setAuthKeysFromTextFile("keys.txt");

                analyzer(market, "first", true);
                extra = market.extraTrade;
                /////////////////////КОСТЫЛЬ//////////////////////////////////////////
                market = new Market(market.name, market.ALT);
                /////////////////////КОСТЫЛЬ//////////////////////////////////////////

                if(extra) {
                    Log.log("Экстра закуп","info");
                    profit = tradeBuy(market, true);
                } else {
                    profit = tradeBuy(market, false);
                }
                log("Выставил на покупку","info");

                TimeUnit.SECONDS.sleep(10);


                /////////////////////КОСТЫЛЬ//////////////////////////////////////////
                String rawOpenOrders = wrapper.getOpenOrders(market.name);
                market.openOrders = Bittrex.getMapsFromResponse(rawOpenOrders);
                /////////////////////КОСТЫЛЬ//////////////////////////////////////////

                if (!(market.openOrders.get(0) == null)) { // && (market.availableALT * (market.topOrderAsks - 0.00000001) < 0.0005))
                    for (HashMap<String, String> map : market.openOrders) {
                        if (map.get("OrderType").equals("LIMIT_BUY")) {
                            log("Купить не удалось.","info");
                            wrapper.cancelOrder(map.get("OrderUuid"));
                            if (analyzer(market, "first", false)) {
                                log("Новая попытка","info");
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
        } catch (Exception e){
            System.err.println(e);
            actions(market);
        }
    }
}
