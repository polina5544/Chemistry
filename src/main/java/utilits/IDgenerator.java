package utilits;


public class IDgenerator {
    private static int currentId = 1;

    public static int nextId() {
        return currentId++;
    }
}