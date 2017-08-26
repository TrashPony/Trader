package Trader;

import static Trader.ReadHistoryMarket.readHistoryMarket;
import static Trader.Utilites.percentageCalculator;

/**
 * Created by trash on 09.08.2017.
 * Надо сделать еще 1 проверку 1го и второго оредара на адекватность первого+
 * -
 * Надо смотреть разностьи между продажным и покупным ордером
 * Возможно имеет смысл продать валюту сразу а не ждать пока ее кто то купит
 * И смотреть процентное соотношение между ними может продать сразу будет даже выгоднее+
 * -
 * Дописать стратегию хайпа, если валюта растет то не обязательно что она начнет прям тут же падать
 * Максимум я потеряю 2% а выйграть могу до овер дохуя+-
 * -
 * Надо собирать статистику суммы по ордерам продажи и покупки если сумма продажи сильно больше
 * то вероятнее всего курс просядет, если покупака больше то значит ее скупают и курс вероятно выростет+
 * -
 * Надо реализовать деление биткоинов между альтами, что бы бот не тратил сразу весь банк на 1 валюту
 * -
 * Если есть ордер на продажу то надо кидать на калькулятор
 * -
 * Мониторить последние 10 ордеров если там есть профитные то не сливать монету
 * -
 * Реализовать проверка на каждой итерации на рентабильность дальнейших торгов, может иметь смысл продать валюту если получить в плюс сразу+
 * -
 * avg начало и конца истории в обоих позициях
 * -
 * сделать анализ рынка в момент продажи на адекватность сброса
 * -
 * не снимать запрос если цена топа такая же как ты выставил
 */

class Analyzer {

    static boolean analyzer (Market market, String params, boolean log) {

        boolean historyProf = readHistoryMarket(market, params, log);
        double differenceAskBind = percentageCalculator(market.topOrderBids + 0.00000001, market.topOrderAsks - 0.00000001);
        double avgLowHigh = (market.high24h + market.low24h) / 2;
        double second = percentageCalculator(market.secondOrderBid, market.topOrderBids);
        double demand = percentageCalculator(market.openSellOrders, market.openBuyOrders);

        boolean action = false;
        boolean secondCheck = second < 0.10;
        boolean differenceAskBindCheck = differenceAskBind > 0.55;
        boolean lastPriceCheck = market.lastPrice >= avgLowHigh;
        boolean openOrdersCheck = demand > 70;
        boolean sumCap = market.summ25QuantityOrderBidsBook > (market.summ25QuantityOrderAsksBook * 1.5);

        if(log) {
            Log.log("Анализ " + market.name,"info");

            Log.log(". Спрос-предлоежние:        " + openOrdersCheck,"info");
            Log.log(". AVG 24h:                  " + lastPriceCheck,"info");
            Log.log(". Коммисия:                 " + differenceAskBindCheck,"info");
            Log.log(". Второй оредар на покупку: " + secondCheck,"info");
            Log.log(". Анализ спроса:            " + sumCap,"info");
            Log.log(". История:                  " + historyProf,"info");
            Log.log("","info");
            Log.log(". Результат                 " + (sumCap && (secondCheck || differenceAskBindCheck) && historyProf),"info");
            Log.log("-----------------------------------------","info");
        }

        if (sumCap && (secondCheck || differenceAskBindCheck) && historyProf) {
            action = true;
            if(!differenceAskBindCheck){
                market.extraTrade = true;
            }
        }
        return action;
    }
}
