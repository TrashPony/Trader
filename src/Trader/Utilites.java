package Trader;

import Trader.BittrexWrapper.Bittrex;

import java.util.HashMap;
import java.util.List;

/**
 * Created by trash on 14.08.2017.
 */
class Utilites {
    static double percentageCalculator (double a, double b){
        double result;
        result = 100 - (a * 100 / b);
        return result;
    }
    static HashMap<String, String> readResponse (String rawResponse){
        List<HashMap<String, String>> responseMapList = Bittrex.getMapsFromResponse(rawResponse);
        return responseMapList.get(0);
    }
}
