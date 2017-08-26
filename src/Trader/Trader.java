package Trader;

import Trader.BittrexWrapper.Bittrex;

import java.util.HashMap;
import java.util.List;


/**
 * Created by TrashPony on 07.08.2017.
 */

class Trader {
    private static Bittrex wrapper = new Bittrex();

    static double tradeBuy (Market market, boolean extra) {
        wrapper.setAuthKeysFromTextFile("keys.txt");

        double priceByu;
        if(extra) {
            priceByu = market.topOrderBids + 0.00000001;
        } else {
            priceByu = market.topOrderAsks;
        }

        double fee = market.availableBTC * 0.0026;
        double ALT = (market.availableBTC - fee) / priceByu;
        Log.log("Покупаю " + market.ALT + " по " + Double.toString(priceByu), "info");
        wrapper.buyLimit(market.name, Double.toString(ALT), Double.toString(priceByu));
        return market.availableBTC/ALT;
    }

    static String tradeSell (Market market, double startProfit, boolean extra) {
        wrapper.setAuthKeysFromTextFile("keys.txt");
        String order;
        double priceSell;
        double startDifference;

        if ((startProfit < market.topOrderBids && (Utilites.percentageCalculator(startProfit, market.topOrderBids)) > 0.3) || extra) {
            priceSell = market.topOrderBids - 0.00000001;
            startDifference = Utilites.percentageCalculator(startProfit, market.topOrderBids);
            Log.log("Продаю относительно начального закупа с выгодой " + startDifference, "info");
        } else {
            priceSell = market.topOrderAsks - 0.00000001;
            startDifference = Utilites.percentageCalculator(startProfit, market.topOrderAsks);
            Log.log("Продаю относительно начального закупа с выгодой " + startDifference, "info");
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
