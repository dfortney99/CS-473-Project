import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;


public class RatingList {

  // UserID::MovieID::Rating::Timestamp


  public static ArrayList<Rating> Ratings;

  public RatingList() {
    Ratings = new ArrayList<Rating>();
  }

  // Reads in input file - 1 line at a time
  public static void populateMovies(String filename) {
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


class Rating {

  int userId;
  int movieId;
  int rating;
  int timestamp; // in Seconds

  public Rating() {}

}
