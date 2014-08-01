package rli;

import static java.lang.System.setProperty;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import static java.rmi.registry.LocateRegistry.createRegistry;
import static java.rmi.registry.LocateRegistry.getRegistry;
import java.rmi.server.UnicastRemoteObject;
import static java.rmi.server.UnicastRemoteObject.exportObject;
import static java.rmi.server.UnicastRemoteObject.unexportObject;
import prolog.kernel.Top;
import prolog.kernel.Machine;
import prolog.logic.Prolog;
import prolog.kernel.PrologReader;
import prolog.kernel.PrologWriter;
import prolog.logic.ObjectDict;
import prolog.core.Hub;
import static prolog.kernel.Top.new_machine;
import static prolog.logic.Prolog.getDefaultProlog;

/**
 *
 * @author Brad
 */
public class RLIServer implements PrologService {

  private final static ObjectDict dict=new ObjectDict();

  private static int lastid=0;

  private String portName;

  private Prolog prolog;

  private PrologReader reader;

  private PrologWriter writer;

  private Machine machine;

  private int id;

  private Hub hub;

    /**
     *
     * @param portName
     * @param prolog
     * @param reader
     * @param writer
     */
    public RLIServer(String portName,Prolog prolog,PrologReader reader,
      PrologWriter writer){
    this.portName=portName;
    this.prolog=prolog;
    this.reader=reader;
    this.writer=writer;
    this.machine=null;
    assignId();
  }

    /**
     *
     * @return
     */
    public String getPort() {
    return portName;
  }

  private void assignId() {
    Integer I=(Integer)dict.get(getPort());
    if(null==I) {
      this.id=++lastid;
      dict.put(getPort(), id);
    } else {
      this.id=I;
    }
  }

  /**
   * equivalent to rmiregistry.exe creates a registry if not already created
   */
  public void create_registry() {
    try {
            setProperty("java.rmi.server.codebase","file:/bin/prolog.jar");
            createRegistry(Registry.REGISTRY_PORT);
    } catch(RemoteException re) {
      // System.err.println("Registry Creation exception: "+ re.toString());
    } catch(Exception e) {
      System.err.println("Registry Creation error: "+e.toString());
    }
    // }
    // try {
    // Thread.sleep(1000);
    // }
    // catch(Exception e) {
    // }
  }

    /**
     *
     * @return
     */
    public int start() {
    create_registry();
    try { // ?? maybe the stub needs to be returned?
      PrologService stub=(PrologService)exportObject(this,0);

      // (Re)Bind the remote object's stub in the registry
      Registry registry=getRegistry();
      registry.rebind(this.portName,stub);

      // System.err.println("Starting Server: " + this.portName);
    } catch(Exception e) {
      System.err.println("Server start exception: "+e.toString()+",port="+portName);
      if(RLIAdaptor.trace) {
          e.printStackTrace();
            }
      System.err.println("Please make sure you have started rmiregistry");
      return -1;
    }
    if(null==hub) {
        hub=new Hub(0L);
        }
    return this.id;
  }

    /**
     *
     * @return
     */
    public Object rli_in() {
    return hub.collect();
  }

    /**
     *
     * @param T
     */
    public void rli_out(Object T) {
    hub.putElement(T);
    // (new Producer(hub,T)).send();
  }

  /*
   * public void stop() { new Thread(this).start(); }
   * 
   * public void run() { try { Thread.sleep(2000); } catch(InterruptedException
   * e) {} bg_stop(); //System.exit(0); }
   */

    /**
     *
     * @return
     */
    

  public boolean bg_stop() {
    System.err.println("Stopping Server: "+this.portName);
    try {
      Registry registry=getRegistry();
      registry.unbind(this.portName);
      return unexportObject(this,true);
    } catch(Exception e) {
      System.err.println("Server stop exception: "+e.toString());
      e.printStackTrace();
      return false;
    }
  }

  // //

    /**
     *
     * @param query
     * @return
     */
      public Object serverCallProlog(Object query) {
    Object R=null;
    //// if(true) return R; // exhibits genuine RMI memory leak !!!

    if(null==query) { // this stops server
      bg_stop();
      return null;
    }

    if(null==this.prolog) {
      this.prolog=getDefaultProlog();
    }

    // if machine is reused, new tasks will stop previous tasks - unexpected
    // behavior !!!

    if(machine!=null) {
        machine.stop(); // make sure no memory leaks !!!
        }    machine=new_machine(this.prolog,reader,writer); // cached
    machine.set_instance_id(this.id); // it seems to be the case anyway ???
    R=machine.query_engine(query); // does work on server

    // System.err.println(getPort()+"id="+machine.get_instance_id()+"=>"+machine.hashCode()
    // +":"+query+"=>"+R);
    return R;
  }
}

