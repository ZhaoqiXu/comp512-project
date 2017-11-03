package MidImpl;
import ResInterface.*;
import MidInterface.*;
import LockManager.*;

import java.util.*;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RMISecurityManager;

public class MiddleWareImpl implements MiddleWare 
{
    public static final int READ = 0;
    public static final int WRITE = 1;
    protected LockManager mw_locks;
    protected int txn_counter; // should be moved to TM
    protected ArrayList<Integer> active_txn; // should be moved to TM
	static ResourceManager rm_flight = null;
    static ResourceManager rm_car = null;
    static ResourceManager rm_room = null;
    static TransactionManager txn_manager = null;

    protected RMHashtable m_itemHT = new RMHashtable();

    public static void main(String args[]) {
        // Figure out where server is running
        String server_flight = "localhost";
        String server_car = "localhost";
        String server_room = "localhost";

        int port_local = 1088;
        int port = 2199;


        if (args.length == 3) {
            server_flight = args[0];
            server_car = args[1];
            server_room = args[2];
            //port = Integer.parseInt(args[1]);
        } else if (args.length != 3) {
            System.err.println ("Wrong usage");
            System.out.println("Usage: java MidImpl.MiddleWareImpl [server_flight] [server_car] [server_room]");
            System.exit(1);
        }

        try {
            // create a new Server object
            MiddleWareImpl obj = new MiddleWareImpl();
            // dynamically generate the stub (client proxy)
            MiddleWare mw = (MiddleWare) UnicastRemoteObject.exportObject(obj, 0);            
            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry(port_local);
            registry.rebind("TripMiddleWare", mw);

            // get a reference to the rmiregistry
            Registry registry_flight = LocateRegistry.getRegistry(server_flight, port);
            Registry registry_car = LocateRegistry.getRegistry(server_car, port);
            Registry registry_room = LocateRegistry.getRegistry(server_room, port);

            // get the proxy and the remote reference by rmiregistry lookup
            rm_flight = (ResourceManager) registry_flight.lookup("FlightRM");
            rm_car = (ResourceManager) registry_car.lookup("CarRM");
            rm_room = (ResourceManager) registry_room.lookup("RoomRM");

            
            if(rm_flight!=null)
            {
                System.out.println("Successful");
                System.out.println("Connected to Flight RM");
            }
            else
            {
                System.out.println("Unsuccessful connection to Flight RM");
            }

            if(rm_car!=null)
            {
                System.out.println("Successful");
                System.out.println("Connected to Car RM");
            }
            else
            {
                System.out.println("Unsuccessful connection to Car RM");
            }

            if(rm_room!=null)
            {
                System.out.println("Successful");
                System.out.println("Connected to Room RM");
            }
            else
            {
                System.out.println("Unsuccessful connection to Room RM");
            }
            // make call on remote method


            System.err.println("MiddleWare Server ready");
        } catch (Exception e) {
            System.err.println("MiddleWare Server exception: " + e.toString());
            e.printStackTrace();
        }

        // Create and install a security manager
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
    }
     
    public MiddleWareImpl() throws RemoteException {
        this.mw_locks = new LockManager();
        this.txn_counter = 0; // should be moved to TM
        this.active_txn = new ArrayList<Integer>(); // should be moved to TM
        this.txn_manager = new TransactionManager();
    }

    // Reads a data item
    private RMItem readData( int id, String key )
    {
        synchronized(m_itemHT) {
            return (RMItem) m_itemHT.get(key);
        }
    }

    // Writes a data item
    private void writeData( int id, String key, RMItem value )
    {
        synchronized(m_itemHT) {
            m_itemHT.put(key, value);
        }
    }
    
    // Remove the item out of storage
    protected RMItem removeData(int id, String key) {
        synchronized(m_itemHT) {
            return (RMItem)m_itemHT.remove(key);
        }
    }
    
    // Create a new flight, or add seats to existing flight
    //  NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
    public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice)
        throws RemoteException
    {
        return rm_flight.addFlight(id,flightNum,flightSeats,flightPrice);
    }


    
    public boolean deleteFlight(int id, int flightNum)
        throws RemoteException
    {
        return rm_flight.deleteFlight(id, flightNum);
    }



    // Create a new room location or add rooms to an existing location
    //  NOTE: if price <= 0 and the room location already exists, it maintains its current price
    public boolean addRooms(int id, String location, int count, int price)
        throws RemoteException
    {
    	return rm_room.addRooms(id, location, count, price);
    }

    // Delete rooms from a location
    public boolean deleteRooms(int id, String location)
        throws RemoteException
    {
        return rm_room.deleteRooms(id, location);
    }

    // Create a new car location or add cars to an existing location
    //  NOTE: if price <= 0 and the location already exists, it maintains its current price
    public boolean addCars(int id, String location, int count, int price)
        throws RemoteException
    {
        return rm_car.addCars(id, location, count, price);
    }


    // Delete cars from a location
    public boolean deleteCars(int id, String location)
        throws RemoteException
    {
    	return rm_car.deleteCars(id, location);
    }



    // Returns the number of empty seats on this flight
    public int queryFlight(int id, int flightNum)
        throws RemoteException
    {
    	return rm_flight.queryFlight(id, flightNum);
    }

    // Returns the number of reservations for this flight. 
//    public int queryFlightReservations(int id, int flightNum)
//        throws RemoteException
//    {
//        Trace.info("RM::queryFlightReservations(" + id + ", #" + flightNum + ") called" );
//        RMInteger numReservations = (RMInteger) readData( id, Flight.getNumReservationsKey(flightNum) );
//        if ( numReservations == null ) {
//            numReservations = new RMInteger(0);
//        } // if
//        Trace.info("RM::queryFlightReservations(" + id + ", #" + flightNum + ") returns " + numReservations );
//        return numReservations.getValue();
//    }


    // Returns price of this flight
    public int queryFlightPrice(int id, int flightNum )
        throws RemoteException
    {
    	return rm_flight.queryFlightPrice(id, flightNum);
    }


    // Returns the number of rooms available at a location
    public int queryRooms(int id, String location)
        throws RemoteException
    {
    	return rm_room.queryRooms(id, location);
    }


    
    // Returns room price at this location
    public int queryRoomsPrice(int id, String location)
        throws RemoteException
    {
    	return rm_room.queryRoomsPrice(id, location);
    }


    // Returns the number of cars available at a location
    public int queryCars(int id, String location)
        throws RemoteException
    {
    	return rm_car.queryCars(id, location);
    }


    // Returns price of cars at this location
    public int queryCarsPrice(int id, String location)
        throws RemoteException
    {
    	return rm_car.queryCarsPrice(id, location);
    }

    // Returns data structure containing customer reservation info. Returns null if the
    //  customer doesn't exist. Returns empty RMHashtable if customer exists but has no
    //  reservations.
    // public RMHashtable getCustomerReservations(int id, int customerID)
    //     throws RemoteException
    // {
    //    return rm.getCustomerReservations(id, customerID);
    // }

    // return a bill
    public String queryCustomerInfo(int id, int customerID)
        throws RemoteException
    {
        try {
            Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + ") called" );
            if (!mw_locks.Lock(id, Customer.getKey(customerID), READ))
            {
                Trace.warn("RM::Lock failed--Can not acquire lock");
                return "";
            }
            Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
            if ( cust == null ) {
                Trace.warn("RM::queryCustomerInfo(" + id + ", " + customerID + ") failed--customer doesn't exist" );
                return "";   // NOTE: don't change this--WC counts on this value indicating a customer does not exist...
            } else {
                    String s = cust.printBill();
                    Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + "), bill follows..." );
                    System.out.println( s );
                    return s;
            }
        }
        catch (DeadlockException dle) {
            Trace.warn("RM::Lock failed--Deadlock exist");
            return "";
        }
    }

    // customer functions
    // new customer just returns a unique customer identifier
    
    public int newCustomer(int id)
        throws RemoteException
    {
        try {
            Trace.info("INFO: RM::newCustomer(" + id + ") called" );
            // Generate a globally unique ID for the new customer
            int cid = Integer.parseInt( String.valueOf(id) +
                                    String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
                                    String.valueOf( Math.round( Math.random() * 100 + 1 )));
            Customer cust = new Customer( cid );
            if (!mw_locks.Lock(id, cust.getKey(), WRITE))
            {
                Trace.warn("RM::Lock failed--Can not acquire lock");
                return -1;
            }
            writeData( id, cust.getKey(), cust );
            Trace.info("RM::newCustomer(" + cid + ") returns ID=" + cid );
            return cid;
        }
        catch (DeadlockException dle) {
            Trace.warn("RM::Lock failed--Deadlock exist");
            return -1;
        }
    }

    // I opted to pass in customerID instead. This makes testing easier
    public boolean newCustomer(int id, int customerID )
        throws RemoteException
    {
        try {
        	Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") called" );
            if (!mw_locks.Lock(id, Customer.getKey(customerID), WRITE))
            {
                Trace.warn("RM::Lock failed--Can not acquire lock");
                return false;
            }
            Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
            if ( cust == null ) {
                cust = new Customer(customerID);
                // if (!mw_locks.Lock(id, cust.getKey(), WRITE))
                // {
                //     Trace.warn("RM::Lock failed--Can not acquire lock");
                //     return false;
                // }
                writeData( id, cust.getKey(), cust );
                Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") created a new customer" );
                return true;
            } else {
                Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") failed--customer already exists");
                return false;
            } // else
        }
        catch (DeadlockException dle) {
            Trace.warn("RM::Lock failed--Deadlock exist");
            return false;
        }
    }


    // Deletes customer from the database. 
    public boolean deleteCustomer(int id, int customerID)
        throws RemoteException
    {
        try {
        	Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") called" );
            if (!mw_locks.Lock(id, Customer.getKey(customerID), WRITE))
            {
                Trace.warn("RM::Lock failed--Can not acquire lock");
                return false;
            }
            Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
            if ( cust == null ) {
                Trace.warn("RM::deleteCustomer(" + id + ", " + customerID + ") failed--customer doesn't exist" );
                return false;
            } else {            
                // Increase the reserved numbers of all reservable items which the customer reserved. 
                RMHashtable reservationHT = cust.getReservations();
                for (Enumeration e = reservationHT.keys(); e.hasMoreElements();) {        
                    String reservedkey = (String) (e.nextElement());
                    ReservedItem reserveditem = cust.getReservedItem(reservedkey);
                    int reservedCount = reserveditem.getCount();

                    switch (reservedkey.charAt(0)) {
                    	case 'c':
                    		rm_car.freeItemRes(id, customerID, reservedkey, reservedCount);
                    		break;
                    	case 'f':
                    		rm_flight.freeItemRes(id, customerID, reservedkey, reservedCount);
                    		break;
                    	case 'r':
                    		rm_room.freeItemRes(id, customerID, reservedkey, reservedCount);
                    		break;
                    	default:
                    		break;
                    }
                    
                    // Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") has reserved " + reserveditem.getKey() + " " +  reserveditem.getCount() +  " times"  );
                    // ReservableItem item  = (ReservableItem) readData(id, reserveditem.getKey());
                    // Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") has reserved " + reserveditem.getKey() + "which is reserved" +  item.getReserved() +  " times and is still available " + item.getCount() + " times"  );
                    // item.setReserved(item.getReserved()-reservedCount);
                    // item.setCount(item.getCount()+reservedCount);
                }
                
                // remove the customer from the storage
                removeData(id, cust.getKey());
                
                Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") succeeded" );
                return true;
            } // if
        }
        catch (DeadlockException dle) {
            Trace.warn("RM::Lock failed--Deadlock exist");
            return false;
        }
    }



    /*
    // Frees flight reservation record. Flight reservation records help us make sure we
    // don't delete a flight if one or more customers are holding reservations
    public boolean freeFlightReservation(int id, int flightNum)
        throws RemoteException
    {
        Trace.info("RM::freeFlightReservations(" + id + ", " + flightNum + ") called" );
        RMInteger numReservations = (RMInteger) readData( id, Flight.getNumReservationsKey(flightNum) );
        if ( numReservations != null ) {
            numReservations = new RMInteger( Math.max( 0, numReservations.getValue()-1) );
        } // if
        writeData(id, Flight.getNumReservationsKey(flightNum), numReservations );
        Trace.info("RM::freeFlightReservations(" + id + ", " + flightNum + ") succeeded, this flight now has "
                + numReservations + " reservations" );
        return true;
    }
    */

    
    // Adds car reservation to this customer. 
    public boolean reserveCar(int id, int customerID, String location)
        throws RemoteException
    {
        try {
            if (!mw_locks.Lock(id, Customer.getKey(customerID), WRITE))
            {
                Trace.warn("RM::Lock failed--Can not acquire lock");
                return false;
            }
        	Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
            String key = ("car-" + location).toLowerCase();

            if ( cust == null ) {
                Trace.warn("RM::reserveCar( " + id + ", " + customerID + ", " + key + ", "+location+")  failed--customer doesn't exist" );
                return false;
            } else {
            	if (rm_car.reserveCar(id, customerID, location) == true){
    	            cust.reserve( key, location, rm_car.queryCarsPrice(id, location));      
    	            writeData( id, cust.getKey(), cust );
    	            return true;
    	        } else {
    	        	Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " + location+") failed" );
                	return false;
    	        }
            }
        }
        catch (DeadlockException dle) {
            Trace.warn("RM::Lock failed--Deadlock exist");
            return false;
        }
    }

    // Adds room reservation to this customer. 
    public boolean reserveRoom(int id, int customerID, String location)
        throws RemoteException
    {
        try {
            if (!mw_locks.Lock(id, Customer.getKey(customerID), WRITE))
            {
                Trace.warn("RM::Lock failed--Can not acquire lock");
                return false;
            }
            Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
            String key = ("room-" + location).toLowerCase();
            if ( cust == null ) {
                Trace.warn("RM::reserveRoom( " + id + ", " + customerID + ", " + key + ", "+location+")  failed--customer doesn't exist" );
                return false;
            } else {
            	if(rm_room.reserveRoom(id, customerID, location) == true){
    	            cust.reserve( key, location, rm_room.queryRoomsPrice(id, location));      
    	            writeData( id, cust.getKey(), cust );
    	            return true;
    	        } else {
    	        	Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " + location+") failed" );
                	return false;
    	        }
            }
        }
        catch (DeadlockException dle) {
            Trace.warn("RM::Lock failed--Deadlock exist");
            return false;
        }
    }
    // Adds flight reservation to this customer.  
    public boolean reserveFlight(int id, int customerID, int flightNum)
        throws RemoteException
    {
        try {
            if (!mw_locks.Lock(id, Customer.getKey(customerID), WRITE))
            {
                Trace.warn("RM::Lock failed--Can not acquire lock");
                return false;
            }
            Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
            String key = ("flight-" + flightNum).toLowerCase();

            if ( cust == null ) {
                Trace.warn("RM::reserveFlight( " + id + ", " + customerID + ", " + key + ", "+String.valueOf(flightNum)+")  failed--customer doesn't exist" );
                return false;
            } else {
                if(rm_flight.reserveFlight(id, customerID, flightNum) == true){
                	cust.reserve( key, String.valueOf(flightNum), rm_flight.queryFlightPrice(id, flightNum));      
                	writeData( id, cust.getKey(), cust );
                	return true;
                } else {
                	Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " + flightNum+") failed" );
                	return false;
                }
            }
        }
        catch (DeadlockException dle) {
            Trace.warn("RM::Lock failed--Deadlock exist");
            return false;
        }
    }

    public RMHashtable getCustomerReservations(int id, int customerID)
        throws RemoteException
    {
        try {
            Trace.info("RM::getCustomerReservations(" + id + ", " + customerID + ") called" );
            if (!mw_locks.Lock(id, Customer.getKey(customerID), READ))
            {
                Trace.warn("RM::Lock failed--Can not acquire lock");
                return null;
            }
            Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
            if ( cust == null ) {
                Trace.warn("RM::getCustomerReservations failed(" + id + ", " + customerID + ") failed--customer doesn't exist" );
                return null;
            } else {
                return cust.getReservations();
            } // if
        }
        catch (DeadlockException dle) {
            Trace.warn("RM::Lock failed--Deadlock exist");
            return null;
        }
    }
    // Reserve an itinerary 
    public boolean itinerary(int id,int customer,Vector flightNumbers,String location,boolean car,boolean room)
        throws RemoteException
    {
        if (flightNumbers.size()==0) {
            return false;
        }
        try {
            if (!mw_locks.Lock(id, Customer.getKey(customer), WRITE))
            {
                Trace.warn("RM::Lock failed--Can not acquire lock");
                return false;
            }
            Customer cust = (Customer) readData( id, Customer.getKey(customer) );        
            if ( cust == null ) {
                return false;
            } 
            // Hashtable<Integer,Integer> f_cnt = new Hashtable<Integer,Integer>();
            // int[] flights = new int[flightNumbers.size()];
            // for (int i = 0; i < flightNumbers.size(); i++) {
            //     try {
            //         flights[i] = gi(flightNumbers.elementAt(i));
            //     }
            //     catch (Exception e){}
            // }
            // for (int i = 0; i < flightNumbers.size(); i++) {
            //     if (f_cnt.containsKey(flights[i]))
            //         f_cnt.put(flights[i], f_cnt.get(flights[i])+1);
            //     else
            //         f_cnt.put(flights[i], 1);
            // }

            // if (car) {
            //     // check if the item is available
            //     int item = rm_car.queryCars(id, location);
            //     if ( item == 0 )
            //         return false;
            // }

            // if (room) {
            //     // check if the item is available
            //     int item = rm_room.queryRooms(id, location);
            //     if ( item == 0 )
            //         return false;
            // }
            // Set<Integer> keys = f_cnt.keySet();
            // for (int key : keys) {
            //     int item = rm_flight.queryFlight(id, key);
            //     if (item < f_cnt.get(key))
            //         return false;
            // }
            String car_key = ("car-" + location).toLowerCase();
            String room_key = ("room-" + location).toLowerCase();
            boolean car_reserved = false;
            boolean room_reserved = false;
            String[] flight_key = new String[flightNumbers.size()];
            boolean[] flight_reserved = new boolean[flightNumbers.size()];
            for (int i = 0; i < flightNumbers.size(); i++ ) {
                int flightNum = Integer.parseInt((String)flightNumbers.elementAt(i));
                flight_key[i] = ("flight-" + flightNum).toLowerCase();
                flight_reserved[i] = false;
            }
            if (car) {
                car_reserved = rm_car.reserveCar(id, customer, location);
                if (!car_reserved) {
                    return false;
                }
            }
            if (room) {
                room_reserved = rm_room.reserveRoom(id, customer, location);
                if (!room_reserved) {
                    if (car_reserved) {
                        rm_car.freeItemRes(id, customer, car_key, 1);
                    }
                    return false;
                }
            }
            for (int i = 0; i < flightNumbers.size(); i++ ) {
                flight_reserved[i] = rm_flight.reserveFlight(id, customer, Integer.parseInt((String)flightNumbers.elementAt(i)));
                if (!flight_reserved[i]) {
                    if (car_reserved) {
                        rm_car.freeItemRes(id, customer, car_key, 1);
                    }
                    if (room_reserved) {
                        rm_room.freeItemRes(id, customer, room_key, 1);
                    }
                    for (int j = 0; j < i; j++ ) {
                        rm_flight.freeItemRes(id, customer, flight_key[j], 1);
                    }
                    return false;
                }
            }
            if (car_reserved) {
                cust.reserve( car_key, location, rm_car.queryCarsPrice(id, location));      
                writeData( id, cust.getKey(), cust );
            }
            if (room_reserved) {
                cust.reserve( room_key, location, rm_room.queryRoomsPrice(id, location));      
                writeData( id, cust.getKey(), cust );
            }
            for (int i = 0; i < flightNumbers.size(); i++ ) {
                int flightNum = Integer.parseInt((String)flightNumbers.elementAt(i));
                cust.reserve( flight_key[i], String.valueOf(flightNum), rm_flight.queryFlightPrice(id, flightNum));      
                writeData( id, cust.getKey(), cust );
            }
            return true;
        }
        catch (DeadlockException dle) {
            Trace.warn("RM::Lock failed--Deadlock exist");
            return false;
        }
    }
    // Convert Object to int
    public int gi(Object temp) throws Exception {
        try {
            return (new Integer((String)temp)).intValue();
        }
        catch(Exception e) {
            throw e;
        }
    }
    // Convert Object to boolean
    public boolean gb(Object temp) throws Exception {
        try {
            return (new Boolean((String)temp)).booleanValue();
            }
        catch(Exception e) {
            throw e;
            }
    }
    // Convert Object to String
    public String gs(Object temp) throws Exception {
        try {    
            return (String)temp;
            }
        catch (Exception e) {
            throw e;
            }
    }

    public boolean reserveItinerary(int id,int customer,Vector flightNumbers,String location, boolean Car, boolean Room)
        throws RemoteException
    {
        return false;
    }

    public int start() throws RemoteException {
        this.txn_counter ++;
        this.active_txn.add(txn_counter);
        return txn_counter;
    }

    public boolean commit(int transactionId) 
        throws RemoteException, TransactionAbortedException, InvalidTransactionException
    {
        if (transactionId < 1 || !this.active_txn.contains(transactionId)) {
            Trace.warn("RM::Commit failed--Invalid transactionId");
            throw new InvalidTransactionException(transactionId);
        }
        else
        {
            Trace.info("RM::Committing transaction : " + transactionId);
            try {
                if (!rm_flight.commit(transactionId)) {
                    return false;
                }
                if (!rm_car.commit(transactionId)) {
                    // recover rm_flight
                    return false;
                }
                if (!rm_room.commit(transactionId)) {
                    // recover rm_flight
                    // recover rm_car
                    return false;
                }
            }
            catch (Exception e)
            {
                return false;
            }
            if (mw_locks.UnlockAll(transactionId)) {
                // while (active_txn.contains(transactionId)) {
                active_txn.remove(transactionId);
                // }
                return true;
            }
            else {
                return false;
            }
        }
    }

    public void abort(int transactionId) throws RemoteException, InvalidTransactionException
    {
        if (transactionId < 1 || !this.active_txn.contains(transactionId)) {
            Trace.warn("RM::Commit failed--Invalid transactionId");
            throw new InvalidTransactionException(transactionId);
        }
        else {
            mw_locks.UnlockAll(transactionId);
            while (active_txn.contains(transactionId)) {
                    active_txn.remove(transactionId);
            }
        }
    }

    public boolean shutdown() throws RemoteException
    {
        if (!active_txn.isEmpty()) {
            Trace.warn("RM::Shutdown failed--transaction active");
            return false;
        }
        else
        {
            /* TODO: store data? */
            if (!rm_car.shutdown()) return false;
            if (!rm_room.shutdown()) return false;
            if (!rm_flight.shutdown()) return false;
        }
        return true;
    }
}