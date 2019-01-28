package common;

import common.error;
import picocli.CommandLine;
import picocli.CommandLine.ParameterException;

public class Utils {
	public static <T> error populateOptions(T options, String[] args) {

        try {
            // Populate the created class from the command line arguments.
            CommandLine.populateCommand(options, args);
        } catch (ParameterException e) {
            // The given command line arguments are invalid, for example there
            // are options specified which do not exist or one of the options
            // is malformed (missing a value, for example).
            System.out.println(e.getMessage());
            CommandLine.usage(options, System.out);
            return error.Errorf(e.getMessage());
        }

        // Print the state.
        System.out.println("Arguments:");
        System.out.print("  ");
        for (String arg : args) {
            System.out.print("\"" + arg + "\" ");
        }
        System.out.println();
        System.out.println(options.toString());
		return null;
	}
}
