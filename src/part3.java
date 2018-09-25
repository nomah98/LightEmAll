import java.util.ArrayList;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.Random;
import java.util.HashMap;

class LightEmAll extends World {

  // a list of columns of GamePieces,
  // i.e., represents the board in column-major order

  ArrayList<ArrayList<GamePiece>> board = new ArrayList<ArrayList<GamePiece>>();

  // a list of all nodes
  ArrayList<GamePiece> nodes;

  // a list of edges of the minimum spanning tree
  ArrayList<Edge> mst;

  // the width and height of the board
  int width;
  int height;

  // the current location of the power station,
  // as well as its effective radius
  int powerRow;
  int powerCol;
  int radius;

  // Random object for random rotation
  Random r;

  LightEmAll(int width, int height, int radius, Random r) {
    this.width = width;
    this.height = height;
    this.powerRow = height / 2;
    this.powerCol = width / 2;
    this.radius = radius;
    this.r = r;
    this.nodes = new ArrayList<GamePiece>();
    this.mst = new ArrayList<Edge>();

    this.boardBuilder();
    this.nodeListMaker();
    this.findNeighbors();
    this.kruskalGen();

    this.findLighting();
  }

  // builds the board as ArrayLists of ArrayLists
  void boardBuilder() {
    ArrayList<ArrayList<GamePiece>> tempBoard = new ArrayList<ArrayList<GamePiece>>();
    for (int i = 0; i < width; i++) {
      ArrayList<GamePiece> tempRow = new ArrayList<GamePiece>();
      for (int j = 0; j < height; j++) {
        tempRow.add(new GamePiece(j, i));
      }
      tempBoard.add(tempRow);
    }
    this.board = tempBoard;
  }

  // Finds where the Powerstation is and calls to lightAll() to light from there
  void findLighting() {
    this.clearLighting();
    for (int i = 0; i < board.size(); i++) {
      for (int j = 0; j < board.get(i).size(); j++) {
        if (i == powerRow && j == powerCol) {
          this.getPiece(i, j).lightAll(this.radius);
        }
      }
    }
  }

  // Clears the lighting of the board
  void clearLighting() {
    for (ArrayList<GamePiece> x : this.board) {
      for (GamePiece g : x) {
        g.power = false;
      }
    }
  }

  // returns an image representation of a board
  WorldImage drawBoard() {
    WorldImage board = new EmptyImage();
    for (ArrayList<GamePiece> x : this.board) {
      WorldImage array = new EmptyImage();
      for (GamePiece g : x) {
        array = new AboveImage(array, g.drawGamePiece());
      }
      board = new BesideImage(board, array);
    }
    return board;
  }

  // the makeScene handler for BigBang
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(width * 75, height * 75);
    scene.placeImageXY(this.drawBoard(), (int) this.drawBoard().getWidth() / 2,
        (int) this.drawBoard().getHeight() / 2);
    return scene;
  }

  // Creates the win condition WorldScene
  public WorldScene lastScene(String msg) {
    WorldScene scene = new WorldScene(width * 75, height * 75);
    scene.placeImageXY(new TextImage("Winner winner chicken dinner", 25, Color.pink),
        width * 75 / 2, height * 75 / 2);
    return scene;
  }

  // Determines when the win condition of all connections and lit is met
  public boolean winCondition() {
    for (ArrayList<GamePiece> x : this.board) {
      for (GamePiece g : x) {
        if (!g.power) {
          return false;
        }
      }
    }
    return true;
  }

  // Places predetermined types of pieces at certain places on the board
  public void genBoard() {
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        if (i < height) {
          this.getPiece(i, j).left = true;
        }
        if (i >= 0) {
          this.getPiece(i, j).right = true;
        }
        if (j < width && i == height / 2) {
          this.getPiece(i, j).bottom = true;
        }
        if (j > 0 && i == height / 2) {
          this.getPiece(i, j).top = true;
        }
        if (i == powerCol && j == powerRow) {
          this.getPiece(i, j).powerStation = true;
        }
      }
    }
  }

  // Generates a board with Kruskal's algorithm
  public void kruskalGen() {
    ArrayList<Edge> allEdges = this.allEdges();
    this.heapify(allEdges, allEdges.size());
    this.kruskals(allEdges);
    for (Edge e : this.mst) {
      this.connnect(e);
    }

    this.getPiece(powerCol, powerRow).powerStation = true;

  }

  // creates a connection between the two gamePieces of an edge
  public void connnect(Edge e) {
    if (e.fromNode.col == e.toNode.col) {
      e.fromNode.bottom = true;
      e.toNode.top = true;
    }
    if (e.fromNode.row == e.toNode.row) {
      e.fromNode.right = true;
      e.toNode.left = true;
    }
  }

  // handler for onMouseClicked for BigBang and rotates wires clockwise
  // also checks for the win condition
  public void onMouseClicked(Posn pos, String button) {
    int cellSize = 75;
    int x = (int) (pos.x) / cellSize;
    int y = (int) (pos.y) / cellSize;
    this.getPiece(x, y).clickCell();

    this.findLighting();

    if (winCondition()) {
      this.endOfWorld("Winner winner chicken dinner");
    }
  }

  // creates a list of the surroundingPieces
  public void findNeighbors() {
    for (int x = 0; x < board.size(); x++) {
      for (int y = 0; y < board.get(x).size(); y++) {
        if (x != 0) {
          getPiece(x, y).add(getPiece(x - 1, y));
        }
        if (x != this.width - 1) {
          getPiece(x, y).add(getPiece(x + 1, y));
        }
        if (y != 0) {
          getPiece(x, y).add(getPiece(x, y - 1));
        }
        if (y != this.height - 1) {
          getPiece(x, y).add(getPiece(x, y + 1));
        }
      }
    }
  }

  // returns a GamePiece at a given index
  public GamePiece getPiece(int x, int y) {
    return this.board.get(x).get(y);
  }

  // Handler for onKeyEvent for BigBang for Powerstation movement
  public void onKeyEvent(String key) {
    if (key.equals("up") && this.powerCol != 0) {
      getPiece(powerRow, powerCol).powerStation = false;
      this.powerCol = this.powerCol - 1;
      getPiece(powerRow, powerCol).powerStation = true;
      this.findLighting();

    }
    if (key.equals("right") && this.powerRow != width - 1) {
      getPiece(powerRow, powerCol).powerStation = false;
      this.powerRow = this.powerRow + 1;
      getPiece(powerRow, powerCol).powerStation = true;
      this.findLighting();

    }
    if (key.equals("down") && this.powerCol != height - 1) {
      getPiece(powerRow, powerCol).powerStation = false;
      this.powerCol = this.powerCol + 1;
      getPiece(powerRow, powerCol).powerStation = true;
      this.findLighting();

    }
    if (key.equals("left") && this.powerRow != 0) {
      getPiece(powerRow, powerCol).powerStation = false;
      this.powerRow = this.powerRow - 1;
      getPiece(powerRow, powerCol).powerStation = true;
      this.findLighting();

    }
    if (winCondition()) {
      this.endOfWorld("Winner winner chicken dinner");
    }
  }

  // Randomly rotates each GamePiece at the start of the game
  public void randomRotation() {
    for (ArrayList<GamePiece> x : this.board) {
      for (GamePiece g : x) {
        int rand = this.r.nextInt(4);
        for (int i = 0; i <= rand; i++) {
          g.clickCellRandom();
        }
      }
    }
  }

  // Creates an ArrayList of every GamePiece on the board
  public void nodeListMaker() {
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        this.nodes.add(this.getPiece(i, j));
      }
    }
  }

  // Adds all of he possible edges to an ArrayList and returns that ArrayList
  public ArrayList<Edge> allEdges() {
    ArrayList<Edge> tempList = new ArrayList<Edge>();
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        if (i < width - 1) {
          tempList.add(new Edge(this.getPiece(i, j), this.getPiece(i + 1, j), r.nextInt(50)));
        }
        if (j < height - 1) {
          tempList.add(new Edge(this.getPiece(i, j), this.getPiece(i, j + 1), r.nextInt(50)));
        }
      }
    }
    return tempList;
  }

  // generates the mst from a given heapsorted arrayList allEdges
  public void kruskals(ArrayList<Edge> allEdges) {
    HashMap<GamePiece, GamePiece> representatives = new HashMap<GamePiece, GamePiece>();
    ArrayList<Edge> edgesInTree = new ArrayList<Edge>();
    Deque<Edge> worklist = new Deque<Edge>();

    for (Edge e : allEdges) {
      worklist.addAtTail(e);
    }
    // initialize every node's representative to itself
    for (int i = 0; i < nodes.size(); i++) {
      representatives.put(nodes.get(i), nodes.get(i));
    }

    while (worklist.size() > 0 && edgesInTree.size() < this.nodes.size() - 1) {
      Edge cheapest = worklist.removeFromHead();
      if (find(representatives, cheapest.fromNode) == find(representatives, cheapest.toNode)) {
        // do nothing
      }
      else {
        edgesInTree.add(cheapest);
        union(representatives, find(representatives, cheapest.fromNode),
            find(representatives, cheapest.toNode));
      }
    }
    this.mst = edgesInTree;
  }

  // puts two gamepieces in the HashMap
  void union(HashMap<GamePiece, GamePiece> representatives, GamePiece g1, GamePiece g2) {
    representatives.put(representatives.get(g1), representatives.get(g2));
  }

  // finds a gamePiece if the gamePiece is in the HashMap
  GamePiece find(HashMap<GamePiece, GamePiece> representatives, GamePiece g) {
    if (g.equals(representatives.get(g))) {
      return g;
    }
    else {
      return find(representatives, representatives.get(g));
    }
  }

  // in place sorting method that calls to downheap method and the swap method
  // Does the building of the heap then sorts
  public void heapify(ArrayList<Edge> edgeList, int edgeListSize) {
    edgeListSize = edgeList.size();
    for (int i = (edgeListSize / 2) - 1; i >= 0; i--) {
      this.downheap(edgeList, edgeListSize, i);
    }
    for (int i = edgeListSize - 1; i >= 0; i--) {
      swap(edgeList, 0, i);
      downheap(edgeList, i, 0);
    }
  }

  // Sets the index to create the heap properties
  void downheap(ArrayList<Edge> edgeList, int edgeListSize, int idx) {
    int biggestIdx = idx;
    int leftChild = 2 * idx + 1;
    int rightChild = 2 * idx + 2;

    if (leftChild < edgeListSize
        && edgeList.get(leftChild).weight > edgeList.get(biggestIdx).weight) {
      biggestIdx = leftChild;
    }
    if (rightChild < edgeListSize
        && edgeList.get(rightChild).weight > edgeList.get(biggestIdx).weight) {
      biggestIdx = rightChild;
    }
    if (idx != biggestIdx) {
      this.swap(edgeList, idx, biggestIdx);
      this.downheap(edgeList, edgeListSize, biggestIdx);
    }
  }

  // Not used attempt to sort by moving upheap instead of downheap
  void upheap(ArrayList<Edge> edgeList, int idx) {
    int parentIdx = (int) Math.floor((double) ((idx - 1) / 2));
    if (edgeList.get(idx).weight > edgeList.get(parentIdx).weight) {
      swap(edgeList, parentIdx, idx);
      upheap(edgeList, parentIdx);
    }
  }

  // swaps two elements in the given ArrayList at the given indices
  void swap(ArrayList<Edge> list, int i, int j) {
    Edge temp = list.get(i);
    list.set(i, list.get(j));
    list.set(j, temp);
  }
}

// Represents a GamePiece
class GamePiece {

  // name to be used for hashmaps
  String name;

  // in logical coordinates, with the origin
  // at the top-left corner of the screen
  int row;
  int col;

  // Size of the board in num cells
  int boardHeight;
  int boardWidth;

  // whether this GamePiece is connected to the
  // adjacent left, right, top, or bottom pieces
  boolean left;
  boolean right;
  boolean top;
  boolean bottom;

  // whether the power station is on this piece
  boolean powerStation;

  // whether this GamePiece is powered
  boolean power;

  // AL of surrounding pieces
  ArrayList<GamePiece> surroundingPieces;

  GamePiece(boolean left, boolean right, boolean top, boolean bottom, boolean powerStation,
      boolean power, int boardWidth, int boardHeight, int row, int col) {
    this.left = left;
    this.right = right;
    this.top = top;
    this.bottom = bottom;
    this.powerStation = powerStation;
    this.surroundingPieces = new ArrayList<GamePiece>();
    this.power = power;
    this.boardHeight = boardHeight;
    this.boardWidth = boardWidth;
    this.row = row;
    this.col = col;
  }

  GamePiece(int row, int col) {
    this.left = false;
    this.right = false;
    this.top = false;
    this.bottom = false;
    this.powerStation = false;
    this.row = row;
    this.col = col;
    this.power = false;
    this.surroundingPieces = new ArrayList<GamePiece>();

  }

  // Returns an image representation of a GamePiece
  WorldImage drawGamePiece() {
    WorldImage block = new RectangleImage(75, 75, "solid", Color.GRAY);
    WorldImage blockFrame = new RectangleImage(75, 75, "outline", Color.BLACK);
    WorldImage blockComplete = new OverlayImage(blockFrame, block);
    WorldImage vertLine = new RectangleImage(5, 75 / 2, "solid", Color.LIGHT_GRAY);
    WorldImage horzLine = new RectangleImage(75 / 2, 5, "solid", Color.LIGHT_GRAY);
    WorldImage vertLineLit = new RectangleImage(5, 75 / 2, "solid", Color.YELLOW);
    WorldImage horzLineLit = new RectangleImage(75 / 2, 5, "solid", Color.YELLOW);

    WorldImage station = new StarImage(25, OutlineMode.SOLID, Color.red);
    if (this.power) {
      if (this.top) {
        blockComplete = new OverlayOffsetImage(vertLineLit, 0, 75 / 4, blockComplete);
      }
      if (this.bottom) {
        blockComplete = new OverlayOffsetImage(vertLineLit, 0, -75 / 4, blockComplete);
      }
      if (this.right) {
        blockComplete = new OverlayOffsetImage(horzLineLit, -75 / 4, 0, blockComplete);
      }
      if (this.left) {
        blockComplete = new OverlayOffsetImage(horzLineLit, 75 / 4, 0, blockComplete);
      }
    }
    if (!this.power) {

      if (this.top && !this.power) {
        blockComplete = new OverlayOffsetImage(vertLine, 0, 75 / 4, blockComplete);
      }
      if (this.bottom && !this.power) {
        blockComplete = new OverlayOffsetImage(vertLine, 0, -75 / 4, blockComplete);
      }
      if (this.right && !this.power) {
        blockComplete = new OverlayOffsetImage(horzLine, -75 / 4, 0, blockComplete);
      }
      if (this.left && !this.power) {
        blockComplete = new OverlayOffsetImage(horzLine, 75 / 4, 0, blockComplete);
      }
    }
    if (this.powerStation) {
      blockComplete = new OverlayImage(station, blockComplete);
    }
    return blockComplete;
  }

  // EFFECT: rotates the GamePiece clockwise by adjusting its directional booleans
  void clickCellRandom() {
    boolean prevTop = this.top;
    boolean prevRight = this.right;
    boolean prevBottom = this.bottom;
    boolean prevLeft = this.left;

    this.top = prevLeft;
    this.right = prevTop;
    this.bottom = prevRight;
    this.left = prevBottom;

  }

  // EFFECT: rotates the GamePiece clockwise by adjusting its directional booleans
  void clickCell() {
    boolean prevTop = this.top;
    boolean prevRight = this.right;
    boolean prevBottom = this.bottom;
    boolean prevLeft = this.left;

    this.top = prevLeft;
    this.right = prevTop;
    this.bottom = prevRight;
    this.left = prevBottom;

  }

  // Adds a GamePiece to the surroundingPieces ArrayList
  void add(GamePiece g) {
    this.surroundingPieces.add(g);
  }

  // Determines whether this GamePiece is connected to the GamePiece left
  boolean connectedToLeft() {
    if (this.col != 0 && this.left) {
      for (GamePiece g : surroundingPieces) {
        if (g.row == this.row && g.col == this.col - 1) {
          return g.right;
        }
      }
    }
    return false;
  }

  // Determines whether this GamePiece is connected to the GamePiece above
  boolean connectedToTop() {
    if (this.row != 0 && this.top) {
      for (GamePiece g : surroundingPieces) {
        if (g.row == this.row - 1 && g.col == this.col) {
          return g.bottom;
        }
      }
    }
    return false;
  }

  // Determines whether this GamePiece is connected to the GamePiece right
  boolean connectedToRight() {
    if (this.col != boardWidth && this.right) {
      for (GamePiece g : surroundingPieces) {
        if (g.row == this.row && g.col == this.col + 1) {
          return g.left;
        }
      }
    }
    return false;
  }

  // Determines whether this GamePiece is connected to the GamePiece below
  boolean connectedToBottom() {
    if (this.row != boardHeight && this.bottom) {
      for (GamePiece g : surroundingPieces) {
        if (g.row == this.row + 1 && g.col == this.col) {
          return g.top;
        }
      }
    }
    return false;
  }

  // Returns the neighbor piece at the given index
  public GamePiece findNeighbor(int row, int col) {
    for (GamePiece g : surroundingPieces) {
      if (col == g.col && row == g.row) {
        return g;
      }

    }
    return new GamePiece(false, false, false, false, false, false, 0, 0, 0, 0);
  }

  // Lights up GamePieces that are connected and surroundingPieces
  void lightAll(int r) {
    this.power = true;
    if (r != 0) {
      if (this.connectedToRight() && !this.findNeighbor(this.row, this.col + 1).power) {
        this.findNeighbor(this.row, this.col + 1).lightAll(r - 1);
      }
      if (this.connectedToBottom() && !this.findNeighbor(this.row + 1, this.col).power) {
        this.findNeighbor(this.row + 1, this.col).lightAll(r - 1);
      }
      if (this.connectedToLeft() && !this.findNeighbor(this.row, this.col - 1).power) {
        this.findNeighbor(this.row, this.col - 1).lightAll(r - 1);
      }
      if (this.connectedToTop() && !this.findNeighbor(this.row - 1, this.col).power) {
        this.findNeighbor(this.row - 1, this.col).lightAll(r - 1);
      }
    }
  }

  /*
   * Incomplete breadth-first search method to find radius for power
   * 
   * ArrayList<GamePiece> bfs(GamePiece start) { 
   * HashMap<String, Edge> cameFromEdge = new HashMap<String, Edge>(); 
   * Deque<GamePiece> worklist = new Deque<GamePiece>(); 
   * // Is a queue 
   * worklist.addAtTail(start);
   * while(worklist.size() > 0) { 
   * GamePiece next = worklist.removeFromHead();
   * if(next.connectedNeighbors(cameFromEdge).size() == 0 && worklist.size() != 0) { 
   * // Do nothing 
   * } else if(next.connectedNeighbors(cameFromEdge).size() == 0&& worklist.size() == 0) { 
   * return this.reconstruct(cameFromEdge, next); 
   * } else { for(GamePiece g : next.connectedNeighbors(cameFromEdge)) {
   * worklist.addAtTail(g); 
   * cameFromEdge.put(g.toString(), new Edge(next, g, 0));
   * } } } }
   * 
   * // returns an ArrayList of the neighbors of a GamePiece that is not in the
   * hashmap and is connected to this ArrayList<GamePiece>
   * connectedNeighbors(HashMap<String, Edge> cameFromEdge) { 
   * ArrayList<GamePiece> neighbors = new ArrayList<GamePiece>(); 
   * for(GamePiece g : this.surroundingPieces) { 
   * if ( ) }
   *  return null; 
   *  }
   * 
   */
  // returns an ArrayList of GamePiece where we have already seen
  ArrayList<GamePiece> reconstruct(HashMap<String, Edge> cameFromEdge, GamePiece start) {
    ArrayList<GamePiece> retrace = new ArrayList<GamePiece>();
    retrace.add(start);
    GamePiece next = start;
    while (cameFromEdge.containsKey(next.toString())) {
      next = cameFromEdge.get(next.toString()).fromNode;
      retrace.add(next);
    }
    return retrace;
  }
}

// Represents an Edge
class Edge {

  GamePiece fromNode;
  GamePiece toNode;
  int weight;

  Edge(GamePiece fromNode, GamePiece toNode, int weight) {
    this.weight = weight;
    this.fromNode = fromNode;
    this.toNode = toNode;
  }

  Edge(int weight) {
    this.fromNode = null;
    this.toNode = null;
    this.weight = weight;
  }
}

// Examples class for testing
class ExamplesGame {

  LightEmAll l1;
  Random r1;
  Random r2;

  LightEmAll l2;

  ArrayList<Edge> list2 = new ArrayList<Edge>();

  void initData() {
    r1 = new Random();
    l1 = new LightEmAll(8, 9, 3, r1);
  }

  void initData2() {
    r2 = new Random(1);
    l2 = new LightEmAll(2, 2, 1, r2);
  }

  void testl1(Tester t) {
    initData();
    l1.bigBang(75 * l1.width, 75 * l1.height, 1);
  }

  void testl2(Tester t) {
    initData2();
    l2.bigBang(75 * l2.width, 75 * l2.height, 1);
  }

  void testBuildBoardl1(Tester t) {
    initData();
    t.checkExpect(l1.board.size(), 8);
    t.checkExpect(l1.board.get(1).size(), 9);
  }

  void testBuildBoardl2(Tester t) {
    initData2();
    t.checkExpect(l2.board.size(), 2);
    t.checkExpect(l2.board.get(1).size(), 2);
  }

  void testTheCol(Tester t) {
    initData2();
    t.checkExpect(l2.getPiece(1, 0).col, 1);
    t.checkExpect(l2.getPiece(0, 0).col, 0);
  }

  void testSPSizel1(Tester t) {
    initData();
    t.checkExpect(l1.getPiece(2, 2).surroundingPieces.size(), 4);
  }

  void testSPSizel2(Tester t) {
    initData2();
    t.checkExpect(l2.getPiece(0, 1).surroundingPieces.size(), 2);
  }

  void testSurroundingPieces(Tester t) {
    initData2();
    t.checkExpect(l2.getPiece(0, 0).connectedToBottom(), false);
  }

  void checkRowColFields(Tester t) {
    initData();
    t.checkExpect(l1.getPiece(5, 6).row, 6);
    t.checkExpect(l1.getPiece(5, 6).col, 5);
  }

  void testGetPiece(Tester t) {
    initData();
    t.checkExpect(l1.getPiece(2, 3).equals(l1.board.get(2).get(3)), true);
  }

  void testPower(Tester t) {
    initData();
    t.checkExpect(l1.getPiece(4, 4).power, true);
  }

  void testClickCell(Tester t) {
    GamePiece one = new GamePiece(2, 3);
    one.top = true;
    GamePiece two = new GamePiece(1, 2);
    two.right = true;
    one.clickCell();
    t.checkExpect(one.right, true);
    two.clickCell();
    t.checkExpect(two.right, false);
    t.checkExpect(two.bottom, true);
  }

  // tests for heapSort

  ArrayList<Edge> list1 = new ArrayList<Edge>();
  Edge e1 = new Edge(5);
  Edge e2 = new Edge(3);
  Edge e3 = new Edge(7);
  Edge e4 = new Edge(50);
  Edge e5 = new Edge(25);

  Edge e6 = new Edge(1);
  Edge e7 = new Edge(2);
  Edge e8 = new Edge(3);
  Edge e9 = new Edge(4);
  Edge e10 = new Edge(5);

  Edge a1 = new Edge(80);
  Edge a2 = new Edge(60);
  Edge a3 = new Edge(50);
  Edge a4 = new Edge(30);
  Edge a5 = new Edge(50);
  Edge a6 = new Edge(40);
  Edge a7 = new Edge(20);
  Edge a8 = new Edge(10);
  Edge a9 = new Edge(20);
  Edge a10 = new Edge(15);

  ArrayList<Edge> list22 = new ArrayList<Edge>();
  ArrayList<Edge> list3 = new ArrayList<Edge>();
  ArrayList<Edge> list4 = new ArrayList<Edge>();
  ArrayList<Edge> list5 = new ArrayList<Edge>();

  void initdata() {
    list1.add(e1);
    list1.add(e4);
    list1.add(e5);
    list1.add(e2);
    list1.add(e3);

    list22.add(e2);
    list22.add(e1);
    list22.add(e3);
    list22.add(e5);
    list22.add(e4);

    list3.add(e4);
    list3.add(e3);
    list3.add(e5);
    list3.add(e2);
    list3.add(e1);

    list4.add(e6);
    list4.add(e7);
    list4.add(e8);
    list4.add(e9);
    list4.add(e10);

    list5.add(e6);
    list5.add(e7);
    list5.add(e8);
    list5.add(e9);
    list5.add(e10);

  }

  void testHeapify(Tester t) {
    initdata();
    l2.heapify(list1, list1.size());
    t.checkExpect(list1, list22);
  }
}