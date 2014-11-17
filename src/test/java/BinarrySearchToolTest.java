import financial.utils.BinarySearchTool;
import financial.utils.TradeInfo;
import org.junit.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: f507303
 * Date: 11/15/14
 * Time: 3:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class BinarrySearchToolTest {
    @Test
    public void indexedBinarySearchTest() throws Exception{
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<TradeInfo> priceResults = new ArrayList<TradeInfo>();

        TradeInfo tradeInfo01 = new TradeInfo();
        TradeInfo tradeInfo02 = new TradeInfo();
        TradeInfo tradeInfo03 = new TradeInfo();
        TradeInfo tradeInfo04 = new TradeInfo();
        TradeInfo tradeInfo05 = new TradeInfo();
        TradeInfo tradeInfo06 = new TradeInfo();
        TradeInfo tradeInfo07 = new TradeInfo();
        TradeInfo tradeInfo08 = new TradeInfo();

        tradeInfo01.setTradeDate(formatter.parse("2014-11-15 02:31:00"));
        tradeInfo02.setTradeDate(formatter.parse("2014-11-15 02:32:00"));
        tradeInfo03.setTradeDate(formatter.parse("2014-11-15 02:33:00"));
        tradeInfo04.setTradeDate(formatter.parse("2014-11-15 02:34:00"));
        tradeInfo05.setTradeDate(formatter.parse("2014-11-15 02:34:00"));
        tradeInfo06.setTradeDate(formatter.parse("2014-11-15 02:34:00"));
        tradeInfo07.setTradeDate(formatter.parse("2014-11-15 02:37:00"));
        tradeInfo08.setTradeDate(formatter.parse("2014-11-15 02:38:00"));

        priceResults.add(tradeInfo01);
        priceResults.add(tradeInfo02);
        priceResults.add(tradeInfo03);
        priceResults.add(tradeInfo04);
        priceResults.add(tradeInfo05);
        priceResults.add(tradeInfo06);
        priceResults.add(tradeInfo07);
        priceResults.add(tradeInfo08);


        Comparator<TradeInfo> dateComparator = new Comparator<TradeInfo>() {
            public int compare(TradeInfo trade1, TradeInfo trade2) {

                return trade1.getTradeDate().compareTo(trade2.getTradeDate());
            }
        };
        String dateStr="2014-11-15 02:34:00";

        Date date = new Date();
        try {
            date = formatter.parse(dateStr);
        }catch(Exception e ){
            e.printStackTrace();
        }
        TradeInfo trade = new TradeInfo();
        trade.setTradeDate(date);
        int i = BinarySearchTool.indexedBinarySearch(priceResults, trade, dateComparator);
        Assert.assertEquals(3,i);
    }
}
