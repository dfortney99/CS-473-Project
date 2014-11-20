import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;


public class RatingList {

  // UserID::MovieID::Rating::Timestamp


  public static ArrayList<Rating> Ratings;

  public RatingList() {
    Ratings = new ArrayList<Rating>();
  }

  public void tester() {

    // populateRatings("ratings.dat");

    for (int i = 0; i < Ratings.size(); i++) {

      System.out.println(Ratings.get(i).userId + " " + Ratings.get(i).movieId + " "
          + Ratings.get(i).rating + " " + Ratings.get(i).timestamp);

    }

  }

  public static void main(String[] args) {
    RatingList user = new RatingList();

    populateRatings("ratings.dat");
    user.saveRatingList();
    Ratings.clear();
    user.loadRatingList();

    user.tester();
  }

  public void saveRatingList() {
    try {
      FileOutputStream fileOut = new FileOutputStream("/tmp/ratingList.ser");
      ObjectOutputStream out = new ObjectOutputStream(fileOut);
      out.writeObject(Ratings);
      out.close();
      fileOut.close();
    } catch (IOException i) {
      i.printStackTrace();
    }
  }

  @SuppressWarnings("unchecked")
  public void loadRatingList() {
    try {
      FileInputStream fileIn = new FileInputStream("/tmp/ratingList.ser");
      ObjectInputStream in = new ObjectInputStream(fileIn);
      Ratings = (ArrayList<Rating>) in.readObject();
      in.close();
      fileIn.close();
    } catch (IOException i) {
      i.printStackTrace();
      return;
    } catch (ClassNotFoundException c) {
      c.printStackTrace();
      return;
    }
  }

  // Reads in input file - 1 line at a time
  public static void populateRatings(String filename) {
    try {
      String[] reuseableArray;
      RandomAccessFile file = new RandomAccessFile(filename, "rw");
      String data;
      file.seek(0);
      data = file.readLine();
      while (data != null) {
        // Perform parsing here
        reuseableArray = data.split("::");

        Rating temp = new Rating();
        temp.userId = Integer.parseInt(reuseableArray[0]);
        temp.movieId = Integer.parseInt(reuseableArray[1]);
        temp.rating = Integer.parseInt(reuseableArray[2]);
        temp.timestamp = Integer.parseInt(reuseableArray[3]);
        Ratings.add(temp);
        data = file.readLine();
      }
      file.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}


class Rating implements java.io.Serializable {
  private static final long serialVersionUID = 1L;

  public int userId;
  public int movieId;
  public int rating;
  public int timestamp; // in Seconds

  public Rating() {}

}
