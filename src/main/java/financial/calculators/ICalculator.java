package financial.calculators;

import financial.utils.TradeInfo;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.RecursiveTask;

/**
 * Created by f507303 on 11/16/2014.
 */
public interface ICalculator {
    public BigDecimal calculateSMA(int periods) ;
    public BigDecimal calculteEMA(final List<TradeInfo> list, final int number);
}
