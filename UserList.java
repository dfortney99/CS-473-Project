import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class UserList {

  // Input input 1::F::1::10::48067
  // UserID::Gender::Age::Occupation::Zip-code
  public static ArrayList<User> Users;

  public UserList() {
    Users = new ArrayList<User>();
  }

  // Reads in input file - 1 line at a time
  public static void populateUsers(String filename) {
    try {
      String[] reuseableArray;
      RandomAccessFile file = new RandomAccessFile(filename, "rw");
      String data;
      file.seek(0);
      data = file.readLine();
      while (data != null) {
        // Perform parsing here
        reuseableArray = data.split("::");

        User temp = new User();
        temp.id = Integer.parseInt(reuseableArray[0]);

        if (reuseableArray[1].equals("F")) {
          temp.gender = Gender.female;
        } else
          temp.gender = Gender.male;

        temp.age = Integer.parseInt(reuseableArray[2]);
        temp.occupation = Integer.parseInt(reuseableArray[3]);
        temp.zipcode = Integer.parseInt(reuseableArray[4]);
        Users.add(temp);
        data = file.readLine();
      }
      file.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}


class User {
  // Everything public for simplicity.
  public int id;
  public Gender gender;
  public int age;
  public int occupation;
  public int zipcode;

  // Not setting values here, so we can add other values later.
  public User() {}

}

/*
 * Age Details 1: "Under 18" 18: "18-24" 25: "25-34" 35: "35-44" 45: "45-49" 50: "50-55" 56: "56+"
 */

/*
 * 0: "other" or not specified 1: "academic/educator" 2: "artist" 3: "clerical/admin" 4:
 * "college/grad student" 5: "customer service" 6: "doctor/health care" 7: "executive/managerial" 8:
 * "farmer" 9: "homemaker" 10: "K-12 student" 11: "lawyer" 12: "programmer" 13: "retired" 14:
 * "sales/marketing" 15: "scientist" 16: "self-employed" 17: "technician/engineer" 18:
 * "tradesman/craftsman" 19: "unemployed" 20: "writer"
 */
