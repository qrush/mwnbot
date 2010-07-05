import java.text.DecimalFormat;
import java.text.NumberFormat;

public class RoulStat {

    private static String highestDeathName = "";
    private static String highestSurvName = "";
    private static String mostShotsName = "";

    private static double highestDeath = 0;
    private static double highestSurv = 0;
    private static int mostShots = 0;
    private static int totalBullets = 0;
    private static double avgChamber = 0;

    static void clear() {
        highestDeathName = "";
        highestSurvName = "";
        mostShotsName = "";

        highestDeath = 0;
        highestSurv = 0;
        mostShots = 0;
        totalBullets = 0;
        avgChamber = 0;
    }

    private int deaths = 0;
    private int lives = 0;

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public int getTotalShots(){
        return deaths + lives;
    }

    public double getSurvRate(){
        return (double)lives / (deaths + lives);
    }

    public double getDeathRate(){
        return (double)deaths / (deaths + lives);
    }

    public static String getHighestDeathName() {
        return highestDeathName;
    }

    public static void setHighestDeathName(String highestDeath) {
        RoulStat.highestDeathName = highestDeath;
    }

    public static String getHighestSurvName() {
        return highestSurvName;
    }

    public static void setHighestSurvName(String highestSurv) {
        RoulStat.highestSurvName = highestSurv;
    }

    public static String getMostShotsName() {
        return mostShotsName;
    }

    public static void setMostShotsName(String mostShots) {
        RoulStat.mostShotsName = mostShots;
    }

    public static double getHighestDeath() {
        return highestDeath;
    }

    public static void setHighestDeath(double highestDeath) {
        RoulStat.highestDeath = highestDeath;
    }

    public static double getHighestSurv() {
        return highestSurv;
    }

    public static void setHighestSurv(double highestSurv) {
        RoulStat.highestSurv = highestSurv;
    }

    public static int getMostShots() {
        return mostShots;
    }

    public static void setMostShots(int mostShots) {
        RoulStat.mostShots = mostShots;
    }

    public static double getAvgChamber() {
        return avgChamber;
    }

    public static int getTotalBullets(){
        return totalBullets;
    }

    public static void setAvgChamber(double avgChamber) {
        RoulStat.avgChamber = avgChamber;
    }

    public static void setTotalBullets(int totalBullets) {
        RoulStat.totalBullets = totalBullets;
    }

    public void death(int chamber) {
        deaths++;
        RoulStat.avgChamber = (RoulStat.avgChamber * (RoulStat.totalBullets++) + chamber)
                / RoulStat.totalBullets;
    }

    public void live() {
        lives++;
    }

    public static String overallStatsString(){
        NumberFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(3);
        return "Most shots: " + mostShotsName + ", " + mostShots +". "+
               "Highest death rate: " + highestDeathName + ", " +
               format.format(highestDeath * 100) +"%. "+
               "Highest survival rate: " + highestSurvName + ", " +
               format.format(highestSurv * 100)+"%. "+
               "Average bullet position: " +  format.format(avgChamber) + ".";
    }

    public String statsString(){
        NumberFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(3);
        return "Shots: " + getTotalShots() + ". "+
               "Death rate: " + format.format(getDeathRate() * 100) +"%. "+
               "Survival rate: " + format.format(getSurvRate() * 100)+"%.";
    }

    @Override
    public String toString(){
        return lives + " " + deaths;
    }
}