import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateBCryptPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "admin";
        String encodedPassword = encoder.encode(password);
        System.out.println("Password: " + password);
        System.out.println("BCrypt: " + encodedPassword);
    }
}