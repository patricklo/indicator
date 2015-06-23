package financial;

import financial.calculators.impl.EMACalculator;
import financial.calculators.impl.SMACalculator;
import financial.utils.BinarySearchTool;
import financial.utils.CSVReader;
import financial.utils.TradeInfo;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

/**
 * Created with IntelliJ IDEA.
 * User: Patrick Lo
 * Date: 11/15/14
 * Time: 12:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class Core {
    private static int shortPeriod;
    private static int longPeriod;
    private static int midPeriod;
    private static int smaPeriod;
    private static String smaDate;

    static Logger log = Logger.getLogger(Core.class.getName());

    public static void main(String[] args) throws IOException {
        log.info("Loading CSV file...");
        CSVReader csvReader = new CSVReader();
        List<TradeInfo> priceResults = csvReader.readCSV("/RIOTrades.csv");
        log.info("Completed");
        log.info("pls select 1.SMA for last n trades,2,SMA for date 3.EMA, 4,MACD");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String selection = br.readLine();
        if (selection != null || !"".equals(selection)) {
            int selectionInt=0;
            try{
                selectionInt = Integer.valueOf(selection);
            }  catch(Exception e){
                log.error("Pls type correct number!");
                System.exit(0);
            }
            ApplicationContext ctx = new ClassPathXmlApplicationContext("beans.xml");
            Core core = (Core) ctx.getBean("coreApp");

            try {
                switch (selectionInt) {
                    case 1:
                        log.info("SMA=" + core.getSMA(priceResults, smaPeriod));
                        break;
                    case 2:
                        String datestr = "2014-02-18 13:34:00";
                        log.info("the SMA after date:" + datestr + " equals to " + core.getSMA(priceResults, smaDate));
                        break;
                    case 3:
                        EMACalculator emaCalculator = new EMACalculator();
                        log.info("the EMA equals to " + emaCalculator.calculteEMA(priceResults, 9));
                        break;
                    case 4:
                        log.info(core.getMACD(priceResults.subList(0, 5000), shortPeriod, longPeriod, midPeriod));
                        break;
                    default:
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            log.error("Pls type correct number 1-4");
        }


        ApplicationContext ctx = new ClassPathXmlApplicationContext("beans.xml");
        Core core = (Core) ctx.getBean("coreApp");
        List<TradeInfo> priceResultsFor10000 = priceResults.subList(0, 1000);
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int threshold = priceResultsFor10000.size() / (availableProcessors - 1);

        try {
            // System.out.println(core.getMACD(priceResults.subList(0,10000), 12, 26, 9));
            log.info("SMA=" + core.getSMA(priceResults, 1000));
            String date = "2014-02-18 13:34:00";
            log.info("the SMA after date:" + date + " equals to " + core.getSMA(priceResults, date));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BigDecimal getSMA(List<TradeInfo> priceResults, int periods) throws InterruptedException, ExecutionException {
        BigDecimal sma = BigDecimal.ZERO;
        int beginIndex = priceResults.size() - periods;
        if (periods <= priceResults.size()) {
            SMACalculator smaCalculator = new SMACalculator(priceResults.subList(beginIndex, priceResults.size()));
            sma = smaCalculator.calculateSMA(periods);
        } else {
            log.error("Error: the periods is out of data size, pls correct.");
        }
        return sma;
    }

    public BigDecimal getSMA(List<TradeInfo> priceResults, String date) throws InterruptedException, ExecutionException, ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date tradeDate = sdf.parse(date);
        Comparator<TradeInfo> dateComparator = new Comparator<TradeInfo>() {
            public int compare(TradeInfo trade1, TradeInfo trade2) {
                Date d1 = trade1.getTradeDate();
                Date d2 = trade2.getTradeDate();
                if (d1 == null && d2 == null)
                    return 0;
                if (d1 == null)
                    return -1;
                if (d2 == null)
                    return 1;
                return d1.compareTo(d2);
            }
        };
        Collections.sort(priceResults, dateComparator);
        TradeInfo trade = new TradeInfo();
        trade.setTradeDate(tradeDate);
        int i = BinarySearchTool.indexedBinarySearch(priceResults, trade, dateComparator);
        List<TradeInfo> lastTradeResults = priceResults.subList(i, priceResults.size());
        return getSMA(lastTradeResults, lastTradeResults.size());
    }

    public BigDecimal getMACD(final List<TradeInfo> list, final int shortPeriod, final int longPeriod, int midPeriod) throws InterruptedException, ExecutionException {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        ForkJoinPool forkJoinPool = new ForkJoinPool(availableProcessors);
        //int threshold = priceList.size()/(availableProcessors-1);
        BigDecimal threshold = new BigDecimal(list.size()).divide(new BigDecimal(availableProcessors + 5), 0, BigDecimal.ROUND_UP);
        int beginIndexForCal = 0;
        int endIndexForCal = 0;
        List<Future<List>> resultList = new CopyOnWriteArrayList<Future<List>>();
        List<TradeInfo> diffList = new CopyOnWriteArrayList<TradeInfo>();
        for (int i = 1; i <= (availableProcessors - 1); i++) {
            endIndexForCal += threshold.intValue();
            EMACalculator task = new EMACalculator(list, shortPeriod, longPeriod, midPeriod, beginIndexForCal, endIndexForCal, threshold, availableProcessors);
            Future<List> result = forkJoinPool.submit(task);
            resultList.add(result);
            beginIndexForCal = endIndexForCal + 1;
        }
        for (Future<List> result : resultList) {
            List<TradeInfo> taskResult = result.get();
            diffList.addAll(taskResult);
        }

        EMACalculator emaCalculator = new EMACalculator();
        BigDecimal dif = emaCalculator.calculteEMA(list, shortPeriod).subtract(emaCalculator.calculteEMA(list, longPeriod));
        BigDecimal dea = emaCalculator.calculteEMA(diffList, midPeriod);
        return dif.subtract(dea).multiply(new BigDecimal(2));

    }

    public int getShortPeriod() {
        return shortPeriod;
    }

    public void setShortPeriod(int shortPeriod) {
        this.shortPeriod = shortPeriod;
    }

    public int getMidPeriod() {
        return midPeriod;
    }

    public void setMidPeriod(int midPeriod) {
        this.midPeriod = midPeriod;
    }

    public int getLongPeriod() {
        return longPeriod;
    }

    public void setLongPeriod(int longPeriod) {
        this.longPeriod = longPeriod;
    }

    public static int getSmaPeriod() {
        return smaPeriod;
    }

    public static void setSmaPeriod(int smaPeriod) {
        Core.smaPeriod = smaPeriod;
    }

    public static String getSmaDate() {
        return smaDate;
    }

    public static void setSmaDate(String smaDate) {
        Core.smaDate = smaDate;
    }
}
