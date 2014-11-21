public class IntPair implements Comparable{
  public int index;
  public double value;
  public IntPair(int index, double value){
    this.index = index;
    this.value = value;
  }
  public int compareTo(Object obj){
    IntPair p = (IntPair)obj;
    if (this.index > p.index){
      return 1; 
    }else if (this.index == p.index){
      return 0;
    }else{
      return -1;
    }
  }
  public int compare(IntPair p1, IntPair p2){
    return p1.index - p2.index;
  }
}