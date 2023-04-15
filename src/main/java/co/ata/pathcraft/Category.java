package co.ata.pathcraft;

public class Category {
    public static boolean Is(String target, String input) {
        if(target == "*")
            return true;
        String[] inputs = input.split(",");
        for (String i : inputs) {
            if (i.equals(target))
                return true;
        }
        return false;
    }
}
