import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.lang.*;

public class MatrixBuilder {
  public static List<SparseVector> colMatrix; //the movie vectors, before some time t
  public static List<SparseVector> rowMatrix; //the user vectors, before some time t
  public static List<SparseVector> baseColMatrix; //the movie vectors
  public static List<SparseVector> baseRowMatrix; //the user vectors
  public static MovieList movies; //List of movie name/id pairs
  public static UserList users; //List of user id's
  public static RatingList ratings; //List of ratings
  public static double total_score;
  public static double total_generous_score;
  public static double novelty_total_score;
  public static int novelty_skip;
  public static int generous_skip;
  public static int number_ran;


  public static void main(String []args){
    initializeObjects();
    //List<Integer> result = calculateRecommendations(1, 20, 30);
 
 number_ran = 5;
 novelty_skip = 0;
 generous_skip = 0;
 
 for(int i = 1; i-1 < number_ran; i++){
   System.out.println(" Testing User " + i*13*7);
  System.out.println("--Core Evaluation--");

   evaluation(i*13*7, 20, 30);
  System.out.println("--Novelty Evaluation--");

   noveltyEvaluation(i*13*7, 20, 30);   
 }
 System.out.println("--RESULTS--");
 System.out.println("Average Score: " + total_score / number_ran + " on " + number_ran + " Documents.");
 System.out.println("Average Generous Score: " + (total_generous_score/(number_ran-generous_skip)) + " on " + number_ran + " Documents. "+generous_skip+" of those documents don't count because there were no hits.");
 System.out.println("Average Novelty Score: " + (novelty_total_score / number_ran) + " on " + number_ran + " Documents. Skips " + novelty_skip);

  } 
  
  public static void initializeObjects(){
    // Initialize the list objects. When finished populating objects, we can comment the populate and save functions out.
    colMatrix = new ArrayList<SparseVector>();
    rowMatrix = new ArrayList<SparseVector>();
    baseColMatrix = new ArrayList<SparseVector>();
    baseRowMatrix = new ArrayList<SparseVector>();
    movies = new MovieList();    
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
    
    //Fill the row matrix.
    int highestMovie = movies.Movies.get(movies.Movies.size()-1).id; 
    int prevUser = -1;
    rowMatrix.add(new SparseVector()); //It's just easier if we add a blank vector at index 0, so the user id matches his index

    for (int i=0; i<RatingList.Ratings.size(); i++){
      Rating r = RatingList.Ratings.get(i);
      if (r.userId != prevUser){
        prevUser = r.userId;
        rowMatrix.add(new SparseVector());
        baseRowMatrix.add(new SparseVector());
      }
      rowMatrix.get(rowMatrix.size()-1).addRating(r.movieId, r.rating);
      baseRowMatrix.get(baseRowMatrix.size()-1).addRating(r.movieId, r.rating);
    }        
    System.out.println("Row matrix construction complete");
    
    //Fill the column matrix.
    for (int i=0; i<=highestMovie; i++){
      baseColMatrix.add(new SparseVector());
      colMatrix.add(new SparseVector());
    }
    for (int i=0; i<RatingList.Ratings.size(); i++){
      Rating r = RatingList.Ratings.get(i);
      colMatrix.get(r.movieId).addRating(r.userId, r.rating);
      baseColMatrix.get(r.movieId).addRating(r.userId, r.rating);
    }
    for (int i=0; i<colMatrix.size(); i++){
      colMatrix.get(i).sortByIndex();
      baseColMatrix.get(i).sortByIndex();
    }
    System.out.println("Column matrix construction complete");
  }

  //Comes up with (max) recommendations for the user with id (user), using the current values of colMatrix and rowMatrix
  //and using a value of (k) to find the k nearest users and k nearest movies.
  public static List<Integer> calculateRecommendations(int user, int k, int max) {  
    
    //Perform k nearest user recommendation.
    List<Integer> neighbors = getKNearestUsers(k, user); //the id's of the k nearest users to user 
    SparseVector baseUser = rowMatrix.get(user); //the user vector of the current user
    List<AverageMovieRating> ratedMovies = new ArrayList<AverageMovieRating>(); //the average ratings of the k nearest users
    for (int i = 0; i < neighbors.size(); i++) {
      SparseVector sv = rowMatrix.get(neighbors.get(i)); //the user vector of one of the k nearest users
      
      //For each movie rated, either add it to the list of rated movies, or update the average.
      for (int j = 0; j < sv.data.size(); j++) {
        //System.out.println("User " + neighbors.get(i) + " rated movie "
        //+ movies.Movies.get(sv.data.get(j).index).title + " a value of " + sv.data.get(j).value);
        
        //We don't want to recommend movies the user has already rated.
        double alreadyRated = baseUser.getRating(sv.data.get(j).index);
        //System.out.println("Movie "+(sv.data.get(j).index)+" was rated "+alreadyRated);
        if (alreadyRated > 0) continue;
        
        int temp = containsId(ratedMovies, sv.data.get(j).index);
        if(temp != -1){
          ratedMovies.get(temp).total += sv.data.get(j).value;
          ratedMovies.get(temp).number_rated++;          
        }
        else
        {
          AverageMovieRating newRating = new AverageMovieRating();
          newRating.id = sv.data.get(j).index;
          newRating.total = sv.data.get(j).value;
          newRating.number_rated = 1;
          ratedMovies.add(newRating);
        }         
      }
    }
    
    //Sort the list of rated movies based on the average rating. If tied, use number of ratings to break.
    Collections.sort(ratedMovies, new Comparator<AverageMovieRating>(){
      public int compare(AverageMovieRating s1, AverageMovieRating s2){
        if((s1.total / s1.number_rated) > (s2.total / s2.number_rated)){
          return -1;
        }
        else if ((s1.total / s1.number_rated) == (s2.total / s2.number_rated)) {
          if (s1.number_rated > s2.number_rated){
            return -1;
          }else if (s1.number_rated == s2.number_rated){
            return 0;
          }else{
            return 1;
          }
        }  
        else{
          return 1;
        }  
      }
    });
    
    //Find the top (max) movies based on the k nearest users' ratings.
    List<AverageMovieRating> coreResult = new ArrayList<AverageMovieRating>();
    int recommendedCount = 0;
    for (int i=0; recommendedCount<max; i++){
      if (ratedMovies.get(i).number_rated<3) continue; //If only 1 or 2 of the k nearest users has rated the movie, this is not enough to recommend it.
      coreResult.add(ratedMovies.get(i));
      recommendedCount++;
     // System.out.println("Recommended "+ratedMovies.get(i).id+". Projected rating: "+(ratedMovies.get(i).total/ratedMovies.get(i).number_rated));
    }
    
    /*for(int w = 0; w < ratedMovies.size(); w++){
      System.out.println(movies.Movies.get(ratedMovies.get(w).id).title + " " + ratedMovies.get(w).total /    ratedMovies.get(w).number_rated);
    }*/
    
    //Find novel movie recommendations for the user, using k nearest movies recommendation.
    //System.out.println("Movies rated by user " + user);
    SparseVector userVector = rowMatrix.get(user);
    /*for (int i=0; i<userVector.data.size(); i++){
      System.out.println(userVector.data.get(i).index);
    }*/
    
    //We select 10 random movies rated by the user, and measure the distance of every movie he's rated to these 10 movies.
    SparseVector noveltyScores = new SparseVector(); //A list of (movieId, noveltyScore) pairs.
    List<Integer> comparisonBase = getRandomMovieSubset(10, user); //10 random movies rated by the user, which we will use as a base for comparison.
    /*for (int i=0; i<comparisonBase.size(); i++){
      System.out.println("Base: Movie "+comparisonBase.get(i));
    }*/
    for (int i=0; i<userVector.data.size(); i++){
      if (userVector.data.get(i).value < 3.5) {
        //System.out.println("Movie "+userVector.data.get(i).index+" ignored. Rating "+userVector.data.get(i).value);
        continue; //If the user rated the movie less than 3.5, ignore it.
      }
      double runningScore = 0; //the running novelty score (the sum of the distances from our 10 base movie vectors)
      SparseVector baseMovie = colMatrix.get(userVector.data.get(i).index); //the movie vector whose novelty score we're measuring
      for (int j=0; j<comparisonBase.size(); j++){        
        double score = baseMovie.distanceFrom(colMatrix.get(comparisonBase.get(j)));
        runningScore+=score;
      }
      noveltyScores.addRating(userVector.data.get(i).index, runningScore/comparisonBase.size());
      //System.out.println("Movie "+userVector.data.get(i).index+" got a score of "+runningScore/comparisonBase.size()+". Rating "+userVector.data.get(i).value);
    }
    
    //Select the n most novel movies, and for each one, select n movies similar to those as candidates for recommendation.
    int n = ((Double)(Math.ceil(Math.sqrt(max)))).intValue(); //Assumes max < 100. If max > 100, we'll want to do this differently.
    List<IntPair> noveltyBases = new ArrayList<IntPair>(); //the three most novel movies we're using as bases for recommendation
    for (int i=0; i<noveltyScores.data.size(); i++){
      IntPair current = noveltyScores.data.get(i);
      if (noveltyBases.size()==0){
        noveltyBases.add(current);
      }else{
        if (noveltyBases.size() < n && current.value < noveltyBases.get(noveltyBases.size()-1).value){
          noveltyBases.add(current);
        }else{
          for (int j=0; j<noveltyBases.size(); j++){        
            if (current.value > noveltyBases.get(j).value){
              noveltyBases.add(j, current);
              if (noveltyBases.size()>n){
                noveltyBases.remove(noveltyBases.size()-1);
              }
              break;
            }
          }
        }
      }
    }    
    List<Integer> novelResult = new ArrayList<Integer>();
    for (int i=0; i<n; i++){
   if(i >= noveltyBases.size())
  break;
      List<Integer> novelSubResult = getKNearestMovies(n, noveltyBases.get(i).index); //the n candidate recommendations derived from this movie (i.e. the n nearest movies)
      for (int j=0; j<novelSubResult.size(); j++){
        novelResult.add(novelSubResult.get(j));
        //System.out.println("Recommended because of "+noveltyBases.get(i).index+": "+novelSubResult.get(j));
      }
    }
    
    //Merge the results from the k nearest users recommendation and k nearest movies recommendation.
    //System.out.println("Final recommendations:");
    double novelWeight = 0.35; //The percentage of recommendations that should come from the k nearest movies recommendation.
    List<Integer> finalResult = new ArrayList<Integer>(); //Our actual list of recommendations to the user.
    for (int i=0; i<novelResult.size(); i++){
      //System.out.println("Novel: "+novelResult.get(i));
      finalResult.add(novelResult.get(i));
      if (finalResult.size() > max*novelWeight) break;
    }
    for (int i=0; i<coreResult.size(); i++){
      //Make sure we haven't recommended it already.
      for (int j=0; j<finalResult.size(); j++){
        if (finalResult.get(j).equals(coreResult.get(i).id)) continue;
      }
      //System.out.println("Core: "+coreResult.get(i).id);
      finalResult.add(coreResult.get(i).id);
      if (finalResult.size() == max) break;
    }
    return finalResult;
  }
  
  public static void noveltyEvaluation(int user, int k, int max) {
    int t = 0; //For evaluation, we'll ignore all ratings that occurred after t.
    colMatrix = new ArrayList<SparseVector>(); //The movie vectors for this round of evaluation.
    rowMatrix = new ArrayList<SparseVector>(); //The user vectors for this round of evaluation.
    
    int highestMovie = movies.Movies.get(movies.Movies.size()-1).id; 
    int prevUser = -1;
    rowMatrix.add(new SparseVector()); //It's just easier if we add a blank vector at index 0, so the user id matches his index
    
    //To pick t, we just take the first rating in the list for the given user and use its timestamp.
    for (int i=0; i<RatingList.Ratings.size(); i++){
      Rating r = RatingList.Ratings.get(i);
      if(r.userId == user){
        t = r.timestamp;
        break;
      }
    }

    //Construct colMatrix and rowMatrix, ignoring ratings after t. Also store the user's ratings after t in postTVector.
    SparseVector postTVector = new SparseVector(); //The user's ratings after t
    for (int i=0; i<RatingList.Ratings.size(); i++){
      Rating r = RatingList.Ratings.get(i);
      if (r.userId != prevUser){
        prevUser = r.userId;
        rowMatrix.add(new SparseVector());
      }
      if (r.timestamp < t){        
        rowMatrix.get(rowMatrix.size()-1).addRating(r.movieId, r.rating);
      }
      if (r.userId == user && r.timestamp >= t){
        postTVector.addRating(r.movieId, r.rating);
      }
    }
    
    if (rowMatrix.get(user).data.size()<max/4){
   novelty_skip++;
      System.out.println("Too few ratings before t for novelty evaluation. Aborting.");
      return;
    }
    
    //We also need to remove max/4 elements. These simulate items the user is familiar with but has not rated. We will
    //lose points for recommending any of the removed items.
    List<Integer> badMovies = getRandomMovieSubset(max/4, user);
    for (int i=0; i<rowMatrix.get(user).data.size(); i++){
      for (int j=0; j<badMovies.size(); j++){
        if(j >= badMovies.size() || i >= rowMatrix.get(user).data.size())
          break;
        if (rowMatrix.get(user).data.get(i).index == badMovies.get(j)){
          rowMatrix.get(user).data.remove(i);
        }
      }
    }
    //System.out.println("Row matrix construction complete");
    System.out.println("Selected t-value leaves user with "+postTVector.data.size()+" ratings after t and "+rowMatrix.get(user).data.size()+" ratings before t");
    /*System.out.println("Movies rated after t");
    for (int i=0; i<postTVector.data.size(); i++){
      System.out.println(postTVector.data.get(i).index);
    }*/
    for (int i=0; i<=highestMovie; i++){
      colMatrix.add(new SparseVector());
    }
    for (int i=0; i<RatingList.Ratings.size(); i++){
      Rating r = RatingList.Ratings.get(i);
      if (r.timestamp < t){
        colMatrix.get(r.movieId).addRating(r.userId, r.rating);
      }
    }
    for (int i=0; i<badMovies.size(); i++){
      colMatrix.get(badMovies.get(i)).data.clear();
    }
    for (int i=0; i<colMatrix.size(); i++){
      colMatrix.get(i).sortByIndex();
    }
    //System.out.println("Column matrix construction complete");
    
    //Get the recommendations.
    List<Integer> recommendations = calculateRecommendations(user, k, max);
    
    //Perform the core evaluation. For each recommendation, we lose a point if it appears in the bad movies list. We get
    //x-2.5 points if the user rated it a value of x after t. Then we divide the sum by # of recommendations.
    double runningNoveltyScore = 0;
    for (int i=0; i<recommendations.size(); i++){
      for (int j=0; j<postTVector.data.size(); j++){
        if (postTVector.data.get(j).index == (int)recommendations.get(i)){
         // System.out.println("Found a match. Rating "+postTVector.data.get(j).value);
          runningNoveltyScore+=(postTVector.data.get(j).value - 2.5);
          break;
        }
      }
    }
    for (int i=0; i<recommendations.size(); i++){
      for (int j=0; j<badMovies.size(); j++){
        if (recommendations.get(i) == badMovies.get(j)){
         // System.out.println("Already known movie recommended. Lose a point.");
          runningNoveltyScore-=1;
          break;
        }
      }
    }
    double avgNoveltyScore = runningNoveltyScore/recommendations.size();
    System.out.println("Novelty evaluation score for user "+user+" using k = "+k+" with "+max+" recommendations is "+avgNoveltyScore+".");
    novelty_total_score += avgNoveltyScore;
  }


  public static void evaluation(int user, int k, int max) {
    int t = 0; //For evaluation, we'll ignore all ratings that occurred after t.
    colMatrix = new ArrayList<SparseVector>(); //The movie vectors for this round of evaluation.
    rowMatrix = new ArrayList<SparseVector>(); //The user vectors for this round of evaluation.
    
    int highestMovie = movies.Movies.get(movies.Movies.size()-1).id; 
    int prevUser = -1;
    rowMatrix.add(new SparseVector()); //It's just easier if we add a blank vector at index 0, so the user id matches his index
    
    //To pick t, we just take the first rating in the list for the given user and use its timestamp.
    for (int i=0; i<RatingList.Ratings.size(); i++){
      Rating r = RatingList.Ratings.get(i);
      if(r.userId == user){
        t = r.timestamp;
        break;
      }
    }

    //Construct colMatrix and rowMatrix, ignoring ratings after t. Also store the user's ratings after t in postTVector.
    SparseVector postTVector = new SparseVector(); //The user's ratings after t
    for (int i=0; i<RatingList.Ratings.size(); i++){
      Rating r = RatingList.Ratings.get(i);
      if (r.userId != prevUser){
        prevUser = r.userId;
        rowMatrix.add(new SparseVector());
      }
      if (r.timestamp < t){        
        rowMatrix.get(rowMatrix.size()-1).addRating(r.movieId, r.rating);
      }
      if (r.userId == user && r.timestamp >= t){
        postTVector.addRating(r.movieId, r.rating);
      }
    }
    //System.out.println("Row matrix construction complete");
    System.out.println("Selected t-value leaves user with "+postTVector.data.size()+" ratings after t and "+rowMatrix.get(user).data.size()+" ratings before t");
    //System.out.println("Movies rated after t");
    /*for (int i=0; i<postTVector.data.size(); i++){
      System.out.println(postTVector.data.get(i).index);
    }*/
    for (int i=0; i<=highestMovie; i++){
      colMatrix.add(new SparseVector());
    }
    for (int i=0; i<RatingList.Ratings.size(); i++){
      Rating r = RatingList.Ratings.get(i);
      if (r.timestamp < t){
        colMatrix.get(r.movieId).addRating(r.userId, r.rating);
      }
    }
    for (int i=0; i<colMatrix.size(); i++){
      colMatrix.get(i).sortByIndex();
    }
    //System.out.println("Column matrix construction complete");
    
    //Get the recommendations.
    List<Integer> recommendations = calculateRecommendations(user, k, max);
    
    //Perform the core evaluation. For each recommendation, we get 0 points if the user didn't rate it after t. We get
    //x-2.5 points if the user rated it a value of x after t. Then we divide the sum by # of recommendations.
    double runningCoreScore = 0;
    int hitCount = 0;
    for (int i=0; i<recommendations.size(); i++){
      for (int j=0; j<postTVector.data.size(); j++){
        if (postTVector.data.get(j).index == (int)recommendations.get(i)){
         // System.out.println("Found a match. Rating "+postTVector.data.get(j).value);
          hitCount++;
          runningCoreScore+=(postTVector.data.get(j).value - 2.5);
          break;
        }
      }
    }
    double avgCoreScore = runningCoreScore/recommendations.size();
    double avgGenerousScore = runningCoreScore/hitCount;
    System.out.println("Core evaluation score for user "+user+" using k = "+k+" with "+max+" recommendations is "+avgCoreScore+".");
    System.out.println("Generous evaluation score for user "+user+" using k = "+k+" with "+max+" recommendations is "+avgGenerousScore+".");
    total_score += avgCoreScore;
    if (hitCount == 0){
      generous_skip++;
    }else{
      total_generous_score += avgGenerousScore;
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
