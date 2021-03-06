package prolog.core;

import prolog.kernel.*;
import static prolog.kernel.Machine.double2string;
import prolog.logic.*;

/**
 *
 * @author Brad
 */
public class RankedData implements Stateful {

    /**
     *
     */
    public final Object data;

    /**
     *
     */
    public double rank;

    /**
     *
     */
    public int component;

    /**
     *
     */
    public int hyper;

    /**
     *
     */
    public int color;

    /**
     *
     * @param data
     */
    public RankedData(Object data) {
        this.data = data;
        clear();
    }

    /**
     *
     */
    public final void clear() {
        this.rank = 0.0;
        this.hyper = 0;
        this.component = -1;
        this.color = 0;
    }

    @Override
    public String toString() {
        String s = "";
        if (null != data) {
            s = data.toString();
        }
        return "<" + s
                + ">:(r=" + double2string(rank, 2)
                + ",h=" + hyper
                + ",c=" + component
                + ",x=" + color
                + ")";
    }
}
