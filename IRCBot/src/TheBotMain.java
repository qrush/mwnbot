public class TheBotMain {

    public static void main(String[] args) throws Exception {

        // Now start our bot up.
        TheBot bot = new TheBot();

        // Enable debugging output.
        bot.setVerbose(true);

        // Connect to the IRC server.
        bot.connect("irc.freenode.net");

        // Join the channel.
        bot.joinChannel("#buildingm");

    }

}