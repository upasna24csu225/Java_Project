import java.io.*;
import java.util.*;

public class BankStore {

    private final File file = new File("users.db");

    public List<User> loadUsers() throws IOException {
        List<User> list = new ArrayList<>();
        if (!file.exists()) return list;

        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;

        while ((line = br.readLine()) != null) {
            if (line.trim().isEmpty()) continue;

            String[] p = line.split(",", -1);

            String username = p[0];
            String hash = p[1];
            double bal = Double.parseDouble(p[2]);
            String role = p[3];

            if (role.equalsIgnoreCase("ADMIN"))
                list.add(new AdminUser(username, hash, bal));
            else
                list.add(new BasicUser(username, hash, bal));
        }

        br.close();
        return list;
    }

    public void saveUsers(List<User> users) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        for (User u : users) {
            bw.write(u.toString());
            bw.newLine();
        }
        bw.close();
    }
}