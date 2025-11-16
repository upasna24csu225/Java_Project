public class User {

    private String passwordHash;

    protected String username;
    protected double balance;
    protected String role;

    public User(String username, String passwordHash, double balance, String role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.balance = balance;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String newHash) {
        this.passwordHash = newHash;
    }

    public double getBalance() {
        return balance;
    }

    public String getRole() {
        return role;
    }

    public void deposit(double amt) {
        if (amt > 0) balance += amt;
    }

    public boolean withdraw(double amt) {
        if (amt > 0 && amt <= balance) {
            balance -= amt;
            return true;
        }
        return false;
    }

    public boolean authorizeAccess() {
        return false;
    }

    @Override
    public String toString() {
        return username + "," + passwordHash + "," + balance + "," + role;
    }
}

class AdminUser extends User {
    public AdminUser(String username, String passwordHash, double balance) {
        super(username, passwordHash, balance, "ADMIN");
    }

    @Override
    public boolean authorizeAccess() {
        return true;
    }
}

class BasicUser extends User {
    public BasicUser(String username, String passwordHash, double balance) {
        super(username, passwordHash, balance, "BASIC");
    }
}