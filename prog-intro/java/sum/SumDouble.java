import static java.lang.Double.parseDouble;

public class SumDouble {
    public static void main(String[] args) {
        double sum = 0; 
        int firstPosition = -1;
	for (int i = 0; i < args.length; i++) {
            for (int j = 0; j <= args[i].length(); j++) {         
                if ((j == args[i].length() || Character.isWhitespace(args[i].charAt(j))) && firstPosition != -1) {
                    sum += parseDouble(args[i].substring(firstPosition, j)); 
                    firstPosition = -1;
                } 
                if (j < args[i].length() && !Character.isWhitespace(args[i].charAt(j)) && firstPosition == -1) {
                    firstPosition = j;
                }
            }
        }
        System.out.println(sum);
    }
}