package ashnext.telegram.api;

public enum Command {
    START, SUB, UNSUB, READ_LATER, TAGS;

    public String getCommand() {
        return "/" + this.name().toLowerCase();
    }

    public static Command valueOfCommand(String value) {
        return Command.valueOf((value.startsWith("/") ? value.substring(1) : value).toUpperCase());
    }
}
