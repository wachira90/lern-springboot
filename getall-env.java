import java.util.Map;
import java.util.Properties;

public class JavaEnvironmentVariables {
    public static void main(String args[]){
        System.out.println(" [ Environment Vars ] ");
        Map<String, String> enviorntmentVars  = System.getenv();
        enviorntmentVars.entrySet().forEach(System.out::println);
    }
}
