import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;





/**
 * This is just a demo for you, please run it on JDK17.
 * This is just a demo, and you can extend and implement functions.
 * based on this demo, or implement it in a different way.
 */
public class OnlineCoursesAnalyzer {

    List<Course> courses = new ArrayList<>();

    public OnlineCoursesAnalyzer(String datasetPath) {
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(datasetPath, StandardCharsets.UTF_8));
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
                Course course = new Course(info[0], info[1], new Date(info[2]),
                        info[3], info[4], info[5],
                        Integer.parseInt(info[6]),
                        Integer.parseInt(info[7]),
                        Integer.parseInt(info[8]),
                        Integer.parseInt(info[9]),
                        Integer.parseInt(info[10]),
                        Double.parseDouble(info[11]),
                        Double.parseDouble(info[12]),
                        Double.parseDouble(info[13]),
                        Double.parseDouble(info[14]),
                        Double.parseDouble(info[15]),
                        Double.parseDouble(info[16]),
                        Double.parseDouble(info[17]),
                        Double.parseDouble(info[18]),
                        Double.parseDouble(info[19]),
                        Double.parseDouble(info[20]),
                        Double.parseDouble(info[21]),
                        Double.parseDouble(info[22]));
                courses.add(course);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //1
    public Map<String, Integer> getPtcpCountByInst() {
        Map<String, Integer> countByInst;
        countByInst = courses.stream()
                .collect(Collectors.groupingBy(Course::getInstitution,
                         Collectors.summingInt(Course::getParticipants)));
        return countByInst;
    }

    //2
    public Map<String, Integer> getPtcpCountByInstAndSubject() {
        Map<String, Integer> countByInstAndSubject;
        countByInstAndSubject = courses.stream()
                .collect(Collectors.groupingBy(course -> course.getInstitution() + "-" + course.getSubject(),
                         Collectors.summingInt(Course::getParticipants)));
        Map<String, Integer> sortedCountByInstAndSubject = new LinkedHashMap<>();
        countByInstAndSubject.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .forEachOrdered(entry -> sortedCountByInstAndSubject.put(entry.getKey(), entry.getValue()));
        return sortedCountByInstAndSubject;
    }

    //3
    public Map<String, List<List<String>>> getCourseListOfInstructor() {
        Map<String, List<List<String>>> courseMap = new HashMap<>();

        // iterate over the dataset
        for (Course course : courses) {
            String[] instructors = course.getInstructors().split(", ");
            String courseTitle = course.getTitle();
            // iterate over the instructors for each course
            for (String instructor : instructors) {
                List<List<String>> courseLists;
                if (!courseMap.containsKey(instructor)) {
                    courseLists = new ArrayList<>();
                    courseLists.add(new ArrayList<>());
                    courseLists.add(new ArrayList<>());
                }
                else {
                    courseLists = courseMap.get(instructor);
                }
                // add the course to the appropriate sub-list
                if (instructors.length == 1) {
                    if (!courseLists.get(0).contains(courseTitle)) {
                        courseLists.get(0).add(courseTitle);
                    }
                } else {
                    if (!courseLists.get(1).contains(courseTitle)) {
                        courseLists.get(1).add(courseTitle);
                    }
                }
                courseMap.put(instructor, courseLists);
            }
        }
        // sort the course titles in alphabetical order for each instructor
        courseMap.forEach((k, v) -> v.forEach(Collections::sort));
        return courseMap;
    }

    //4
    public List<String> getCourses(int topK, String by) {
        // Sort the courses by the given criterion
        if (by.equals("hours")) {
            courses.sort(Comparator.comparing(Course::getTotalHours).reversed()
                    .thenComparing(Course::getTitle));
        } else if (by.equals("participants")) {
            courses.sort(Comparator.comparing(Course::getParticipants).reversed()
                    .thenComparing(Course::getTitle));
        }
        // Get the top K courses and return their titles
        List<String> result = new ArrayList<>();
        for (Course course : courses) {
            String title = course.getTitle();
            if(result.size() == topK){
                break;
            }
            if (!result.contains(title)) {
                result.add(title);
            }
        }
        return result;
    }

    //5
    public List<String> searchCourses(String courseSubject, double percentAudited, double totalCourseHours) {
        List<String> result = new ArrayList<>();
        for (Course course : courses) {
            if (course.getSubject().toLowerCase().contains(courseSubject.toLowerCase()) &&
                course.getPercentAudited() >= percentAudited && course.getTotalHours() <= totalCourseHours) {
                if (!result.contains(course.getTitle())) {
                    result.add(course.getTitle());
                }
            }
        }
        Collections.sort(result);
        return result;
    }

    //6
    public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
        List<String> recommendedCourses = new ArrayList<>();
        // calculate average Median Age, average % Male, and average % Bachelor's Degree or Higher for each course
        Map<String, Double> courseAverageMedianAge = courses.stream().collect(Collectors.groupingBy(Course::getNumber, Collectors.averagingDouble(Course::getMedianAge)));
        Map<String, Double> courseAveragePercentMale = courses.stream().collect(Collectors.groupingBy(Course::getNumber, Collectors.averagingDouble(Course::getPercentMale)));
        Map<String, Double> courseAveragePercentDegree = courses.stream().collect(Collectors.groupingBy(Course::getNumber, Collectors.averagingDouble(Course::getPercentDegree)));
        Map<String, Optional<Course>> courseNumber2CourseTitle = courses.stream().collect(Collectors.groupingBy(Course::getNumber, Collectors.maxBy(Comparator.comparing(Course::getLaunchDate))));
        // calculate similarity values for each course
        Map<String, Double> similarityMap = new HashMap<>();
        for (Course course : courses) {
            String courseNumber = course.getNumber();
            double similarity = Math.pow(age - courseAverageMedianAge.get(courseNumber), 2)
                    + Math.pow(gender * 100 - courseAveragePercentMale.get(courseNumber), 2)
                    + Math.pow(isBachelorOrHigher * 100 - courseAveragePercentDegree.get(courseNumber), 2);
            String courseTitle = courseNumber2CourseTitle.get(courseNumber).isPresent() ? courseNumber2CourseTitle.get(courseNumber).get().getTitle() : null;
            if (courseTitle == null) continue;
            if (similarityMap.containsKey(courseTitle) && similarityMap.get(courseTitle) < similarity) continue;
            similarityMap.put(courseTitle, similarity);
        }
        List<String> sortedCourseTitleBySimilarity = new ArrayList<>();
        similarityMap.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().thenComparing(Map.Entry.comparingByKey()))
                .forEachOrdered(entry -> sortedCourseTitleBySimilarity.add(entry.getKey()));
        // add recommended courses to the list
        int count = 0;
        for (String courseTitle : sortedCourseTitleBySimilarity) {
            if (count == 10) {
                break;
            }
            if (!recommendedCourses.contains(courseTitle)) {
                recommendedCourses.add(courseTitle);
                count++;
            }
        }
        return recommendedCourses;
    }
}

class Course {
    String institution;
    String number;
    Date launchDate;
    String title;
    String instructors;
    String subject;
    int year;
    int honorCode;
    int participants;
    int audited;
    int certified;
    double percentAudited;
    double percentCertified;
    double percentCertified50;
    double percentVideo;
    double percentForum;
    double totalHours;
    double medianHoursCertification;
    double medianAge;
    double percentMale;
    double percentFemale;
    double percentDegree;

    public String getInstitution() {
        return institution;
    }

    public String getNumber() {
        return number;
    }

    public Date getLaunchDate() {
        return launchDate;
    }

    public String getTitle() {
        return title;
    }

    public String getInstructors() {
        return instructors;
    }

    public String getSubject() {
        return subject;
    }

    public double getPercentAudited() {
        return percentAudited;
    }

    public double getMedianAge() {
        return medianAge;
    }

    public double getPercentMale() {
        return percentMale;
    }

    public double getPercentDegree() {
        return percentDegree;
    }

    double gradeHigherZero;

    public int getParticipants() {
        return participants;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public Course(String institution, String number, Date launchDate,
                  String title, String instructors, String subject,
                  int year, int honorCode, int participants,
                  int audited, int certified, double percentAudited,
                  double percentCertified, double percentCertified50,
                  double percentVideo, double percentForum, double gradeHigherZero,
                  double totalHours, double medianHoursCertification,
                  double medianAge, double percentMale, double percentFemale,
                  double percentDegree) {
        this.institution = institution;
        this.number = number;
        this.launchDate = launchDate;
        if (title.startsWith("\"")) title = title.substring(1);
        if (title.endsWith("\"")) title = title.substring(0, title.length() - 1);
        this.title = title;
        if (instructors.startsWith("\"")) instructors = instructors.substring(1);
        if (instructors.endsWith("\"")) instructors = instructors.substring(0, instructors.length() - 1);
        this.instructors = instructors;
        if (subject.startsWith("\"")) subject = subject.substring(1);
        if (subject.endsWith("\"")) subject = subject.substring(0, subject.length() - 1);
        this.subject = subject;
        this.year = year;
        this.honorCode = honorCode;
        this.participants = participants;
        this.audited = audited;
        this.certified = certified;
        this.percentAudited = percentAudited;
        this.percentCertified = percentCertified;
        this.percentCertified50 = percentCertified50;
        this.percentVideo = percentVideo;
        this.percentForum = percentForum;
        this.gradeHigherZero = gradeHigherZero;
        this.totalHours = totalHours;
        this.medianHoursCertification = medianHoursCertification;
        this.medianAge = medianAge;
        this.percentMale = percentMale;
        this.percentFemale = percentFemale;
        this.percentDegree = percentDegree;
    }
}