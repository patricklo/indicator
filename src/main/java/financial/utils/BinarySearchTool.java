package financial.utils;

import org.apache.log4j.Logger;

import java.util.Comparator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Patrick Lo
 * Date: 11/15/14
 * Time: 3:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class BinarySearchTool {
    static Logger log = Logger.getLogger(BinarySearchTool.class.getName());
    public static <T> int indexedBinarySearch(List<? extends T> l, T key, Comparator<? super T> c) {
        int low = 0;
        int high = l.size() - 1;
        int preCmp = 0;
        int mid = (low + high) >>> 1;
        while (low <= high) {
            T midVal = l.get(mid);
            int cmp = c.compare(midVal, key);

            if (cmp < 0) {
                if (preCmp > 0) {
                    log.info("find the date between index[" + preCmp+"] and ["+mid+"]");
                    return mid; // find the index where between > and <
                }
                low += 1;
                mid += 1;
            } else if (cmp > 0) {
                if (preCmp < 0) {
                    log.info("find the date between index[" + preCmp+"] and ["+mid+"]");
                    return mid; // find the index where between > and <
                }
                log.info("cmp <0 mid=" + mid);
                high -= 1;
                mid -= 1;
            } else {
                log.info("find the date at index[" + mid+"]");
                return mid; // key found
            }
            preCmp = cmp;// save the last compare result
        }
        return -(low + 1);  // key not found
    }
}
