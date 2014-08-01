package prolog.logic;

import static java.lang.System.arraycopy;
import static prolog.logic.Prolog.dump;

/**
Dynamic Stack for int data.
 */
public class IntStack implements Stateful {
  private int stack[];
  private int top;
  
    /**
     *
     */
    public static int SIZE=16; // power of 2

    /**
     *
     */
    public static int MINSIZE=1<<15; // power of 2
  
    /**
     *
     */
    public IntStack() {
    this(SIZE);
  }
  
    /**
     *
     * @param size
     */
    public IntStack(int size) {
    stack=new int[size];
    clear();
  }
  
    /**
     *
     * @param array
     */
    public IntStack(int[] array) {
    this(array.length);
        arraycopy(array,0,stack,0,array.length);
    setTop(array.length-1);
  }

  final int getTop() {
    return top;
  }
  
  final int getFree() {
    return stack.length-size();
  }
   
  final int setTop(int top) {
    return this.top=top;
  }
   
  void clear() {
    top= -1;
  }
  
  void destroy() {
    clear();
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
   * Pushes an element - top is incremented frirst than the
   * element is assigned. This means top point to the last assigned
   * element - which can be returned with peek().
     * @param i
   */
  public final void push(int i) {
    //IO.dump("push:"+i);
    ++top;
    try {
      stack[top]=i;  
    }
    catch(ArrayIndexOutOfBoundsException ignore) {
      // add MAXSIZE checking for more robust server apps
      expand();
      stack[top]=i;
    }
    // alternative implementation:
    //if(++top>=stack.length) expand();
    //stack[top]=i;
  }

    /**
     *
     * @return
     */
    public final  int pop() {
    return stack[top--];
  }
  
    /**
     *
     * @return
     */
    public final  int peek() {
    return stack[top];
  }
  
    /**
     *
     * @param i
     * @return
     */
    public final  int at(int i) {
    return stack[i];
  }
  
    /**
     *
     * @param val
     * @return
     */
    public final int find(int val) {
    for(int i=0;i<size();i++) {
      if(stack[i]==val) {
          return i;
            }
    }
    return -1;
  }
  
    /**
     *
     * @param val
     * @return
     */
    public final int del_swap(int val) {
    int i=find(val);
    if(i>=0) {
      int tval=pop();
      if(i<=top) {
          update(i,tval);
            }
    }
    return i;
  }
  
    /**
     *
     * @param i
     * @param val
     */
    public final  void update(int i,int val) {
    stack[i]=val;
  }
  
    /**
     *
     * @return
     */
    public final int size() {
    return top+1;
  }
  
  /**
   * dynamic array operation: doubles when full
   */
  protected void expand() {
    int l=stack.length;
    int[] newstack=new int[l<<1];
    if(PrologGC.trace>=2) {
            dump("IntStack: expanding to:"+(l<<1));
        }
        arraycopy(stack,0,newstack,0,l);
    stack=newstack;
  }
  
  /**
  * dynamic array operation: shrinks to 1/2 if more than than 3/4 empty
  */
  protected void shrink() {
    int l=stack.length;
    if(l<=MINSIZE || top<<2>=l) {
        return;
        }
    l=1+(top<<1); // still means shrink to at 1/2 or less of the heap
    if(top<MINSIZE) {
        l=MINSIZE;
        }
    if(PrologGC.trace>=2) {
            dump("IntStack shrinking to: "+l);
        }
    int[] newstack=new int[l];
        arraycopy(stack,0,newstack,0,top+1);
    stack=newstack;
  }
  
    /**
     *
     * @return
     */
    public int[] toArray() {
    int[] array=new int[size()];
    //Prolog.dump("toArray:"+size());
    if(size()>0) {
            arraycopy(stack, 0, array, 0, size());
        }
    return array;
  }
  
    /**
     *
     * @return
     */
    public byte[] toByteArray() {
    byte[] array=new byte[size()];
    //Prolog.dump("toArray:"+size());
    for(int i=0;i<size(); i++) {
      array[i]=(byte)stack[i];
    }
    return array;
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
  
  /** Used for removing unused bindings from the trail.
  * When backtracking to the current choice point to start a new clause
  * bindings may safely be removed if they are above the current heap top.
  *
  *<p>This is an optional operation saving a lot of trail space,
  * but it can cost a small amount of extra time.
  */
  final void compact(int from, int heapTop) {
	  from++;
    while (from <= top) {
      if (stack[from]<=heapTop) {
		    from++; // keep
		  }    
		  else {
          stack[from] = stack[top--]; // remove
            }
	  }
	}
  
    /**
     *
     * @param heap
     * @return
     */
    public int toList(HeapStack heap) {
    return toList(heap,heap.prolog.G_NIL);
  }
  
    /**
     *
     * @param heap
     * @param tail
     * @return
     */
    public int toList(HeapStack heap,int tail) {
    if(isEmpty()) {
        return tail;
        }
    int h=heap.getHeapTop()+1;
    for(int i=0; i<=top; i++) {
      heap.pushList(stack[i]);
    }
    heap.pushTerm(tail);
    return h;
  }
}
