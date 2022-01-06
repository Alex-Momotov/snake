package alex.momotov;

import alex.momotov.reality.Reality;


import java.io.IOException;

public class App {




    public static void main(String[] args) throws IOException {

        Reality reality = new Reality(20, 15);
        Thread t = new Thread(reality);
        t.start();

    }



}
