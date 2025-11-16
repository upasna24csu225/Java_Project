import java.security.MessageDigest;
import java.util.*;

// Thread that prints "*" while typing password
class MaskThread extends Thread {
    private boolean running = true;

    public void stopMasking() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            System.out.print("\010*");
            try { Thread.sleep(1); } catch (Exception e) {}
        }
    }
}

public class BankApp {

    private final Scanner sc = new Scanner(System.in);
    private final BankStore store = new BankStore();
    private List<User> users = new ArrayList<>();

    public static void main(String[] args) {
        BankApp app = new BankApp();
        app.start();
    }

    public void start() {
        try {
            users = store.loadUsers();
            if (users.isEmpty()) {
                System.out.println("Creating default admin (admin/admin123)");
                users.add(new AdminUser("admin", hash("admin123"), 0.0));
                store.saveUsers(users);
            }
        } catch (Exception e) {
            System.out.println("Error loading users: " + e.getMessage());
        }

        while (true) {
            System.out.println("\n=== MAIN MENU ===");
            System.out.println("1. Signup");
            System.out.println("2. Login");
            System.out.println("3. Change Password");
            System.out.println("4. Exit");
            System.out.print("Enter choice: ");

            String c = sc.nextLine();
            switch (c) {
                case "1": signup(); break;
                case "2": login(); break;
                case "3": changePassword(); break;
                case "4": return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    // SIGNUP
    private void signup() {
        try {
            System.out.print("Enter username (spaces allowed): ");
            String username = sc.nextLine();

            if (username.contains(",")) {
                System.out.println("Username cannot contain commas.");
                return;
            }

            if (exists(username)) {
                System.out.println("User already exists.");
                return;
            }

            System.out.print("Enter password: ");
            MaskThread mask = new MaskThread();
            mask.start();
            String password = sc.nextLine();
            mask.stopMasking();
            System.out.println();

            users.add(new BasicUser(username, hash(password), 0.0));
            store.saveUsers(users);

            System.out.println("Signup successful!");

        } catch (Exception e) {
            System.out.println("Signup error: " + e.getMessage());
        }
    }

    // LOGIN
    private void login() {
        try {
            System.out.print("Username: ");
            String username = sc.nextLine();

            System.out.print("Password: ");
            MaskThread mask = new MaskThread();
            mask.start();
            String password = sc.nextLine();
            mask.stopMasking();
            System.out.println();

            User user = authenticate(username, password);
            if (user == null) {
                System.out.println("Invalid login.");
                return;
            }

            System.out.println("Welcome " + user.getUsername() + "! Role: " + user.getRole());
            if (user.authorizeAccess()) adminMenu(user);
            else userMenu(user);

        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
        }
    }

    // CHANGE PASSWORD
    private void changePassword() {
        try {
            System.out.print("Username: ");
            String username = sc.nextLine();

            System.out.print("Current password: ");
            MaskThread mask1 = new MaskThread();
            mask1.start();
            String oldPwd = sc.nextLine();
            mask1.stopMasking();
            System.out.println();

            User user = authenticate(username, oldPwd);
            if (user == null) {
                System.out.println("Incorrect username/password.");
                return;
            }

            System.out.print("New password: ");
            MaskThread mask2 = new MaskThread();
            mask2.start();
            String newPwd = sc.nextLine();
            mask2.stopMasking();
            System.out.println();

            user.setPasswordHash(hash(newPwd));
            store.saveUsers(users);

            System.out.println("Password changed successfully!");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ADMIN MENU
    private void adminMenu(User admin) {
        while (true) {
            System.out.println("\n--- ADMIN MENU ---");
            System.out.println("1. View all users");
            System.out.println("2. Total bank balance");
            System.out.println("3. Logout");

            String c = sc.nextLine();
            switch (c) {
                case "1":
                    for (User u : users)
                        System.out.println(u.getUsername() + " (" + u.getRole() + ") : " + u.getBalance());
                    break;

                case "2":
                    double total = users.stream().mapToDouble(User::getBalance).sum();
                    System.out.println("Total bank balance = " + total);
                    break;

                case "3": return;

                default:
                    System.out.println("Invalid.");
            }
        }
    }

    // USER MENU
    private void userMenu(User user) {
        while (true) {
            System.out.println("\n--- USER MENU ---");
            System.out.println("1. Check balance");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Logout");

            String c = sc.nextLine();
            switch (c) {
                case "1":
                    System.out.println("Your balance: " + user.getBalance());
                    break;

                case "2":
                    System.out.print("Enter deposit amount: ");
                    try {
                        double amt = Double.parseDouble(sc.nextLine());
                        user.deposit(amt);
                        save();
                        System.out.println("Deposited!");
                    } catch (Exception e) {
                        System.out.println("Invalid amount.");
                    }
                    break;

                case "3":
                    System.out.print("Enter withdrawal amount: ");
                    try {
                        double amt = Double.parseDouble(sc.nextLine());
                        if (user.withdraw(amt)) {
                            save();
                            System.out.println("Withdrawn!");
                        } else {
                            System.out.println("Insufficient balance.");
                        }
                    } catch (Exception e) {
                        System.out.println("Invalid amount.");
                    }
                    break;

                case "4": return;

                default: System.out.println("Invalid.");
            }
        }
    }

    // HELPERS
    private boolean exists(String username) {
        return users.stream().anyMatch(u -> u.getUsername().equals(username));
    }

    private User authenticate(String username, String password) throws Exception {
        String h = hash(password);
        for (User u : users)
            if (u.getUsername().equals(username) && u.getPasswordHash().equals(h))
                return u;
        return null;
    }

    private void save() {
        try {
            store.saveUsers(users);
        } catch (Exception e) {
            System.out.println("Save error: " + e.getMessage());
        }
    }

    public static String hash(String plain) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] b = md.digest(plain.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02x", x));
        return sb.toString();
    }
}