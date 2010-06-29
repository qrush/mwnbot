import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import org.jibble.pircbot.*;

public class TheBot extends PircBot {
    private static final Map<String, Integer> karmaMap = new HashMap<String, Integer>();
    private static final Map<String, RoulStat> roulMap = new HashMap<String, RoulStat>();
    private static final Map<String, Integer> wcMap = new HashMap<String, Integer>();
    private static boolean[] roulGun;
    private static int currChamber = 0;
    private static final List<String> admins = new LinkedList<String>(){{
       add("admin login here");
    }};
    private static final String roulStatsFile = "./roul.txt";
    private static final String karmaFile = "./karma.txt";
    private static final String wordFile = "./words.txt";
    private static final Map<String, LinkedList<String>> laterMap =
            new HashMap<String, LinkedList<String>>();
    private static final LinkedList<String> karmaQ = new LinkedList<String>();
    private static final String NICK = "MrDurden";
    private static final String PASS = "";//password here

    public TheBot() {
        this.setName(NICK);
        this.setLogin(NICK);
        sendMessage("NickServ", "identify " + PASS);
        initRoul();
        initKarma();
        initWc();
        initRoulGun();
    }

    @Override
    protected void onConnect() {
        super.onConnect();
        changeNick(NICK);
    }

    @Override
    protected void onDisconnect() {
        PrintWriter rOutput = null;
        PrintWriter kOutput = null;
        PrintWriter wOutput = null;
        try {
            rOutput = new PrintWriter(new BufferedWriter(
                    new FileWriter(roulStatsFile)), true);
            rOutput.println(RoulStat.getAvgChamber() + " " + RoulStat.getTotalBullets());
            for(String name : roulMap.keySet()){
                rOutput.println(name +"\t" + roulMap.get(name));
            }

            kOutput = new PrintWriter(new BufferedWriter(
                    new FileWriter(karmaFile)), true);
            for(String name : karmaMap.keySet()){
                if(karmaMap.get(name) != 0){
                    kOutput.println(name +"\t" + karmaMap.get(name));
                }
            }

            wOutput = new PrintWriter(new BufferedWriter(
                    new FileWriter(wordFile)), true);
            for(String name : wcMap.keySet()){
                if(wcMap.get(name) != 0){
                    wOutput.println(name +"\t" + wcMap.get(name));
                }
            }
        } catch (IOException ex) {
        }finally{
            if(rOutput != null) rOutput.close();
            if(kOutput != null) kOutput.close();
            if(wOutput != null) wOutput.close();
        }
        System.exit(0);
    }

    @Override
    protected void onPrivateMessage(String sender, String login, String hostname, String message) {
        if(message.contains("http://") || message.contains("https://")){
            String[] parts = message.split(" ");
            urlScan(parts, sender);
        }
        if (message.startsWith("!help")) {
            String[] parts = message.split(" ");
            helpMessage(parts, sender);
        }

        if(message.equals("disconnect") && admins.contains(login)){
            disconnect();
        }else if(message.equals("disconnect") && !admins.contains(login)){
            sendMessage(sender, "You're not my admin.");
        }
    }

    @Override
    protected void onJoin(String channel, String sender, String login, String hostname) {
        super.onJoin(channel, sender, login, hostname);
        if(laterMap.get(sender) != null){
            for(String msg : laterMap.get(sender)){
                sendMessage(channel, msg);
            }
            laterMap.remove(sender);
        }
    }


    @Override
    public void onMessage(String channel, String sender,
            String login, String hostname, String message) {
        if(laterMap.get(sender) != null){
            for(String msg : laterMap.get(sender)){
                sendMessage(channel, msg);
            }
            laterMap.remove(sender);
        }
        if (message.startsWith("!")){
            message = message.substring(1).trim();
            if(message.startsWith("karma")){
                String[] parts = message.split(" ");
                String name = parts.length > 1 ? parts[1] : sender;
                if(karmaMap.get(name) == null){
                    karmaMap.put(name, 0);
                }
                sendMessage(channel, name + "'s karma: " + karmaMap.get(name));
            }
            if(message.equals("roulette")){
                if(roulMap.get(sender) == null){
                    roulMap.put(sender, new RoulStat());
                }

                RoulStat senderStat = roulMap.get(sender);
                sendMessage(channel, "Firing chamber " + (currChamber + 1) + " of 6...");
                fireGun(channel, sender, senderStat);
            }
            if(message.startsWith("rstats")){
                String[] parts = message.split(" ");
                if(parts.length == 1){
                    sendMessage(channel, RoulStat.overallStatsString());
                }else{
                    RoulStat stat = roulMap.get(parts[1]);
                    if(stat == null){
                        sendMessage(channel, parts[1] + " has not played Russian Roulette.");
                    }else{
                        sendMessage(channel, parts[1] + "'s Russian Roulette stats: " +
                                stat.statsString());
                    }
                }
            }
            if(message.equals("reload")){
                if (currChamber >= 4) {
                    if (Math.random() >= .5) {
                        if (roulMap.get(sender) == null) {
                            roulMap.put(sender, new RoulStat());
                        }
                        RoulStat senderStat = roulMap.get(sender);
                        sendMessage(channel, "You try to reload, but you fire the gun instead.");
                        fireGun(channel, sender, senderStat);
                    }else{
                        initRoulGun();
                        sendMessage(channel, "Reloaded");
                    }
                }else{
                    initRoulGun();
                    sendMessage(channel, "Reloaded");
                }
            }
            if(message.startsWith("clear")){
                if(!admins.contains(login)){
                    sendMessage(channel, "You're not my admin.");
                }else{
                    String[] parts = message.split(" ");
                    String command = parts[1];
                    if(command.equals("all")){
                         roulMap.clear();
                         initRoulGun();
                         RoulStat.clear();
                         karmaMap.clear();
                    }else if(command.equals("roulette")){
                        if(parts.length == 2){
                            roulMap.clear();
                            initRoulGun();
                            RoulStat.clear();
                            return;
                        }
                        for(int i = 2; i < parts.length; i++){
                            roulMap.remove(parts[i]);
                        }
                    }else if(command.equals("karma")){
                        if(parts.length == 2){
                            karmaMap.clear();
                            return;
                        }
                        for(int i = 2; i < parts.length; i++){
                            karmaMap.remove(parts[i]);
                        }
                    }
                    else if(command.equals("words")){
                        wcMap.clear();
                    }
                }
            }
            if(message.startsWith("later")){
                String[] parts = message.split(" ");
                if(parts[1].equals(this.getName()) || parts[1].equals(this.getNick())){
                    sendMessage(channel, "I'm right here dude.");
                }else if(parts.length >= 3){
                    String msg = parts[1] + ", " + sender + " says: ";
                    for(int i = 2;i < parts.length;i++){
                        msg += parts[i] + " ";
                    }
                    LinkedList<String> msgList = laterMap.get(parts[1]);
                    if(msgList == null){
                        LinkedList<String> addList = new LinkedList<String>();
                        addList.add(msg);
                        laterMap.put(parts[1], addList);
                        sendMessage(channel, "Ok, " + sender +". I'll tell " +
                            parts[1] + " that when they send a message or " +
                            "join the channel.");
                    }else{
                        if(msgList.size() < 5){
                            msgList.add(msg);
                            sendMessage(channel, "Ok, " + sender +". I'll tell " +
                                parts[1] + " that when they send a message.");
                        }else{
                            sendMessage(channel, parts[1] +"'s inbox is full.");
                        }
                    }
                }
            }
            if(message.equals("coin")){
                sendMessage(channel, (Math.random() >= .5) ? "Heads." : "Tails.");
            }
            if(message.startsWith("help")){
                String[] parts = message.split(" ");
                helpMessage(parts, channel);
            }
            if(message.equals("wstats")){
                sendMessage(channel, "Five most commonly used words: " + getWStatsStr());
            }
            return; //was a command, dont count it in word stats
        }else if(message.contains("http://") || message.contains("https://")){
            String[] parts = message.split(" ");
            urlScan(parts, channel);
        }else if(message.endsWith("++") || message.endsWith("--")){
            String[] parts = message.split(" ");
            int karmaAdd = message.endsWith("++") ? 1 : -1;
            String name = parts[parts.length - 1].replace("++", "")
                    .replace("--", "").trim();
            if(name.equals(sender)){
                sendMessage(channel, "You can't change your own karma.");
                karmaAdd = -1;
                name += "-";
                if(karmaQ.size() > 5){
                    sendMessage(channel, "Karma queue is full. Stop spamming.");
                }else{
                    karmaQ.add(name);
                }
            } else {
                 name += message.charAt(message.length() - 1);
                if(karmaQ.size() > 5){
                    sendMessage(channel, "Karma queue is full. Stop spamming.");
                }else{
                    karmaQ.add(name);
                }
            }
        }
        String[] parts = message.toLowerCase().replaceAll("[^A-Za-z0-9\\s'\\x2D]", " ").split(" ");
        wordCount(parts);
    }

    private void initRoulGun() {
        roulGun = new boolean[6];
        roulGun[(int)(Math.random() * 6)] = true;
        currChamber = 0;
    }

    private void initRoul() {
        try {
            BufferedReader in = new BufferedReader(new FileReader(roulStatsFile));
            String[] avgLine = in.readLine().split("[\\s]");
            RoulStat.setAvgChamber(Double.parseDouble(avgLine[0]));
            RoulStat.setTotalBullets(Integer.parseInt(avgLine[1]));
            while(in.ready()){
                String line = in.readLine();
                String[] parts = line.split("[\\s]");
                if(parts.length != 3) continue;
                RoulStat stat = new RoulStat();
                stat.setDeaths(Integer.parseInt(parts[2]));
                stat.setLives(Integer.parseInt(parts[1]));
                roulMap.put(parts[0], stat);
            }
            in.close();
        } catch (IOException ex) {
            System.err.println("Error reading roulette file: " + ex.getMessage());
        }
        calcRoulStats();
    }

    private void initKarma() {
        try {
            BufferedReader in = new BufferedReader(new FileReader(karmaFile));
            while(in.ready()){
                String line = in.readLine();
                String[] parts = line.split("[\\s]");
                if(parts.length != 2) continue;
                karmaMap.put(parts[0], Integer.valueOf(parts[1]));
            }
            in.close();
        } catch (IOException ex) {
            System.err.println("Error reading karma file: " + ex.getMessage());
        }
        TimerTask task = new TimerTask() {
            public void run() {
                if (!karmaQ.isEmpty()) {
                    String name = karmaQ.removeFirst();
                    int karmaAdd = 0;
                    if (name.charAt(name.length() - 1) == '-') {
                        karmaAdd = -1;
                    } else if (name.charAt(name.length() - 1) == '+') {
                        karmaAdd = 1;
                    } else {
                        return;
                    }
                    name = name.substring(0, name.length() - 1);
                    if (karmaMap.get(name) == null) {
                        karmaMap.put(name, karmaAdd);
                    } else {
                        karmaMap.put(name, karmaMap.get(name) + karmaAdd);
                    }
                }
            }
        };
        Timer timer = new Timer(false);
        timer.schedule(task, 0, 1500);
    }

    private void initWc() {
        try {
            BufferedReader in = new BufferedReader(new FileReader(wordFile));
            while(in.ready()){
                String line = in.readLine();
                String[] parts = line.split("[\\s]");
                if(parts.length != 2) continue;
                wcMap.put(parts[0], Integer.valueOf(parts[1]));
            }
            in.close();
        } catch (IOException ex) {
            System.err.println("Error reading word file: " + ex.getMessage());
        }
    }

    private void calcRoulStats() {
        double maxDeath = 0;
        String maxDeathName = "";
        double maxSurv = 0;
        String maxSurvName = "";
        int maxShots = 0;
        String maxShotsName = "";
        for (Map.Entry<String, RoulStat> entry : roulMap.entrySet()) {
            String name = entry.getKey();
            RoulStat stats = entry.getValue();
            if (stats.getDeathRate() > maxDeath) {
                maxDeath = stats.getDeathRate();
                maxDeathName = name;
            }

            if (stats.getSurvRate() > maxSurv) {
                maxSurv = stats.getSurvRate();
                maxSurvName = name;
            }

            if (stats.getTotalShots() > maxShots) {
                maxShots = stats.getTotalShots();
                maxShotsName = name;
            }
        }
        RoulStat.setHighestDeathName(maxDeathName);
        RoulStat.setHighestDeath(maxDeath);
        RoulStat.setHighestSurvName(maxSurvName);
        RoulStat.setHighestSurv(maxSurv);
        RoulStat.setMostShotsName(maxShotsName);
        RoulStat.setMostShots(maxShots);
    }

    private void fireGun(String channel, String sender, RoulStat senderStat) {
         if(roulGun[currChamber++]){
                    sendMessage(channel, "*Bang!* " + sender + " dies.");
                    senderStat.death(currChamber);
                    initRoulGun();
                    sendMessage(channel, "Reloaded");
                }else{
                    sendMessage(channel, "*Click* " + sender +" lives.");
                    senderStat.live();
                }
                if(currChamber == 6){
                    initRoulGun();
                    sendMessage(channel, "Reloaded");
                }

                calcRoulStats();
    }

    private void urlScan(String[] parts, String channel) {
        for (String part : parts) {
            if (part.startsWith("http://") || part.startsWith("https://")) {
                try {
                    URLConnection urlConn = new URL(part).openConnection();
                    urlConn.setReadTimeout(20000);
                    if (!urlConn.getContentType().startsWith("text")) {
                        continue;
                    }
                    Scanner sc = new Scanner(urlConn.getURL().openStream());
                    String titleTag = "";
                    boolean append = false;
                    while (sc.hasNext()) {
                        String line = sc.nextLine().trim();
                        if (line.toLowerCase().contains("<title")) {
                            append = true;
                        }
                        if (append) {
                            titleTag += line;
                        }
                        if (line.toLowerCase().contains("</title")) {
                            append = false;
                        }
                    }
                    titleTag = titleTag.replaceAll("</title>.*|</TITLE>.*", "").
                            replaceAll(".*<title.*>|.*<TITLE.*>", "").trim();
                    if (titleTag.length() > 0) {
                        sendMessage(channel, "Link title: " + titleTag);
                    } else {
                        sendMessage(channel, "Could not get link title");
                    }
                } catch (IOException ex) {
                }
            }
        }
    }

    private void helpMessage(String[] parts, String channel) {
        if (parts.length < 2 || parts[1].equals("help")) {
            sendMessage(channel, "Usage: !help <command>.");
            sendMessage(channel, "Commands: roulette, rstats, reload,"
                    + " coin, later, karma, clear, other.");
            return;
        }
        if (parts[1].equals("roulette")) {
            sendMessage(channel, "Plays one shot of Russian Roulette.");
            sendMessage(channel, "One bullet out of six chambers -- ");
            sendMessage(channel, "Will you live?");
        }
        if (parts[1].equals("rstats")) {
            sendMessage(channel, "Displays the stats leaders in Russian Roulette.");
            sendMessage(channel, "Shows highest shot total, death rate, and survival rate.");
            sendMessage(channel, "If a nick is given after \"!rstats\", that user's stats will be shown.");
        }
        if (parts[1].equals("coin")) {
            sendMessage(channel, "Flips a coin and shows the result.");
        }
        if (parts[1].equals("later")) {
            sendMessage(channel, "Usage: !later <nick> <message>");
            sendMessage(channel, "Sends a message directed at <nick> in the channel");
            sendMessage(channel, "when they send a message or join.");
            sendMessage(channel, "Each person is limited to five messages, so make it good.");
        }
        if (parts[1].equals("reload")) {
            sendMessage(channel, "Reloads the Russian Roulette gun"
                    + " before all six chambers are fired.");
        }
        if (parts[1].equals("karma")) {
            sendMessage(channel, "Usage: !karma <nick>");
            sendMessage(channel, "Shows the current karma of <nick>.");
            sendMessage(channel, "Sending \"!karma\" with no nick shows your karma.");
            sendMessage(channel, "To increase someone's karma, send \"<nick>++\".");
            sendMessage(channel, "To decrease someone's karma, send \"<nick>--\".");
            sendMessage(channel, "If too many karma changes are sent in"
                    + " a short amount of time, some will be ignored.");
        }
        if (parts[1].equals("clear")) {
            sendMessage(channel, "Admin operation. Usage: !clear <all|roulette|karma>");
            sendMessage(channel, "Clears the data stored for Russian roulette, karma, or both.");
            sendMessage(channel, "When \"roulette\" or \"karma\" are specified, nicks may be given" +
                    " after to clear the karma or roulette information for only those nicks.");
        }
        if (parts[1].equals("other")) {
            sendMessage(channel, "This bot will also check link titles when"
                    + " http or https links are sent.");
            sendMessage(channel, "It will ignore direct links to image and video files.");
        }
    }

    private void wordCount(String[] parts) {
        for(String word : parts){
            if(word.length() == 0) continue;
            if(wcMap.get(word) == null){
                wcMap.put(word, 1);
            }else{
                wcMap.put(word, wcMap.get(word) + 1);
            }
        }
    }

    private String getWStatsStr() {
        if(wcMap.size() == 0){
            return "No word counts recorded yet";
        }
        Set<Map.Entry<String, Integer>> words = new TreeSet<Map.Entry<String, Integer>>(new Comparator<Map.Entry<String, Integer>>(){
            public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
                if(o2.getValue().equals(o1.getValue())){
                    return o1.getKey().compareTo(o2.getKey());
                }
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        words.addAll(wcMap.entrySet());
        String top5 = "";
        int i = 0;
        for(Map.Entry<String, Integer> entry : words){
            top5 += ++i + ": " + entry.getKey() + ", " + entry.getValue() + " occurrences. ";
            if(i == 5){
                break;
            }
        }
        return top5;
    }
}
