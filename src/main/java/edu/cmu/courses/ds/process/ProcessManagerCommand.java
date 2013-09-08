package edu.cmu.courses.ds.process;

public enum ProcessManagerCommand {
    HELP("help"),
    PS("ps"),
    LS("ls"),
    RUN("run"),
    MG("mg"),
    QUIT("quit"),
    UNKNOWN("unknown");

    private String value;
    private ProcessManagerCommand(String value){
        this.value = value;
    }
    public String getValue(){
        return value;
    }

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
