import java.util.ArrayList;
import java.util.List;
import java.util.*;

public class MatrixBuilder {
  public static List<SparseVector> colMatrix;
  public static List<SparseVector> rowMatrix;
  public static List<SparseVector> baseColMatrix;
  public static List<SparseVector> baseRowMatrix;
  public static List<AverageMovieRating> ratedMovies = new ArrayList<AverageMovieRating>();
  public static MovieList movies;
  public static UserList users;
  public static RatingList ratings;

  public static void main(String []args){
	//calculateRecommendations(1, 10, 10);
	//evaluation(1, 10, 10);
	
  }	


  public static void calculateRecommendations(int User, int k, int max) {
    colMatrix = new ArrayList<SparseVector>();
    rowMatrix = new ArrayList<SparseVector>();
    movies = new MovieList();
    // When finished populating objects. comment the populate and save functions out.
    
    //movies.populateMovies("movies.dat");
    // movies.saveMovieList();
    movies.loadMovieList();
    users = new UserList();
    //users.populateUsers("users.dat");
    //users.saveUserList();
    users.loadUserList();
    ratings = new RatingList();
    //ratings.populateRatings("ratings.dat");
    //ratings.saveRatingList();
    ratings.loadRatingList();
    System.out.println("Parsing complete");
    int highestMovie = movies.Movies.get(movies.Movies.size()-1).id; 
    int prevUser = -1;
    rowMatrix.add(new SparseVector()); //It's just easier if we add a blank vector at index 0, so the user id matches his index

    for (int i=0; i<RatingList.Ratings.size(); i++){
      Rating r = RatingList.Ratings.get(i);
      if (r.userId != prevUser){
        prevUser = r.userId;
        rowMatrix.add(new SparseVector());
      }
      rowMatrix.get(rowMatrix.size()-1).addRating(r.movieId, r.rating);
    }
	baseRowMatrix = rowMatrix;
    System.out.println("Row matrix construction complete");
    for (int i=0; i<=highestMovie; i++){
      colMatrix.add(new SparseVector());
    }
    System.out.println("Column matrix initialization complete");
    for (int i=0; i<RatingList.Ratings.size(); i++){
      Rating r = RatingList.Ratings.get(i);
      colMatrix.get(r.movieId).addRating(r.userId, r.rating);
    }
    System.out.println("Unstructured column matrix complete");
    for (int i=0; i<colMatrix.size(); i++){
      colMatrix.get(i).sortByIndex();
    }
    System.out.println("Full column matrix complete");
	baseColMatrix = colMatrix;
    
    List<Integer> neighbors = getKNearestUsers(k, User);
    
    
    for (int i = 0; i < neighbors.size(); i++) {
      SparseVector sv = rowMatrix.get(neighbors.get(i));
      for (int j = 0; j < sv.data.size(); j++) {
        //System.out.println("User " + neighbors.get(i) + " rated movie "
        //+ movies.Movies.get(sv.data.get(j).index - 1).title + " a value of " + sv.data.get(j).value);
        
        int temp = containsId(ratedMovies, sv.data.get(j).index -1) ;
        if(temp != -1){
          ratedMovies.get(temp).total += sv.data.get(j).value;
          ratedMovies.get(temp).number_rated++;
          
        }
        else
        {
          AverageMovieRating newRating = new AverageMovieRating();
          newRating.id = sv.data.get(j).index - 1;
          newRating.total = sv.data.get(j).value;
          newRating.number_rated = 1;
          ratedMovies.add(newRating);
        }         
      }
    }
    
    //Collections.sort(ratedMovies);
    Collections.sort(ratedMovies, new Comparator<AverageMovieRating>(){
      public int compare(AverageMovieRating s1, AverageMovieRating s2){
        if((s1.total / s1.number_rated) > (s2.total / s2.number_rated)){
          return -1;
        }
        else if ((s1.total / s1.number_rated) == (s2.total / s2.number_rated)) {
          return 0;
        }  
        else{
          return 1;
        }  
      }
    });
    
    for(int w = 0; w < ratedMovies.size(); w++){
      System.out.println(movies.Movies.get(ratedMovies.get(w).id).title + " " + ratedMovies.get(w).total /    ratedMovies.get(w).number_rated);
    }
    
    //Find novel movie recommendations for user 1.
    System.out.println("Movies rated by user " + User);
    SparseVector user = rowMatrix.get(User);
    for (int i=0; i<user.data.size(); i++){
      System.out.println(user.data.get(i).index);
    }
    SparseVector noveltyScores = new SparseVector();
    List<Integer> comparisonBase = getRandomMovieSubset(10, 1);
    for (int i=0; i<comparisonBase.size(); i++){
      System.out.println("Base: Movie "+comparisonBase.get(i));
    }
    for (int i=0; i<user.data.size(); i++){
      if (user.data.get(i).value < 3.5) {
        System.out.println("Movie "+user.data.get(i).index+" ignored. Rating "+user.data.get(i).value);
        continue; //If the user rated the movie less than 3.5, ignore it.
      }
      double runningScore = 0;
      SparseVector baseMovie = colMatrix.get(user.data.get(i).index);
      for (int j=0; j<comparisonBase.size(); j++){        
        double score = baseMovie.distanceFrom(colMatrix.get(comparisonBase.get(j)));
        runningScore+=score;
      }
      noveltyScores.addRating(user.data.get(i).index, runningScore/comparisonBase.size());
      System.out.println("Movie "+user.data.get(i).index+" got a score of "+runningScore/comparisonBase.size()+". Rating "+user.data.get(i).value);
    }
    List<IntPair> recommendedMovies = new ArrayList<IntPair>();
    //int max = 10;
    for (int i=0; i<noveltyScores.data.size(); i++){
      IntPair current = noveltyScores.data.get(i);
      if (recommendedMovies.size()==0){
        recommendedMovies.add(current);
      }else{
        for (int j=0; j<recommendedMovies.size(); j++){        
          if (current.value > recommendedMovies.get(j).value){
            recommendedMovies.add(j, current);
            if (recommendedMovies.size()>max){
              recommendedMovies.remove(recommendedMovies.size()-1);
            }
            break;
          }
        }
      }
    }
    for (int i=0; i<recommendedMovies.size(); i++){
      System.out.println("Selected movie "+recommendedMovies.get(i).index);
    }
    for (int i=0; i<3; i++){
      List<Integer> recommendations = getKNearestMovies(3, recommendedMovies.get(i).index);
      for (int j=0; j<recommendations.size(); j++){
        System.out.println("Recommended because of "+recommendedMovies.get(i).index+": "+recommendations.get(j));
      }
    }
    
    //The below code needs to be changed.
    /*for (int i=0; i<rowMatrix.size(); i++){
      for (int j=0; j<rowMatrix.get(i).data.size(); j++){
        System.out.println("User "+i+" rated movie "+rowMatrix.get(i).data.get(j).index+" a value of "+rowMatrix.get(i).data.get(j).value);
      }
    }
    for (int i=1; i<2; i++){
      for (int j=0; j<rowMatrix.get(i).data.size(); j++){
        System.out.println("User "+i+" rated movie "+movies.Movies.get(rowMatrix.get(i).data.get(j).index-1)+" a value of "+rowMatrix.get(i).data.get(j).value);
      }
    }
    List<Integer> neighbors = getKNearestUsers(10, 1);
    for (int i=0; i<neighbors.size(); i++){
      SparseVector sv = rowMatrix.get(neighbors.get(i));
      for (int j=0; j<sv.data.size(); j++){
        System.out.println("User "+neighbors.get(i)+" rated movie "+movies.Movies.get(sv.data.get(j).index-1)+" a value of "+sv.data.get(j).value);
      }
    }*/
    
  }


  public static void evaluation(int User, int k, int max) {
	int t = 0;
    colMatrix = new ArrayList<SparseVector>();
    rowMatrix = new ArrayList<SparseVector>();
    movies = new MovieList();

    movies.loadMovieList();
    users = new UserList();
    users.loadUserList();

    ratings = new RatingList();
    ratings.loadRatingList();

    System.out.println("Parsing complete");
    int highestMovie = movies.Movies.get(movies.Movies.size()-1).id; 
    int prevUser = -1;
    rowMatrix.add(new SparseVector()); //It's just easier if we add a blank vector at index 0, so the user id matches his index

	for (int i=0; i<RatingList.Ratings.size(); i++){
      Rating r = RatingList.Ratings.get(i);
		if(r.userId == User){
			t = r.timestamp;
		}
	 }


    for (int i=0; i<RatingList.Ratings.size(); i++){
      Rating r = RatingList.Ratings.get(i);
      if (r.userId != prevUser){
        prevUser = r.userId;
        rowMatrix.add(new SparseVector());
      }
	  if(r.timestamp < t){
		
        rowMatrix.get(rowMatrix.size()-1).addRating(r.movieId, r.rating);
		}
    }
    System.out.println("Row matrix construction complete");
    for (int i=0; i<=highestMovie; i++){
      colMatrix.add(new SparseVector());
    }
    System.out.println("Column matrix initialization complete");
    for (int i=0; i<RatingList.Ratings.size(); i++){
      Rating r = RatingList.Ratings.get(i);
      colMatrix.get(r.movieId).addRating(r.userId, r.rating);
    }
    System.out.println("Unstructured column matrix complete");
    for (int i=0; i<colMatrix.size(); i++){
      colMatrix.get(i).sortByIndex();
    }
    System.out.println("Full column matrix complete");
    
    List<Integer> neighbors = getKNearestUsers(k, User);
    
    
    for (int i = 0; i < neighbors.size(); i++) {
      SparseVector sv = rowMatrix.get(neighbors.get(i));
      for (int j = 0; j < sv.data.size(); j++) {
        int temp = containsId(ratedMovies, sv.data.get(j).index -1) ;
        if(temp != -1){
          ratedMovies.get(temp).total += sv.data.get(j).value;
          ratedMovies.get(temp).number_rated++;
        }
        else
        {
          AverageMovieRating newRating = new AverageMovieRating();
          newRating.id = sv.data.get(j).index - 1;
          newRating.total = sv.data.get(j).value;
          newRating.number_rated = 1;
          ratedMovies.add(newRating);
        }         
      }
    }
    Collections.sort(ratedMovies, new Comparator<AverageMovieRating>(){
      public int compare(AverageMovieRating s1, AverageMovieRating s2){
        if((s1.total / s1.number_rated) > (s2.total / s2.number_rated)){
          return -1;
        }
        else if ((s1.total / s1.number_rated) == (s2.total / s2.number_rated)) {
          return 0;
        }  
        else{
          return 1;
        }  
      }
    });
    
    for(int w = 0; w < ratedMovies.size(); w++){
      System.out.println(movies.Movies.get(ratedMovies.get(w).id).title + " " + ratedMovies.get(w).total /    ratedMovies.get(w).number_rated);
    }
    
    //Find novel movie recommendations for user 1.
    System.out.println("Movies rated by user " + User);
    SparseVector user = rowMatrix.get(User);
    for (int i=0; i<user.data.size(); i++){
      System.out.println(user.data.get(i).index);
    }
    SparseVector noveltyScores = new SparseVector();
    List<Integer> comparisonBase = getRandomMovieSubset(10, 1);
    for (int i=0; i<comparisonBase.size(); i++){
      System.out.println("Base: Movie "+comparisonBase.get(i));
    }
    for (int i=0; i<user.data.size(); i++){
      if (user.data.get(i).value < 3.5) {
        System.out.println("Movie "+user.data.get(i).index+" ignored. Rating "+user.data.get(i).value);
        continue; //If the user rated the movie less than 3.5, ignore it.
      }
      double runningScore = 0;
      SparseVector baseMovie = colMatrix.get(user.data.get(i).index);
      for (int j=0; j<comparisonBase.size(); j++){        
        double score = baseMovie.distanceFrom(colMatrix.get(comparisonBase.get(j)));
        runningScore+=score;
      }
      noveltyScores.addRating(user.data.get(i).index, runningScore/comparisonBase.size());
      System.out.println("Movie "+user.data.get(i).index+" got a score of "+runningScore/comparisonBase.size()+". Rating "+user.data.get(i).value);
    }
    List<IntPair> recommendedMovies = new ArrayList<IntPair>();
    //int max = 10;
    for (int i=0; i<noveltyScores.data.size(); i++){
      IntPair current = noveltyScores.data.get(i);
      if (recommendedMovies.size()==0){
        recommendedMovies.add(current);
      }else{
        for (int j=0; j<recommendedMovies.size(); j++){        
          if (current.value > recommendedMovies.get(j).value){
            recommendedMovies.add(j, current);
            if (recommendedMovies.size()>max){
              recommendedMovies.remove(recommendedMovies.size()-1);
            }
            break;
          }
        }
      }
    }
    for (int i=0; i<recommendedMovies.size(); i++){
      System.out.println("Selected movie "+recommendedMovies.get(i).index);
    }
    for (int i=0; i<3; i++){
      List<Integer> recommendations = getKNearestMovies(3, recommendedMovies.get(i).index);
      for (int j=0; j<recommendations.size(); j++){
        System.out.println("Recommended because of "+recommendedMovies.get(i).index+": "+recommendations.get(j));
      }
    }
    
  }

  public static int containsId(List<AverageMovieRating> list, long id) {
    int list_id = 0;
    if(list == null)
      return -1;
    
    for (AverageMovieRating object : list) {
      if (object.getId() == id) {
        return list_id;
      }
      list_id++;
    }
    return -1;
  }
  
  public static List<Integer> getRandomMovieSubset(int n, int index){
    SparseVector base = rowMatrix.get(index);
    List<Integer> randomIndices = new ArrayList<Integer>();
    if (n > base.data.size()){
      for (int i=0; i<base.data.size(); i++){
        randomIndices.add(base.data.get(i).index);
      }
    }else{
      Random rand = new Random();
      for (int i=0; i<n; i++){
        int num = rand.nextInt(base.data.size());
        if (randomIndices.contains(base.data.get(num).index)) continue;
        randomIndices.add(base.data.get(num).index);
      }
    }
    return randomIndices;
  }

  // K = 20 best results
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
  public static List<Integer> getKNearestMovies(int k, int index)
  {
    SparseVector base = colMatrix.get(index);
    List<Integer> nearestIndices = new ArrayList<Integer>();
    List<Double> nearestScores = new ArrayList<Double>();
    for (int i=0; i<colMatrix.size(); i++){
      if (i == index) continue;
      double score = colMatrix.get(i).distanceFrom(base);
      if (nearestIndices.size()==0){
        nearestIndices.add(i);
        nearestScores.add(score);
      }else if (nearestIndices.size()<k){
        for (int j=0; j<nearestIndices.size(); j++){
          if (score < nearestScores.get(j)){
            nearestScores.add(j, score);
            nearestIndices.add(j, i);
            break;
          }
        }
      }else{
        for (int j=0; j<nearestIndices.size(); j++){
          if (score < nearestScores.get(j)){
            nearestScores.add(j, score);
            nearestIndices.add(j, i);
            nearestScores.remove(nearestIndices.size()-1);
            nearestIndices.remove(nearestIndices.size()-1);
            break;
          }
        }        
      }
    }
    return nearestIndices;
  }
}

class AverageMovieRating{
 
 public int id;
 public double total;
 public int number_rated;
 
 public AverageMovieRating(){
 }
 
 public int getId(){
  return id;
 }
 
}
