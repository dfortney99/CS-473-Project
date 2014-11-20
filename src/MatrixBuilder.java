import java.util.ArrayList;
import java.util.List;

public class MatrixBuilder {
  public static List<SparseVector> colMatrix;
  public static List<SparseVector> rowMatrix;
  public static MovieList movies;
  public static UserList users;
  public static RatingList ratings;

  public static void main(String[] args) {
    colMatrix = new ArrayList<SparseVector>();
    rowMatrix = new ArrayList<SparseVector>();
    movies = new MovieList();
	//When finished populating objects. comment the populate and save functions out.

    movies.populateMovies("movies.dat");
	movies.saveMovieList();
	movies.loadMovieList();
    users = new UserList();
    users.populateUsers("users.dat");
	users.saveUserList();
	users.loadUserList();
    ratings = new RatingList();
    ratings.populateRatings("ratings.dat");
    ratings.saveRatingList();
	ratings.loadRatingList();
    System.out.println("Parsing complete");
    int prevUser = -1;
    for (int i = 0; i < RatingList.Ratings.size(); i++) {
      Rating r = RatingList.Ratings.get(i);
      if (r.userId != prevUser) {
        prevUser = r.userId;
        rowMatrix.add(new SparseVector());
      }
      rowMatrix.get(rowMatrix.size() - 1).addRating(r.movieId, r.rating);
    }

	//List<Integer> neighbors = getKNearestUsers(10, 1);
	
	//for(int i = 0; i < neighbors.size(); i++){
	//	System.out.println(neighbors.get(i));
	//}
    
     /*for (int i=0; i<rowMatrix.size(); i++){ for (int j=0; j<rowMatrix.get(i).data.size(); j++){
     System.out.println("User "+i+" rated movie "+rowMatrix.get(i).data.get(j).index+" a value of "
     +rowMatrix.get(i).data.get(j).value); } }
     */
    /*
     for (int i=1; i<2; i++){ for (int j=0; j<rowMatrix.get(i).data.size(); j++){
     System.out.println("User "+i+" rated movie "+movies.Movies.get(rowMatrix.get(i).data.get(j).index
	 -1)+" a value of "+rowMatrix.get(i).data.get(j).value); } }*/
	 List<Integer> neighbors = getKNearestUsers(10, 1); 
	
	 for (int i=0; i<neighbors.size(); i++){
	 SparseVector sv =  rowMatrix.get(neighbors.get(i));
	 	for (int j=0; j<sv.data.size(); j++){
     	System.out.println("User " +neighbors.get(i)
	   // +" rated movie "+movies.Movies.get(sv.data.get(j).index-1)
		+" a value of "+sv.data.get(j).value); 
		} 
	}
     
  }

  public static List<Integer> getKNearestUsers(int k, int index) {
    SparseVector base = rowMatrix.get(index);
    List<Integer> nearestIndices = new ArrayList<Integer>();
    List<Double> nearestScores = new ArrayList<Double>();
    for (int i = 0; i < rowMatrix.size(); i++) {
      if (i == index)
        continue;
      double score = rowMatrix.get(i).distanceFrom(base);
      if (nearestIndices.size() == 0) {
        nearestIndices.add(i);
        nearestScores.add(score);
      } else if (nearestIndices.size() < k) {
        for (int j = 0; j < nearestIndices.size(); j++) {
          if (score < nearestScores.get(j)) {
            nearestScores.add(j, score);
            nearestIndices.add(j, i);
            break;
          }
        }
      } else {
        for (int j = 0; j < nearestIndices.size(); j++) {
          if (score < nearestScores.get(j)) {
            nearestScores.add(j, score);
            nearestIndices.add(j, i);
            nearestScores.remove(nearestIndices.size() - 1);
            nearestIndices.remove(nearestIndices.size() - 1);
            break;
          }
        }
      }
    }
    return nearestIndices;
  }
}
