package org.example;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "FlanVimCLI", version = "FlanVimCLI 1.0", mixinStandardHelpOptions = true) 
public class FlanVimCLI implements Runnable { 

    @Option(names = { "-s", "--font-size" }, description = "Font size") 
    int fontSize = 19;

    @Parameters(paramLabel = "<word>", defaultValue = "Hello, picocli", 
               description = "Words to be translated into ASCII art.")
    private String[] words = { "Hello,", "picocli" }; 

    @Override
    public void run() {
        //TODO
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new FlanVimCLI()).execute(args); 
        System.exit(exitCode); 
    }
}
