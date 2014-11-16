import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class MovieList {

  // MovieID::Title::Genres

  public static ArrayList<Movie> Movies;

  public MovieList() {
    Movies = new ArrayList<Movie>();
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

        Movie temp = new Movie();
        temp.id = Integer.parseInt(reuseableArray[0]);
        temp.title = reuseableArray[1];
        temp.genre = reuseableArray[2];
        Movies.add(temp);
        data = file.readLine();
      }
      file.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}


class Movie {

  int id;
  String title;
  String genre;

  public Movie() {}

}
