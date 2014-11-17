package financial.utils;

import au.com.bytecode.opencsv.CSVParser;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Patrick Lo
 * Date: 11/13/14
 * Time: 3:59 PM
 * To change this template use File | Settings | File Templates.
 */


public class CSVReader {
    static Logger log = Logger.getLogger(CSVReader.class.getName());
    public List<TradeInfo> readCSV(String filePath) throws IOException {
        final InputStream stream = this.getClass().getResourceAsStream(filePath);
        List<TradeInfo> tradeReportSet = new ArrayList<TradeInfo>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        CSVParser parser = new CSVParser();
        String line;
        int row = 0;
        log.info("Start reading CSV.." + Calendar.getInstance().getTime());
        while ((line = reader.readLine()) != null) {
            row++;
            if (row > 1) {
                TradeInfo singleTradeInfo = new TradeInfo();
                final String[] fields = parser.parseLine(line);
                singleTradeInfo.setPrice(new BigDecimal(fields[2]));
                singleTradeInfo.setStock(fields[1]);
                singleTradeInfo.setQuantity(Long.parseLong(fields[3]));
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
                try {
                    Date date = formatter.parse(fields[0]+fields[5]);
                    singleTradeInfo.setTradeDate(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                tradeReportSet.add(singleTradeInfo);
            }
        }
        log.info("Complete reading CSV.." + Calendar.getInstance().getTime());
        return tradeReportSet;
    }

    public static void main(String[] args)throws IOException{
        List<TradeInfo> priceResults = new CSVReader().readCSV("/RIOTrades.csv");
        log.info("Results size=" + priceResults.size());
    }
}
