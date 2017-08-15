package Trader;

import Trader.BittrexWrapper.Bittrex;

import java.util.HashMap;
import java.util.List;


/**
 * Created by TrashPony on 07.08.2017.
 */

class Trader {
    private static Bittrex wrapper = new Bittrex();

    static double tradeBuy (Market market) {
        wrapper.setAuthKeysFromTextFile("keys.txt");
        double priceByu = market.topOrderBids + 0.00000001;
        double fee = market.availableBTC * 0.0026;
        double ALT = (market.availableBTC - fee) / priceByu;
        System.out.println("Покупаю " + market.ALT + " по " + Double.toString(priceByu));
        Log.log("Покупаю " + market.ALT + " по " + Double.toString(priceByu));
        wrapper.buyLimit(market.name, Double.toString(ALT), Double.toString(priceByu));
        return market.availableBTC/ALT;
    }

    static String tradeSell (Market market, double startProfit) {
        wrapper.setAuthKeysFromTextFile("keys.txt");
        String order;
        double priceSell;
        double startDifference;

        if (startProfit < market.topOrderBids && (Utilites.percentageCalculator(startProfit, market.topOrderBids)) > 0.6 ) {
            priceSell = market.topOrderBids - 0.00000001;
            startDifference = Utilites.percentageCalculator(startProfit, market.topOrderBids);
            System.out.println("Продаю относительно начального закупа с выгодой " + startDifference);
            Log.log("Продаю относительно начального закупа с выгодой " + startDifference);
        } else {
            priceSell = market.topOrderAsks - 0.00000001;
            startDifference = Utilites.percentageCalculator(startProfit, market.topOrderAsks);
            System.out.println("Продаю относительно начального закупа с выгодой " + startDifference);
            Log.log("Продаю относительно начального закупа с выгодой " + startDifference);
        }

        order = wrapper.sellLimit(market.name, Double.toString(market.availableALT), Double.toString(priceSell));
        List<HashMap<String, String>> uuidOrder = Bittrex.getMapsFromResponse(order);

        if(uuidOrder.get(0) == null){
            return "";
        } else {
            return uuidOrder.get(0).get("uuid");
        }
    }
}
