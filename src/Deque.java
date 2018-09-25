import tester.*;

//class representing a Deque
class Deque<T> {
  /*
   * Fields:
   * header -- Sentinel<T>
   * 
   * Methods:
   * size()   --  int
   * addAtHead(T data) -- void
   * addAtTail(T data) -- void
   * removeFromHead() -- T
   * removeFromTail() -- T
   * find(IPred<T> thing) -- ANode<T>
   * removeNode(ANode<T> target) -- void
   * 
   * Method on Fields:
   * header.size()   --  int
   * header.addAtHead(T data) -- void
   * header.addAtTail(T data) -- void
   * header.removeFromHead() -- T
   * header.removeFromTail() -- T
   * header.find(IPred<T> thing) -- ANode<T>
   * header.removeNode(ANode<T> target) -- void
   * 
   */
  
  
  Sentinel<T> header;

  Deque(Sentinel<T> header) {
    this.header = header;
  }

  Deque() {
    this.header = new Sentinel<T>();
  }

  // counts the number of nodes in a list Deque, not including the header node
  public int size() {
    return this.header.next.sizeHelper();
  }

  // consumes a <T> and inserts it at the front of the list
  public void addAtHead(T data) {
    this.header.next = new Node<T>(data, this.header.next, this.header); 
  }

  // consumes a <T> and inserts it at the front of the list
  public void addAtTail(T data) {
    this.header.prev = new Node<T>(data, this.header, this.header.prev); 
  }

  // Removes the sentinel from front of list
  public T removeFromHead() {
    return this.header.next.removeHelp();
  }

  //Removes the sentinel from back of list
  public T removeFromTail() {
    return this.header.prev.removeHelp();
  }

  //returns the first element in the list that the IPred is true for
  public ANode<T> find(IPred<T> thing) {
    return this.header.next.checkNode(thing); 
  }

  //removes the given node from the lists
  public void removeNode(ANode<T> target) {
    this.header.next.findAndRemoveNode(target);
  }
}

//Abstract class representing an ANode
abstract class ANode<T> {
  /*
   *   
   * Fields:
   * next -- ANode<T>
   * prev -- ANode<T>
   * 
   * Methods:
   * sizeHelper()  -- int
   * checkNode(IPred<T> thing) -- ANode<T>
   * findAndRemoveNode(ANode<T> target) -- void
   * removeHelp() -- T
   * 
   * Methods on Fields:
   * sizeHelper()  -- int
   * checkNode(IPred<T> thing) -- ANode<T>
   * findAndRemoveNode(ANode<T> target) -- void
   * removeHelp() -- T 
   * 
   */
  ANode<T> next;
  ANode<T> prev;

  ANode(ANode<T> next, ANode<T> prev) {
    this.next = next;
    this.prev = prev;
  }

  // Returns what the size would be of the ANode<T>
  public int sizeHelper() {
    return 0;
  }

  
  // returns if the Node is true for the IPred
  public ANode<T> checkNode(IPred<T> thing) {
    return this;
  }

  // Returns the handling of remove
  public T removeHelp() {
    throw new RuntimeException("Can't remove from an empty list");
  }

  public void findAndRemoveNode(ANode<T> target) {
    // Doing nothing!
  }
}


//class representing a Sentinel
class Sentinel<T> extends ANode<T> {
  /*
   * Fields:
   * next -- ANode<T>
   * prev -- ANode<T>
   * 
   * Methods:
   * sizeHelper() -- int
   * size() -- int
   * checkNode(IPred<T> thing) -- ANode<T>
   * findAndRemoveNode(ANode<T> target) -- void
   * removeHelp()  -- T
   * 
   */
  Sentinel(ANode<T> next, ANode<T> prev) {
    super(next, prev);
  }

  Sentinel() {
    super(null, null);
    this.next = this;
    this.prev = this;
  }

}

//class representing a Node
class Node<T> extends ANode<T> {
  /* 
   * Fields:
   * data  -- T
   * next -- ANode<T>
   * prev -- ANode<T>
   * 
   * Methods:
   * sizeHelper()  -- int
   * checkNode(IPred<T> thing) -- ANode<T>
   * findAndRemoveNode(ANode<T> target) -- void
   * removeHelp() -- T
   * 
   * Methods on Fields:
   * sizeHelper()  -- int
   * checkNode(IPred<T> thing) -- ANode<T>
   * findAndRemoveNode(ANode<T> target) -- void
   * removeHelp() -- T
   * 
   */

  T data;

  Node(T data, ANode<T> next, ANode<T> prev) {
    super(next, prev);
    if (next == null) {
      throw new IllegalArgumentException("Null next node");
    }
    if (prev == null) {
      throw new IllegalArgumentException("Null prev node");
    }
    prev.next = this;
    next.prev = this;
    this.data = data;
  }

  Node(T data) {
    this(data, null, null);

  }

  // The counting of the size of the list
  @Override
  public int sizeHelper() {
    return 1 + this.next.sizeHelper();
  }

  //checks for first Node IPred returns true for
  @Override
  public ANode<T> checkNode(IPred<T> thing) {
    if (thing.apply(this.data)) {
      return this;
    }
    else {
      return this.next.checkNode(thing);
    }
  }

  //Takes the target Node out of the list
  @Override
  public void findAndRemoveNode(ANode<T> target) {
    if (target.equals(this)) {
      this.prev.next = this.next;
    }
    else {
      this.next.findAndRemoveNode(target);
    }
  }

  //sets what the neigboring elements are next and prev to
  // after a removal
  @Override
  public T removeHelp() {
    this.prev.next = this.next;
    this.next.prev = this.prev;
    return this.data;
  }

}

//Represents a boolean-valued question over values of type T
interface IPred<T> {
  boolean apply(T t);
}

//class that is designed to test 
class IsGrape implements IPred<String> {
  public boolean apply(String s) {
    return s.equals("grape");
  }
}

class ExamplesDeque {

  Sentinel<String> mt2 = new Sentinel<String>();
  ANode<String> abc;
  ANode<String> bcd;
  ANode<String> cde;
  ANode<String> def;

  ANode<String> xyz;

  Sentinel<String> mt3 = new Sentinel<String>();
  ANode<String> n1;
  ANode<String> n2;
  ANode<String> n3;
  ANode<String> n4;
  ANode<String> n5;

  Deque<String> deque1;
  Deque<String> deque2;
  Deque<String> deque3;
  Deque<String> deque4;
  Deque<String> deque5;
  
  IPred<String> isGrape = new IsGrape();


  public void initData() {

    this.mt2 = new Sentinel<String>();
    this.abc = new Node<String>("abc", new Sentinel<String>(), mt2);
    this.bcd = new Node<String>("bcd", new Sentinel<String>(), abc);
    this.cde = new Node<String>("cde", new Sentinel<String>(), bcd);
    this.def = new Node<String>("def", mt2, cde);

    this.mt3 = new Sentinel<String>();
    this.n1 = new Node<String>("dragonfruit", new Sentinel<String>(), mt3);
    this.n2 = new Node<String>("grape", new Sentinel<String>(), n1);
    this.n3 = new Node<String>("clementine", new Sentinel<String>(), n2);
    this.n4 = new Node<String>("orange", new Sentinel<String>(), n3);
    this.n5 = new Node<String>("banana", mt3, n4);

    this.deque1 = new Deque<String>();
    this.deque2 = new Deque<String>(mt2);
    this.deque3 = new Deque<String>(mt3);
  }

  boolean testSize(Tester t) {
    initData();
    return t.checkExpect(deque1.size(), 0)
        && t.checkExpect(deque2.size(), 4)
        && t.checkExpect(deque3.size(), 5);
  }

  void testAddAtHead(Tester t) {
    initData();
    t.checkExpect(deque1.header.next, deque1.header);
    deque1.addAtHead("xyz");
    t.checkExpect(deque1.header.next, new Node<String>("xyz", deque1.header, deque1.header));

    t.checkExpect(deque2.header.next, abc);
    deque2.addAtHead("xyz");
    t.checkExpect(deque2.header.next, new Node<String>("xyz", abc, mt2));
  }

  void testAddAtTail(Tester t) {
    initData();
    t.checkExpect(deque1.header.next, deque1.header);
    deque1.addAtTail("xyz");
    t.checkExpect(deque1.header.next, new Node<String>("xyz", deque1.header, deque1.header));

    t.checkExpect(deque3.header.prev, n5);
    deque3.addAtTail("xyz");
    t.checkExpect(deque3.header.prev, new Node<String>("xyz", mt3, n5));
  }

  void testRemoveFromHead(Tester t) {
    initData();
    t.checkExpect(deque1.header.next, deque1.header);
    t.checkException(new RuntimeException("Can't remove from an empty list"),
        deque1, "removeFromHead");
    t.checkExpect(deque2.header.next, abc);
    deque2.removeFromHead();
    t.checkExpect(deque2.header.next, bcd);
    t.checkExpect(deque2.removeFromHead(), "bcd" );
  }

  void testRemoveFromTail(Tester t) {
    initData();
    t.checkExpect(deque1.header.next, deque1.header);
    t.checkException(new RuntimeException("Can't remove from an empty list"),
        deque1, "removeFromTail");
    t.checkExpect(deque3.header.prev, n5);
    deque3.removeFromTail();
    t.checkExpect(deque3.header.prev, n4);
    t.checkExpect(deque3.removeFromHead(), "dragonfruit");
  }

  void testFind(Tester t) {
    initData();
    t.checkExpect(deque3.find(isGrape), n2);
  }

  void testRemoveNode(Tester t) {
    initData();
    t.checkExpect(deque1, deque1);
    deque1.removeNode(mt2);
    t.checkExpect(deque1, deque1);
    deque3.removeNode(n2);
    t.checkExpect(n1.next, n3);

  }
}