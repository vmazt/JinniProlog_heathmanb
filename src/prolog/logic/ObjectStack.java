package prolog.logic;

import static java.lang.System.arraycopy;
import static prolog.logic.Prolog.dump;

/**
Generic Dynamic Stack.
 */
public class ObjectStack implements Stateful {
  private Object stack[];
  private int top;
  
    /**
     *
     */
    public static final int SIZE=16; // power of 2

    /**
     *
     */
    public static final int MINSIZE=1<<15; // power of 2
                            
    /**
     *
     */
    public ObjectStack() {
    this(SIZE);
  }
  
    /**
     *
     * @param size
     */
    public ObjectStack(int size) {
    reset();
    stack=new Object[size];
  }
  
    /**
     *
     * @param os
     */
    public ObjectStack(Object[] os) {
    reset();
    stack=os;
  }
   
  private void reset() {
    top= -1;
  }
  
    /**
     *
     */
    public void clear() {
    reset();
    // stack=new Object[stack.length]; // introduces bug at end of bm2
  }
  
  void destroy() {
    reset();
    //Prolog.dump("destroying ObjectStack of length: "+stack.length);
    stack=null;
  }
   
    /**
     *
     * @return
     */
    public final boolean isEmpty() {
    return top<0;
  }
  
    /**
     *
     * @param i
     */
    public final void push(Object i) {
    ++top;
    try {
      stack[top]=i;  
    }
    catch(Exception ignore) {
      expand();
      stack[top]=i;
    }
  }

    /**
     *
     * @return
     */
    public final Object pop() {
    Object o=stack[top];
    stack[top--]=null;
    return o;
  }
  
    /**
     *
     * @param i
     * @return
     */
    public final Object at(int i) {
    return stack[i];
  }
  
  final Object peek() {
    return stack[top];
  }
  
    /**
     *
     * @return
     */
    public final int size() {
    return top+1;
  }
  
  final int getFree() {
    return stack.length-size();
  }
    
  final int getTop() {
    return top;
  }
  
  final void setTop(int top) {
    for(int i=top+1;i<this.top;i++) {
        stack[i]=null; // DO THIS - it prevents memory leak
        }    this.top=top;
  }
  
    /**
     *
     */
    protected void expand() {
    int l=stack.length;
    Object[] newstack=new Object[l<<1];
    if(PrologGC.trace>=2) {
            dump("ObjectStack shrinking: "+(l<<1));
        }
        arraycopy(stack,0,newstack,0,l);
    stack=newstack;
  }
  
  /**
  * dynamic array operation: shrinks to 1/2 if more than than 3/4 empty
  */
  final void shrink() {
    int l=stack.length;
    if(l<=MINSIZE || top<<2>=l) {
        return;
        }
    l=1+(top<<1);
    if(top<MINSIZE) {
        l=MINSIZE;
        }
    if(PrologGC.trace>=2) {
            dump("ObjectStack shrinking: "+l);
        }
    Object[] newstack=new Object[l];
        arraycopy(stack,0,newstack,0,top+1);
    stack=newstack;
  }
  
    /**
     *
     * @return
     */
    public final Object[] toArray() {
    Object[] newstack=new Object[top+1];
        arraycopy(stack,0,newstack,0,top+1);
    return newstack;
  }
  
  public String toString() {
    if(isEmpty()) {
        return "[]";
        }
    StringBuilder b=new StringBuilder(top<<2);
    b.append("[");
    for(int i=0; i<=top; i++) {
      if(i==0) {
          b.append("").append(stack[i]);
            } else {
          b.append(",").append(stack[i]);
            }
    }
    b.append("]");
    return b.toString();
  }
}
