import java.util.*;
import java.lang.*;
public class SparseVector{
  public List<IntPair> data;
  public SparseVector(){
    data = new ArrayList<IntPair>();
  }
  
  //Appends a rating to the vector.
  public void addRating(int index, double value){
    data.add(new IntPair(index, value));
  }
  
  //If for some reason the ratings are not added in order of index, call this function.
  public void sortByIndex(){
    Collections.sort(data);
  }
  
  //Gets the rating at a given index. -1 indicates no rating.
  public double getRating(int index){
    for (int i=0; i<data.size(); i++){
      if (data.get(i).index == index){
        return data.get(i).value;
      }
    }
    return -1;
  }
  
  public double distanceFrom(SparseVector sv){
    double assumedDiff = 1.5; //The assumed difference in rating if we don't have two ratings for a movie.
    this.sortByIndex();
    sv.sortByIndex();
    double runningSum = 0;
    double diffsCounted = 0;
    for (int i=0; i<data.size(); i++){
      double otherRating = sv.getRating(data.get(i).index);
      if (otherRating < 0) continue;
      double diff = (data.get(i).value - otherRating);
      diffsCounted++;
      runningSum += diff * diff;
    }
    double diffsNotCounted = MatrixBuilder.movies.Movies.size()-diffsCounted;
    runningSum += assumedDiff*assumedDiff*diffsNotCounted;
    return Math.sqrt(runningSum);
  }
  
  //For debugging.
  public static void main(String[] args){
    SparseVector sv = new SparseVector();
    sv.addRating(23, 4);
    sv.addRating(30, 3.5);
    sv.addRating(52, 3.5);
    SparseVector sv2 = new SparseVector();
    sv2.addRating(10, 2);
    sv2.addRating(30, 5);
    sv2.addRating(52, 4);
    System.out.println(sv.distanceFrom(sv2));
  }
  
  //For debugging. Prints the contents of the vector.
  private void printContents(){
    for (int i=0; i<data.size(); i++){
      System.out.println(data.get(i).index + "\t" + data.get(i).value);
    }
  }
}