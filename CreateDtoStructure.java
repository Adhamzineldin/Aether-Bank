import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CreateDtoStructure {
    public static void main(String[] args) throws Exception {
        String basePath = "C:\\Users\\moham\\IdeaProjects\\Aether-Bank\\backend\\iam-service\\src\\main\\java\\com\\maayn\\iamservice\\dto";
        
        // Create directories
        Files.createDirectories(Paths.get(basePath, "request"));
        Files.createDirectories(Paths.get(basePath, "response"));
        Files.createDirectories(Paths.get(basePath, "admin"));
        Files.createDirectories(Paths.get(basePath, "audit"));
        
        System.out.println("Directories created successfully!");
    }
}
