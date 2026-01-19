package game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static java.lang.Math.max;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("You want play playoff tournament? Y/N");
        String ans;
        while(true) {
            if(in.hasNext()) {
                ans = in.next();
                if(ans.equals("Y") || ans.equals("N")) {
                    break;
                }
                System.out.println("You write wrong value! Use Y/N");
            } else {
                System.out.println("You broke this game :( Please don't press ctrl+D");
                return;
            }
        }

        if (ans.equals("N")) {
            final Game game = new Game(false, new HumanPlayer(), new HumanPlayer());
            try {
                while (true) {
                    int result = game.play(new TicTacToeBoard());
                    switch (result) {
                        case 0: System.out.println("Draw");
                            break;
                        case 1: System.out.println("Player One Win!");
                            break;
                        case 2: System.out.println("Player Two Win!");
                            break;

                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            }

        } else {
            int n, m, k;
            int[] values = new int[3];
            System.out.println("Enter the field size and the row length required to win.");
            while(true) {
                boolean flag = true;
                for(int i = 0; i < 3; i++) {
                    if(!in.hasNextInt()) {
                        flag = false;
                        if(in.hasNext()) {
                            System.out.println("You write unreadable values. Check and write again");
                            in.next();
                        }
                        else {
                            System.out.println("You broke this game :( Please don't press ctrl+D");
                            return;
                        }
                        break;
                    }
                    values[i] = in.nextInt();
                }
                if(flag && (values[2] <= 0 || values[1] <= 0 || values[0] <= 0)) {
                    System.out.println("You must write natural values.");
                    flag = false;
                }
                if(flag && (values[0] * values[1] > 100000)) {
                    System.out.println("Your board is very big!");
                    flag = false;
                }
                if(flag && values[2] > max(values[0], values[1])) {
                    System.out.println("You never can win, because your \"row-to-win\" more when field's size.");
                    flag = false;
                }
                if(flag) {
                    n = values[0];
                    m = values[1];
                    k = values[2];
                    break;
                }
            }

            System.out.println("How many players in tournament?");
            int count;
            while (true) {
                if (in.hasNextInt()) {
                    count = in.nextInt();
                    if(count > 0) {
                        break;
                    }
                    System.out.println("You must write natural value!!!");
                }
                if(in.hasNext()) {
                    in.next();
                }
                else {
                    System.out.println("You broke this game :( Please don't press ctrl+D");
                    return;
                }
                System.out.println("You write wrong value!!!");
            }

            List<String> list = new ArrayList<>();
            System.out.println("Write players' names.");
            for(int i = 0; i < count; i++) {
                System.out.print((i + 1) + ": ");
                if (in.hasNext()) {
                    list.add(in.next());
                } else {
                    System.out.println("You broke this game :( Please don't press ctrl+D");
                    return;
                }
            }


            try {
                while(list.size() > 1) {
                    int ind = (list.size() / 2) - 1;
                    while(ind >= 0) {
                        System.out.println("Right now play: " + list.get(ind * 2) + " and " + list.get(ind * 2 + 1));
                        Game game = new Game(false, new HumanPlayer(), new HumanPlayer());

                        int result = game.play(new TicTacToeBoard(n, m, k));
                        switch (result) {
                            case 0: System.out.println("Draw," + list.get(ind * 2) + " and " + list.get(ind * 2 + 1) + " play again!");
                                break;
                            case 1: System.out.println(list.get(ind * 2) + " Win! " + list.get(ind * 2 + 1) + " bye-bye!");
                                list.remove(ind * 2 + 1);
                                ind--;
                                break;
                            case 2: System.out.println(list.get(ind * 2 + 1) + " Win! " + list.get(ind * 2) + " bye-bye!");
                                list.remove(ind * 2);
                                ind--;
                                break;
                        }
                    }
                }
                System.out.println(list.get(0) + " Winner!");
            } catch(IOException e) {
                System.out.println(e);
            }


        }




    }

}


