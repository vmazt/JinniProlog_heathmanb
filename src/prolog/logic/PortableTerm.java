package prolog.logic;

import java.util.Arrays;
import static prolog.logic.Defs.GETSYMNO;
import static prolog.logic.Defs.PUTSYMNO;
import static prolog.logic.Defs.isIDENTIFIER;

/**
 * Compact binary representation of a Prolog term which has its own symbol table
 * and is designed to be consistent with saving to persistent storage.
 */
public class PortableTerm implements Stateful {

    private final Object[] refs;
    private final int cells[];

    /**
     * creates a PortableTerm from a bundle made of refs and cells - seen as an
     * Object[2] array with no reference to internals of this package - such
     * that a serialized bundle can be generically read in by this program
     *
     * @param bundle
     * @param cloned
     */
    public PortableTerm(Object[] bundle, int cloned) {
        if (0 == cloned) {
            this.refs = ((Object[]) bundle[0]);
            this.cells = ((int[]) bundle[1]);
        } else {
            this.refs = ((Object[]) bundle[0]).clone();
            this.cells = ((int[]) bundle[1]).clone();
        }
    }

    /**
     * creates a bundle made of refs and cells - seen as an Object[2] array with
     * no reference to internals of this package - such that a serialized term
     * of this form can be generically read in by any other program
     *
     * @param cloned
     * @return
     */
    public Object[] export(int cloned) {
        Object[] bundle = new Object[2];
        if (0 == cloned) {
            bundle[0] = refs;
            bundle[1] = cells;
        } else {
            bundle[0] = refs.clone();
            bundle[1] = cells.clone();
        }
        return bundle;
    }

    /**
     *
     * @param encoded
     * @param prolog
     */
    public PortableTerm(EncodedTerm encoded, Prolog prolog) {
        cells = encoded.getCells();
        refs = inputFrom(prolog);
        //code=computeHashCode();
    }

    /**
     * succeeds if both are ground terms with same hash key or if one of them is
     * non-ground
     *
     * this means that ground facts would look as equal to terms having
     * variables or the same hash key - in both cases, once equals succeeds,
     * unification should be called to see if the two terms are really unifiable
     *
     * it is the case though, that if equals fails the terms are NOT unifiable -
     * this makes search cheaper
     */

    /*
     public boolean equals(Object other) {
     if(!(other instanceof PortableTerm)) return false;
     int ocode=((PortableTerm)other).hashCode();
     return code<0 || ocode<0 || code==ocode;
     }
  
     public final int hashCode() {
     return code;
     }
  
     public int computeHashCode() {
     //Prolog.dump("here!!!"+this+(new Integer(-33).hashCode()));
     int s=0;
     int l=cells.length;
     for(int i=1;i<l;i++) { // skip addr 0;
     int xval=cells[i];
     if(Defs.isVAR(xval)) {
     if(cells[xval]==xval) return -1;
     else continue;
     }
     else if(Defs.isIDENTIFIER(xval)) {
     int ires=Defs.GETSYMNO(xval);
     Object O=refs[ires];
     if(!(O instanceof String)) continue;
     s=s ^ AtomTable.string_hash((String)O,Defs.GETARITY(xval));
     }
     else
     s=s^Defs.OUTPUT_INT(xval);  
     }
     s=Math.abs(s);
     s=s&((1<<29)-1);
     return s;
     }
     */
    private Object[] inputFrom(Prolog prolog) {
        // produce refs and adjust cells
        ObjectDict symTable = new ObjectDict();
        ObjectStack refStack = new ObjectStack();
        for (int i = 0; i < cells.length; i++) {
            int xval = cells[i];
            if (isIDENTIFIER(xval)) {
                Object O = prolog.atomTable.getSymObject(xval);
                Object XSymNo = symTable.get(O);
                int o;
                if (null == XSymNo) {
                    refStack.push(O);
                    o = refStack.getTop();
                    symTable.put(O, o);
                } else {
                    Integer SymNo = (Integer) XSymNo;
                    o = SymNo;
                }
                cells[i] = PUTSYMNO(xval, o);
            }
        }
        return refStack.toArray();
    }

    /**
     *
     * @param prolog
     * @return
     */
    public EncodedTerm outputTo(Prolog prolog) {
    // adjust cells
        //Prolog.dump("PT:"+this);
        final int[] _cells = this.cells.clone();
        final int[] result = Arrays.stream(_cells).map(
                xval -> {
            if (isIDENTIFIER(xval)) {
                int ires = GETSYMNO(xval);
                int symno = prolog.atomTable.addObject(refs[ires]);
                return PUTSYMNO(xval, symno);
            } else {
                return xval;
            }
                }).toArray();
        return new EncodedTerm(result);
    }

    @Override
    public String toString() {
        String scells = (new EncodedTerm(cells)).toString();
        if (refs.length == 0) {
            return "[]=>" + scells;
        }
        StringBuilder buf = new StringBuilder(4 * refs.length + scells.length());
        buf.append("[0:").append(refs[0]);
        for (int i = 1; i < refs.length; i++) {
            buf.append(",").append(i).append(":").append(refs[i]);
        }
        buf.append("]=>");
        buf.append(scells);
        return buf.toString();
    }

    /**
     *
     * @param T
     * @param prolog
     * @return
     */
    static public EncodedTerm test(EncodedTerm T, Prolog prolog) {
        //Prolog.dump("encoded="+T+" size="+T.size());
        PortableTerm PT = new PortableTerm(T, prolog);
        //Prolog.dump("portable="+PT);
        return PT.outputTo(prolog);
    }
}
