package Trader;

import Trader.BittrexWrapper.Bittrex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static Trader.Utilites.readResponse;

class Market {
    String name;
    String ALT;
    double lastPrice;
    double topOrderBids;
    double secondOrderBid;
    double topOrderAsks;
    double secondOrderAsk;
    double openBuyOrders;
    double openSellOrders;
    double low24h;
    double high24h;
    double availableBTC;
    double availableALT;

    ArrayList<Double> top10AsksOrder = new ArrayList<>();
    ArrayList<Double> top10BidsOrder = new ArrayList<>();

    double summ10QuantityOrderAsksBook = 0;
    double summ10QuantityOrderBidsBook = 0;

    double summ25QuantityOrderAsksBook = 0;
    double summ25QuantityOrderBidsBook = 0;


    List<HashMap<String, String>> marketHistory;
    List<HashMap<String, String>> openOrders;
    List<HashMap<String, String>> OrderSellBook;
    List<HashMap<String, String>> OrderBuyBook;


    private static Bittrex wrapper = new Bittrex();

    Market(String name, String ALT){
        this.name = name;
        this.ALT = ALT;

        wrapper.setAuthKeysFromTextFile("keys.txt");
        String rawMarketSummary = wrapper.getMarketSummary(name);
        HashMap<String, String> marketSummary = readResponse(rawMarketSummary);

        lastPrice = Double.parseDouble(marketSummary.get("Last"));
        topOrderBids = Double.parseDouble(marketSummary.get("Bid"));
        topOrderAsks = Double.parseDouble(marketSummary.get("Ask"));
        openBuyOrders = Double.parseDouble(marketSummary.get("OpenBuyOrders"));
        openSellOrders = Double.parseDouble(marketSummary.get("OpenSellOrders"));
        low24h = Double.parseDouble(marketSummary.get("Low"));
        high24h = Double.parseDouble(marketSummary.get("High"));

        String rawAvailableBTC = wrapper.getBalance("BTC");
        HashMap<String, String>  walletBTC = readResponse(rawAvailableBTC);
        availableBTC = Double.parseDouble(walletBTC.get("Available"));

        String rawAvailableALT = wrapper.getBalance(ALT);
        HashMap<String, String>  walletALT = readResponse(rawAvailableALT);

        if(!(walletALT.get("Available") == null)) {
            availableALT = Double.parseDouble(walletALT.get("Available"));
        } else {
            availableALT = 0;
        }

        String rawMarketHistory = wrapper.getMarketHistory(name);
        marketHistory = Bittrex.getMapsFromResponse(rawMarketHistory);

        String rawOpenOrders = wrapper.getOpenOrders(name);
        openOrders = Bittrex.getMapsFromResponse(rawOpenOrders);
        String rawOrderSellBook = wrapper.getOrderBook(name, "sell");
        OrderSellBook = Bittrex.getMapsFromResponse(rawOrderSellBook);
        int count = 0;

        for (HashMap<String, String> map :OrderSellBook) {
            if(count == 1){
                secondOrderAsk = Double.parseDouble(map.get("Rate"));
            }
            if(count < 9) {
                summ10QuantityOrderAsksBook = summ10QuantityOrderAsksBook + (Double.parseDouble(map.get("Quantity")) * Double.parseDouble(map.get("Rate")));
                top10AsksOrder.add(Double.parseDouble(map.get("Rate")));
            }
            if(count < 24) summ25QuantityOrderAsksBook = summ25QuantityOrderAsksBook + (Double.parseDouble(map.get("Quantity")) * Double.parseDouble(map.get("Rate")));
            if(count > 24) break;
            count++;
        }

        String rawOrderBuyBook = wrapper.getOrderBook(name, "buy");
        OrderBuyBook = Bittrex.getMapsFromResponse(rawOrderBuyBook);
        count = 0;

        for (HashMap<String, String> map :OrderBuyBook) {
            if(count == 1){
                secondOrderBid = Double.parseDouble(map.get("Rate"));
            }
            if(count < 9) {
                summ10QuantityOrderBidsBook = summ10QuantityOrderBidsBook + (Double.parseDouble(map.get("Quantity")) * Double.parseDouble(map.get("Rate")));
                top10BidsOrder.add(Double.parseDouble(map.get("Rate")));
            }
            if(count < 24) summ25QuantityOrderBidsBook = summ25QuantityOrderBidsBook + (Double.parseDouble(map.get("Quantity")) * Double.parseDouble(map.get("Rate")));
            if(count > 24) break;
            count++;
        }
    }
}
