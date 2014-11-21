import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class MovieList {

  // MovieID::Title::Genres

  public static ArrayList<Movie> Movies;
  public static int LastId;

  public MovieList() {
    Movies = new ArrayList<Movie>();
  }

  public void tester() {

    // populateMovies("movies.dat");

    for (int i = 0; i < Movies.size(); i++) {

      System.out.println(Movies.get(i).id + " " + Movies.get(i).title + " " + Movies.get(i).genre);

    }

  }

  public static void main(String[] args) {
    MovieList user = new MovieList();

    populateMovies("movies.dat");
    user.saveMovieList();
    Movies.clear();
    user.loadMovieList();

    user.tester();
  }


  public void saveMovieList() {
    try {
      FileOutputStream fileOut = new FileOutputStream("/tmp/movieList.ser");
      ObjectOutputStream out = new ObjectOutputStream(fileOut);
      out.writeObject(Movies);
      out.close();
      fileOut.close();
    } catch (IOException i) {
      i.printStackTrace();
    }
  }

  @SuppressWarnings("unchecked")
  public void loadMovieList() {
    try {
      FileInputStream fileIn = new FileInputStream("/tmp/movieList.ser");
      ObjectInputStream in = new ObjectInputStream(fileIn);
      Movies = (ArrayList<Movie>) in.readObject();
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
  public static void populateMovies(String filename) {
	LastId = 0;
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
		LastId++;
        temp.id = Integer.parseInt(reuseableArray[0]);
	
		while(LastId != temp.id){
			
			Movie padding = new Movie();
			padding.id = -1;
			padding.title = "";
			padding.genre = "";
			
			Movies.add(padding);
			
			LastId++;
		}

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


class Movie implements java.io.Serializable {
  private static final long serialVersionUID = 1L;

  public int id;
  public String title;
  public String genre;

  public Movie() {}

}
