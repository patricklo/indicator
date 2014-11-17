package financial.calculators.impl;

import financial.calculators.ICalculator;
import financial.utils.TradeInfo;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

/**
 * Created with IntelliJ IDEA.
 * User: f507303
 * Date: 11/15/14
 * Time: 1:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class SMACalculator extends RecursiveTask<BigDecimal> {
    private static final int THRESHOLD = 100;
    private final List<TradeInfo> priceList;

    public SMACalculator(List<TradeInfo> priceList) {
        this.priceList = priceList;
    }

    @Override
    protected BigDecimal compute() {
        BigDecimal sum = BigDecimal.ZERO;
        int size = priceList.size();
        boolean canCompute = (size <= THRESHOLD);
        if (canCompute) {
            for (TradeInfo tradeInfo:priceList) {
                sum = sum.add(tradeInfo.getPrice());
            }
        } else {
            int middle = size / 2;
            SMACalculator leftTask = new SMACalculator(priceList.subList(0, middle));
            SMACalculator rightTask = new SMACalculator(priceList.subList(middle + 1, size));
            leftTask.fork();
            rightTask.fork();
            BigDecimal leftResult = leftTask.join();
            BigDecimal rightResult = rightTask.join();
            sum = leftResult.add(rightResult);
        };
        return sum;
    }

    public BigDecimal calculateSMA(int periods) throws InterruptedException, ExecutionException {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        SMACalculator task = new SMACalculator(priceList);
        Future<BigDecimal> result = forkJoinPool.submit(task);
        return result.get().divide(new BigDecimal(periods),3,BigDecimal.ROUND_HALF_UP);
    }
}
