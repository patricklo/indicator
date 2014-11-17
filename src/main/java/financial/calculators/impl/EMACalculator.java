package financial.calculators.impl;

import financial.utils.TradeInfo;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RecursiveTask;

/**
 * Created with IntelliJ IDEA.
 * User: Patrick Lo
 * Date: 11/15/14
 * Time: 3:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class EMACalculator extends RecursiveTask<List> {

    // private static final int THRESHOLD = 5000;
    private List<TradeInfo> priceList;
    private int shortPeriod;
    private int longPeriod;
    private int midPeriod;
    private int beginIndex;
    private int endIndex;
    private BigDecimal threshold;
    private int availableProcessors;
    private Object lock = new Object();
    static Logger log = Logger.getLogger(EMACalculator.class.getName());

    public EMACalculator(List<TradeInfo> priceList, final int shortPeriod, final int longPeriod, final int midPeriod, final int beginIndex, final int endIndex, final BigDecimal threshold, final int availableProcessors) {
        this.priceList = priceList;
        this.shortPeriod = shortPeriod;
        this.longPeriod = longPeriod;
        this.midPeriod = midPeriod;
        this.beginIndex = beginIndex;
        this.endIndex = endIndex;
        this.threshold = threshold;
        this.availableProcessors = availableProcessors;
    }

    public EMACalculator() {

    }

    /**
     * Calculate EMA,
     * Formula:
     * <p/>
     * Multiplier: (2 / (Time periods + 1) ) = (2 / (10 + 1) ) = 0.1818 (18.18%)     *
     * EMA: {Close - EMA(previous day)} x multiplier + EMA(previous day).
     *
     * @return Double
     */
    public final BigDecimal calculteEMA(final List<TradeInfo> list, final int number) {
        // calculate EMA value
        BigDecimal denominatorOfK = new BigDecimal(number).add(BigDecimal.ONE);
        BigDecimal k = new BigDecimal(2).divide(denominatorOfK, 3, BigDecimal.ROUND_HALF_UP);
        BigDecimal ema = list.get(0).getPrice();// first day's ema equals to intraday close price
        for (int i = 1; i < list.size(); i++) {
            // after the first day, current EMA equals to (intraday close price * k )+ last EMA*(1-k)
            BigDecimal emaPart1 = list.get(i).getPrice().multiply(k);
            BigDecimal emaPart2 = ema.multiply(BigDecimal.ONE.subtract(k));
            ema = emaPart1.add(emaPart2);
        }
        return ema;
    }

    @Override
    protected List compute() {
        List<TradeInfo> diffList = new CopyOnWriteArrayList<TradeInfo>();
        BigDecimal shortEMA;
        BigDecimal longEMA;
        BigDecimal dif;
        log.info("Start calculating.." + Calendar.getInstance().getTime());

        synchronized (lock) {
            if (endIndex > priceList.size()) {
                endIndex = priceList.size();
            }
            for (int i = endIndex - 1; i >= beginIndex; i--) {
                TradeInfo tradeInfo = new TradeInfo();
                List<TradeInfo> sublist = priceList.subList(0, priceList.size() - i);
                shortEMA = calculteEMA(sublist, shortPeriod);
                longEMA = calculteEMA(sublist, longPeriod);
                dif = shortEMA.subtract(longEMA);
                tradeInfo.setPrice(dif);
                diffList.add(tradeInfo);
            }
        }

        log.info("Complete calculating.." + Calendar.getInstance().getTime());
        return diffList;
    }

//    public BigDecimal calculteMACD(final List<TradeInfo> list, final int shortPeriod, final int longPeriod, int midPeriod) throws InterruptedException, ExecutionException {
//        int availableProcessors = Runtime.getRuntime().availableProcessors();
//        ForkJoinPool forkJoinPool = new ForkJoinPool(availableProcessors);
//        //int threshold = priceList.size()/(availableProcessors-1);
//        BigDecimal threshold = new BigDecimal(priceList.size()).divide(new BigDecimal(availableProcessors - 1), 0, BigDecimal.ROUND_UP);
//        EMACalculator emaCalculator = new EMACalculator(priceList, shortPeriod, longPeriod, midPeriod, 0, priceList.size(), threshold, availableProcessors);
//        Future<List> result = forkJoinPool.submit(emaCalculator);
//        BigDecimal dif = calculteEMA(list, shortPeriod).subtract(calculteEMA(list, longPeriod));
//        BigDecimal dea = calculteEMA(result.get(), midPeriod);
//        return dif.subtract(dea).multiply(new BigDecimal(2));
//
//    }

}
