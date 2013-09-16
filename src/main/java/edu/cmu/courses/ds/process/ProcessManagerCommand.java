<<<<<<< HEAD
package edu.cmu.courses.ds.process;

/**
 * The enum of process manager's commands
 *
 * @author Jian Fang(jianf)
 * @author Fangyu Gao(fangyug)
 */
public enum ProcessManagerCommand {
    /**
     * Prints help information
     */
    HELP("help"),

    /**
     * Prints all running process
     */
    PS("ps"),

    /**
     * Prints all inherited classes from <code>MigratableProcess</code>
     */
    LS("ls"),

    /**
     * Start a process
     */
    RUN("run"),

    /**
     * Migrate a process
     */
    MG("mg"),

    /**
     * Quit the program
     */
    QUIT("quit"),

    /**
     * Unknown command
     */
    UNKNOWN("unknown");

    /**
     * The value of the enum
     */
    private String value;

    /**
     * Constructor of the num
     *
     * @param value
     */
    private ProcessManagerCommand(String value){
        this.value = value;
    }

    /**
     * Get the value of enum
     * @return value
     */
    public String getValue(){
        return value;
    }

    /**
     * Get the enum instance. Search all support commands
     * If the command value equals specific value return
     * the command. If no command found, return null
     *
     * @param value
     * @return if found return the command, else return null
     */
    public static ProcessManagerCommand getInstance(String value){
        ProcessManagerCommand[] instances = ProcessManagerCommand.values();
        for(ProcessManagerCommand instance: instances){
            if(instance.getValue().equals(value)){
                return instance;
            }
        }
        return UNKNOWN;
    }

}
=======
package edu.cmu.courses.ds.process;

/**
 * The enum of process manager's commands
 *
 * @author Jian Fang(jianf)
 * @author Fangyu Gao(fangyug)
 */
public enum ProcessManagerCommand {
    /**
     * All support commands
     */
    HELP("help"),
    PS("ps"),
    LS("ls"),
    RUN("run"),
    MG("mg"),
    QUIT("quit"),
    UNKNOWN("unknown");

    /**
     * The value of the enum
     */
    private String value;

    /**
     * Constructor of the num
     *
     * @param value
     */
    private ProcessManagerCommand(String value){
        this.value = value;
    }

    /**
     * Get the value of enum
     * @return value
     */
    public String getValue(){
        return value;
    }

    /**
     * Get the enum instance. Search all support commands
     * If the command value equals specific value return
     * the command. If no command found, return null
     *
     * @param value
     * @return if found return the command, else return null
     */
    public static ProcessManagerCommand getInstance(String value){
        ProcessManagerCommand[] instances = ProcessManagerCommand.values();
        for(ProcessManagerCommand instance: instances){
            if(instance.getValue().equals(value)){
                return instance;
            }
        }
        return UNKNOWN;
    }

}
>>>>>>> origin/Task_DeleteDeadLoop
